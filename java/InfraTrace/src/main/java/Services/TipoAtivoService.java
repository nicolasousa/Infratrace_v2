package Services;

import Configuracao.Conexao;
import Dao.TipoAtivoDAO;
import Exceptions.ErrosBanco;
import Exceptions.NegocioException;
import Model.TipoAtivo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Regras de negócio dos tipos de ativo. Mesmo padrão do SalaService.
 *
 * Regras:
 *   - nome obrigatório, com até 128 caracteres; descrição opcional, até 512;
 *   - nome único (UNIQUE uq_tpa_nome no banco -> erro 1062);
 *   - tipo usado por algum ativo não pode ser excluído (FK fk_atv_tipo_ativo -> erro 1451).
 */
public class TipoAtivoService {

    private static final int TAMANHO_NOME = 128;
    private static final int TAMANHO_DESCRICAO = 512;

    private final TipoAtivoDAO tipoAtivoDAO = new TipoAtivoDAO();

    public List<TipoAtivo> listar() throws SQLException {
        try (Connection con = Conexao.getConexao()) {
            return tipoAtivoDAO.listar(con);
        }
    }

    /** Sem id = tipo novo (INSERT); com id = alteração (UPDATE). Devolve o tipo com o id. */
    public TipoAtivo salvar(TipoAtivo tipo) throws SQLException {
        tipo.setNome(Validacoes.texto(tipo.getNome(), "Nome", TAMANHO_NOME));
        tipo.setDescricao(Validacoes.textoOpcional(tipo.getDescricao(), "Descrição", TAMANHO_DESCRICAO));

        try (Connection con = Conexao.getConexao()) {
            if (tipo.getId() == null) {
                tipo.setId(tipoAtivoDAO.inserir(con, tipo));
            } else if (!tipoAtivoDAO.atualizar(con, tipo)) {
                throw new NegocioException("O tipo de ativo não existe mais (pode ter sido excluído). Atualize a lista.");
            }
            return tipo;

        } catch (SQLException e) {
            if (e.getErrorCode() == ErrosBanco.VALOR_DUPLICADO) {
                throw new NegocioException("Já existe um tipo de ativo com o nome '" + tipo.getNome() + "'.");
            }
            throw e;
        }
    }

    public void excluir(int id) throws SQLException {
        try (Connection con = Conexao.getConexao()) {
            if (!tipoAtivoDAO.excluir(con, id)) {
                throw new NegocioException("O tipo de ativo não existe mais (pode ter sido excluído). Atualize a lista.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ErrosBanco.REGISTRO_EM_USO) {
                throw new NegocioException("Este tipo é usado por ativos cadastrados e não pode ser excluído. "
                        + "Troque o tipo desses ativos primeiro.");
            }
            throw e;
        }
    }
}
