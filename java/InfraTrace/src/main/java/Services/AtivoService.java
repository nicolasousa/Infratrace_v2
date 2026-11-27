package Services;

import Configuracao.Conexao;
import Dao.AtivoDAO;
import Dao.MovimentacaoDAO;
import Exceptions.ErrosBanco;
import Exceptions.NegocioException;
import Model.Ativo;
import Model.FiltroAtivo;
import Model.Movimentacao;
import Model.StatusAtivo;
import br.com.infratrace.util.Formatos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Regras de negócio dos ativos.
 *
 * Cadastro e alteração:
 *   - nome, fabricante e modelo obrigatórios (até 128); nº de série obrigatório (até 64);
 *     patrimônio opcional (até 32);
 *   - nº de série e patrimônio únicos (UNIQUE no banco -> erro 1062);
 *   - tipo e status obrigatórios; DESCARTADO não pode ser escolhido aqui (só pelo descarte);
 *   - aquisição não pode estar no futuro; garantia não vence antes da aquisição;
 *   - valor de compra de 0 a 99.999.999,99 (coluna DECIMAL(10,2));
 *   - ativo novo entra no ESTOQUE. A instalação em rack é feita no módulo Ocupação e Movimentação;
 *   - ativo descartado não pode ser alterado.
 *
 * Descarte (uma TRANSAÇÃO, ver o método descartar):
 *   - data obrigatória (não pode estar no futuro nem antes da aquisição) e motivo obrigatório;
 *   - se o ativo estiver num rack, ele sai do rack e a saída é registrada em movimentacao.
 */
public class AtivoService {

    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("99999999.99");

    /**
     * Na movimentação, o motivo é gravado como "Descarte: <motivo>" (mov_motivo VARCHAR(512)).
     * Por isso o motivo do descarte aceita 512 menos o tamanho do prefixo, e não 512.
     */
    private static final String PREFIXO_MOVIMENTACAO = "Descarte: ";
    private static final int TAMANHO_MOTIVO = 512 - PREFIXO_MOVIMENTACAO.length();

    private final AtivoDAO ativoDAO = new AtivoDAO();
    private final MovimentacaoDAO movimentacaoDAO = new MovimentacaoDAO();

    public List<Ativo> listar(FiltroAtivo filtro) throws SQLException {
        try (Connection con = Conexao.getConexao()) {
            return ativoDAO.listar(con, filtro);
        }
    }

    /** Sem id = ativo novo (INSERT, no estoque); com id = alteração (UPDATE). Devolve o ativo com o id. */
    public Ativo salvar(Ativo ativo) throws SQLException {
        validar(ativo);

        try (Connection con = Conexao.getConexao()) {
            if (ativo.getId() == null) {
                ativo.setId(ativoDAO.inserir(con, ativo));
            } else if (!ativoDAO.atualizar(con, ativo)) {
                // O UPDATE não achou a linha: ou o ativo não existe, ou foi descartado
                Ativo atual = ativoDAO.buscarPorId(con, ativo.getId(), false);
                throw new NegocioException(atual == null
                        ? "O ativo não existe mais. Atualize a lista."
                        : "Este ativo foi descartado e não pode mais ser alterado.");
            }
            return ativo;

        } catch (SQLException e) {
            if (e.getErrorCode() == ErrosBanco.VALOR_DUPLICADO) {
                throw new NegocioException(mensagemDuplicado(e, ativo));
            }
            if (e.getErrorCode() == ErrosBanco.REFERENCIA_INEXISTENTE) {
                throw new NegocioException("O tipo de ativo escolhido não existe mais. Atualize a tela.");
            }
            throw e;
        }
    }

