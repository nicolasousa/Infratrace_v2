package View;

import Controllers.SalaController;
import Model.Sala;

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
 * Tela de cadastro de salas: tabela com as salas em cima, formulário embaixo.
 *
 * Esta classe SÓ cuida da parte visual:
 *   - monta os componentes (tabela, campos, botões);
 *   - repassa os cliques para o SalaController;
 *   - oferece métodos para o controller ler os campos e mostrar resultados.
 * Ela não acessa o banco e não tem regra de negócio.
 * As mensagens (sucesso, aviso, erro, confirmação) vêm do PainelBase.
 *
 * Como usar a tela:
 *   - clicar numa linha da tabela carrega a sala no formulário (modo edição);
 *   - "Novo" limpa o formulário para cadastrar outra sala;
 *   - "Salvar" grava (INSERT se for nova, UPDATE se estiver editando);
 *   - "Excluir" apaga a sala selecionada, depois de pedir confirmação.
 */
public class PainelSalas extends PainelBase {

    private final SalaController controller;

    // Modelo da tabela: as colunas e as linhas. isCellEditable = false impede editar na própria tabela.
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

    /** Salas mostradas na tabela, na mesma ordem das linhas (linha 0 = salas.get(0)). */
    private List<Sala> salas = List.of();

    public PainelSalas() {
        controller = new SalaController(this);
        montarTela();
        ligarEventos();
        limparCampos();
    }

    // ------------------------------------------------------------------
    // Montagem dos componentes
    // ------------------------------------------------------------------

    private void montarTela() {
        add(Estilo.titulo("Cadastro de Salas"), BorderLayout.NORTH);

        configurarTabela(tabela);
        tabela.getColumnModel().getColumn(0).setMaxWidth(70);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(480);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        add(montarFormulario(), BorderLayout.SOUTH);
    }

    private JPanel montarFormulario() {
        JPanel formulario = criarCaixaFormulario();

        // GridBagLayout: cada componente vai numa "célula" (gridx = coluna, gridy = linha)
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

    /** Liga cada ação do usuário ao método que trata dela. */
    private void ligarEventos() {
        botaoNovo.addActionListener(e -> limparFormulario());
        botaoSalvar.addActionListener(e -> controller.salvar());
        botaoExcluir.addActionListener(e -> controller.excluir());
        botaoAtualizar.addActionListener(e -> controller.carregar());   // relê as salas do banco
        campoNome.addActionListener(e -> controller.salvar());   // Enter no campo Nome também salva

        // Linha selecionada na tabela -> carrega a sala no formulário
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Sala sala = getSalaSelecionada();
                if (sala != null) {
                    preencherCampos(sala);
                } else {
                    limparCampos();
                }
            }
        });

        // Sempre que a tela aparecer (ao abrir o módulo), recarrega as salas do banco
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                controller.carregar();
            }
        });
    }

    // ------------------------------------------------------------------
    // Métodos usados pelo controller
    // ------------------------------------------------------------------

    /** Substitui as linhas da tabela pelas salas recebidas. */
    public void mostrarSalas(List<Sala> salas) {
        this.salas = salas;
        modeloTabela.setRowCount(0);   // apaga as linhas antigas (isso também limpa a seleção)
        for (Sala sala : salas) {
            modeloTabela.addRow(new Object[]{sala.getId(), sala.getNome(), sala.getDescricao()});
        }
    }

    /** A sala da linha selecionada, ou null se nenhuma linha estiver selecionada. */
    public Sala getSalaSelecionada() {
        int linha = tabela.getSelectedRow();
        return (linha < 0) ? null : salas.get(linha);
    }

    /** Seleciona na tabela a linha da sala com esse id (usado depois de salvar). */
    public void selecionarSala(int id) {
        for (int linha = 0; linha < salas.size(); linha++) {
            if (salas.get(linha).getId() == id) {
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

    /** Volta para o modo "nova sala": tira a seleção da tabela e esvazia os campos. */
    public void limparFormulario() {
        tabela.clearSelection();
        limparCampos();
        campoNome.requestFocusInWindow();
    }

    // ------------------------------------------------------------------
    // Auxiliares internos
    // ------------------------------------------------------------------

    private void limparCampos() {
        rotuloModo.setText("Nova sala");
        campoNome.setText("");
        campoDescricao.setText("");
        botaoExcluir.setEnabled(false);
        limparMensagem();
    }

    private void preencherCampos(Sala sala) {
        rotuloModo.setText("Editando a sala " + sala.getId());
        campoNome.setText(sala.getNome());
        campoDescricao.setText(sala.getDescricao() == null ? "" : sala.getDescricao());
        botaoExcluir.setEnabled(true);
        limparMensagem();
    }
}
