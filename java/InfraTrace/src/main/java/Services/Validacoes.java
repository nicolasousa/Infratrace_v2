package Services;

import Exceptions.NegocioException;

public final class Validacoes {

    private Validacoes() {
    }

    public static String texto(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            throw new NegocioException("O campo '" + campo + "' é obrigatório.");
        }
        return textoOpcional(valor, campo, tamanhoMaximo);
    }

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
