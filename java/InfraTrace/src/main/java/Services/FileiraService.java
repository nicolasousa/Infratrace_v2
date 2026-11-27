package Services;

import Configuracao.Conexao;
import Dao.FileiraDAO;
import Exceptions.ErrosBanco;
import Exceptions.NegocioException;
import Model.Fileira;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Regras de negócio das fileiras. Mesmo padrão do SalaService.
 *
 * Regras:
 *   - nome obrigatório, com até 10 caracteres (coluna fil_nome VARCHAR(10));
 *   - a fileira precisa de uma sala (fk_sal_id NOT NULL);
 *   - o nome é único DENTRO da sala: a Sala 1 e a Sala 2 podem ter, cada uma, uma fileira "A"
 *     (UNIQUE (fk_sal_id, fil_nome) no banco -> erro 1062);
 *   - fileira com racks não pode ser excluída (FK fk_rac_fileira -> erro 1451).
 */
public class FileiraService {

    private static final int TAMANHO_NOME = 10;

    private final FileiraDAO fileiraDAO = new FileiraDAO();

    public List<Fileira> listar() throws SQLException {
        try (Connection con = Conexao.getConexao()) {
            return fileiraDAO.listar(con);
        }
    }

    /** Sem id = fileira nova (INSERT); com id = alteração (UPDATE). Devolve a fileira com o id. */
    public Fileira salvar(Fileira fileira) throws SQLException {
        fileira.setNome(Validacoes.texto(fileira.getNome(), "Nome", TAMANHO_NOME));
        if (fileira.getSalaId() == null) {
            throw new NegocioException("Escolha a sala da fileira.");
        }

        try (Connection con = Conexao.getConexao()) {
            if (fileira.getId() == null) {
                fileira.setId(fileiraDAO.inserir(con, fileira));
            } else if (!fileiraDAO.atualizar(con, fileira)) {
                throw new NegocioException("A fileira não existe mais (pode ter sido excluída). Atualize a lista.");
            }
            return fileira;

        } catch (SQLException e) {
            if (e.getErrorCode() == ErrosBanco.VALOR_DUPLICADO) {
                throw new NegocioException("Esta sala já tem uma fileira chamada '" + fileira.getNome() + "'.");
            }
            if (e.getErrorCode() == ErrosBanco.REFERENCIA_INEXISTENTE) {
                throw new NegocioException("A sala escolhida não existe mais (pode ter sido excluída). Atualize a tela.");
            }
            throw e;
        }
    }

    public void excluir(int id) throws SQLException {
        try (Connection con = Conexao.getConexao()) {
            if (!fileiraDAO.excluir(con, id)) {
                throw new NegocioException("A fileira não existe mais (pode ter sido excluída). Atualize a lista.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ErrosBanco.REGISTRO_EM_USO) {
                throw new NegocioException("Esta fileira tem racks cadastrados e não pode ser excluída. "
                        + "Exclua os racks dela primeiro.");
            }
            throw e;
        }
    }
}
