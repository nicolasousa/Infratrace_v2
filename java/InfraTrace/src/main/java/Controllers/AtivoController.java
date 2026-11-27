package Controllers;

import Configuracao.Sessao;
import Exceptions.NegocioException;
import Model.Ativo;
import Model.TipoAtivo;
import Services.AtivoService;
import Services.RackService;
import Services.TipoAtivoService;
import br.com.infratrace.util.Formatos;
import View.PainelAtivos;

import java.time.LocalDate;

/**
 * Liga a tela de ativos (PainelAtivos) aos services.
 *
 * Usa três services: TipoAtivoService (tipos, para o filtro e o formulário), RackService
 * (racks, para o filtro de local) e AtivoService (a lista, a gravação e o descarte).
 *
 * Aqui também acontece a conversão do texto digitado (datas e valores) para os tipos do Java,
 * com o Formatos. Se o texto não servir, o Formatos lança NegocioException e o aviso aparece
 * como qualquer outra regra.
 */
public class AtivoController {

    private final PainelAtivos tela;
    private final AtivoService ativoService = new AtivoService();
    private final TipoAtivoService tipoAtivoService = new TipoAtivoService();
    private final RackService rackService = new RackService();

    public AtivoController(PainelAtivos tela) {
        this.tela = tela;
    }

    /** Carrega as opções dos filtros e do formulário e depois a lista. Chamado quando a tela aparece. */
    public void carregar() {
        try {
            tela.mostrarOpcoes(tipoAtivoService.listar(), rackService.listar());
            tela.mostrarAtivos(ativoService.listar(tela.getFiltro()));
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    /** Recarrega só a lista, com os filtros atuais. Chamado quando um filtro muda. */
    public void filtrar() {
        try {
            tela.mostrarAtivos(ativoService.listar(tela.getFiltro()));
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    /** Botão Salvar: cadastra um ativo novo (no estoque) ou grava as alterações do selecionado. */
    public void salvar() {
        Ativo selecionado = tela.getAtivoSelecionado();

        try {
            Ativo ativo = new Ativo();
            ativo.setId(selecionado == null ? null : selecionado.getId());
            ativo.setNome(tela.getNome());
            ativo.setFabricante(tela.getFabricante());
            ativo.setModelo(tela.getModelo());
            ativo.setNumSerie(tela.getNumSerie());
            ativo.setPatrimonio(tela.getPatrimonio());
            TipoAtivo tipo = tela.getTipoEscolhido();
            ativo.setTipoId(tipo == null ? null : tipo.getId());
            ativo.setStatus(tela.getStatusEscolhido());
            // Texto -> LocalDate / BigDecimal. Texto inválido vira aviso (NegocioException).
            ativo.setDataAquisicao(Formatos.lerData(tela.getDataAquisicao(), "Data de aquisição"));
            ativo.setValorCompra(Formatos.lerDinheiro(tela.getValorCompra(), "Valor de compra"));
            ativo.setGarantia(Formatos.lerData(tela.getGarantia(), "Garantia até"));

            boolean novo = (ativo.getId() == null);
            Ativo gravado = ativoService.salvar(ativo);

            tela.mostrarAtivos(ativoService.listar(tela.getFiltro()));
            boolean apareceNaLista = tela.selecionarAtivo(gravado.getId());
            String mensagem = novo
                    ? "Ativo '" + gravado.getNome() + "' cadastrado no estoque (id " + gravado.getId() + ")."
                    : "Alterações do ativo '" + gravado.getNome() + "' salvas.";
            if (!apareceNaLista) {
                mensagem += " Ele não aparece na lista por causa dos filtros.";
            }
            tela.mostrarInformacao(mensagem);
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    /**
     * Botão Descartar: pede data e motivo e descarta o ativo selecionado.
     * Se a data ou o motivo não servirem, mostra o aviso e reabre a janela com o que já foi
     * digitado, para o usuário só corrigir (e não digitar tudo de novo).
     */
    public void descartar() {
        Ativo selecionado = tela.getAtivoSelecionado();
        if (selecionado == null) {
            tela.mostrarAviso("Selecione na tabela o ativo que deseja descartar.");
            return;
        }

        PainelAtivos.DadosDescarte dados = tela.pedirDadosDescarte(selecionado, null);
        while (dados != null) {   // null = o usuário clicou em Cancelar
            try {
                LocalDate data = Formatos.lerData(dados.data(), "Data do descarte");
                Ativo antes = ativoService.descartar(selecionado.getId(), data, dados.motivo(), Sessao.getUsuarioId());

                tela.mostrarAtivos(ativoService.listar(tela.getFiltro()));
                tela.selecionarAtivo(antes.getId());
                tela.mostrarInformacao("Ativo '" + antes.getNome() + "' descartado."
                        + (antes.estaEmRack()
                                ? " Ele saiu do rack " + antes.getRackNome() + " e a saída foi registrada no histórico."
                                : ""));
                return;

            } catch (NegocioException e) {
                tela.mostrarAviso(e.getMessage());
                dados = tela.pedirDadosDescarte(selecionado, dados);   // reabre com o que foi digitado
            } catch (Exception e) {
                TratamentoDeErros.mostrar(tela, e);
                return;
            }
        }
    }
}
