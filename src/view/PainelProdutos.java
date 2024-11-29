package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.*;
import manager.GerenciadorEstoque;
import model.Produto;

public final class PainelProdutos extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(PainelProdutos.class.getName());
    
   private final GerenciadorEstoque gerenciadorEstoque;
private JTable tabelaAlimentos;
private JTable tabelaHigieneLimpeza;
private DefaultTableModel modeloTabelaAlimentos;
private DefaultTableModel modeloTabelaHigieneLimpeza;
private JTextField txtNome;
private final JTextField txtDescricao;
private JComboBox<String> cbCategoria;
private JSpinner spnQuantidade;
private JTextField txtValor;
private JButton btnAdicionar;
private JButton btnAtualizar;
private JButton btnRemover;
private JButton btnLimpar;;
private JTextArea txtAlerta;


    public PainelProdutos(GerenciadorEstoque gerenciadorEstoque) {
    this.gerenciadorEstoque = gerenciadorEstoque;
    setLayout(new BorderLayout(10, 10));
    inicializarComponentes();
    configurarTabelaItens(); // Adicione esta linha
    configurarEventos();
    atualizarDados();
        txtNome = new JTextField();
        txtDescricao = new JTextField(); // Inicializa o campo de descrição
        cbCategoria = new JComboBox<>();
        spnQuantidade = new JSpinner();
        txtValor = new JTextField();
        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Nome:"));
        panel.add(txtNome);
        panel.add(new JLabel("Descrição:")); // Label para descrição
        panel.add(txtDescricao); // Adiciona o campo de descrição
        panel.add(new JLabel("Categoria:"));
        panel.add(cbCategoria);
        panel.add(new JLabel("Quantidade:"));
        panel.add(spnQuantidade);
        panel.add(new JLabel("Valor:"));
        panel.add(txtValor);

        add(panel, BorderLayout.CENTER);

        inicializarComponentes();
        configurarEventos();
        atualizarDados();
    }
    
