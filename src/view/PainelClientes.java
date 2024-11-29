package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.logging.*;
import manager.GerenciadorClientes;
import model.Cliente;

public final class PainelClientes extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(PainelClientes.class.getName());
    
    private final GerenciadorClientes gerenciadorClientes;
    private JTable tabelaClientes;
    private DefaultTableModel modeloTabela;
    private JTextField txtNome;
    private JTextField txtCPF;
    private JTextField txtEndereco;
    private JTextField txtContato;
    private JButton btnAdicionar;
    private JButton btnAtualizar;
    private JButton btnRemover;
    private JButton btnLimpar;

    public PainelClientes(GerenciadorClientes gerenciadorClientes) {
        this.gerenciadorClientes = gerenciadorClientes;
        setLayout(new BorderLayout(10, 10));
        inicializarComponentes();
        configurarEventos();
        atualizarDados();
    }

    private void inicializarComponentes() {
        // Painel de formulário
        JPanel painelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campos do formulário
        txtNome = new JTextField(30);
        txtCPF = new JTextField(14);
        txtEndereco = new JTextField(50);
        txtContato = new JTextField(20);

        // Layout do formulário
        gbc.gridx = 0; gbc.gridy = 0;
        painelFormulario.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        painelFormulario.add(txtNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        painelFormulario.add(new JLabel("CPF:"), gbc);
        gbc.gridx = 1;
        painelFormulario.add(txtCPF, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        painelFormulario.add(new JLabel("Endereço:"), gbc);
        gbc.gridx = 1;
        painelFormulario.add(txtEndereco, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        painelFormulario.add(new JLabel("Contato:"), gbc);
        gbc.gridx = 1;
        painelFormulario.add(txtContato, gbc);

        // Botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAdicionar = new JButton("Adicionar");
        btnAtualizar = new JButton("Atualizar");
        btnRemover = new JButton("Remover");
        btnLimpar = new JButton("Limpar");

        painelBotoes.add(btnAdicionar);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnRemover);
        painelBotoes.add(btnLimpar);

        // Configurar tabela
        String[] colunas = {"ID", "Nome", "CPF", "Endereço", "Contato"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaClientes = new JTable(modeloTabela);
        tabelaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaClientes.getTableHeader().setReorderingAllowed(false);

        // Adicionar componentes ao painel
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.add(painelFormulario, BorderLayout.CENTER);
        painelSuperior.add(painelBotoes, BorderLayout.SOUTH);

        add(painelSuperior, BorderLayout.NORTH);
        add(new JScrollPane(tabelaClientes), BorderLayout.CENTER);
    }

    private void configurarEventos() {
    btnAdicionar.addActionListener(e -> adicionarCliente());
    btnAtualizar.addActionListener(e -> atualizarCliente());
    btnRemover.addActionListener(e -> removerCliente());
    btnLimpar.addActionListener(e -> limparCampos());

    tabelaClientes.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            carregarClienteSelecionado();
        }
    });

    // Máscara para CPF
    txtCPF.addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
            formatarCPF();
        }
    });

    // Máscara para telefone
    txtContato.addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
            formatarTelefone();
        }
    });
}

private void formatarCPF() {
    String cpf = txtCPF.getText().replaceAll("[^0-9]", "");
    
    // Verifica se tem 11 dígitos
    if (cpf.length() == 11) {
        // Formata o CPF no padrão XXX.XXX.XXX-XX
        cpf = cpf.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        txtCPF.setText(cpf);
    }
}

private boolean validarCPF(String cpf) {
    // Verifica se tem 11 dígitos
    if (cpf.length() != 11) {
        return false;
    }

    // Verifica se todos os dígitos são iguais
    if (cpf.matches("(\\d)\\1{10}")) {
        return false;
    }

    // Calcula primeiro dígito verificador
    int soma = 0;
    for (int i = 0; i < 9; i++) {
        soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
    }
    int primeiroDigito = 11 - (soma % 11);
    if (primeiroDigito > 9) {
        primeiroDigito = 0;
    }
    if (Character.getNumericValue(cpf.charAt(9)) != primeiroDigito) {
        return false;
    }

    // Calcula segundo dígito verificador
    soma = 0;
    for (int i = 0; i < 10; i++) {
        soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
    }
    int segundoDigito = 11 - (soma % 11);
    if (segundoDigito > 9) {
        segundoDigito = 0;
    }
    return Character.getNumericValue(cpf.charAt(10)) == segundoDigito;
}

