package Configuracao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Abre conexões JDBC com o MySQL, usando os dados lidos pela classe Configuracao.
 *
 * Cada chamada de getConexao() abre uma conexão NOVA. Quem pede é responsável por fechá-la,
 * sempre com try-with-resources:
 *
 *     try (Connection con = Conexao.getConexao()) {
 *         ... usa a conexão ...
 *     }   // aqui o Java chama con.close() sozinho, mesmo se der erro
 *
 * Não é preciso chamar Class.forName("com.mysql.cj.jdbc.Driver"): desde o JDBC 4 o
 * DriverManager encontra o driver sozinho, porque o mysql-connector-j está no pom.xml.
 */
public final class Conexao {

    private Conexao() {
    }

    public static Connection getConexao() throws SQLException {
        // Ex.: jdbc:mysql://localhost:3306/infratrace_v2
        String url = "jdbc:mysql://"
                + Configuracao.obter("INFRATRACE_DB_HOST") + ":"
                + Configuracao.obter("INFRATRACE_DB_PORT") + "/"
                + Configuracao.obter("INFRATRACE_DB_NAME");

        return DriverManager.getConnection(url,
                Configuracao.obter("INFRATRACE_DB_USER"),
                Configuracao.obter("INFRATRACE_DB_PASSWORD"));
    }
}
