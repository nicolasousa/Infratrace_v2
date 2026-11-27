package Controllers;

import Exceptions.NegocioException;
import br.com.infratrace.integracao.RelatorioApi;
import View.DialogoRelatorioGarantias;

public class RelatorioGarantiasController {

    private final DialogoRelatorioGarantias tela;
    private final RelatorioApi api = new RelatorioApi();

    public RelatorioGarantiasController(DialogoRelatorioGarantias tela) {
        this.tela = tela;
    }

    public void gerar() {
        try {
            tela.mostrarRelatorio(api.garantias(tela.getDias()));
        } catch (NegocioException e) {
            tela.mostrarAviso(e.getMessage());
        } catch (Exception e) {
            tela.limparRelatorio();
            tela.mostrarErro(e.getMessage());
        }
    }
}
