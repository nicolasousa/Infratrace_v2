package Services;

import Configuracao.Conexao;
import Dao.RackDAO;
import Exceptions.ErrosBanco;
import Exceptions.NegocioException;
import Model.Rack;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio dos racks. Mesmo padrão do FileiraService.
 *
 * Regras:
 *   - nome obrigatório, com até 128 caracteres (coluna rac_nome VARCHAR(128));
 *   - o rack precisa de uma fileira (fk_fil_id NOT NULL);
 *   - capacidade de 1 a 60 U (CHECK ck_rac_total_slots). O Java confere antes, para dar
 *     uma mensagem clara; o CHECK do banco continua valendo como última defesa;
 *   - o nome é único DENTRO da fileira (UNIQUE (fk_fil_id, rac_nome) -> erro 1062);
 *   - a capacidade não pode ficar menor que o slot mais alto ocupado. Quem confere é o
 *     Trigger_rack_reducao_capacidade, no banco (erro 1644). O Java não repete essa conta:
 *     assim a regra vale para qualquer programa que altere o rack, inclusive o Python;
 *   - rack com ativos, aluguéis ou movimentações não pode ser excluído (FKs -> erro 1451).
 *     A mensagem diz quantos de cada um o rack tem.
 */
public class RackService {

    private static final int TAMANHO_NOME = 128;
    private static final int CAPACIDADE_MINIMA = 1;
    private static final int CAPACIDADE_MAXIMA = 60;

    private final RackDAO rackDAO = new RackDAO();

    public List<Rack> listar() throws SQLException {
        try (Connection con = Conexao.getConexao()) {
            return rackDAO.listar(con);
        }
    }

    /** Sem id = rack novo (INSERT); com id = alteração (UPDATE). Devolve o rack com o id. */
    public Rack salvar(Rack rack) throws SQLException {
        rack.setNome(Validacoes.texto(rack.getNome(), "Nome", TAMANHO_NOME));
        if (rack.getFileiraId() == null) {
            throw new NegocioException("Escolha a fileira do rack.");
        }
        validarCapacidade(rack.getTotalSlots());

        try (Connection con = Conexao.getConexao()) {
            if (rack.getId() == null) {
                rack.setId(rackDAO.inserir(con, rack));
            } else if (!rackDAO.atualizar(con, rack)) {
                throw new NegocioException("O rack não existe mais (pode ter sido excluído). Atualize a lista.");
            }
            return rack;

        } catch (SQLException e) {
            if (e.getErrorCode() == ErrosBanco.VALOR_DUPLICADO) {
                throw new NegocioException("Esta fileira já tem um rack chamado '" + rack.getNome() + "'.");
            }
            if (e.getErrorCode() == ErrosBanco.REFERENCIA_INEXISTENTE) {
                throw new NegocioException("A fileira escolhida não existe mais (pode ter sido excluída). Atualize a tela.");
            }
            if (e.getErrorCode() == ErrosBanco.REGRA_DO_BANCO) {
                // Na tabela rack, o único trigger que dispara SIGNAL é o da redução de capacidade
                throw new NegocioException("O banco recusou a capacidade de " + rack.getTotalSlots() + " U: "
                        + e.getMessage() + ". Mova ou retire os ativos dos slots acima de "
                        + rack.getTotalSlots() + " antes de reduzir.");
            }
            throw e;
        }
    }

    public void excluir(int id) throws SQLException {
        try (Connection con = Conexao.getConexao()) {
            try {
                if (!rackDAO.excluir(con, id)) {
                    throw new NegocioException("O rack não existe mais (pode ter sido excluído). Atualize a lista.");
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == ErrosBanco.REGISTRO_EM_USO) {
                    // Quem decide que não pode é o banco (a FK). Só depois perguntamos o motivo,
                    // com a mesma conexão, para montar uma mensagem completa.
                    throw new NegocioException(mensagemRackEmUso(rackDAO.contarVinculos(con, id)));
                }
                throw e;
            }
        }
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private static void validarCapacidade(Integer capacidade) {
        if (capacidade == null) {
            throw new NegocioException("Informe a capacidade do rack: um número inteiro de "
                    + CAPACIDADE_MINIMA + " a " + CAPACIDADE_MAXIMA + ".");
        }
        if (capacidade < CAPACIDADE_MINIMA || capacidade > CAPACIDADE_MAXIMA) {
            throw new NegocioException("A capacidade deve ficar entre " + CAPACIDADE_MINIMA + " e "
                    + CAPACIDADE_MAXIMA + " U (você informou " + capacidade + ").");
        }
    }

    /**
     * Monta a mensagem do rack que não pode ser excluído, com tudo o que aponta para ele.
     * Exemplo: "Este rack não pode ser excluído porque tem 3 ativos instalados,
     * 1 contrato de aluguel e 1 movimentação no histórico. ..."
     */
    private static String mensagemRackEmUso(RackDAO.Vinculos vinculos) {
        List<String> motivos = new ArrayList<>();
        if (vinculos.ativos() > 0) {
            motivos.add(quantidade(vinculos.ativos(), "ativo instalado", "ativos instalados"));
        }
        if (vinculos.alugueis() > 0) {
            motivos.add(quantidade(vinculos.alugueis(), "contrato de aluguel", "contratos de aluguel"));
        }
        if (vinculos.movimentacoes() > 0) {
            motivos.add(quantidade(vinculos.movimentacoes(), "movimentação no histórico", "movimentações no histórico"));
        }
        if (motivos.isEmpty()) {
            // Raro: o vínculo foi apagado entre o DELETE recusado e a contagem
            return "Este rack está sendo usado por outros cadastros e não pode ser excluído. Tente de novo.";
        }

        String mensagem = "Este rack não pode ser excluído porque tem " + juntar(motivos) + ".";
        if (vinculos.alugueis() > 0 || vinculos.movimentacoes() > 0) {
            return mensagem + " Contratos e movimentações são histórico e não são apagados, "
                    + "por isso o rack continua cadastrado.";
        }
        return mensagem + " Retire os ativos do rack primeiro.";
    }

    /** 1 -> "1 ativo instalado"; 3 -> "3 ativos instalados". */
    private static String quantidade(int numero, String singular, String plural) {
        return numero + " " + (numero == 1 ? singular : plural);
    }

    /** ["a"] -> "a"; ["a", "b", "c"] -> "a, b e c". */
    private static String juntar(List<String> itens) {
        if (itens.size() == 1) {
            return itens.get(0);
        }
        return String.join(", ", itens.subList(0, itens.size() - 1)) + " e " + itens.get(itens.size() - 1);
    }
}
