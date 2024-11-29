package view;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.*;
import manager.GerenciadorPedidos;
import model.Pedido;

public final class PainelHistorico extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(PainelHistorico.class.getName());
    private final GerenciadorPedidos gerenciadorPedidos;
    private JTable tabelaPedidos;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtBusca;
    private JComboBox<String> cbTipoBusca;
    private JButton btnVisualizar;
    private JButton btnConcluir;
    private JButton btnCancelar;

    public PainelHistorico(GerenciadorPedidos gerenciadorPedidos) {
        this.gerenciadorPedidos = gerenciadorPedidos;
        setLayout(new BorderLayout(UIConstants.GAP, UIConstants.GAP));
        setBorder(BorderFactory.createEmptyBorder(
            UIConstants.PADDING, 
            UIConstants.PADDING, 
            UIConstants.PADDING, 
            UIConstants.PADDING
        ));
        inicializarComponentes();
        configurarEventos();
        atualizarDados();
    }

    private void inicializarComponentes() {
        // Painel de busca
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbTipoBusca = new JComboBox<>(new String[]{"Nome", "CPF", "Data"});
        txtBusca = new JTextField(30);
        
        painelBusca.add(new JLabel("Buscar por:"));
        painelBusca.add(cbTipoBusca);
        painelBusca.add(txtBusca);

        // Configuração da tabela
        String[] colunas = {"Nº Pedido", "Data", "Cliente", "CPF", "Valor Total", "PDF", "Status"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaPedidos = new JTable(modeloTabela);
        tabelaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaPedidos.getTableHeader().setReorderingAllowed(false);

        // Configurar ordenação
        sorter = new TableRowSorter<>(modeloTabela);
        tabelaPedidos.setRowSorter(sorter);

        // Configurar renderizador personalizado
        tabelaPedidos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, 
            boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        
        int modelRow = table.convertRowIndexToModel(row);
        String status = (String) table.getModel().getValueAt(modelRow, 6);
        
        if (null == status) {
            setForeground(Color.BLACK);
        } else switch (status) {
            case "CANCELADO" -> setForeground(Color.RED);
            case "CONCLUÍDO" -> setForeground(new Color(0, 128, 0)); // Verde
            default -> setForeground(Color.BLACK);
        }
        
        return c;
        }
      });

        // Botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnVisualizar = new JButton("Visualizar PDF");
        btnConcluir = new JButton("Concluir Pedido");
        btnCancelar = new JButton("Cancelar Pedido");
    
        btnVisualizar.setPreferredSize(UIConstants.BUTTON_SIZE);
        btnConcluir.setPreferredSize(UIConstants.BUTTON_SIZE);
        btnCancelar.setPreferredSize(UIConstants.BUTTON_SIZE);
    
        painelBotoes.add(btnVisualizar);
        painelBotoes.add(btnConcluir);
        painelBotoes.add(btnCancelar);
    


        // Painel superior com busca e botões
        JPanel painelSuperior = new JPanel(new BorderLayout(UIConstants.GAP, UIConstants.GAP));
        painelSuperior.add(painelBusca, BorderLayout.CENTER);
        painelSuperior.add(painelBotoes, BorderLayout.EAST);

        // Adiciona os componentes ao painel principal
        add(painelSuperior, BorderLayout.NORTH);
        add(new JScrollPane(tabelaPedidos), BorderLayout.CENTER);
    }

    private void configurarEventos() {
        btnVisualizar.addActionListener(e -> visualizarPDF());
        btnCancelar.addActionListener(e -> cancelarPedidoSelecionado());
        btnConcluir.addActionListener(e -> concluirPedidoSelecionado());
        
        // Evento de duplo clique na tabela
        tabelaPedidos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    visualizarPDF();
                }
            }
        });

        // Evento de busca
        txtBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });

        // Evento de mudança no tipo de busca
        cbTipoBusca.addActionListener(e -> filtrar());
    }
    
    private void concluirPedidoSelecionado() {
    int selectedRow = tabelaPedidos.getSelectedRow();
    if (selectedRow < 0) {
        JOptionPane.showMessageDialog(this,
            "Selecione um pedido para concluir",
            "Aviso",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    selectedRow = tabelaPedidos.convertRowIndexToModel(selectedRow);
    String status = (String) modeloTabela.getValueAt(selectedRow, 6);
    
    if ("CANCELADO".equals(status)) {
        JOptionPane.showMessageDialog(this,
            "Não é possível concluir um pedido cancelado",
            "Aviso",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    if ("CONCLUÍDO".equals(status)) {
        JOptionPane.showMessageDialog(this,
            "Este pedido já está concluído",
            "Aviso",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    Long pedidoId = (Long) modeloTabela.getValueAt(selectedRow, 0);
    
    int confirm = JOptionPane.showConfirmDialog(this,
        "Tem certeza que deseja concluir este pedido?",
        "Confirmar Conclusão",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
        
    if (confirm == JOptionPane.YES_OPTION) {
        try {
            gerenciadorPedidos.concluirPedido(pedidoId);
            JOptionPane.showMessageDialog(this,
                "Pedido concluído com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
            atualizarDados();
        } catch (HeadlessException e) {
            LOGGER.log(Level.SEVERE, "Erro ao concluir pedido", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao concluir pedido: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

   private void cancelarPedidoSelecionado() {
    int selectedRow = tabelaPedidos.getSelectedRow();
    if (selectedRow < 0) {
        JOptionPane.showMessageDialog(this,
            "Selecione um pedido para cancelar",
            "Aviso",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    selectedRow = tabelaPedidos.convertRowIndexToModel(selectedRow);
    String status = (String) modeloTabela.getValueAt(selectedRow, 6);
    
    if ("CANCELADO".equals(status)) {
        JOptionPane.showMessageDialog(this,
            "Este pedido já está cancelado",
            "Aviso",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    Long pedidoId = (Long) modeloTabela.getValueAt(selectedRow, 0);
    
    int confirm = JOptionPane.showConfirmDialog(this,
        "Tem certeza que deseja cancelar este pedido?",
        "Confirmar Cancelamento",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
        
    if (confirm == JOptionPane.YES_OPTION) {
        try {
            gerenciadorPedidos.cancelarPedido(pedidoId, "Cancelado pelo usuário");
            JOptionPane.showMessageDialog(this,
                "Pedido cancelado com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
            atualizarDados();
        } catch (HeadlessException e) {
            LOGGER.log(Level.SEVERE, "Erro ao cancelar pedido", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao cancelar pedido: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

    private void filtrar() {
        String texto = txtBusca.getText();
        String tipoBusca = cbTipoBusca.getSelectedItem().toString();
        
        if (texto.trim().length() == 0) {
        sorter.setRowFilter(null);
        } else {
        int coluna;
        coluna = switch (tipoBusca) {
        case "Nome" -> 2;
        case "CPF" -> 3;
        case "Data" -> 1;
        default -> 2;
            };
            
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, coluna));
        }
    }

    public void atualizarDados() {
        try {
            List<Pedido> pedidos = gerenciadorPedidos.listarPedidos();
            modeloTabela.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            for (Pedido pedido : pedidos) {
                String statusPDF = new File(String.format("pdf/pedido_%d.pdf", 
                    pedido.getId())).exists() ? "Disponível" : "Não encontrado";
                
                Object[] row = {
                    pedido.getId(),
                    sdf.format(pedido.getDataPedido()),
                    pedido.getCliente().getNome(),
                    pedido.getCliente().getCpf(),
                    String.format("R$ %.2f", pedido.getValorTotal()),
                    statusPDF,
                    pedido.getStatus()
                };
                modeloTabela.addRow(row);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar dados", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao atualizar dados: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void visualizarPDF() {
        int selectedRow = tabelaPedidos.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Selecione um pedido para visualizar",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        selectedRow = tabelaPedidos.convertRowIndexToModel(selectedRow);
        Long pedidoId = (Long) modeloTabela.getValueAt(selectedRow, 0);
        File pdfFile = new File(String.format("pdf/pedido_%d.pdf", pedidoId));
        
        if (pdfFile.exists()) {
            try {
                Desktop.getDesktop().open(pdfFile);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erro ao abrir PDF", e);
                JOptionPane.showMessageDialog(this,
                    "Erro ao abrir PDF: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "PDF não encontrado",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
        }
    }
}