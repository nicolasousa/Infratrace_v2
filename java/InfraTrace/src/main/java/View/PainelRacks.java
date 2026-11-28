package View;

import Controllers.RackController;
import Model.Ativo;
import Model.Fileira;
import Model.Rack;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.HierarchyEvent;
import java.util.List;

public class PainelRacks extends PainelBase {

    private static final int CAPACIDADE_PADRAO = 42;

    private final RackController controller;

    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new Object[]{"ID", "Sala", "Fileira", "Rack", "Capacidade (U)", "Ativos", "Slot mais alto"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modeloTabela);

    private final JLabel rotuloModo = new JLabel();
    private final DefaultComboBoxModel<Fileira> modeloFileiras = new DefaultComboBoxModel<>();
    private final JComboBox<Fileira> campoFileira = new JComboBox<>(modeloFileiras);
    private final JTextField campoNome = new JTextField(20);

    private final JSpinner campoCapacidade = new JSpinner(new SpinnerNumberModel(CAPACIDADE_PADRAO, 1, 60, 1));
    private final JFormattedTextField textoCapacidade =
            ((JSpinner.DefaultEditor) campoCapacidade.getEditor()).getTextField();
    private final JLabel dicaCapacidade = new JLabel();

    private final JButton botaoNovo = new JButton("Novo");
    private final JButton botaoCadastrar = new JButton("Cadastrar");
    private final JButton botaoAlterar = new JButton("Alterar");
    private final JButton botaoExcluir = new JButton("Excluir");
    private final JButton botaoVerAtivos = new JButton("Ver ativos");
    private final JButton botaoAtualizar = new JButton("Atualizar");

    private List<Rack> racks = List.of();

    public PainelRacks() {
        controller = new RackController(this);
        montarTela();
        ligarEventos();
        limparCampos();
    }

