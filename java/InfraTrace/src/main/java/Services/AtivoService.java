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

public class AtivoService {

    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("99999999.99");

    private static final String PREFIXO_MOVIMENTACAO = "Descarte: ";
    private static final int TAMANHO_MOTIVO = 512 - PREFIXO_MOVIMENTACAO.length();

    private final AtivoDAO ativoDAO = new AtivoDAO();
    private final MovimentacaoDAO movimentacaoDAO = new MovimentacaoDAO();

    public List<Ativo> listar(FiltroAtivo filtro) throws SQLException {
        try (Connection con = Conexao.getConexao()) {
            return ativoDAO.listar(con, filtro);
        }
    }

    public List<Ativo> listarDoRack(int rackId) throws SQLException {
        try (Connection con = Conexao.getConexao()) {
            return ativoDAO.listarDoRack(con, rackId);
        }
    }

    public Ativo inserir(Ativo ativo) throws SQLException {
        ativo.setId(null);
        return gravar(ativo, true);
    }

    public Ativo alterar(Ativo ativo) throws SQLException {
        if (ativo.getId() == null) {
            throw new NegocioException("Selecione na tabela o ativo que deseja alterar.");
        }
        return gravar(ativo, false);
    }

    private Ativo gravar(Ativo ativo, boolean novo) throws SQLException {
        validar(ativo);

        try (Connection con = Conexao.getConexao()) {
            if (novo) {
                ativo.setId(ativoDAO.inserir(con, ativo));
            } else if (!ativoDAO.atualizar(con, ativo)) {
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

    public Ativo descartar(int ativoId, LocalDate data, String motivo, int usuarioId) throws SQLException {
        String motivoLimpo = Validacoes.texto(motivo, "Motivo do descarte", TAMANHO_MOTIVO);
        if (data == null) {
            throw new NegocioException("Informe a data do descarte.");
        }
        if (data.isAfter(LocalDate.now())) {
            throw new NegocioException("A data do descarte não pode estar no futuro.");
        }

        try (Connection con = Conexao.getConexao()) {
            Ativo ativo = ativoDAO.buscarPorId(con, ativoId, false);
            conferirSePodeDescartar(ativo, data);

            if (ativo.estaEmRack()) {
                movimentacaoDAO.movimentar(con, saidaDoRack(ativo, motivoLimpo, usuarioId));
            }
            ativoDAO.descartar(con, ativoId, data, motivoLimpo);
            return ativo;
        }
    }

    private static void validar(Ativo ativo) {
        ativo.setNome(Validacoes.texto(ativo.getNome(), "Nome", 128));
        ativo.setFabricante(Validacoes.texto(ativo.getFabricante(), "Fabricante", 128));
        ativo.setModelo(Validacoes.texto(ativo.getModelo(), "Modelo", 128));
        ativo.setNumSerie(Validacoes.texto(ativo.getNumSerie(), "Nº de série", 64));
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

    private static Movimentacao saidaDoRack(Ativo ativo, String motivo, int usuarioId) {
        Movimentacao mov = new Movimentacao();
        mov.setMotivo(PREFIXO_MOVIMENTACAO + motivo);
        mov.setAtivoId(ativo.getId());
        mov.setUsuarioId(usuarioId);
        return mov;
    }

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
