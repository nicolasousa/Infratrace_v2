package Configuracao;

import Configuracao.Conexao;
import Dao.SalaDAO;
import Model.Sala;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

public class TesteConexao {

    public static void main(String[] args) {
        SalaDAO salaDAO = new SalaDAO();

        try (Connection con = Conexao.getConexao()) {

            DatabaseMetaData banco = con.getMetaData();
            System.out.println("1. Conectado ao " + banco.getDatabaseProductName() + " "
                    + banco.getDatabaseProductVersion() + ", banco '" + con.getCatalog() + "'");

            List<Sala> salas = salaDAO.listar(con);
            System.out.println("2. Salas cadastradas: " + salas.size());
            for (Sala sala : salas) {
                System.out.println("     " + sala.getId() + " - " + sala.getNome()
                        + " (" + sala.getDescricao() + ")");
            }

            con.setAutoCommit(false);
            int novoId = salaDAO.inserir(con, new Sala(null, "Sala de Teste", "Criada pelo TesteConexao"));
            System.out.println("3. INSERT funcionou: a sala de teste recebeu o id " + novoId
                    + " e o banco agora tem " + salaDAO.listar(con).size() + " salas (dentro da transação)");
            con.rollback();
            System.out.println("   Depois do rollback: " + salaDAO.listar(con).size() + " salas (nada foi gravado)");

            if (!salas.isEmpty()) {
                try {
                    salaDAO.inserir(con, new Sala(null, salas.get(0).getNome(), null));
                } catch (SQLException e) {
                    System.out.println("4. Erro esperado do MySQL, código " + e.getErrorCode() + ": " + e.getMessage());
                }
                con.rollback();
            }

            System.out.println("Tudo certo: Java + JDBC + MySQL funcionando.");

        } catch (SQLException e) {
            System.err.println("Erro de banco (código " + e.getErrorCode() + "): " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Erro de configuração: " + e.getMessage());
        }
    }
}
