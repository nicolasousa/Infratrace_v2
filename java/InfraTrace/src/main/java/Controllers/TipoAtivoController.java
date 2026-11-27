package Controllers;

import Model.TipoAtivo;
import Services.TipoAtivoService;
import View.PainelTiposAtivo;

public class TipoAtivoController {

    private final PainelTiposAtivo tela;
    private final TipoAtivoService service = new TipoAtivoService();

    public TipoAtivoController(PainelTiposAtivo tela) {
        this.tela = tela;
    }

    public void carregar() {
        try {
            tela.mostrarTipos(service.listar());
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    public void salvar() {
        TipoAtivo selecionado = tela.getTipoSelecionado();
        Integer id = (selecionado == null) ? null : selecionado.getId();
        TipoAtivo tipo = new TipoAtivo(id, tela.getNome(), tela.getDescricao());

        try {
            boolean novo = (id == null);
            TipoAtivo gravado = service.salvar(tipo);

            tela.mostrarTipos(service.listar());
            tela.selecionarTipo(gravado.getId());
            tela.mostrarInformacao(novo
                    ? "Tipo '" + gravado.getNome() + "' cadastrado (id " + gravado.getId() + ")."
                    : "Alterações do tipo '" + gravado.getNome() + "' salvas.");
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    public void excluir() {
        TipoAtivo selecionado = tela.getTipoSelecionado();
        if (selecionado == null) {
            tela.mostrarAviso("Selecione na tabela o tipo que deseja excluir.");
            return;
        }
        if (!tela.confirmar("Excluir o tipo '" + selecionado.getNome() + "'?\n"
                + "Esta operação não pode ser desfeita.")) {
            return;
        }

        try {
            service.excluir(selecionado.getId());
            tela.mostrarTipos(service.listar());
            tela.mostrarInformacao("Tipo '" + selecionado.getNome() + "' excluído.");
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }
}
