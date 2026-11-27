package Controllers;

import Model.Fileira;
import Model.Sala;
import Services.FileiraService;
import Services.SalaService;
import View.PainelFileiras;

public class FileiraController {

    private final PainelFileiras tela;
    private final FileiraService fileiraService = new FileiraService();
    private final SalaService salaService = new SalaService();

    public FileiraController(PainelFileiras tela) {
        this.tela = tela;
    }

    public void carregar() {
        try {
            tela.mostrarSalas(salaService.listar());
            tela.mostrarFileiras(fileiraService.listar());
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    public void salvar() {
        Fileira selecionada = tela.getFileiraSelecionada();
        Integer id = (selecionada == null) ? null : selecionada.getId();
        Sala sala = tela.getSalaEscolhida();
        Fileira fileira = new Fileira(id, tela.getNome(), sala == null ? null : sala.getId());

        try {
            boolean nova = (id == null);
            Fileira gravada = fileiraService.salvar(fileira);

            tela.mostrarFileiras(fileiraService.listar());
            tela.selecionarFileira(gravada.getId());
            tela.mostrarInformacao(nova
                    ? "Fileira '" + gravada.getNome() + "' cadastrada na " + sala.getNome() + "."
                    : "Alterações da fileira '" + gravada.getNome() + "' salvas.");
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    public void excluir() {
        Fileira selecionada = tela.getFileiraSelecionada();
        if (selecionada == null) {
            tela.mostrarAviso("Selecione na tabela a fileira que deseja excluir.");
            return;
        }
        if (!tela.confirmar("Excluir a fileira '" + selecionada.getNome() + "' da "
                + selecionada.getSalaNome() + "?\nEsta operação não pode ser desfeita.")) {
            return;
        }

        try {
            fileiraService.excluir(selecionada.getId());
            tela.mostrarFileiras(fileiraService.listar());
            tela.mostrarInformacao("Fileira '" + selecionada.getNome() + "' excluída.");
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }
}