    /**
     * Descarta o ativo. Devolve o ativo como ele estava ANTES do descarte (com o rack e os
     * slots), para a tela poder dizer de onde ele saiu.
     *
     * Por que uma transação? Se o ativo está num rack, são DOIS comandos:
     *   1. INSERT em movimentacao (registra a saída do rack no histórico);
     *   2. UPDATE em ativo (status DESCARTADO, data, motivo e rack/slots NULL).
     * Os dois precisam acontecer juntos. Se o 2 falhasse depois do 1, o histórico diria que o
     * ativo saiu do rack, mas ele continuaria lá. Com setAutoCommit(false), nada fica gravado
     * de verdade até o commit; se algo falhar, o rollback desfaz tudo.
     */
    public Ativo descartar(int ativoId, LocalDate data, String motivo, int usuarioId) throws SQLException {
        String motivoLimpo = Validacoes.texto(motivo, "Motivo do descarte", TAMANHO_MOTIVO);
        if (data == null) {
            throw new NegocioException("Informe a data do descarte.");
        }
        if (data.isAfter(LocalDate.now())) {
            throw new NegocioException("A data do descarte não pode estar no futuro.");
        }

        try (Connection con = Conexao.getConexao()) {
            con.setAutoCommit(false);   // início da transação: nada é gravado até o commit
            try {
                // FOR UPDATE: trava o ativo até o fim da transação (ninguém o move enquanto isso)
                Ativo ativo = ativoDAO.buscarPorId(con, ativoId, true);
                conferirSePodeDescartar(ativo, data);

                if (ativo.estaEmRack()) {
                    movimentacaoDAO.inserir(con, saidaDoRack(ativo, motivoLimpo, usuarioId));   // passo 1
                }
                ativoDAO.descartar(con, ativoId, data, motivoLimpo);                          // passo 2

                con.commit();       // os dois passos ficam gravados juntos
                return ativo;

            } catch (SQLException | RuntimeException e) {
                con.rollback();     // desfaz o passo 1 se o passo 2 falhar (e libera a trava)
                throw e;
            }
        }
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private static void validar(Ativo ativo) {
        ativo.setNome(Validacoes.texto(ativo.getNome(), "Nome", 128));
        ativo.setFabricante(Validacoes.texto(ativo.getFabricante(), "Fabricante", 128));
        ativo.setModelo(Validacoes.texto(ativo.getModelo(), "Modelo", 128));
        ativo.setNumSerie(Validacoes.texto(ativo.getNumSerie(), "Nº de série", 64));
        // Patrimônio vazio vira NULL. O UNIQUE aceita vários NULL, mas não aceitaria dois "".
        ativo.setPatrimonio(Validacoes.textoOpcional(ativo.getPatrimonio(), "Patrimônio", 32));

        if (ativo.getTipoId() == null) {
            throw new NegocioException("Escolha o tipo do ativo.");
        }
        if (ativo.getStatus() == null) {
            throw new NegocioException("Escolha o status do ativo.");
        }
        if (ativo.getStatus() == StatusAtivo.DESCARTADO) {
            throw new NegocioException("Para descartar um ativo, use o botão Descartar: ele pede a data "
                    + "e o motivo e tira o ativo do rack.");
        }

        LocalDate aquisicao = ativo.getDataAquisicao();
        if (aquisicao != null && aquisicao.isAfter(LocalDate.now())) {
            throw new NegocioException("A data de aquisição não pode estar no futuro.");
        }
        if (aquisicao != null && ativo.getGarantia() != null && ativo.getGarantia().isBefore(aquisicao)) {
            throw new NegocioException("A garantia não pode vencer antes da data de aquisição ("
                    + Formatos.data(aquisicao) + ").");
        }

        BigDecimal valor = ativo.getValorCompra();
        if (valor != null) {
            if (valor.signum() < 0) {
                throw new NegocioException("O valor de compra não pode ser negativo.");
            }
            if (valor.compareTo(VALOR_MAXIMO) > 0) {
                throw new NegocioException("O valor de compra aceita no máximo R$ 99.999.999,99.");
            }
        }
    }

    private static void conferirSePodeDescartar(Ativo ativo, LocalDate data) {
        if (ativo == null) {
            throw new NegocioException("O ativo não existe mais. Atualize a lista.");
        }
        if (ativo.isDescartado()) {
            throw new NegocioException("Este ativo já foi descartado em " + Formatos.data(ativo.getDataDescarte()) + ".");
        }
        if (ativo.getDataAquisicao() != null && data.isBefore(ativo.getDataAquisicao())) {
            throw new NegocioException("A data do descarte não pode ser anterior à data de aquisição ("
                    + Formatos.data(ativo.getDataAquisicao()) + ").");
        }
    }

    /** Movimentação "do rack para o estoque": origem preenchida, destino vazio. */
    private static Movimentacao saidaDoRack(Ativo ativo, String motivo, int usuarioId) {
        Movimentacao mov = new Movimentacao();
        mov.setMotivo(PREFIXO_MOVIMENTACAO + motivo);
        mov.setRackOrigemId(ativo.getRackId());
        mov.setSlotInicioOrigem(ativo.getSlotInicio());
        mov.setSlotFimOrigem(ativo.getSlotFim());
        mov.setAtivoId(ativo.getId());
        mov.setUsuarioId(usuarioId);
        return mov;
    }

    /**
     * O erro 1062 traz o nome da chave repetida, por exemplo:
     *   Duplicate entry 'SN-SRV-01' for key 'ativo.uq_atv_num_serie'
     * Com o nome da chave, dá para dizer qual campo está repetido.
     */
    private static String mensagemDuplicado(SQLException e, Ativo ativo) {
        String mensagem = String.valueOf(e.getMessage());
        if (mensagem.contains("uq_atv_patrimonio")) {
            return "Já existe um ativo com o patrimônio '" + ativo.getPatrimonio() + "'.";
        }
        if (mensagem.contains("uq_atv_num_serie")) {
            return "Já existe um ativo com o número de série '" + ativo.getNumSerie() + "'.";
        }
        return "Já existe um ativo com esses dados.";
    }
}
