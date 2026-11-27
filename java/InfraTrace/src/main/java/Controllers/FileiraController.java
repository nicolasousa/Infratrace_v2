package Controllers;

import Model.Fileira;
import Model.Sala;
import Services.FileiraService;
import Services.SalaService;
import View.PainelFileiras;

/**
 * Liga a tela de fileiras (PainelFileiras) aos services. Mesmo papel do SalaController.
 *
 * Diferença: esta tela também precisa da lista de salas (para o campo "Sala"), então o
 * controller usa dois services: SalaService para as salas e FileiraService para as fileiras.
 */
public class FileiraController {

    private final PainelFileiras tela;
    private final FileiraService fileiraService = new FileiraService();
    private final SalaService salaService = new SalaService();

    public FileiraController(PainelFileiras tela) {
        this.tela = tela;
    }

    /**
     * Carrega as salas (para o campo de escolha) e as fileiras (para a tabela).
     * A tela chama isto sempre que aparece, então uma sala criada na aba Salas já aparece aqui.
     */
    public void carregar() {
        try {
            tela.mostrarSalas(salaService.listar());
            tela.mostrarFileiras(fileiraService.listar());
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    /** Botão Salvar: cadastra uma fileira nova ou grava as alterações da selecionada. */
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

    /** Botão Excluir: pede confirmação e exclui a fileira selecionada. */
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
