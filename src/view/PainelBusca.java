package view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class PainelBusca extends JPanel {
    private final JTextField txtBusca;
    private final JTable tabela;
    private final TableRowSorter<TableModel> sorter;

    public PainelBusca(JTable tabela) {
        this.tabela = tabela;
        this.sorter = new TableRowSorter<>(tabela.getModel());
        this.tabela.setRowSorter(sorter);
        
        setLayout(new FlowLayout(FlowLayout.LEFT));
        
        txtBusca = new JTextField(20);
        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void insertUpdate(DocumentEvent e) { filtrar(); }
        });
        
        add(new JLabel("Buscar: "));
        add(txtBusca);
    }

    private void filtrar() {
        String texto = txtBusca.getText();
        if (texto.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
        }
    }
}