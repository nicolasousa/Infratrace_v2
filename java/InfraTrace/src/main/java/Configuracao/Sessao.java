package Configuracao;

/**
 * Quem está usando o sistema agora. Algumas gravações precisam saber o usuário: por exemplo,
 * toda movimentação guarda quem a fez (movimentacao.fk_usr_id é NOT NULL).
 *
 * PROVISÓRIO: o login ainda não existe (ele vem no módulo Acesso e Usuários). Até lá, a sessão
 * usa o Administrador criado pelo script infratrace_v2.sql (usr_id = 1). Quando o login
 * existir, a tela de login chamará Sessao.iniciar(...) com o usuário que entrou, e nenhuma
 * outra classe precisará mudar.
 */
public final class Sessao {

    private static int usuarioId = 1;
    private static String usuarioNome = "Administrador (provisório, sem login)";

    private Sessao() {
    }

    /** Chamado pelo login (módulo Acesso e Usuários). */
    public static void iniciar(int id, String nome) {
        usuarioId = id;
        usuarioNome = nome;
    }

    public static int getUsuarioId() {
        return usuarioId;
    }

    public static String getUsuarioNome() {
        return usuarioNome;
    }
}
