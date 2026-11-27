package View;

import Controllers.AtivoController;
import Model.Ativo;
import Model.FiltroAtivo;
import Model.Rack;
import Model.StatusAtivo;
import Model.TipoAtivo;
import br.com.infratrace.util.Formatos;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.HierarchyEvent;
import java.time.LocalDate;
import java.util.List;

public class PainelAtivos extends PainelBase {

    private static final String TODOS_STATUS = "Todos os status";
    private static final String TODOS_TIPOS = "Todos os tipos";
    private static final String TODOS_LOCAIS = "Todos os locais";
    private static final String SOMENTE_ESTOQUE = "Somente estoque";

    public record DadosDescarte(String data, String motivo) {
    }

    private final AtivoController controller;

    private final JComboBox<Object> filtroStatus = new JComboBox<>();
    private final JComboBox<Object> filtroTipo = new JComboBox<>();
    private final JComboBox<Object> filtroLocal = new JComboBox<>();
    private final JTextField filtroTexto = new JTextField(14);
    private final JButton botaoLimparFiltros = new JButton("Limpar filtros");
    private final JLabel rotuloQuantidade = new JLabel();
    private boolean preenchendoFiltros;

    private final DefaultTableModel modeloTabela = new DefaultTableModel(new Object[]{
        "ID", "Nome", "Tipo", "Fabricante / modelo", "Nº de série", "Patrimônio", "Status", "Local", "Garantia"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modeloTabela);
    private List<Ativo> ativos = List.of();

    private final JLabel rotuloModo = new JLabel();
    private final JLabel rotuloLocal = new JLabel();
    private final JTextField campoNome = new JTextField(20);
    private final DefaultComboBoxModel<TipoAtivo> modeloTipos = new DefaultComboBoxModel<>();
    private final JComboBox<TipoAtivo> campoTipo = new JComboBox<>(modeloTipos);
    private final JTextField campoFabricante = new JTextField(20);
    private final JTextField campoModelo = new JTextField(20);
    private final JTextField campoNumSerie = new JTextField(20);
    private final JTextField campoPatrimonio = new JTextField(20);
    private final DefaultComboBoxModel<StatusAtivo> modeloStatus =
            new DefaultComboBoxModel<>(StatusAtivo.escolhiveisNoCadastro().toArray(new StatusAtivo[0]));
    private final JComboBox<StatusAtivo> campoStatus = new JComboBox<>(modeloStatus);
    private final JTextField campoValorCompra = new JTextField(10);
    private final JTextField campoDataAquisicao = new JTextField(10);
    private final JTextField campoGarantia = new JTextField(10);
    private final JButton botaoNovo = new JButton("Novo");
    private final JButton botaoSalvar = new JButton("Salvar");
    private final JButton botaoDescartar = new JButton("Descartar...");
    private final JButton botaoAtualizar = new JButton("Atualizar");
    private final JButton botaoRelatorio = new JButton("Relatório de garantias");

    public PainelAtivos() {
        controller = new AtivoController(this);
        montarTela();
        ligarEventos();
        limparCampos();
    }

    private void montarTela() {
        JPanel topo = new JPanel(new BorderLayout(0, 6));
        topo.add(Estilo.titulo("Cadastro de Ativos"), BorderLayout.NORTH);
        topo.add(montarFiltros(), BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        configurarTabela(tabela);
        int[] larguras = {45, 110, 90, 190, 110, 90, 130, 150, 140};
        for (int coluna = 0; coluna < larguras.length; coluna++) {
            tabela.getColumnModel().getColumn(coluna).setPreferredWidth(larguras[coluna]);
        }
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        tabela.getColumnModel().getColumn(8).setMinWidth(150);
        tabela.getColumnModel().getColumn(8).setCellRenderer(new RenderizadorGarantia());
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        add(montarFormulario(), BorderLayout.SOUTH);
    }

    private JPanel montarFiltros() {
        filtroStatus.addItem(TODOS_STATUS);
        for (StatusAtivo status : StatusAtivo.values()) {
            filtroStatus.addItem(status);
        }
        filtroTipo.addItem(TODOS_TIPOS);
        filtroLocal.addItem(TODOS_LOCAIS);
        filtroLocal.addItem(SOMENTE_ESTOQUE);
        fixarLargura(filtroStatus, 165);
        fixarLargura(filtroTipo, 130);
        fixarLargura(filtroLocal, 240);
        filtroTexto.setToolTipText("Parte do nome, do nº de série ou do patrimônio. Tecle Enter para buscar.");
        rotuloQuantidade.setForeground(Estilo.CINZA_TEXTO);

        JPanel barra = new JPanel(new GridBagLayout());
        barra.setBorder(BorderFactory.createTitledBorder("Filtros"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 8);
        barra.add(filtroStatus, c);
        barra.add(filtroTipo, c);
        barra.add(filtroLocal, c);
        barra.add(new JLabel("Buscar"), c);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        barra.add(filtroTexto, c);
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        barra.add(botaoLimparFiltros, c);
        barra.add(rotuloQuantidade, c);
        return barra;
    }

    private JPanel montarFormulario() {
        JPanel formulario = criarCaixaFormulario();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 4, 3, 4);

        rotuloModo.setFont(Estilo.FONTE_NEGRITO);
        rotuloLocal.setForeground(Estilo.CINZA_TEXTO);
        JPanel linhaModo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaModo.add(rotuloModo);
        linhaModo.add(Box.createHorizontalStrut(14));
        linhaModo.add(rotuloLocal);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        formulario.add(linhaModo, c);
        c.gridwidth = 1;

        int larguraCampo = campoNome.getPreferredSize().width;
        fixarLargura(campoTipo, larguraCampo);
        fixarLargura(campoStatus, larguraCampo);

        adicionarPar(formulario, c, 1, "Nome *", campoNome, "Tipo *", campoTipo);
        adicionarPar(formulario, c, 2, "Fabricante *", campoFabricante, "Modelo *", campoModelo);
        adicionarPar(formulario, c, 3, "Nº de série *", campoNumSerie, "Patrimônio", campoPatrimonio);
        adicionarPar(formulario, c, 4, "Status *", campoStatus,
                "Valor de compra (R$)", linhaComDica(campoValorCompra, "ex.: 25.000,00"));
        adicionarPar(formulario, c, 5, "Data de aquisição", linhaComDica(campoDataAquisicao, "dd/mm/aaaa"),
                "Garantia até", linhaComDica(campoGarantia, "dd/mm/aaaa"));

        c.gridx = 1;
        c.gridy = 6;
        c.gridwidth = 3;
        formulario.add(criarLinhaBotoes(botaoNovo, botaoSalvar, botaoDescartar, botaoAtualizar, botaoRelatorio), c);
        return formulario;
    }

    private void ligarEventos() {
        botaoNovo.addActionListener(e -> limparFormulario());
        botaoSalvar.addActionListener(e -> controller.salvar());
        botaoDescartar.addActionListener(e -> controller.descartar());
        botaoAtualizar.addActionListener(e -> controller.carregar());
        botaoRelatorio.addActionListener(e -> new DialogoRelatorioGarantias(this).abrir());

        filtroStatus.addActionListener(e -> filtroMudou());
        filtroTipo.addActionListener(e -> filtroMudou());
        filtroLocal.addActionListener(e -> filtroMudou());
        filtroTexto.addActionListener(e -> controller.filtrar());
        botaoLimparFiltros.addActionListener(e -> limparFiltros());

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Ativo ativo = getAtivoSelecionado();
                if (ativo != null) {
                    preencherCampos(ativo);
                } else {
                    limparCampos();
                }
            }
        });

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                controller.carregar();
            }
        });
    }

    public void mostrarOpcoes(List<TipoAtivo> tipos, List<Rack> racks) {
        preenchendoFiltros = true;
        try {
            Integer tipoFiltrado = (filtroTipo.getSelectedItem() instanceof TipoAtivo t) ? t.getId() : null;
            filtroTipo.removeAllItems();
            filtroTipo.addItem(TODOS_TIPOS);
            for (TipoAtivo tipo : tipos) {
                filtroTipo.addItem(tipo);
            }
            selecionarPorId(filtroTipo, tipoFiltrado);

            Object localAtual = filtroLocal.getSelectedItem();
            filtroLocal.removeAllItems();
            filtroLocal.addItem(TODOS_LOCAIS);
            filtroLocal.addItem(SOMENTE_ESTOQUE);
            for (Rack rack : racks) {
                filtroLocal.addItem(rack);
            }
            if (SOMENTE_ESTOQUE.equals(localAtual)) {
                filtroLocal.setSelectedItem(SOMENTE_ESTOQUE);
            } else {
                selecionarPorId(filtroLocal, (localAtual instanceof Rack r) ? r.getId() : null);
            }

            TipoAtivo tipoEscolhido = getTipoEscolhido();
            modeloTipos.removeAllElements();
            for (TipoAtivo tipo : tipos) {
                modeloTipos.addElement(tipo);
            }
            escolherTipo(tipoEscolhido == null ? null : tipoEscolhido.getId());
        } finally {
            preenchendoFiltros = false;
        }
    }

    public void mostrarAtivos(List<Ativo> ativos) {
        this.ativos = ativos;
        modeloTabela.setRowCount(0);
        for (Ativo a : ativos) {
            modeloTabela.addRow(new Object[]{
                a.getId(), a.getNome(), a.getTipoNome(), a.getFabricante() + " " + a.getModelo(),
                a.getNumSerie(), a.getPatrimonio() == null ? "" : a.getPatrimonio(),
                a.getStatus(),
                localResumido(a),
                a.getGarantia()
            });
        }
        rotuloQuantidade.setText(ativos.size() + (ativos.size() == 1 ? " ativo" : " ativos"));
    }

    public FiltroAtivo getFiltro() {
        FiltroAtivo filtro = new FiltroAtivo();
        if (filtroStatus.getSelectedItem() instanceof StatusAtivo status) {
            filtro.setStatus(status);
        }
        if (filtroTipo.getSelectedItem() instanceof TipoAtivo tipo) {
            filtro.setTipoId(tipo.getId());
        }
        Object local = filtroLocal.getSelectedItem();
        if (local instanceof Rack rack) {
            filtro.setRackId(rack.getId());
        } else if (SOMENTE_ESTOQUE.equals(local)) {
            filtro.setSomenteEstoque(true);
        }
        filtro.setTexto(filtroTexto.getText());
        return filtro;
    }

    public Ativo getAtivoSelecionado() {
        int linha = tabela.getSelectedRow();
        return (linha < 0) ? null : ativos.get(linha);
    }

    public boolean selecionarAtivo(int id) {
        for (int linha = 0; linha < ativos.size(); linha++) {
            if (ativos.get(linha).getId() == id) {
                tabela.setRowSelectionInterval(linha, linha);
                tabela.scrollRectToVisible(tabela.getCellRect(linha, 0, true));
                return true;
            }
        }
        return false;
    }

    public String getNome() {
        return campoNome.getText();
    }

    public String getFabricante() {
        return campoFabricante.getText();
    }

    public String getModelo() {
        return campoModelo.getText();
    }

    public String getNumSerie() {
        return campoNumSerie.getText();
    }

    public String getPatrimonio() {
        return campoPatrimonio.getText();
    }

    public TipoAtivo getTipoEscolhido() {
        return (TipoAtivo) modeloTipos.getSelectedItem();
    }

    public StatusAtivo getStatusEscolhido() {
        return (StatusAtivo) modeloStatus.getSelectedItem();
    }

    public String getDataAquisicao() {
        return campoDataAquisicao.getText();
    }

    public String getValorCompra() {
        return campoValorCompra.getText();
    }

    public String getGarantia() {
        return campoGarantia.getText();
    }

    public DadosDescarte pedirDadosDescarte(Ativo ativo, DadosDescarte anterior) {
        JTextField campoData = new JTextField(anterior == null ? Formatos.data(LocalDate.now()) : anterior.data(), 10);
        JTextArea campoMotivo = new JTextArea(anterior == null ? "" : anterior.motivo(), 4, 36);
        campoMotivo.setLineWrap(true);
        campoMotivo.setWrapStyleWord(true);

        JLabel titulo = new JLabel("Descartar o ativo '" + ativo.getNome() + "'");
        titulo.setFont(Estilo.FONTE_NEGRITO);

        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 0, 2, 8);
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 0;
        painel.add(titulo, c);
        c.gridy = 1;
        painel.add(new JLabel(ativo.estaEmRack()
                ? "Ele está no rack " + ativo.getRackNome() + " (" + slots(ativo) + "). Ao descartar, ele sai do rack"
                : "Ele está no estoque (fora de rack)."), c);
        if (ativo.estaEmRack()) {
            c.gridy = 2;
            painel.add(new JLabel("e a saída fica registrada no histórico de movimentações."), c);
        }
        c.gridy = 3;
        painel.add(new JLabel("O descarte não pode ser desfeito."), c);

        c.gridwidth = 1;
        c.insets = new Insets(12, 0, 2, 8);
        c.gridy = 4;
        painel.add(new JLabel("Data do descarte *"), c);
        c.gridx = 1;
        painel.add(linhaComDica(campoData, "dd/mm/aaaa"), c);

        c.insets = new Insets(4, 0, 2, 8);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 5;
        painel.add(new JLabel("Motivo *"), c);
        c.gridx = 1;
        painel.add(new JScrollPane(campoMotivo), c);

        Object[] botoes = {"Descartar", "Cancelar"};
        int resposta = JOptionPane.showOptionDialog(this, painel, "Descartar ativo", JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, botoes, botoes[1]);
        return (resposta == 0) ? new DadosDescarte(campoData.getText(), campoMotivo.getText()) : null;
    }

    public void limparFormulario() {
        tabela.clearSelection();
        limparCampos();
        campoNome.requestFocusInWindow();
    }

    private void filtroMudou() {
        if (!preenchendoFiltros) {
            controller.filtrar();
        }
    }

    private void limparFiltros() {
        preenchendoFiltros = true;
        try {
            filtroStatus.setSelectedIndex(0);
            filtroTipo.setSelectedIndex(0);
            filtroLocal.setSelectedIndex(0);
            filtroTexto.setText("");
        } finally {
            preenchendoFiltros = false;
        }
        controller.filtrar();
    }

    private void limparCampos() {
        rotuloModo.setText("Novo ativo");
        rotuloLocal.setText("Será cadastrado no estoque.");
        rotuloLocal.setToolTipText(null);
        for (JTextField campo : camposDeTexto()) {
            campo.setText("");
        }
        modeloTipos.setSelectedItem(null);
        modeloStatus.setSelectedItem(StatusAtivo.ATIVO);
        permitirEdicao(true);
        botaoDescartar.setEnabled(false);
        limparMensagem();
    }

    private void preencherCampos(Ativo a) {
        campoNome.setText(a.getNome());
        campoFabricante.setText(a.getFabricante());
        campoModelo.setText(a.getModelo());
        campoNumSerie.setText(a.getNumSerie());
        campoPatrimonio.setText(a.getPatrimonio() == null ? "" : a.getPatrimonio());
        escolherTipo(a.getTipoId());
        modeloStatus.setSelectedItem(a.getStatus());
        campoValorCompra.setText(Formatos.dinheiro(a.getValorCompra()));
        campoDataAquisicao.setText(Formatos.data(a.getDataAquisicao()));
        campoGarantia.setText(Formatos.data(a.getGarantia()));
        rotuloLocal.setText(localCompleto(a));
        rotuloLocal.setToolTipText(a.isDescartado() ? a.getMotivoDescarte() : null);

        if (a.isDescartado()) {
            rotuloModo.setText("Ativo " + a.getId() + " (descartado: somente leitura)");
            permitirEdicao(false);
            botaoDescartar.setEnabled(false);
        } else {
            rotuloModo.setText("Editando o ativo " + a.getId());
            permitirEdicao(true);
            botaoDescartar.setEnabled(true);
        }
        limparMensagem();
    }

    private void permitirEdicao(boolean pode) {
        for (JTextField campo : camposDeTexto()) {
            campo.setEditable(pode);
        }
        campoTipo.setEnabled(pode);
        campoStatus.setEnabled(pode);
        botaoSalvar.setEnabled(pode);
    }

    private List<JTextField> camposDeTexto() {
        return List.of(campoNome, campoFabricante, campoModelo, campoNumSerie, campoPatrimonio,
                campoValorCompra, campoDataAquisicao, campoGarantia);
    }

    private void escolherTipo(Integer tipoId) {
        modeloTipos.setSelectedItem(null);
        if (tipoId == null) {
            return;
        }
        for (int i = 0; i < modeloTipos.getSize(); i++) {
            if (modeloTipos.getElementAt(i).getId().equals(tipoId)) {
                modeloTipos.setSelectedItem(modeloTipos.getElementAt(i));
                return;
            }
        }
    }

    private static void selecionarPorId(JComboBox<Object> combo, Integer id) {
        combo.setSelectedIndex(0);
        if (id == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            Integer idDoItem = (item instanceof TipoAtivo t) ? t.getId()
                    : (item instanceof Rack r) ? r.getId()
                    : null;
            if (id.equals(idDoItem)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private static String localResumido(Ativo a) {
        if (a.isDescartado()) {
            return "Descartado";
        }
        return a.estaEmRack() ? a.getRackNome() + " · " + slots(a) : "Estoque";
    }

    private static String localCompleto(Ativo a) {
        if (a.isDescartado()) {
            String motivo = a.getMotivoDescarte();
            if (motivo != null && motivo.length() > 80) {
                motivo = motivo.substring(0, 80) + "…";
            }
            return "Descartado em " + Formatos.data(a.getDataDescarte()) + ": " + motivo;
        }
        if (!a.estaEmRack()) {
            return "Local: estoque (fora de rack).";
        }
        return "Local: " + a.getSalaNome() + " › Fileira " + a.getFileiraNome() + " › " + a.getRackNome()
                + ", " + slots(a) + ".";
    }

    private static String slots(Ativo a) {
        return a.getSlotInicio().equals(a.getSlotFim())
                ? "slot " + a.getSlotInicio()
                : "slots " + a.getSlotInicio() + " a " + a.getSlotFim();
    }

    private static void fixarLargura(JComponent componente, int largura) {
        componente.setPreferredSize(new Dimension(largura, componente.getPreferredSize().height));
    }

    private static void adicionarPar(JPanel formulario, GridBagConstraints c, int linha,
                                     String rotulo1, Component campo1, String rotulo2, Component campo2) {
        c.gridy = linha;
        c.weightx = 0;
        c.insets = new Insets(3, 4, 3, 4);
        c.gridx = 0;
        formulario.add(new JLabel(rotulo1), c);
        c.gridx = 1;
        formulario.add(campo1, c);
        c.gridx = 2;
        c.insets = new Insets(3, 28, 3, 4);
        formulario.add(new JLabel(rotulo2), c);
        c.gridx = 3;
        c.weightx = 1;
        c.insets = new Insets(3, 4, 3, 4);
        formulario.add(campo2, c);
        c.weightx = 0;
    }

    private static JPanel linhaComDica(Component campo, String textoDica) {
        JLabel dica = new JLabel(textoDica);
        dica.setForeground(Estilo.CINZA_TEXTO);
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linha.add(campo);
        linha.add(Box.createHorizontalStrut(8));
        linha.add(dica);
        return linha;
    }

    private class RenderizadorGarantia extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionado,
                                                       boolean comFoco, int linha, int coluna) {
            LocalDate garantia = (LocalDate) valor;
            String texto = (garantia == null) ? "—" : Formatos.data(garantia);
            boolean vencida = garantia != null && garantia.isBefore(LocalDate.now())
                    && !ativos.get(linha).isDescartado();
            return super.getTableCellRendererComponent(tabela, vencida ? texto + " (vencida)" : texto,
                    selecionado, comFoco, linha, coluna);
        }
    }
}
