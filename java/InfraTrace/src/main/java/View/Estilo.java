package View;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;

/**
 * Fontes, cores e o título padrão das telas, num lugar só.
 * O visual é o padrão do Swing, só com tons neutros (branco e cinza).
 */
public final class Estilo {

    public static final Color CINZA_TEXTO = new Color(0x55, 0x55, 0x55);   // textos secundários

    public static final Font FONTE_PADRAO = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font FONTE_NEGRITO = FONTE_PADRAO.deriveFont(Font.BOLD);
    public static final Font FONTE_TITULO = FONTE_PADRAO.deriveFont(Font.BOLD, 16f);

    private Estilo() {
    }

    /** Título do módulo, mostrado no topo de cada tela. */
    public static JLabel titulo(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setFont(FONTE_TITULO);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return titulo;
    }
}
