package Services;

import Configuracao.Conexao;
import Dao.FileiraDAO;
import Exceptions.ErrosBanco;
import Exceptions.NegocioException;
import Model.Fileira;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class FileiraService {

    private static final int TAMANHO_NOME = 10;

    private final FileiraDAO fileiraDAO = new FileiraDAO();

    public List<Fileira> listar() throws SQLException {
        try (Connection con = Conexao.getConexao()) {
            return fileiraDAO.listar(con);
        }
    }

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
