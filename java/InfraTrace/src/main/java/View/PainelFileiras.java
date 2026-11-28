package View;

import Controllers.FileiraController;
import Model.Fileira;
import Model.Sala;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.HierarchyEvent;
import java.util.List;

public class PainelFileiras extends PainelBase {

    private final FileiraController controller;

    private final DefaultTableModel modeloTabela =
            new DefaultTableModel(new Object[]{"ID", "Sala", "Fileira", "Racks"}, 0) {
                @Override
                public boolean isCellEditable(int linha, int coluna) {
                    return false;
                }
            };
    private final JTable tabela = new JTable(modeloTabela);

    private final JLabel rotuloModo = new JLabel();
    private final DefaultComboBoxModel<Sala> modeloSalas = new DefaultComboBoxModel<>();
    private final JComboBox<Sala> campoSala = new JComboBox<>(modeloSalas);
    private final JTextField campoNome = new JTextField(10);
    private final JButton botaoNovo = new JButton("Novo");
    private final JButton botaoCadastrar = new JButton("Cadastrar");
    private final JButton botaoAlterar = new JButton("Alterar");
    private final JButton botaoExcluir = new JButton("Excluir");
    private final JButton botaoAtualizar = new JButton("Atualizar");

    private List<Fileira> fileiras = List.of();

    public PainelFileiras() {
        controller = new FileiraController(this);
        montarTela();
        ligarEventos();
        limparCampos();
    }

    private void montarTela() {
        add(Estilo.titulo("Cadastro de Fileiras"), BorderLayout.NORTH);

        configurarTabela(tabela);
        tabela.getColumnModel().getColumn(0).setMaxWidth(70);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(320);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(3).setMaxWidth(90);
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        tabela.getColumnModel().getColumn(3).setCellRenderer(centralizado);
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
        formulario.add(new JLabel("Sala *"), c);
        campoSala.setPreferredSize(new Dimension(300, campoSala.getPreferredSize().height));
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoSala, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0;
        formulario.add(new JLabel("Nome *"), c);
        JLabel dica = new JLabel("até 10 caracteres (ex.: A, B, C1)");
        dica.setForeground(Estilo.CINZA_TEXTO);
        JPanel linhaNome = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaNome.setOpaque(false);
        linhaNome.add(campoNome);
        linhaNome.add(Box.createHorizontalStrut(10));
        linhaNome.add(dica);
        c.gridx = 1; c.weightx = 1;
        formulario.add(linhaNome, c);

        c.gridx = 1; c.gridy = 3;
        formulario.add(criarLinhaBotoes(botaoNovo, botaoCadastrar, botaoAlterar, botaoExcluir, botaoAtualizar), c);

        return formulario;
    }

    private void ligarEventos() {
        botaoNovo.addActionListener(e -> limparFormulario());
        botaoCadastrar.addActionListener(e -> controller.cadastrar());
        botaoAlterar.addActionListener(e -> controller.alterar());
        botaoExcluir.addActionListener(e -> controller.excluir());
        botaoAtualizar.addActionListener(e -> controller.carregar());
        campoNome.addActionListener(e -> (botaoAlterar.isEnabled() ? botaoAlterar : botaoCadastrar).doClick());

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Fileira fileira = getFileiraSelecionada();
                if (fileira != null) {
                    preencherCampos(fileira);
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

    public void mostrarSalas(List<Sala> salas) {
        Sala escolhida = getSalaEscolhida();
        modeloSalas.removeAllElements();
        for (Sala sala : salas) {
            modeloSalas.addElement(sala);
        }
        escolherSala(escolhida == null ? null : escolhida.getId());
    }

    public void mostrarFileiras(List<Fileira> fileiras) {
        this.fileiras = fileiras;
        modeloTabela.setRowCount(0);
        for (Fileira f : fileiras) {
            modeloTabela.addRow(new Object[]{f.getId(), f.getSalaNome(), f.getNome(), f.getQuantidadeRacks()});
        }
    }

    public Fileira getFileiraSelecionada() {
        int linha = tabela.getSelectedRow();
        return (linha < 0) ? null : fileiras.get(linha);
    }

    public void selecionarFileira(int id) {
        for (int linha = 0; linha < fileiras.size(); linha++) {
            if (fileiras.get(linha).getId() == id) {
                tabela.setRowSelectionInterval(linha, linha);
                tabela.scrollRectToVisible(tabela.getCellRect(linha, 0, true));
                return;
            }
        }
    }

    public Sala getSalaEscolhida() {
        return (Sala) modeloSalas.getSelectedItem();
    }

    public String getNome() {
        return campoNome.getText();
    }

    public void limparFormulario() {
        tabela.clearSelection();
        limparCampos();
        campoNome.requestFocusInWindow();
    }

    private void limparCampos() {
        rotuloModo.setText("Nova fileira");
        campoNome.setText("");
        botaoExcluir.setEnabled(false);
        botaoCadastrar.setEnabled(true);
        botaoAlterar.setEnabled(false);
        limparMensagem();
    }

    private void preencherCampos(Fileira fileira) {
        rotuloModo.setText("Editando a fileira " + fileira.getId());
        escolherSala(fileira.getSalaId());
        campoNome.setText(fileira.getNome());
        botaoExcluir.setEnabled(true);
        botaoCadastrar.setEnabled(false);
        botaoAlterar.setEnabled(true);
        limparMensagem();
    }

    private void escolherSala(Integer salaId) {
        modeloSalas.setSelectedItem(null);
        if (salaId == null) {
            return;
        }
        for (int i = 0; i < modeloSalas.getSize(); i++) {
            if (modeloSalas.getElementAt(i).getId().equals(salaId)) {
                modeloSalas.setSelectedItem(modeloSalas.getElementAt(i));
                return;
            }
        }
    }
}
