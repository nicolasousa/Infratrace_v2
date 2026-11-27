package Controllers;

import Exceptions.ErrosBanco;
import Exceptions.NegocioException;
import View.PainelBase;

import java.sql.SQLException;

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
