package View;

import Controllers.TipoAtivoController;
import Model.TipoAtivo;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.HierarchyEvent;
import java.util.List;

/**
 * Tela de cadastro de tipos de ativo (Servidor, Switch, Storage...).
 * Mesmo padrão da tela de salas: tabela em cima, formulário embaixo.
 */
public class PainelTiposAtivo extends PainelBase {

    private final TipoAtivoController controller;

    private final DefaultTableModel modeloTabela = new DefaultTableModel(new Object[]{"ID", "Nome", "Descrição"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modeloTabela);

    private final JLabel rotuloModo = new JLabel();
    private final JTextField campoNome = new JTextField(30);
    private final JTextArea campoDescricao = new JTextArea(3, 30);
    private final JButton botaoNovo = new JButton("Novo");
    private final JButton botaoSalvar = new JButton("Salvar");
    private final JButton botaoExcluir = new JButton("Excluir");
    private final JButton botaoAtualizar = new JButton("Atualizar");

    /** Tipos mostrados na tabela, na mesma ordem das linhas. */
    private List<TipoAtivo> tipos = List.of();

    public PainelTiposAtivo() {
        controller = new TipoAtivoController(this);
        montarTela();
        ligarEventos();
        limparCampos();
    }

    // ------------------------------------------------------------------
    // Montagem dos componentes
    // ------------------------------------------------------------------

    private void montarTela() {
        add(Estilo.titulo("Cadastro de Tipos de Ativo"), BorderLayout.NORTH);

        configurarTabela(tabela);
        tabela.getColumnModel().getColumn(0).setMaxWidth(70);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(480);
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
        formulario.add(new JLabel("Nome *"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(campoNome, c);

        c.gridx = 0; c.gridy = 2; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        formulario.add(new JLabel("Descrição"), c);
        campoDescricao.setLineWrap(true);
        campoDescricao.setWrapStyleWord(true);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        formulario.add(new JScrollPane(campoDescricao), c);

        c.gridx = 1; c.gridy = 3; c.anchor = GridBagConstraints.WEST;
        formulario.add(criarLinhaBotoes(botaoNovo, botaoSalvar, botaoExcluir, botaoAtualizar), c);

        return formulario;
    }

    private void ligarEventos() {
        botaoNovo.addActionListener(e -> limparFormulario());
        botaoSalvar.addActionListener(e -> controller.salvar());
        botaoExcluir.addActionListener(e -> controller.excluir());
        botaoAtualizar.addActionListener(e -> controller.carregar());
        campoNome.addActionListener(e -> controller.salvar());   // Enter no campo Nome também salva

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                TipoAtivo tipo = getTipoSelecionado();
                if (tipo != null) {
                    preencherCampos(tipo);
                } else {
                    limparCampos();
                }
            }
        });

        // Sempre que a tela aparecer (ao abrir a aba), recarrega os tipos do banco
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                controller.carregar();
            }
        });
    }

    // ------------------------------------------------------------------
    // Métodos usados pelo controller
    // ------------------------------------------------------------------

    public void mostrarTipos(List<TipoAtivo> tipos) {
        this.tipos = tipos;
        modeloTabela.setRowCount(0);
        for (TipoAtivo tipo : tipos) {
            modeloTabela.addRow(new Object[]{tipo.getId(), tipo.getNome(), tipo.getDescricao()});
        }
    }

    public TipoAtivo getTipoSelecionado() {
        int linha = tabela.getSelectedRow();
        return (linha < 0) ? null : tipos.get(linha);
    }

    public void selecionarTipo(int id) {
        for (int linha = 0; linha < tipos.size(); linha++) {
            if (tipos.get(linha).getId() == id) {
                tabela.setRowSelectionInterval(linha, linha);
                tabela.scrollRectToVisible(tabela.getCellRect(linha, 0, true));
                return;
            }
        }
    }

    public String getNome() {
        return campoNome.getText();
    }

    public String getDescricao() {
        return campoDescricao.getText();
    }

    /** Volta para o modo "novo tipo": tira a seleção da tabela e esvazia os campos. */
    public void limparFormulario() {
        tabela.clearSelection();
        limparCampos();
        campoNome.requestFocusInWindow();
    }

    // ------------------------------------------------------------------
    // Auxiliares internos
    // ------------------------------------------------------------------

    private void limparCampos() {
        rotuloModo.setText("Novo tipo de ativo");
        campoNome.setText("");
        campoDescricao.setText("");
        botaoExcluir.setEnabled(false);
        limparMensagem();
    }

    private void preencherCampos(TipoAtivo tipo) {
        rotuloModo.setText("Editando o tipo " + tipo.getId());
        campoNome.setText(tipo.getNome());
        campoDescricao.setText(tipo.getDescricao() == null ? "" : tipo.getDescricao());
        botaoExcluir.setEnabled(true);
        limparMensagem();
    }
}
