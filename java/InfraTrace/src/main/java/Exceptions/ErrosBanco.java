package Exceptions;

import java.sql.SQLException;

/**
 * Códigos de erro do MySQL usados no sistema e a tradução deles em mensagens para o usuário.
 *
 * Quando o MySQL recusa um comando, o JDBC lança SQLException e e.getErrorCode() diz o motivo.
 * Os services tratam os casos em que sabem dar uma mensagem mais específica
 * (ex.: "Já existe uma sala com o nome 'X'"). O que sobrar chega ao controller, que usa
 * traduzir() para mostrar algo compreensível em vez da mensagem técnica.
 */
public final class ErrosBanco {

    /** Valor repetido em coluna UNIQUE. */
    public static final int VALOR_DUPLICADO = 1062;
    /** Tentativa de excluir um registro que outro registro referencia (FK). */
    public static final int REGISTRO_EM_USO = 1451;
    /** FK apontando para um registro que não existe. */
    public static final int REFERENCIA_INEXISTENTE = 1452;
    /** SIGNAL SQLSTATE '45000' disparado por um dos nossos triggers ou procedures. */
    public static final int REGRA_DO_BANCO = 1644;
    /** Violação de uma CHECK constraint. */
    public static final int CHECK_VIOLADO = 3819;
    /** Usuário ou senha do MySQL incorretos. */
    public static final int ACESSO_NEGADO = 1045;
    /** O banco informado não existe. */
    public static final int BANCO_INEXISTENTE = 1049;

    private ErrosBanco() {
    }

    public static String traduzir(SQLException e) {
        // SQLState começando com "08" = falha de conexão (MySQL desligado, porta errada...)
        if (e.getSQLState() != null && e.getSQLState().startsWith("08")) {
            return "Não foi possível conectar ao MySQL. Verifique se o serviço está ligado "
                    + "e se o host e a porta do arquivo .env estão corretos.";
        }
        return switch (e.getErrorCode()) {
            // A mensagem do trigger já foi escrita para o usuário (ex.: "Slots já ocupados...")
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
