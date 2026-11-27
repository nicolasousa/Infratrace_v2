package View;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

/**
 * Base das telas de cadastro (PainelSalas, PainelFileiras...).
 *
 * Reúne o que TODA tela de cadastro faz igual:
 *   - margens;
 *   - mensagens: sucesso (texto ao lado dos botões), aviso, erro e confirmação;
 *   - formatação padrão da tabela, do quadro do formulário e da linha de botões.
 *
 * Cada tela estende esta classe e cuida só dos seus próprios campos.
 * É "abstract" porque não faz sentido abrir um PainelBase sozinho, só as telas filhas.
 */
public abstract class PainelBase extends JPanel {

    /** Mensagem de sucesso. Fica na linha de botões (ver criarLinhaBotoes). */
    protected final JLabel rotuloMensagem = new JLabel(" ");

    protected PainelBase() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    // ------------------------------------------------------------------
    // Mensagens (usadas pelos controllers)
    // ------------------------------------------------------------------

    /** Mensagem de sucesso, ao lado dos botões (não abre janela). */
    public void mostrarInformacao(String mensagem) {
        rotuloMensagem.setText(mensagem);
    }

    /** Aviso: algo que o usuário pode corrigir (campo vazio, nome repetido...). */
    public void mostrarAviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    /** Erro: problema que o usuário não resolve na tela (banco fora do ar, configuração...). */
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    /** Pergunta Sim/Não. Devolve true se o usuário escolher Sim. */
    public boolean confirmar(String pergunta) {
        return JOptionPane.showConfirmDialog(this, pergunta, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    protected void limparMensagem() {
        rotuloMensagem.setText(" ");
    }

    // ------------------------------------------------------------------
    // Peças visuais padrão
    // ------------------------------------------------------------------

    /** Tabela: uma linha selecionada por vez e colunas fixas. */
    protected static void configurarTabela(JTable tabela) {
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(20);
        tabela.getTableHeader().setReorderingAllowed(false);
    }

    /** Quadro com título "Cadastro" onde fica o formulário (use GridBagConstraints para os campos). */
    protected static JPanel criarCaixaFormulario() {
        JPanel caixa = new JPanel(new GridBagLayout());
        caixa.setBorder(BorderFactory.createTitledBorder("Cadastro"));
        return caixa;
    }

    /** Botões lado a lado e, depois deles, a mensagem de sucesso. */
    protected JPanel criarLinhaBotoes(JButton... botoes) {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        for (JButton botao : botoes) {
            linha.add(botao);
        }
        linha.add(rotuloMensagem);
        return linha;
    }
}
