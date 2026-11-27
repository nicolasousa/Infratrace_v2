package Infratrace;

import View.Estilo;
import View.TelaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.util.Collections;

public class App {

    public static void main(String[] args) {
        aplicarVisual();
        SwingUtilities.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }

    public static void aplicarVisual() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Não foi possível aplicar o visual do sistema: " + e.getMessage());
        }

        for (Object chave : Collections.list(UIManager.getDefaults().keys())) {
            if (UIManager.get(chave) instanceof FontUIResource) {
                UIManager.put(chave, new FontUIResource(Estilo.FONTE_PADRAO));
            }
        }
    }
}