private void inicializarComponentes() {
    // Configuração layout
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Painel superior que conterá o formulário e o alerta
    JPanel painelSuperior = new JPanel(new BorderLayout(10, 10));
    
    // Painel de formulário
    JPanel painelFormulario = criarPainelFormulario();
    painelSuperior.add(painelFormulario, BorderLayout.CENTER);
    
    // Painel de alerta
    JPanel painelAlerta = new JPanel(new BorderLayout());
    painelAlerta.setBorder(BorderFactory.createTitledBorder("Alerta de Estoque"));
    
    txtAlerta = new JTextArea(5, 20);
    txtAlerta.setEditable(false);
    txtAlerta.setForeground(Color.RED);
    txtAlerta.setFont(new Font("Arial", Font.BOLD, 12));
    JScrollPane scrollAlerta = new JScrollPane(txtAlerta);
    painelAlerta.add(scrollAlerta);
    
    painelSuperior.add(painelAlerta, BorderLayout.EAST);
    
    // Painel de tabelas
    JPanel painelTabelas = new JPanel(new GridLayout(1, 2, 10, 10));
    
    // Tabela Alimentos
    String[] colunas = {"ID", "Nome", "Categoria", "Quantidade", "Valor"};
    modeloTabelaAlimentos = new DefaultTableModel(colunas, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    tabelaAlimentos = new JTable(modeloTabelaAlimentos);
    
    
    JPanel painelAlimentos = new JPanel(new BorderLayout());
    JLabel labelAlimentos = new JLabel("ALIMENTOS", SwingConstants.CENTER);
    labelAlimentos.setFont(new Font("Arial", Font.BOLD, 14));
    painelAlimentos.add(labelAlimentos, BorderLayout.NORTH);
    painelAlimentos.add(new JScrollPane(tabelaAlimentos), BorderLayout.CENTER);
    
    // Tabela Higiene/Limpeza
    modeloTabelaHigieneLimpeza = new DefaultTableModel(colunas, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    tabelaHigieneLimpeza = new JTable(modeloTabelaHigieneLimpeza);
   
    
    JPanel painelHigieneLimpeza = new JPanel(new BorderLayout());
    JLabel labelHigiene = new JLabel("HIGIENE/LIMPEZA", SwingConstants.CENTER);
    labelHigiene.setFont(new Font("Arial", Font.BOLD, 14));
    painelHigieneLimpeza.add(labelHigiene, BorderLayout.NORTH);
    painelHigieneLimpeza.add(new JScrollPane(tabelaHigieneLimpeza), BorderLayout.CENTER);
    
    // Adiciona as tabelas ao painel
    painelTabelas.add(painelAlimentos);
    painelTabelas.add(painelHigieneLimpeza);
    
    // Adiciona os painéis ao painel principal
    add(painelSuperior, BorderLayout.NORTH);
    add(painelTabelas, BorderLayout.CENTER);
}
    
private void atualizarAlertaEstoque() {
    List<Produto> produtosBaixoEstoque = gerenciadorEstoque.listarProdutosEstoqueBaixo();
    StringBuilder mensagem = new StringBuilder();
    
    if (produtosBaixoEstoque.isEmpty()) {
        txtAlerta.setText("Não há produtos com estoque baixo");
        txtAlerta.setForeground(Color.BLACK);
    } else {
        for (Produto produto : produtosBaixoEstoque) {
            mensagem.append(String.format("ALERTA: %s - Quantidade: %d (Mínimo: %d)\n", 
                produto.getNome(), 
                produto.getQuantidade(),
                produto.getEstoqueMinimo()));
        }
        txtAlerta.setText(mensagem.toString());
        txtAlerta.setForeground(Color.RED);
    }
}
  
    private JPanel criarPainelFormulario() {
        JPanel painelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campos do formulário
        txtNome = new JTextField(30);
        cbCategoria = new JComboBox<>(new String[]{"ALIMENTO", "HIGIENE/LIMPEZA"});
        spnQuantidade = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
        txtValor = new JTextField(10);

        // Botões
        btnAdicionar = new JButton("Adicionar");
        btnAtualizar = new JButton("Atualizar");
        btnRemover = new JButton("Remover");
        btnLimpar = new JButton("Limpar");

        // Layout do formulário
        gbc.gridx = 0; gbc.gridy = 0;
        painelFormulario.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        painelFormulario.add(txtNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        painelFormulario.add(new JLabel("Categoria:"), gbc);
        gbc.gridx = 1;
        painelFormulario.add(cbCategoria, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        painelFormulario.add(new JLabel("Quantidade:"), gbc);
        gbc.gridx = 1;
        painelFormulario.add(spnQuantidade, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        painelFormulario.add(new JLabel("Valor:"), gbc);
        gbc.gridx = 1;
        painelFormulario.add(txtValor, gbc);

        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelBotoes.add(btnAdicionar);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnRemover);
        painelBotoes.add(btnLimpar);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        painelFormulario.add(painelBotoes, gbc);

        return painelFormulario;
    }
    
    private void configurarTabelaItens() {
    // Configuração da tabela Alimentos
    tabelaAlimentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tabelaAlimentos.getTableHeader().setReorderingAllowed(false);
    tabelaAlimentos.setRowHeight(25);
    tabelaAlimentos.setFont(new Font("Arial", Font.PLAIN, 12));
    tabelaAlimentos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

    // Configuração da tabela Higiene/Limpeza
    tabelaHigieneLimpeza.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tabelaHigieneLimpeza.getTableHeader().setReorderingAllowed(false);
    tabelaHigieneLimpeza.setRowHeight(25);
    tabelaHigieneLimpeza.setFont(new Font("Arial", Font.PLAIN, 12));
    tabelaHigieneLimpeza.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

    // Configurar larguras das colunas 
    int[] columnWidths = {50, 200, 100, 80, 100}; // ID, Nome, Categoria, Quantidade, Valor
    
    for (int i = 0; i < columnWidths.length; i++) {
        tabelaAlimentos.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        tabelaHigieneLimpeza.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
      }
    }

    private void configurarEventos() {
        btnAdicionar.addActionListener(e -> adicionarProduto());
        btnAtualizar.addActionListener(e -> atualizarProduto());
        btnRemover.addActionListener(e -> removerProduto());
        btnLimpar.addActionListener(e -> limparCampos());

        tabelaAlimentos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tabelaHigieneLimpeza.clearSelection();
                carregarProdutoSelecionado(tabelaAlimentos);
            }
        });

        tabelaHigieneLimpeza.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tabelaAlimentos.clearSelection();
                carregarProdutoSelecionado(tabelaHigieneLimpeza);
            }
        });
    }

   public void atualizarDados() {
    try {
        List<Produto> produtos = gerenciadorEstoque.listarProdutos();
        atualizarTabelas(produtos);
        atualizarAlertaEstoque(); 
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Erro ao atualizar dados: {0}", e.getMessage());
        JOptionPane.showMessageDialog(this,
            "Erro ao carregar produtos: " + e.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE);
    }
}

    private void atualizarTabelas(List<Produto> produtos) {
        modeloTabelaAlimentos.setRowCount(0);
        modeloTabelaHigieneLimpeza.setRowCount(0);
        
        for (Produto produto : produtos) {
            Object[] row = {
                produto.getId(),
                produto.getNome(),
                produto.getCategoria(),
                produto.getQuantidade(),
                String.format("R$ %.2f", produto.getValor())
            };
            
            if (produto.getCategoria().equals("ALIMENTO")) {
                modeloTabelaAlimentos.addRow(row);
            } else {
                modeloTabelaHigieneLimpeza.addRow(row);
            }
        }
    }

   private void adicionarProduto() {
    try {
        validarCampos();
        
        String nome = txtNome.getText();
        String categoria = cbCategoria.getSelectedItem().toString();
        String descricao = txtDescricao.getText();
        int quantidade = (Integer) spnQuantidade.getValue();
        double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
        
        Produto produto = gerenciadorEstoque.adicionarProduto(
            nome,        
            categoria,   
            descricao,  
            valor,       
            quantidade   
        );
        
        LOGGER.log(Level.INFO, "Produto adicionado com sucesso: {0}", produto.getId());
        JOptionPane.showMessageDialog(this,
            "Produto adicionado com sucesso!",
            "Sucesso",
            JOptionPane.INFORMATION_MESSAGE);

        limparCampos();
        atualizarDados();
      
       

    } catch (HeadlessException | NumberFormatException e) {
        LOGGER.log(Level.SEVERE, "Erro ao adicionar produto: {0}", e.getMessage());
        JOptionPane.showMessageDialog(this,
            "Erro ao adicionar produto: " + e.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE);
    }
}

