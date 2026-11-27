package Controllers;

import Exceptions.NegocioException;
import br.com.infratrace.integracao.RelatorioApi;
import View.DialogoRelatorioGarantias;

/**
 * Liga a janela do relatório de garantias à API Python.
 *
 * Diferente dos outros controllers, este não usa um service com JDBC: quem consulta o banco
 * e monta o relatório é o Python. O Java só pede (RelatorioApi) e mostra o resultado.
 */
public class RelatorioGarantiasController {

    private final DialogoRelatorioGarantias tela;
    private final RelatorioApi api = new RelatorioApi();

    public RelatorioGarantiasController(DialogoRelatorioGarantias tela) {
        this.tela = tela;
    }

    /** Botão Gerar: busca o relatório com o prazo escolhido. */
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
