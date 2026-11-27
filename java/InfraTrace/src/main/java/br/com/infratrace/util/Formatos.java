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

public final class Formatos {

    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final Locale BRASIL = Locale.of("pt", "BR");

    private static final Pattern DINHEIRO = Pattern.compile("\\d{1,3}(\\.\\d{3})*(,\\d{1,2})?|\\d+(,\\d{1,2})?");

    private Formatos() {
    }

    public static String data(LocalDate data) {
        return (data == null) ? "" : DATA.format(data);
    }

    public static String dinheiro(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        NumberFormat formato = NumberFormat.getNumberInstance(BRASIL);
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return formato.format(valor);
    }

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
        return new BigDecimal(limpo.replace(".", "").replace(',', '.'));
    }
}
