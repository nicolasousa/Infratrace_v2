package Infratrace;

import View.Estilo;
import View.TelaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.util.Collections;

/**
 * Ponto de partida da aplicação desktop (é a classe executada pelo "Run Project" do NetBeans).
 *
 * 1. Usa o visual do próprio sistema operacional (no Windows, botões e campos cinza do Windows)
 *    e a fonte SansSerif em todos os componentes.
 * 2. Abre a janela principal.
 *
 * Por que SwingUtilities.invokeLater? O Swing não é "thread-safe": todas as telas devem ser
 * criadas e alteradas por uma única thread, a Event Dispatch Thread (EDT). O invokeLater
 * agenda a criação da janela nessa thread.
 */
public class App {

    public static void main(String[] args) {
        aplicarVisual();
        SwingUtilities.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }

    public static void aplicarVisual() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Se não der, fica o visual padrão do Java: a aplicação funciona igual.
            System.err.println("Não foi possível aplicar o visual do sistema: " + e.getMessage());
        }

        // Troca a fonte de todos os componentes (botões, campos, tabelas...) pela SansSerif
        for (Object chave : Collections.list(UIManager.getDefaults().keys())) {
            if (UIManager.get(chave) instanceof FontUIResource) {
                UIManager.put(chave, new FontUIResource(Estilo.FONTE_PADRAO));
            }
        }
    }
}
