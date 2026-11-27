package Configuracao;

import Configuracao.Conexao;
import Dao.SalaDAO;
import Model.Sala;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * Teste da Etapa 6.2, executado no console (ainda sem telas).
 * No NetBeans: botão direito no projeto → Run (ou F6).
 *
 * Confere, em ordem:
 *   1. se o Java encontra o .env e conecta no MySQL;
 *   2. se o SalaDAO lê dados (SELECT);
 *   3. se o SalaDAO grava (INSERT) — dentro de uma transação que é DESFEITA no fim,
 *      então o banco não muda;
 *   4. como chega no Java um erro do banco (nome de sala repetido → código 1062).
 *
 * Esta classe é temporária: na etapa das telas, a classe principal passa a ser a App (login).
 */
public class TesteConexao {

    public static void main(String[] args) {
        SalaDAO salaDAO = new SalaDAO();

        try (Connection con = Conexao.getConexao()) {

            // 1. Conexão
            DatabaseMetaData banco = con.getMetaData();
            System.out.println("1. Conectado ao " + banco.getDatabaseProductName() + " "
                    + banco.getDatabaseProductVersion() + ", banco '" + con.getCatalog() + "'");

            // 2. SELECT pelo DAO
            List<Sala> salas = salaDAO.listar(con);
            System.out.println("2. Salas cadastradas: " + salas.size());
            for (Sala sala : salas) {
                System.out.println("     " + sala.getId() + " - " + sala.getNome()
                        + " (" + sala.getDescricao() + ")");
            }

            // 3. INSERT dentro de uma transação, desfeito com rollback
            con.setAutoCommit(false);           // a partir daqui, nada é gravado sem commit()
            int novoId = salaDAO.inserir(con, new Sala(null, "Sala de Teste", "Criada pelo TesteConexao"));
            System.out.println("3. INSERT funcionou: a sala de teste recebeu o id " + novoId
                    + " e o banco agora tem " + salaDAO.listar(con).size() + " salas (dentro da transação)");
            con.rollback();                     // desfaz o INSERT
            System.out.println("   Depois do rollback: " + salaDAO.listar(con).size() + " salas (nada foi gravado)");

            // 4. Erro do banco: nome repetido viola o UNIQUE uq_sal_nome
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
