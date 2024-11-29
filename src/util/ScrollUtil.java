package util;

import javax.swing.*;
import java.awt.event.MouseWheelListener;

public class ScrollUtil {
    public static void configurarScrollMouse(JTable tabela) {
        MouseWheelListener listener = e -> {
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, tabela);
            if (scrollPane != null) {
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                int scrollAmount = e.getWheelRotation() * verticalBar.getUnitIncrement();
                int newValue = verticalBar.getValue() + scrollAmount;
                
                // Limitar o valor entre o mínimo e máximo da barra de rolagem
                newValue = Math.max(verticalBar.getMinimum(), 
                          Math.min(newValue, verticalBar.getMaximum() - verticalBar.getVisibleAmount()));
                
                verticalBar.setValue(newValue);
            }
        };
        
        tabela.addMouseWheelListener(listener);
    }
}