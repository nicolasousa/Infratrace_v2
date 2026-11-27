package Services;

import Configuracao.Conexao;
import Dao.SalaDAO;
import Exceptions.ErrosBanco;
import Exceptions.NegocioException;
import Model.Sala;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Regras de negócio das salas. É a camada entre o controller (tela) e o DAO (SQL).
 *
 * Responsabilidades:
 *   - validar os dados antes de ir ao banco (nome obrigatório, tamanhos das colunas);
 *   - abrir a conexão e chamar o DAO;
 *   - transformar erros do banco que têm significado para o usuário em NegocioException
 *     com mensagem clara (nome repetido = 1062, sala com fileiras = 1451).
 *
 * Aqui não há SQL (fica no DAO) nem nada de Swing (fica na view).
 * Cada operação é um único comando SQL, então o auto-commit do JDBC basta: não é preciso
 * abrir transação manual. Ela aparece nas operações com mais de um comando (ex.: descarte de ativo).
 */
public class SalaService {

    // Tamanhos das colunas na tabela sala (VARCHAR(128) e VARCHAR(512))
    private static final int TAMANHO_NOME = 128;
    private static final int TAMANHO_DESCRICAO = 512;

    private final SalaDAO salaDAO = new SalaDAO();

    public List<Sala> listar() throws SQLException {
        try (Connection con = Conexao.getConexao()) {
            return salaDAO.listar(con);
        }
    }

    /**
     * Grava a sala: sem id é uma sala nova (INSERT); com id é uma alteração (UPDATE).
     * Devolve a sala gravada, já com o id preenchido.
     */
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
            // A regra "nome único" é garantida pelo UNIQUE do banco (uq_sal_nome).
            // Em vez de fazer um SELECT antes, deixamos o banco recusar e traduzimos o erro.
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
            // A FK fk_fil_sala impede excluir sala que tem fileiras.
            if (e.getErrorCode() == ErrosBanco.REGISTRO_EM_USO) {
                throw new NegocioException("Esta sala tem fileiras cadastradas e não pode ser excluída. "
                        + "Exclua as fileiras dela primeiro.");
            }
            throw e;
        }
    }
}
