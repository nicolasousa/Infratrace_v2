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

public class AtivoController {

    private final PainelAtivos tela;
    private final AtivoService ativoService = new AtivoService();
    private final TipoAtivoService tipoAtivoService = new TipoAtivoService();
    private final RackService rackService = new RackService();

    public AtivoController(PainelAtivos tela) {
        this.tela = tela;
    }

    public void carregar() {
        try {
            tela.mostrarOpcoes(tipoAtivoService.listar(), rackService.listar());
            tela.mostrarAtivos(ativoService.listar(tela.getFiltro()));
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

    public void filtrar() {
        try {
            tela.mostrarAtivos(ativoService.listar(tela.getFiltro()));
        } catch (Exception e) {
            TratamentoDeErros.mostrar(tela, e);
        }
    }

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

    public void descartar() {
        Ativo selecionado = tela.getAtivoSelecionado();
        if (selecionado == null) {
            tela.mostrarAviso("Selecione na tabela o ativo que deseja descartar.");
            return;
        }

        PainelAtivos.DadosDescarte dados = tela.pedirDadosDescarte(selecionado, null);
        while (dados != null) {
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
                dados = tela.pedirDadosDescarte(selecionado, dados);
            } catch (Exception e) {
                TratamentoDeErros.mostrar(tela, e);
                return;
            }
        }
    }
}
