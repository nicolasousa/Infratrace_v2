package Exceptions;

import java.sql.SQLException;

public final class ErrosBanco {

    public static final int VALOR_DUPLICADO = 1062;
    public static final int REGISTRO_EM_USO = 1451;
    public static final int REFERENCIA_INEXISTENTE = 1452;
    public static final int REGRA_DO_BANCO = 1644;
    public static final int CHECK_VIOLADO = 3819;
    public static final int ACESSO_NEGADO = 1045;
    public static final int BANCO_INEXISTENTE = 1049;

    private ErrosBanco() {
    }

    public static String traduzir(SQLException e) {
        if (e.getSQLState() != null && e.getSQLState().startsWith("08")) {
            return "Não foi possível conectar ao MySQL. Verifique se o serviço está ligado "
                    + "e se o host e a porta do arquivo .env estão corretos.";
        }
        return switch (e.getErrorCode()) {
            case REGRA_DO_BANCO -> e.getMessage();
            case VALOR_DUPLICADO -> "Já existe um registro com esse valor.";
            case REGISTRO_EM_USO -> "Este registro está sendo usado por outro cadastro e não pode ser excluído.";
            case REFERENCIA_INEXISTENTE -> "O registro relacionado não existe mais. Atualize a tela.";
            case CHECK_VIOLADO -> "Valor não permitido pelo banco: " + e.getMessage();
            case ACESSO_NEGADO -> "Usuário ou senha do MySQL incorretos. Confira o arquivo .env.";
            case BANCO_INEXISTENTE -> "O banco de dados não existe. Rode o script database/infratrace_v2.sql.";
            default -> "Erro no banco de dados (código " + e.getErrorCode() + "): " + e.getMessage();
        };
    }
}
