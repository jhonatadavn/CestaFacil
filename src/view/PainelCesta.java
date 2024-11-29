package view;

import javax.swing.*;
import java.io.File;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import manager.GerenciadorPedidos;
import manager.GerenciadorClientes;
import manager.GerenciadorEstoque;
import model.Cliente;
import model.Produto;
import model.Pedido;
import model.ItemPedido;

public class PainelCesta extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(PainelCesta.class.getName());
    private static final String SELECIONE_CLIENTE = "Selecione um cliente";
    private static final String SELECIONE_PRODUTO = "Selecione um produto";

    private final GerenciadorPedidos gerenciadorPedidos;
    private final GerenciadorClientes gerenciadorClientes;
    private final GerenciadorEstoque gerenciadorEstoque;

    private JTable tabelaItens;
    private DefaultTableModel modeloTabela;
    private JComboBox<String> cbClientes;
    private JComboBox<String> cbProdutosAlimentos;
    private JComboBox<String> cbProdutosHigieneLimpeza;
    private JComboBox<String> cbFormaPagamento;
    private JSpinner spnQuantidade;
    private JSpinner spnParcelas;
    private JTextField txtSubtotal;
    private JTextField txtDesconto;
    private JTextField txtTotal;
    private final List<ItemPedido> itensCarrinho;
    private JButton btnAdicionar;
    private JButton btnRemover;
    private JButton btnFinalizar;

    public PainelCesta(GerenciadorPedidos gerenciadorPedidos,
                   GerenciadorClientes gerenciadorClientes,
                   GerenciadorEstoque gerenciadorEstoque) {
    this.gerenciadorPedidos = gerenciadorPedidos;
    this.gerenciadorClientes = gerenciadorClientes;
    this.gerenciadorEstoque = gerenciadorEstoque;
    this.itensCarrinho = new ArrayList<>();

    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    inicializarComponentes();
    configurarTabelaItens(); // Adicione esta linha
    configurarEventos();
}

    private void inicializarComponentes() {
        // Configurar botões com o novo tamanho
        btnAdicionar = new JButton("Adicionar ao Carrinho");
        btnAdicionar.setPreferredSize(UIConstants.BUTTON_SIZE);
        btnAdicionar.setFont(UIConstants.BUTTON_FONT);

        // Configurar ComboBoxes
        cbClientes = new JComboBox<>();
        cbClientes.setPreferredSize(UIConstants.COMBO_BOX_SIZE);
        cbClientes.setFont(UIConstants.INPUT_FONT);

          // Configurar Spinners
        spnQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnQuantidade.setPreferredSize(UIConstants.SPINNER_SIZE);
        ((JSpinner.DefaultEditor)spnQuantidade.getEditor()).getTextField().setFont(UIConstants.INPUT_FONT);
        // Painel Superior (Cliente e Produto)
        JPanel painelSuperior = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Comboboxes e Spinners
        cbClientes = new JComboBox<>();
        cbProdutosAlimentos = new JComboBox<>();
        cbProdutosHigieneLimpeza = new JComboBox<>();
        spnQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        btnAdicionar = new JButton("Adicionar ao Carrinho");

        // Adiciona componentes ao painel superior
        gbc.gridx = 0; gbc.gridy = 0;
        painelSuperior.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        painelSuperior.add(cbClientes, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        painelSuperior.add(new JLabel("Alimentos:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        painelSuperior.add(cbProdutosAlimentos, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        painelSuperior.add(new JLabel("Higiene/Limpeza:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        painelSuperior.add(cbProdutosHigieneLimpeza, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        painelSuperior.add(new JLabel("Quantidade:"), gbc);
        gbc.gridx = 1;
        painelSuperior.add(spnQuantidade, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2; // Faz o botão ocupar duas colunas
        painelSuperior.add(btnAdicionar, gbc);

        // Tabela de Itens
        String[] colunas = {"Produto", "Quantidade", "Valor Unit.", "Subtotal"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaItens = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabelaItens);

        // Painel Lateral (Pagamento e Totais)
        JPanel painelLateral = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbFormaPagamento = new JComboBox<>(new String[]{
            "Dinheiro", "Cartão de Crédito", "Cartão de Débito", "PIX"
        });
        spnParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        txtSubtotal = new JTextField(10);
        txtDesconto = new JTextField(10);
        txtTotal = new JTextField(10);
        btnRemover = new JButton("Remover Item");
        btnFinalizar = new JButton("Finalizar Pedido");

        txtSubtotal.setEditable(false);
        txtTotal.setEditable(false);
        spnParcelas.setEnabled(false);

        // Adiciona componentes ao painel lateral
        gbc.gridx = 0; gbc.gridy = 0;
        painelLateral.add(new JLabel("Forma de Pagamento:"), gbc);
        gbc.gridy = 1;
        painelLateral.add(cbFormaPagamento, gbc);

        gbc.gridy = 2;
        painelLateral.add(new JLabel("Parcelas:"), gbc);
        gbc.gridy = 3;
        painelLateral.add(spnParcelas, gbc);

        gbc.gridy = 4;
        painelLateral.add(new JLabel("Subtotal:"), gbc);
        gbc.gridy = 5;
        painelLateral.add(txtSubtotal, gbc);

        gbc.gridy = 6;
        painelLateral.add(new JLabel("Desconto:"), gbc);
        gbc.gridy = 7;
        painelLateral.add(txtDesconto, gbc);

        gbc.gridy = 8;
        painelLateral.add(new JLabel("Total:"), gbc);
        gbc.gridy = 9;
        painelLateral.add(txtTotal, gbc);

        gbc.gridy = 10;
        gbc.insets = new Insets(20, 5, 5, 5);
        painelLateral.add(btnRemover, gbc);

        gbc.gridy = 11;
        painelLateral.add(btnFinalizar, gbc);

        // Adiciona os painéis ao painel principal
        add(painelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(painelLateral, BorderLayout.EAST);

        // Inicializa os valores
        limparCampos();
    }
    
   private void configurarTabelaItens() {
    // Configuração básica da tabela
    tabelaItens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tabelaItens.getTableHeader().setReorderingAllowed(false);
    tabelaItens.setRowHeight(25);
    tabelaItens.setFont(UIConstants.INPUT_FONT);
    tabelaItens.getTableHeader().setFont(UIConstants.LABEL_FONT);

    // Configurar larguras das colunas
    if (tabelaItens.getColumnModel().getColumnCount() > 0) {
        int[] columnWidths = {200, 80, 100, 100}; // Produto, Quantidade, Valor Unit., Subtotal
        for (int i = 0; i < columnWidths.length; i++) {
            tabelaItens.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
    }

    // Adicionar o JScrollPane com suporte a scroll do mouse
    JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, tabelaItens);
    if (scrollPane != null) {
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Adicionar suporte ao scroll do mouse usando lambda
        tabelaItens.addMouseWheelListener(e -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            int scrollAmount = e.getWheelRotation() * verticalBar.getUnitIncrement();
            int newValue = verticalBar.getValue() + scrollAmount;
            
            // Verificar limites do scroll
            newValue = Math.max(verticalBar.getMinimum(), 
                      Math.min(newValue, verticalBar.getMaximum() - verticalBar.getVisibleAmount()));
            verticalBar.setValue(newValue);
        });
    }

    // Adicionar listener para seleção de linha
    tabelaItens.getSelectionModel().addListSelectionListener(e -> 
        btnRemover.setEnabled(!e.getValueIsAdjusting() && tabelaItens.getSelectedRow() != -1));
}

    private void configurarEventos() {
        
        cbProdutosAlimentos.addActionListener(e -> {
    if (cbProdutosAlimentos.getSelectedIndex() > 0) {
        cbProdutosHigieneLimpeza.setSelectedIndex(0);
        cbProdutosHigieneLimpeza.setEnabled(false);
    } else {
        cbProdutosHigieneLimpeza.setEnabled(true);
    }
});

        cbProdutosHigieneLimpeza.addActionListener(e -> {
    if (cbProdutosHigieneLimpeza.getSelectedIndex() > 0) {
        cbProdutosAlimentos.setSelectedIndex(0);
        cbProdutosAlimentos.setEnabled(false);
    } else {
        cbProdutosAlimentos.setEnabled(true);
    }
});

        btnAdicionar.addActionListener(e -> adicionarItem());
        btnRemover.addActionListener(e -> removerItem());
        btnFinalizar.addActionListener(e -> finalizarPedido());

        cbFormaPagamento.addActionListener(e -> {
            boolean isCartaoCredito = "Cartão de Crédito".equals(cbFormaPagamento.getSelectedItem());
            spnParcelas.setEnabled(isCartaoCredito);
            if (!isCartaoCredito) {
                spnParcelas.setValue(1);
            }
        });

        txtDesconto.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            String valor = txtDesconto.getText().replace("%", "").trim();
            txtDesconto.setText(valor);
        }

        @Override
        public void focusLost(FocusEvent e) {
            String valor = txtDesconto.getText().trim();
            if (!valor.isEmpty()) {
                try {
                    double percentual = Double.parseDouble(valor);
                    if (percentual >= 0 && percentual <= 100) {
                        txtDesconto.setText(valor + "%");
                        atualizarTotais();
                    } else {
                        JOptionPane.showMessageDialog(null, 
                            "Percentual de desconto deve estar entre 0 e 100",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null,
                        "Digite um valor válido para o desconto",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    });
}

    private void adicionarItem() {
    try {
        if (cbClientes.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this,
                "Selecione um cliente",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verifica qual combobox está selecionado
        String produtoSelecionado = null;
        if (cbProdutosAlimentos.getSelectedIndex() > 0) {
            produtoSelecionado = cbProdutosAlimentos.getSelectedItem().toString();
        } else if (cbProdutosHigieneLimpeza.getSelectedIndex() > 0) {
            produtoSelecionado = cbProdutosHigieneLimpeza.getSelectedItem().toString();
        }

        if (produtoSelecionado == null || produtoSelecionado.equals(SELECIONE_PRODUTO)) {
            JOptionPane.showMessageDialog(this,
                "Selecione um produto",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long idProduto = Long.valueOf(produtoSelecionado.split(" - ")[0]);
        Produto produto = gerenciadorEstoque.buscarProduto(idProduto);
        int quantidade = (Integer) spnQuantidade.getValue();

        // Verifica se há quantidade suficiente em estoque
        if (produto.getQuantidade() < quantidade) {
            JOptionPane.showMessageDialog(this,
                "Quantidade indisponível em estoque. Disponível: " + produto.getQuantidade(),
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verifica se o produto já está no carrinho
        for (ItemPedido item : itensCarrinho) {
            if (item.getProduto().getId().equals(produto.getId())) {
                if (item.getQuantidade() + quantidade > produto.getQuantidade()) {
                    JOptionPane.showMessageDialog(this,
                        "Quantidade total excede o estoque disponível",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                item.setQuantidade(item.getQuantidade() + quantidade);
                
                // Atualiza a quantidade no produto
                produto.setQuantidade(produto.getQuantidade() - quantidade);
                
                atualizarTabelaItens();
                atualizarTotais();
                atualizarComboProdutos(); // Atualiza os combos com a nova quantidade
                return;
            }
        }

        // Se chegou aqui, é um novo item
        ItemPedido item = new ItemPedido();
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setValorUnitario(produto.getValor());

        // Atualiza a quantidade no produto
        produto.setQuantidade(produto.getQuantidade() - quantidade);

        itensCarrinho.add(item);
        atualizarTabelaItens();
        atualizarTotais();
        atualizarComboProdutos(); // Atualiza os combos com a nova quantidade

        // Limpa seleção
        cbProdutosAlimentos.setSelectedIndex(0);
        cbProdutosHigieneLimpeza.setSelectedIndex(0);
        spnQuantidade.setValue(1);

    } catch (HeadlessException | NumberFormatException e) {
        LOGGER.log(Level.SEVERE, "Erro ao adicionar item", e);
        JOptionPane.showMessageDialog(this,
            "Erro ao adicionar item: " + e.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE);
    }
}

   private void removerItem() {
    int selectedRow = tabelaItens.getSelectedRow();
    if (selectedRow >= 0) {
        ItemPedido item = itensCarrinho.get(selectedRow);
        
        // Restaura a quantidade no estoque
        Produto produto = item.getProduto();
        produto.setQuantidade(produto.getQuantidade() + item.getQuantidade());
        
        itensCarrinho.remove(selectedRow);
        atualizarTabelaItens();
        atualizarTotais();
        
        // Atualiza os combos de produtos
        atualizarComboProdutos();
    }
}

   private void finalizarPedido() {
    if (itensCarrinho.isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Adicione itens ao carrinho antes de finalizar",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        String clienteSelecionado = cbClientes.getSelectedItem().toString();
        String[] partsCliente = clienteSelecionado.split(" - ");
        Long idCliente = Long.valueOf(partsCliente[0]);
        Cliente cliente = gerenciadorClientes.buscarCliente(idCliente);

        double valorTotal = calcularSubtotal() - calcularDesconto();

        Pedido pedido = gerenciadorPedidos.criarPedido(
                cliente,
                new ArrayList<>(itensCarrinho),
                cbFormaPagamento.getSelectedItem().toString(),
                (Integer) spnParcelas.getValue(),
                valorTotal,
                calcularDesconto()
        );

        // Verificar se o PDF foi gerado
        File pdfFile = new File(String.format("pdf/pedido_%d.pdf", pedido.getId()));
        if (pdfFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    String.format("Pedido finalizado com sucesso!\nNúmero do pedido: %d\nPDF gerado em: %s",
                            pedido.getId(), pdfFile.getAbsolutePath()),
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    String.format("Pedido finalizado com sucesso!\nNúmero do pedido: %d\nAtenção: Não foi possível gerar o PDF",
                            pedido.getId()),
                    "Sucesso com Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }

        limparCampos();

    } catch (HeadlessException | NumberFormatException e) {
        LOGGER.log(Level.SEVERE, "Erro ao finalizar pedido", e);
        JOptionPane.showMessageDialog(this,
                "Erro ao finalizar pedido: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
    }
}

   private void atualizarTabelaItens() {
    modeloTabela.setRowCount(0);
    for (ItemPedido item : itensCarrinho) {
        // Criar um spinner para a quantidade
        JSpinner spnQuantidadeItem = new JSpinner(new SpinnerNumberModel(item.getQuantidade(), 1, item.getProduto().getQuantidade(), 1));
        
        // Adicionar listener para atualizar a quantidade quando alterada
        spnQuantidadeItem.addChangeListener(e -> {
            int novaQuantidade = (Integer) spnQuantidadeItem.getValue();
            item.setQuantidade(novaQuantidade);
            atualizarTotais();
        });

        modeloTabela.addRow(new Object[]{
            item.getProduto().getNome(),
            item.getQuantidade(), // Usar o número diretamente ao invés do JSpinner
            String.format("R$ %.2f", item.getValorUnitario()),
            String.format("R$ %.2f", item.getQuantidade() * item.getValorUnitario())
        });
    }
}


    private void atualizarTotais() {
        double subtotal = calcularSubtotal();
        double desconto = calcularDesconto();
        double total = subtotal - desconto;

        txtSubtotal.setText(String.format("R$ %.2f", subtotal));
        txtTotal.setText(String.format("R$ %.2f", total));
    }

    private double calcularSubtotal() {
        return itensCarrinho.stream()
                .mapToDouble(item -> item.getQuantidade() * item.getValorUnitario())
                .sum();
    }

    private double calcularDesconto() {
    try {
        String valor = txtDesconto.getText().replace("%", "").trim();
        if (valor.isEmpty()) {
            return 0.0;
        }
        double percentual = Double.parseDouble(valor);
        if (percentual < 0 || percentual > 100) {
            throw new IllegalArgumentException("Percentual de desconto deve estar entre 0 e 100");
        }
        return (calcularSubtotal() * percentual) / 100;
    } catch (NumberFormatException e) {
        return 0.0;
    }
}


    public void atualizarDados() {
    try {
        // Primeiro, atualize os dados
        atualizarComboClientes();
        atualizarComboProdutos();
        
        // Depois, atualize os totais apenas se houver itens no carrinho
        if (!itensCarrinho.isEmpty()) {
            atualizarTotais();
        } else {
            // Se não houver itens, limpe os campos de totais
            txtSubtotal.setText("R$ 0,00");
            txtDesconto.setText("");
            txtTotal.setText("R$ 0,00");
        }
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Erro ao atualizar dados", e);
        JOptionPane.showMessageDialog(this,
            "Erro ao atualizar dados: " + e.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE);
    }
}

    private void atualizarComboClientes() {
    try {
        cbClientes.removeAllItems();
        cbClientes.addItem(SELECIONE_CLIENTE);

        List<Cliente> clientes = gerenciadorClientes.listarClientes();
        if (clientes != null) {
            for (Cliente cliente : clientes) {
                if (cliente != null && cliente.getId() != null) {
                    cbClientes.addItem(String.format("%d - %s (%s)",
                        cliente.getId(),
                        cliente.getNome(),
                        cliente.getCpf()));
                }
            }
        }
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Erro ao carregar lista de clientes", e);
        throw new RuntimeException("Erro ao carregar lista de clientes: " + e.getMessage());
    }
}

    private void atualizarComboProdutos() {
    try {
        // Limpa os comboboxes
        cbProdutosAlimentos.removeAllItems();
        cbProdutosHigieneLimpeza.removeAllItems();
        
        // Adiciona item padrão
        cbProdutosAlimentos.addItem(SELECIONE_PRODUTO);
        cbProdutosHigieneLimpeza.addItem(SELECIONE_PRODUTO);

        List<Produto> produtos = gerenciadorEstoque.listarProdutos();
        if (produtos != null) {
            for (Produto produto : produtos) {
                if (produto != null && produto.getId() != null) {
                    // Calcula quantidade disponível considerando itens no carrinho
                    int quantidadeDisponivel = produto.getQuantidade();
                    
                    // Reduz a quantidade disponível baseado nos itens já no carrinho
                    for (ItemPedido item : itensCarrinho) {
                        if (item.getProduto().getId().equals(produto.getId())) {
                            quantidadeDisponivel -= item.getQuantidade();
                            break;
                        }
                    }
                    
                    // Só adiciona ao combo se ainda houver quantidade disponível
                    if (quantidadeDisponivel > 0) {
                        String itemText = String.format("%d - %s (R$ %.2f) - Estoque: %d",
                            produto.getId(),
                            produto.getNome(),
                            produto.getValor(),
                            quantidadeDisponivel);
                        
                        if ("ALIMENTO".equals(produto.getCategoria())) {
                            cbProdutosAlimentos.addItem(itemText);
                        } else if ("HIGIENE/LIMPEZA".equals(produto.getCategoria())) {
                            cbProdutosHigieneLimpeza.addItem(itemText);
                        }
                    }
                }
            }
        }
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Erro ao carregar lista de produtos", e);
        throw new RuntimeException("Erro ao carregar lista de produtos: " + e.getMessage());
    }
}
    private void limparCampos() {
    // Primeiro, limpe os itens do carrinho e a tabela
    itensCarrinho.clear();
    modeloTabela.setRowCount(0);

    // Atualize os campos monetários
    txtDesconto.setText("");
    txtSubtotal.setText("R$ 0,00");
    txtTotal.setText("R$ 0,00");
  
    // Reabilitar ambos os ComboBoxes
    cbProdutosAlimentos.setEnabled(true);
    cbProdutosHigieneLimpeza.setEnabled(true);

    // Configure os spinners
    spnQuantidade.setValue(1);
    spnParcelas.setValue(1);
    spnParcelas.setEnabled(false);

    // Configure o combo de forma de pagamento
    
    if (cbProdutosAlimentos.getItemCount() > 0) {
        cbProdutosAlimentos.setSelectedIndex(0);
    }
    if (cbProdutosHigieneLimpeza.getItemCount() > 0) {
        cbProdutosHigieneLimpeza.setSelectedIndex(0);
    }
    
    if (cbFormaPagamento.getItemCount() > 0) {
        cbFormaPagamento.setSelectedIndex(0);
    }

    // Atualize os combos de clientes e produtos
    atualizarComboClientes();
    atualizarComboProdutos();
   }
}