private void atualizarProduto() {
    int selectedRowAlimentos = tabelaAlimentos.getSelectedRow();
    int selectedRowHigiene = tabelaHigieneLimpeza.getSelectedRow();
    
    if (selectedRowAlimentos < 0 && selectedRowHigiene < 0) {
        JOptionPane.showMessageDialog(this,
            "Selecione um produto para atualizar",
            "Aviso",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        validarCampos();
        
        JTable tabelaSelecionada = selectedRowAlimentos >= 0 ? tabelaAlimentos : tabelaHigieneLimpeza;
        int selectedRow = selectedRowAlimentos >= 0 ? selectedRowAlimentos : selectedRowHigiene;
        DefaultTableModel modeloSelecionado = (DefaultTableModel) tabelaSelecionada.getModel();
        
        Long id = (Long) modeloSelecionado.getValueAt(selectedRow, 0);
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome(txtNome.getText());
        produto.setCategoria(cbCategoria.getSelectedItem().toString());
        produto.setQuantidade((Integer) spnQuantidade.getValue());
        produto.setValor(Double.parseDouble(txtValor.getText().replace(",", ".")));

        gerenciadorEstoque.atualizarProduto(produto);
        
        LOGGER.log(Level.INFO, "Produto atualizado com sucesso: {0}", id);
        JOptionPane.showMessageDialog(this,
            "Produto atualizado com sucesso!",
            "Sucesso",
            JOptionPane.INFORMATION_MESSAGE);

        limparCampos();
        atualizarDados();
       

    } catch (HeadlessException | NumberFormatException e) {
        LOGGER.log(Level.SEVERE, "Erro ao atualizar produto: {0}", e.getMessage());
        JOptionPane.showMessageDialog(this,
            "Erro ao atualizar produto: " + e.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE);
    }
}

private void removerProduto() {
    int selectedRowAlimentos = tabelaAlimentos.getSelectedRow();
    int selectedRowHigiene = tabelaHigieneLimpeza.getSelectedRow();
    
    if (selectedRowAlimentos < 0 && selectedRowHigiene < 0) {
        JOptionPane.showMessageDialog(this,
            "Selecione um produto para remover",
            "Aviso",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(this,
        "Tem certeza que deseja remover este produto?",
        "Confirmar Remoção",
        JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
        try {
            JTable tabelaSelecionada = selectedRowAlimentos >= 0 ? tabelaAlimentos : tabelaHigieneLimpeza;
            int selectedRow = selectedRowAlimentos >= 0 ? selectedRowAlimentos : selectedRowHigiene;
            DefaultTableModel modeloSelecionado = (DefaultTableModel) tabelaSelecionada.getModel();
            
            Long id = (Long) modeloSelecionado.getValueAt(selectedRow, 0);
            gerenciadorEstoque.removerProduto(id);
            
            LOGGER.log(Level.INFO, "Produto removido com sucesso: {0}", id);
            JOptionPane.showMessageDialog(this,
                "Produto removido com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);

            limparCampos();
            atualizarDados();
            
           

        } catch (HeadlessException e) {
            LOGGER.log(Level.SEVERE, "Erro ao remover produto: {0}", e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Erro ao remover produto: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

private void carregarProdutoSelecionado(JTable tabela) {
    int selectedRow = tabela.getSelectedRow();
    if (selectedRow >= 0) {
        DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
        txtNome.setText(modelo.getValueAt(selectedRow, 1).toString());
        cbCategoria.setSelectedItem(modelo.getValueAt(selectedRow, 2).toString());
        spnQuantidade.setValue(Integer.valueOf(modelo.getValueAt(selectedRow, 3).toString()));
        txtValor.setText(modelo.getValueAt(selectedRow, 4).toString().replace("R$ ", ""));
    }
}

private void limparCampos() {
    txtNome.setText("");
    cbCategoria.setSelectedIndex(0);
    spnQuantidade.setValue(1);
    txtValor.setText("");
    tabelaAlimentos.clearSelection();
    tabelaHigieneLimpeza.clearSelection();
}

private void validarCampos() {
        if (txtNome.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (cbCategoria.getSelectedIndex() == -1) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
        try {
            Double.valueOf(txtValor.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido");
        }
    }
} 