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

public abstract class PainelBase extends JPanel {

    protected final JLabel rotuloMensagem = new JLabel(" ");

    protected PainelBase() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public void mostrarInformacao(String mensagem) {
        rotuloMensagem.setText(mensagem);
    }

    public void mostrarAviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public boolean confirmar(String pergunta) {
        return JOptionPane.showConfirmDialog(this, pergunta, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    protected void limparMensagem() {
        rotuloMensagem.setText(" ");
    }

    protected static void configurarTabela(JTable tabela) {
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(20);
        tabela.getTableHeader().setReorderingAllowed(false);
    }

    protected static JPanel criarCaixaFormulario() {
        JPanel caixa = new JPanel(new GridBagLayout());
        caixa.setBorder(BorderFactory.createTitledBorder("Cadastro"));
        return caixa;
    }

    protected JPanel criarLinhaBotoes(JButton... botoes) {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        for (JButton botao : botoes) {
            linha.add(botao);
        }
        linha.add(rotuloMensagem);
        return linha;
    }
}
