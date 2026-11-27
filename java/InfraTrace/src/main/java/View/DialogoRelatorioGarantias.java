package View;

import Controllers.RelatorioGarantiasController;
import Model.RelatorioGarantias;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

public class DialogoRelatorioGarantias extends JDialog {

    private final RelatorioGarantiasController controller;

    private final JSpinner campoDias = new JSpinner(new SpinnerNumberModel(90, 0, 3650, 30));
    private final JButton botaoGerar = new JButton("Gerar");
    private final JButton botaoFechar = new JButton("Fechar");
    private final JLabel rotuloResumo = new JLabel(" ");

    private final DefaultTableModel modeloTabela = new DefaultTableModel(new Object[]{
        "ID", "Nome", "Tipo", "Patrimônio", "Status", "Garantia", "Dias", "Situação", "Local"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modeloTabela);

    public DialogoRelatorioGarantias(Component pai) {
        super(JOptionPane.getFrameForComponent(pai), "Relatório de garantias", true);
        controller = new RelatorioGarantiasController(this);
        montarTela();

        botaoGerar.addActionListener(e -> controller.gerar());
        botaoFechar.addActionListener(e -> dispose());

        setSize(980, 480);
        setLocationRelativeTo(pai);
    }

    public void abrir() {
        controller.gerar();
        setVisible(true);
    }

    private void montarTela() {
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        topo.add(new JLabel("Garantias vencidas e que vencem nos próximos"));
        topo.add(campoDias);
        topo.add(new JLabel("dias"));
        topo.add(botaoGerar);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);
        int[] larguras = {40, 110, 90, 90, 110, 80, 50, 90, 300};
        for (int coluna = 0; coluna < larguras.length; coluna++) {
            tabela.getColumnModel().getColumn(coluna).setPreferredWidth(larguras[coluna]);
        }
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        tabela.getColumnModel().getColumn(6).setCellRenderer(centralizado);

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(rotuloResumo, BorderLayout.WEST);
        rodape.add(botaoFechar, BorderLayout.EAST);

        JPanel conteudo = new JPanel(new BorderLayout(0, 8));
        conteudo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        conteudo.add(topo, BorderLayout.NORTH);
        conteudo.add(new JScrollPane(tabela), BorderLayout.CENTER);
        conteudo.add(rodape, BorderLayout.SOUTH);
        setContentPane(conteudo);
    }

    public int getDias() {
        return (Integer) campoDias.getValue();
    }

    public void mostrarRelatorio(RelatorioGarantias relatorio) {
        modeloTabela.setRowCount(0);
        for (RelatorioGarantias.Item item : relatorio.getItens()) {
            modeloTabela.addRow(new Object[]{
                item.getId(), item.getNome(), item.getTipo(), item.getPatrimonio(), item.getStatus(),
                item.getGarantia(), item.getDiasRestantes(), item.getSituacao(), item.getLocal()
            });
        }
        rotuloResumo.setText(relatorio.getTotal() + " ativo(s): " + relatorio.getVencidas() + " vencida(s) e "
                + relatorio.getAVencer() + " a vencer. Gerado pela API Python em " + relatorio.getGeradoEm() + ".");
    }

    public void limparRelatorio() {
        modeloTabela.setRowCount(0);
        rotuloResumo.setText(" ");
    }

    public void mostrarAviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
