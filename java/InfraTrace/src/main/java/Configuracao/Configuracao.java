package Configuracao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Configuracao {

    private static final String NOME_ARQUIVO = ".env";

    private static Map<String, String> valoresDoArquivo;

    private Configuracao() {
    }

    public static String obter(String chave) {
        String valor = System.getenv(chave);
        if (valor != null) {
            return valor;
        }
        valor = valoresDoArquivo().get(chave);
        if (valor != null) {
            return valor;
        }
        throw new IllegalStateException("Configuração '" + chave + "' não encontrada. "
                + "Copie o arquivo .env.example para .env, na raiz do projeto, e preencha os valores.");
    }

    private static synchronized Map<String, String> valoresDoArquivo() {
        if (valoresDoArquivo == null) {
            valoresDoArquivo = lerArquivo(localizarArquivo());
        }
        return valoresDoArquivo;
    }

    private static Path localizarArquivo() {
        Path pasta = Path.of("").toAbsolutePath();
        while (pasta != null) {
            Path candidato = pasta.resolve(NOME_ARQUIVO);
            if (Files.isRegularFile(candidato)) {
                return candidato;
            }
            pasta = pasta.getParent();
        }
        return null;
    }

    private static Map<String, String> lerArquivo(Path arquivo) {
        Map<String, String> valores = new HashMap<>();
        if (arquivo == null) {
            return valores;
        }

        List<String> linhas;
        try {
            linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler o arquivo " + arquivo + ": " + e.getMessage(), e);
        }

        for (String linha : linhas) {
            String texto = linha.replace("﻿", "").trim();
            if (texto.isEmpty() || texto.startsWith("#")) {
                continue;
            }
            int posicaoIgual = texto.indexOf('=');
            if (posicaoIgual <= 0) {
                continue;
            }
            String chave = texto.substring(0, posicaoIgual).trim();
            String valor = texto.substring(posicaoIgual + 1).trim();
            valores.put(chave, valor);
        }
        return valores;
    }
}
