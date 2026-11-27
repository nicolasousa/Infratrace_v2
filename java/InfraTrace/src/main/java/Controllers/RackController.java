package Controllers;

import Model.Fileira;
import Model.Rack;
import Services.FileiraService;
import Services.RackService;
import View.PainelRacks;

public class RackController {

    private final PainelRacks tela;
    private final RackService rackService = new RackService();
    private final FileiraService fileiraService = new FileiraService();

    public RackController(PainelRacks tela) {
        this.tela = tela;
    }

    public void carregar() {
        try {
            tela.mostrarFileiras(fileiraService.listar());
            tela.mostrarRacks(rackService.listar());
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    public void salvar() {
        Rack selecionado = tela.getRackSelecionado();
        Integer id = (selecionado == null) ? null : selecionado.getId();
        Fileira fileira = tela.getFileiraEscolhida();
        Rack rack = new Rack(id, tela.getNome(), tela.getCapacidade(), fileira == null ? null : fileira.getId());

        try {
            boolean novo = (id == null);
            Rack gravado = rackService.salvar(rack);

            tela.mostrarRacks(rackService.listar());
            tela.selecionarRack(gravado.getId());
            tela.mostrarInformacao(novo
                    ? "Rack '" + gravado.getNome() + "' cadastrado na " + fileira + ", com "
                            + gravado.getTotalSlots() + " U."
                    : "Alterações do rack '" + gravado.getNome() + "' salvas.");
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    public void excluir() {
        Rack selecionado = tela.getRackSelecionado();
        if (selecionado == null) {
            tela.mostrarAviso("Selecione na tabela o rack que deseja excluir.");
            return;
        }
        if (!tela.confirmar("Excluir o rack " + selecionado + "?\nEsta operação não pode ser desfeita.")) {
            return;
        }

        try {
            rackService.excluir(selecionado.getId());
            tela.mostrarRacks(rackService.listar());
            tela.mostrarInformacao("Rack '" + selecionado.getNome() + "' excluído.");
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }
}
