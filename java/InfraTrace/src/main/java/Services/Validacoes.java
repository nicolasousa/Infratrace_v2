package Services;

import Exceptions.NegocioException;

/**
 * Validações simples reaproveitadas pelos services (mesma ideia do Validacoes do v1).
 * Todas lançam NegocioException com uma mensagem pronta para a tela.
 */
public final class Validacoes {

    private Validacoes() {
    }

    /**
     * Texto obrigatório: não pode ficar vazio nem passar do tamanho da coluna.
     * Devolve o texto sem espaços nas pontas.
     */
    public static String texto(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            throw new NegocioException("O campo '" + campo + "' é obrigatório.");
        }
        return textoOpcional(valor, campo, tamanhoMaximo);
    }

    /**
     * Texto opcional: pode ficar vazio (vira null, e o banco grava NULL),
     * mas, se preenchido, precisa caber na coluna.
     */
    public static String textoOpcional(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpo = valor.trim();
        if (limpo.length() > tamanhoMaximo) {
            throw new NegocioException("O campo '" + campo + "' aceita no máximo "
                    + tamanhoMaximo + " caracteres (você digitou " + limpo.length() + ").");
        }
        return limpo;
    }
}
