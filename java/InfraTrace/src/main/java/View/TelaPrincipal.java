package View;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class TelaPrincipal extends JFrame {

    public TelaPrincipal() {
        super("InfraTrace - Cadastro de Ativos e Racks");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1100, 700);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Sala", new PainelSalas());
        abas.addTab("Fileira", new PainelFileiras());
        abas.addTab("Rack", new PainelRacks());
        abas.addTab("Ativo", new PainelAtivos());
        abas.addTab("Tipo Ativo", new PainelTiposAtivo());
        add(abas, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }
}
