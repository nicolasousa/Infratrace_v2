package br.com.infratrace.integracao;

import Configuracao.Configuracao;
import Exceptions.NegocioException;
import Model.RelatorioGarantias;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Conversa com a API Python (pasta python/ do projeto) por HTTP.
 *
 * É o equivalente do DAO para a API: o DAO fala SQL com o MySQL, esta classe fala HTTP/JSON
 * com o Python. O endereço vem do .env (INFRATRACE_API_URL, ex.: http://127.0.0.1:5000).
 *
 * Usa o HttpClient que já vem no Java 11+ e o Gson para transformar o JSON em objetos.
 */
public class RelatorioApi {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    // LOWER_CASE_WITH_UNDERSCORES: "dias_restantes" no JSON -> campo diasRestantes no Java
    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    /** GET /relatorios/garantias?dias=N: garantias vencidas e as que vencem nos próximos N dias. */
    public RelatorioGarantias garantias(int dias) {
        String endereco = Configuracao.obter("INFRATRACE_API_URL") + "/relatorios/garantias?dias=" + dias;
        HttpRequest requisicao = HttpRequest.newBuilder(URI.create(endereco))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> resposta;
        try {
            resposta = http.send(requisicao, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (ConnectException | HttpConnectTimeoutException e) {
            throw new IllegalStateException("A API Python não está respondendo em "
                    + Configuracao.obter("INFRATRACE_API_URL") + ".\n"
                    + "Inicie a API: na pasta python do projeto, rode 'python app.py'.");
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao chamar a API Python: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A chamada à API Python foi interrompida.");
        }

        try {
            if (resposta.statusCode() == 200) {
                return gson.fromJson(resposta.body(), RelatorioGarantias.class);
            }
            // A API devolve {"erro": "..."}: 400 = parâmetro inválido; 500 = erro no banco
            String mensagem = gson.fromJson(resposta.body(), JsonObject.class).get("erro").getAsString();
            if (resposta.statusCode() == 400) {
                throw new NegocioException(mensagem);
            }
            throw new IllegalStateException("A API Python respondeu com erro: " + mensagem);
        } catch (JsonParseException | NullPointerException e) {
            throw new IllegalStateException("A API Python devolveu uma resposta inesperada (HTTP "
                    + resposta.statusCode() + ").");
        }
    }
}
