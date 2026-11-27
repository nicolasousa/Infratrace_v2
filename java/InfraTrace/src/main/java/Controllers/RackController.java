package Controllers;

import Model.Fileira;
import Model.Rack;
import Services.FileiraService;
import Services.RackService;
import View.PainelRacks;

/**
 * Liga a tela de racks (PainelRacks) aos services. Mesmo papel do FileiraController.
 *
 * Usa dois services: FileiraService para as fileiras (campo "Fileira") e RackService
 * para os racks (tabela e gravação).
 */
public class RackController {

    private final PainelRacks tela;
    private final RackService rackService = new RackService();
    private final FileiraService fileiraService = new FileiraService();

    public RackController(PainelRacks tela) {
        this.tela = tela;
    }

    /**
     * Carrega as fileiras (para o campo de escolha) e os racks (para a tabela).
     * A tela chama isto sempre que aparece, então uma fileira criada na aba Fileiras já aparece aqui.
     */
    public void carregar() {
        try {
            tela.mostrarFileiras(fileiraService.listar());
            tela.mostrarRacks(rackService.listar());
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    /** Botão Salvar: cadastra um rack novo ou grava as alterações do selecionado. */
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

    /** Botão Excluir: pede confirmação e exclui o rack selecionado. */
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
