package Configuracao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Conexao {

    private Conexao() {
    }

    public static Connection getConexao() throws SQLException {
        String url = "jdbc:mysql://"
                + Configuracao.obter("INFRATRACE_DB_HOST") + ":"
                + Configuracao.obter("INFRATRACE_DB_PORT") + "/"
                + Configuracao.obter("INFRATRACE_DB_NAME");

        return DriverManager.getConnection(url,
                Configuracao.obter("INFRATRACE_DB_USER"),
                Configuracao.obter("INFRATRACE_DB_PASSWORD"));
    }
}
