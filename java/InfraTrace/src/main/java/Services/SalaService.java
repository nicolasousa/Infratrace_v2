package Services;

import Configuracao.Conexao;
import Dao.SalaDAO;
import Exceptions.ErrosBanco;
import Exceptions.NegocioException;
import Model.Sala;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SalaService {

    private static final int TAMANHO_NOME = 128;
    private static final int TAMANHO_DESCRICAO = 512;

    private final SalaDAO salaDAO = new SalaDAO();

    public List<Sala> listar() throws SQLException {
        try (Connection con = Conexao.getConexao()) {
            return salaDAO.listar(con);
        }
    }

    public Sala salvar(Sala sala) throws SQLException {
        sala.setNome(Validacoes.texto(sala.getNome(), "Nome", TAMANHO_NOME));
        sala.setDescricao(Validacoes.textoOpcional(sala.getDescricao(), "Descrição", TAMANHO_DESCRICAO));

        try (Connection con = Conexao.getConexao()) {
            if (sala.getId() == null) {
                sala.setId(salaDAO.inserir(con, sala));
            } else if (!salaDAO.atualizar(con, sala)) {
                throw new NegocioException("A sala não existe mais (pode ter sido excluída). Atualize a lista.");
            }
            return sala;

        } catch (SQLException e) {
            if (e.getErrorCode() == ErrosBanco.VALOR_DUPLICADO) {
                throw new NegocioException("Já existe uma sala com o nome '" + sala.getNome() + "'.");
            }
            throw e;
        }
    }

    public void excluir(int id) throws SQLException {
        try (Connection con = Conexao.getConexao()) {
            if (!salaDAO.excluir(con, id)) {
                throw new NegocioException("A sala não existe mais (pode ter sido excluída). Atualize a lista.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ErrosBanco.REGISTRO_EM_USO) {
                throw new NegocioException("Esta sala tem fileiras cadastradas e não pode ser excluída. "
                        + "Exclua as fileiras dela primeiro.");
            }
            throw e;
        }
    }
}
