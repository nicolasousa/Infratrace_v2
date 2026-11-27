package br.com.infratrace.util;

import Exceptions.NegocioException;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Conversão entre o texto da tela e os tipos do Java, no formato brasileiro:
 *   datas:   "25/11/2026"  <->  LocalDate
 *   valores: "25.000,50"   <->  BigDecimal
 *
 * A tela usa os métodos de formatar (mostrar); o controller usa os de ler (o que foi digitado).
 * Os de ler lançam NegocioException com uma mensagem pronta quando o texto não serve.
 */
public final class Formatos {

    /**
     * "uuuu" é o ano. STRICT recusa datas que não existem, como 31/02/2026
     * (sem STRICT, o Java "corrigiria" para 28/02 sem avisar).
     */
    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final Locale BRASIL = Locale.of("pt", "BR");

    /**
     * Valores aceitos: "25000", "25000,5", "25000,50" e "25.000,50".
     * O ponto só é aceito como separador de milhar (de 3 em 3 dígitos) e os centavos usam vírgula.
     * "25000.50" é recusado de propósito: no Brasil, isso poderia ser lido como 2.500.050.
     */
    private static final Pattern DINHEIRO = Pattern.compile("\\d{1,3}(\\.\\d{3})*(,\\d{1,2})?|\\d+(,\\d{1,2})?");

    private Formatos() {
    }

    /** LocalDate -> "25/11/2026" (null -> texto vazio). */
    public static String data(LocalDate data) {
        return (data == null) ? "" : DATA.format(data);
    }

    /** BigDecimal -> "25.000,00" (null -> texto vazio). */
    public static String dinheiro(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        NumberFormat formato = NumberFormat.getNumberInstance(BRASIL);
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return formato.format(valor);
    }

    /** "25/11/2026" -> LocalDate. Texto vazio -> null (campo opcional). */
    public static LocalDate lerData(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim(), DATA);
        } catch (DateTimeParseException e) {
            throw new NegocioException("O campo '" + campo + "' precisa de uma data válida no formato "
                    + "dd/mm/aaaa (ex.: 25/11/2026). Você digitou: " + texto.trim());
        }
    }

    /** "25.000,50" -> 25000.50. Texto vazio -> null (campo opcional). */
    public static BigDecimal lerDinheiro(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpo = texto.trim();
        if (limpo.startsWith("R$")) {
            limpo = limpo.substring(2).trim();
        }
        if (!DINHEIRO.matcher(limpo).matches()) {
            throw new NegocioException("O campo '" + campo + "' precisa de um valor em reais, com vírgula "
                    + "nos centavos (ex.: 25000,00 ou 25.000,00). Você digitou: " + texto.trim());
        }
        // Tira os pontos de milhar e troca a vírgula por ponto, que é o formato do BigDecimal
        return new BigDecimal(limpo.replace(".", "").replace(',', '.'));
    }
}
