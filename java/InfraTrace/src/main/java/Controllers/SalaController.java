package Controllers;

import Model.Sala;
import Services.SalaService;
import View.PainelSalas;

public class SalaController {

    private final PainelSalas tela;
    private final SalaService service = new SalaService();

    public SalaController(PainelSalas tela) {
        this.tela = tela;
    }

    public void carregar() {
        try {
            tela.mostrarSalas(service.listar());
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    public void salvar() {
        Sala selecionada = tela.getSalaSelecionada();
        Integer id = (selecionada == null) ? null : selecionada.getId();
        Sala sala = new Sala(id, tela.getNome(), tela.getDescricao());

        try {
            boolean nova = (id == null);
            Sala gravada = service.salvar(sala);

            tela.mostrarSalas(service.listar());
            tela.selecionarSala(gravada.getId());
            tela.mostrarInformacao(nova
                    ? "Sala '" + gravada.getNome() + "' cadastrada (id " + gravada.getId() + ")."
                    : "Alterações da sala '" + gravada.getNome() + "' salvas.");
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    public void excluir() {
        Sala selecionada = tela.getSalaSelecionada();
        if (selecionada == null) {
            tela.mostrarAviso("Selecione na tabela a sala que deseja excluir.");
            return;
        }
        if (!tela.confirmar("Excluir a sala '" + selecionada.getNome() + "'?\n"
                + "Esta operação não pode ser desfeita.")) {
            return;
        }

        try {
            service.excluir(selecionada.getId());
            tela.mostrarSalas(service.listar());
            tela.mostrarInformacao("Sala '" + selecionada.getNome() + "' excluída.");
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }
}
