package Controllers;

import Exceptions.ErrosBanco;
import Exceptions.NegocioException;
import View.PainelBase;

import java.sql.SQLException;

/**
 * Decide como mostrar cada tipo de erro. Usado por todos os controllers, para que todas as
 * telas tratem os erros do mesmo jeito:
 *
 *   NegocioException      -> aviso (o usuário pode corrigir: campo vazio, nome repetido...);
 *   SQLException          -> erro, com a mensagem traduzida pelo ErrosBanco;
 *   IllegalStateException -> erro de configuração (ex.: falta o arquivo .env);
 *   qualquer outro        -> erro inesperado (o detalhe vai para a aba Output do NetBeans).
 */
final class TratamentoDeErros {

    private TratamentoDeErros() {
    }

    static void mostrar(PainelBase tela, Exception e) {
        if (e instanceof NegocioException) {
            tela.mostrarAviso(e.getMessage());
        } else if (e instanceof SQLException erroBanco) {
            tela.mostrarErro(ErrosBanco.traduzir(erroBanco));
        } else if (e instanceof IllegalStateException) {
            tela.mostrarErro(e.getMessage());
        } else {
            e.printStackTrace();
            tela.mostrarErro("Erro inesperado: " + e.getMessage());
        }
    }
}
