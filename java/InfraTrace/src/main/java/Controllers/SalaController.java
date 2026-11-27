package Controllers;

import Model.Sala;
import Services.SalaService;
import View.PainelSalas;

/**
 * Liga a tela de salas (PainelSalas) às regras de negócio (SalaService).
 *
 * Quando o usuário clica num botão, a tela chama um método daqui. O controller:
 *   1. pega os dados digitados na tela;
 *   2. chama o service;
 *   3. manda a tela se atualizar ou mostrar uma mensagem.
 *
 * Não tem SQL (fica no DAO), não tem regra de negócio (fica no service) e não monta
 * componentes Swing (fica na view). É ele que decide O QUE a tela mostra depois de cada ação.
 * Os erros são mostrados pelo TratamentoDeErros, igual em todas as telas.
 */
public class SalaController {

    private final PainelSalas tela;
    private final SalaService service = new SalaService();

    public SalaController(PainelSalas tela) {
        this.tela = tela;
    }

    /** Busca as salas no banco e mostra na tabela. A tela chama isto sempre que aparece. */
    public void carregar() {
        try {
            tela.mostrarSalas(service.listar());
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    /** Botão Salvar: cadastra uma sala nova ou grava as alterações da sala selecionada. */
    public void salvar() {
        Sala selecionada = tela.getSalaSelecionada();
        Integer id = (selecionada == null) ? null : selecionada.getId();
        Sala sala = new Sala(id, tela.getNome(), tela.getDescricao());

        try {
            boolean nova = (id == null);
            Sala gravada = service.salvar(sala);

            tela.mostrarSalas(service.listar());   // recarrega a tabela com o que está no banco
            tela.selecionarSala(gravada.getId());  // deixa a sala gravada selecionada
            tela.mostrarInformacao(nova
                    ? "Sala '" + gravada.getNome() + "' cadastrada (id " + gravada.getId() + ")."
                    : "Alterações da sala '" + gravada.getNome() + "' salvas.");
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    /** Botão Excluir: pede confirmação e exclui a sala selecionada. */
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