    private void montarTela() {
        add(Estilo.titulo("Cadastro de Racks"), BorderLayout.NORTH);

        configurarTabela(tabela);
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(240);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(200);
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int coluna = 4; coluna <= 6; coluna++) {
            tabela.getColumnModel().getColumn(coluna).setCellRenderer(centralizado);
            tabela.getColumnModel().getColumn(coluna).setMinWidth(coluna == 5 ? 70 : 125);
            tabela.getColumnModel().getColumn(coluna).setMaxWidth(140);
        }
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        add(montarFormulario(), BorderLayout.SOUTH);
    }

    private JPanel montarFormulario() {
        JPanel formulario = criarCaixaFormulario();

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        rotuloModo.setFont(Estilo.FONTE_NEGRITO);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        formulario.add(rotuloModo, c);
        c.gridwidth = 1;

        c.gridx = 0; c.gridy = 1;
        formulario.add(new JLabel("Fileira *"), c);
        campoFileira.setPreferredSize(new Dimension(340, campoFileira.getPreferredSize().height));
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoFileira, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0;
        formulario.add(new JLabel("Nome *"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(linhaComDica(campoNome, dica("ex.: R06 (não pode repetir na mesma fileira)")), c);

        c.gridx = 0; c.gridy = 3; c.weightx = 0;
        formulario.add(new JLabel("Capacidade *"), c);
        textoCapacidade.setColumns(3);
        textoCapacidade.setFocusLostBehavior(JFormattedTextField.PERSIST);
        JPanel linhaCapacidade = linhaComDica(campoCapacidade, new JLabel("U"));
        linhaCapacidade.add(Box.createHorizontalStrut(10));
        dicaCapacidade.setForeground(Estilo.CINZA_TEXTO);
        linhaCapacidade.add(dicaCapacidade);
        c.gridx = 1; c.weightx = 1;
        formulario.add(linhaCapacidade, c);

        c.gridx = 1; c.gridy = 4;
        formulario.add(criarLinhaBotoes(botaoNovo, botaoCadastrar, botaoAlterar, botaoExcluir, botaoVerAtivos, botaoAtualizar), c);

        return formulario;
    }

    private void ligarEventos() {
        botaoNovo.addActionListener(e -> limparFormulario());
        botaoCadastrar.addActionListener(e -> controller.cadastrar());
        botaoAlterar.addActionListener(e -> controller.alterar());
        botaoExcluir.addActionListener(e -> controller.excluir());
        botaoVerAtivos.addActionListener(e -> controller.verAtivos());
        botaoAtualizar.addActionListener(e -> controller.carregar());
        campoNome.addActionListener(e -> (botaoAlterar.isEnabled() ? botaoAlterar : botaoCadastrar).doClick());

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Rack rack = getRackSelecionado();
                if (rack != null) {
                    preencherCampos(rack);
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

    public void mostrarFileiras(List<Fileira> fileiras) {
        Fileira escolhida = getFileiraEscolhida();
        modeloFileiras.removeAllElements();
        for (Fileira fileira : fileiras) {
            modeloFileiras.addElement(fileira);
        }
        escolherFileira(escolhida == null ? null : escolhida.getId());
    }

    public void mostrarRacks(List<Rack> racks) {
        this.racks = racks;
        modeloTabela.setRowCount(0);
        for (Rack r : racks) {
            modeloTabela.addRow(new Object[]{
                r.getId(), r.getSalaNome(), r.getFileiraNome(), r.getNome(), r.getTotalSlots(),
                r.getQuantidadeAtivos(), r.getMaiorSlotOcupado() == null ? "—" : r.getMaiorSlotOcupado()
            });
        }
    }

    public void mostrarAtivosDoRack(Rack rack, List<Ativo> ativos) {
        if (ativos.isEmpty()) {
            mostrarInformacao("O rack '" + rack.getNome() + "' não tem ativos instalados.");
            return;
        }
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Nome", "Status", "Slots"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        for (Ativo a : ativos) {
            String slots = a.getSlotInicio().equals(a.getSlotFim())
                    ? String.valueOf(a.getSlotInicio())
                    : a.getSlotInicio() + " a " + a.getSlotFim();
            modelo.addRow(new Object[]{a.getId(), a.getNome(), a.getStatus(), slots});
        }
        JTable lista = new JTable(modelo);
        configurarTabela(lista);
        lista.getColumnModel().getColumn(0).setMaxWidth(60);
        JScrollPane rolagem = new JScrollPane(lista);
        rolagem.setPreferredSize(new Dimension(520, 220));
        JOptionPane.showMessageDialog(this, rolagem, "Ativos do rack " + rack, JOptionPane.PLAIN_MESSAGE);
    }

    public Rack getRackSelecionado() {
        int linha = tabela.getSelectedRow();
        return (linha < 0) ? null : racks.get(linha);
    }

    public void selecionarRack(int id) {
        for (int linha = 0; linha < racks.size(); linha++) {
            if (racks.get(linha).getId() == id) {
                tabela.setRowSelectionInterval(linha, linha);
                tabela.scrollRectToVisible(tabela.getCellRect(linha, 0, true));
                return;
            }
        }
    }

    public Fileira getFileiraEscolhida() {
        return (Fileira) modeloFileiras.getSelectedItem();
    }

    public String getNome() {
        return campoNome.getText();
    }

    public Integer getCapacidade() {
        try {
            return Integer.valueOf(textoCapacidade.getText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void limparFormulario() {
        tabela.clearSelection();
        limparCampos();
        campoNome.requestFocusInWindow();
    }

    private void limparCampos() {
        rotuloModo.setText("Novo rack");
        campoNome.setText("");
        definirCapacidade(CAPACIDADE_PADRAO);
        dicaCapacidade.setText("de 1 a 60 (42 U é o tamanho mais comum)");
        botaoExcluir.setEnabled(false);
        botaoCadastrar.setEnabled(true);
        botaoAlterar.setEnabled(false);
        botaoVerAtivos.setEnabled(false);
        limparMensagem();
    }

    private void preencherCampos(Rack rack) {
        rotuloModo.setText("Editando o rack " + rack.getId());
        escolherFileira(rack.getFileiraId());
        campoNome.setText(rack.getNome());
        definirCapacidade(rack.getTotalSlots());
        dicaCapacidade.setText(rack.getMaiorSlotOcupado() == null
                ? "de 1 a 60 (rack vazio)"
                : "de " + rack.getMaiorSlotOcupado() + " a 60 (o slot " + rack.getMaiorSlotOcupado()
                        + " está ocupado)");
        botaoExcluir.setEnabled(true);
        botaoCadastrar.setEnabled(false);
        botaoAlterar.setEnabled(true);
        botaoVerAtivos.setEnabled(true);
        limparMensagem();
    }

    private void definirCapacidade(int capacidade) {
        campoCapacidade.setValue(capacidade);
        textoCapacidade.setValue(capacidade);
    }

    private void escolherFileira(Integer fileiraId) {
        modeloFileiras.setSelectedItem(null);
        if (fileiraId == null) {
            return;
        }
        for (int i = 0; i < modeloFileiras.getSize(); i++) {
            if (modeloFileiras.getElementAt(i).getId().equals(fileiraId)) {
                modeloFileiras.setSelectedItem(modeloFileiras.getElementAt(i));
                return;
            }
        }
    }

    private static JLabel dica(String texto) {
        JLabel dica = new JLabel(texto);
        dica.setForeground(Estilo.CINZA_TEXTO);
        return dica;
    }

    private static JPanel linhaComDica(Component campo, JLabel dica) {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linha.setOpaque(false);
        linha.add(campo);
        linha.add(Box.createHorizontalStrut(10));
        linha.add(dica);
        return linha;
    }
}
