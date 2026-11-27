package Configuracao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lê as configurações do sistema: dados de acesso ao banco e endereço da API Python.
 *
 * Para cada chave pedida (ex.: "INFRATRACE_DB_HOST"), a busca é nesta ordem:
 *   1. Variável de ambiente do sistema operacional. Se existir, ela vence.
 *   2. Arquivo ".env" na raiz do projeto (o mesmo arquivo que o Python lê).
 *
 * O .env tem uma configuração por linha, no formato CHAVE=VALOR.
 * Linhas vazias e linhas começando com # são ignoradas.
 *
 * A senha fica só no .env de cada integrante, que está no .gitignore.
 * Por isso ela nunca aparece no código nem no GitHub.
 */
public final class Configuracao {

    private static final String NOME_ARQUIVO = ".env";

    /** Conteúdo do .env. Fica null até a primeira consulta; o arquivo é lido uma única vez. */
    private static Map<String, String> valoresDoArquivo;

    private Configuracao() {
        // Só tem métodos estáticos: ninguém precisa criar um objeto Configuracao.
    }

    /**
     * Devolve o valor de uma configuração.
     * Lança IllegalStateException se a chave não estiver nem no ambiente nem no .env.
     */
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

    /**
     * Procura o .env na pasta onde o programa foi iniciado e, se não achar, nas pastas acima.
     * No NetBeans o programa é iniciado em java/InfraTrace, e o .env fica duas pastas acima,
     * na raiz do repositório. Devolve null se não encontrar.
     */
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

    /** Lê as linhas CHAVE=VALOR do arquivo. Sem arquivo, devolve um mapa vazio. */
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
            // Alguns editores gravam um caractere invisível (BOM) no início do arquivo: removemos.
            String texto = linha.replace("﻿", "").trim();
            if (texto.isEmpty() || texto.startsWith("#")) {
                continue;
            }
            int posicaoIgual = texto.indexOf('=');
            if (posicaoIgual <= 0) {
                continue; // linha sem "CHAVE=": ignorada
            }
            // Corta só no PRIMEIRO "=": assim uma senha que contenha "=" continua inteira.
            String chave = texto.substring(0, posicaoIgual).trim();
            String valor = texto.substring(posicaoIgual + 1).trim();
            valores.put(chave, valor);
        }
        return valores;
    }
}
