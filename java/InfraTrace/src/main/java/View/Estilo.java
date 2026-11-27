package View;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;

public final class Estilo {

    public static final Color CINZA_TEXTO = new Color(0x55, 0x55, 0x55);

    public static final Font FONTE_PADRAO = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font FONTE_NEGRITO = FONTE_PADRAO.deriveFont(Font.BOLD);
    public static final Font FONTE_TITULO = FONTE_PADRAO.deriveFont(Font.BOLD, 16f);

    private Estilo() {
    }

    public static JLabel titulo(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setFont(FONTE_TITULO);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return titulo;
    }
}
