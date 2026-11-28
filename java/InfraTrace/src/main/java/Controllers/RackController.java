package Controllers;

import Model.Fileira;
import Model.Rack;
import Services.AtivoService;
import Services.FileiraService;
import Services.RackService;
import View.PainelRacks;

public class RackController {

    private final PainelRacks tela;
    private final RackService rackService = new RackService();
    private final FileiraService fileiraService = new FileiraService();
    private final AtivoService ativoService = new AtivoService();

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

    public void cadastrar() {
        gravar(true);
    }

    public void alterar() {
        gravar(false);
    }

    private void gravar(boolean novo) {
        Rack selecionado = tela.getRackSelecionado();
        Integer id = (novo || selecionado == null) ? null : selecionado.getId();
        Fileira fileira = tela.getFileiraEscolhida();
        Rack rack = new Rack(id, tela.getNome(), tela.getCapacidade(), fileira == null ? null : fileira.getId());

        try {
            Rack gravado = (novo ? rackService.inserir(rack) : rackService.alterar(rack));

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

    public void verAtivos() {
        Rack selecionado = tela.getRackSelecionado();
        if (selecionado == null) {
            tela.mostrarAviso("Selecione na tabela o rack para ver os ativos.");
            return;
        }
        try {
            tela.mostrarAtivosDoRack(selecionado, ativoService.listarDoRack(selecionado.getId()));
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
