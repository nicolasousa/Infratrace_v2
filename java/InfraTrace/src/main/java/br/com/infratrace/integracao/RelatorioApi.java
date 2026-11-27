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

public class RelatorioApi {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

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
