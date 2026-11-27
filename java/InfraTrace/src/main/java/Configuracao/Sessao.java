package Configuracao;

public final class Sessao {

    private static int usuarioId = 1;
    private static String usuarioNome = "Administrador (provisório, sem login)";

    private Sessao() {
    }

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
