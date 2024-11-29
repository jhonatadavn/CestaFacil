package view;

import java.awt.Dimension;
import java.awt.Font;

public class UIConstants {
    // Tamanhos
    public static final Dimension WINDOW_MIN_SIZE = new Dimension(800, 600);
    public static final Dimension WINDOW_PREFERRED_SIZE = new Dimension(1024, 768);
    public static final Dimension BUTTON_SIZE = new Dimension(150, 30);
    public static final Dimension COMBO_BOX_SIZE = new Dimension(200, 30);
    public static final Dimension SPINNER_SIZE = new Dimension(80, 30);
    
    // Fontes
    public static final Font BUTTON_FONT = new Font("Arial", Font.PLAIN, 12);
    public static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 12);
    public static final Font INPUT_FONT = new Font("Arial", Font.PLAIN, 12);
    
    // Espaçamento
    public static final int PADDING = 10;
    public static final int GAP = 5;
    
    // Impede instanciação
    private UIConstants() {}
}