private void formatarTelefone() {
    String telefone = txtContato.getText().replaceAll("[^0-9]", "");
    if (telefone.length() == 11) {
        telefone = telefone.replaceFirst("(\\d{2})(\\d{1})(\\d{4})(\\d{4})", "($1) $2 $3-$4");
        txtContato.setText(telefone);
    }
}
  private void adicionarCliente() {
    try {
        validarCampos();
        
        // Remove formatação do CPF antes de enviar
        String cpfLimpo = txtCPF.getText().replaceAll("[^0-9]", "");
        
        // Formata o CPF
        String cpfFormatado = cpfLimpo.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        
        try {
            Cliente cliente = gerenciadorClientes.adicionarCliente(
                txtNome.getText().trim(),
                cpfFormatado,  // Usa o CPF formatado
                txtEndereco.getText().trim(),
                txtContato.getText().trim()
            );
            
            LOGGER.log(Level.INFO, "Cliente adicionado com sucesso: {0}", cliente.getId());
            JOptionPane.showMessageDialog(this,
                "Cliente adicionado com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);

            limparCampos();
            atualizarDados();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("UNIQUE constraint failed: clientes.cpf")) {
                JOptionPane.showMessageDialog(this,
                    "CPF já cadastrado no sistema",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            } else {
                throw e;
            }
        }

    } catch (RuntimeException e) {
        LOGGER.log(Level.SEVERE, "Erro ao adicionar cliente: {0}", e.getMessage());
        JOptionPane.showMessageDialog(this,
            "Erro ao adicionar cliente: " + e.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE);
    }
}
    
    private void atualizarCliente() {
        int selectedRow = tabelaClientes.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Selecione um cliente para atualizar",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            validarCampos();
            
            Long id = (Long) modeloTabela.getValueAt(selectedRow, 0);
            Cliente cliente = new Cliente();
            cliente.setId(id);
            cliente.setNome(txtNome.getText());
            cliente.setCpf(txtCPF.getText());
            cliente.setEndereco(txtEndereco.getText());
            cliente.setContato(txtContato.getText());

            gerenciadorClientes.atualizarCliente(cliente);
            
            LOGGER.log(Level.INFO, "Cliente atualizado com sucesso: {0}", id);
            JOptionPane.showMessageDialog(this,
                "Cliente atualizado com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);

            limparCampos();
            atualizarDados();

        } catch (HeadlessException e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar cliente: {0}", e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Erro ao atualizar cliente: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removerCliente() {
        int selectedRow = tabelaClientes.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Selecione um cliente para remover",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Tem certeza que deseja remover este cliente?",
            "Confirmar Remoção",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Long id = (Long) modeloTabela.getValueAt(selectedRow, 0);
                gerenciadorClientes.removerCliente(id);
                
                LOGGER.log(Level.INFO, "Cliente removido com sucesso: {0}", id);
                JOptionPane.showMessageDialog(this,
                    "Cliente removido com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);

                limparCampos();
                atualizarDados();

            } catch (HeadlessException e) {
                LOGGER.log(Level.SEVERE, "Erro ao remover cliente: {0}", e.getMessage());
                JOptionPane.showMessageDialog(this,
                    "Erro ao remover cliente: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void carregarClienteSelecionado() {
        int selectedRow = tabelaClientes.getSelectedRow();
        if (selectedRow >= 0) {
            txtNome.setText(modeloTabela.getValueAt(selectedRow, 1).toString());
            txtCPF.setText(modeloTabela.getValueAt(selectedRow, 2).toString());
            txtEndereco.setText(modeloTabela.getValueAt(selectedRow, 3).toString());
            txtContato.setText(modeloTabela.getValueAt(selectedRow, 4).toString());
        }
    }

    public void atualizarDados() {
        try {
            List<Cliente> clientes = gerenciadorClientes.listarClientes();
            atualizarTabela(clientes);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar dados: {0}", e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar clientes: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarTabela(List<Cliente> clientes) {
        modeloTabela.setRowCount(0);
        for (Cliente cliente : clientes) {
            Object[] row = {
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getEndereco(),
                cliente.getContato()
            };
            modeloTabela.addRow(row);
        }
    }

    private void limparCampos() {
        txtNome.setText("");
        txtCPF.setText("");
        txtEndereco.setText("");
        txtContato.setText("");
        tabelaClientes.clearSelection();
    }

   private void validarCampos() {
    if (txtNome.getText().trim().isEmpty()) {
        throw new IllegalArgumentException("Nome é obrigatório");
    }
    
    String cpf = txtCPF.getText().replaceAll("[^0-9]", "");
    if (cpf.isEmpty()) {
        throw new IllegalArgumentException("CPF é obrigatório");
    }
    
    if (!validarCPF(cpf)) {
        throw new IllegalArgumentException("CPF inválido");
    }
  }
}