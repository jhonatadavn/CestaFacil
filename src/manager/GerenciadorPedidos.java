package manager;

import model.*;
import util.PDFGenerator;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.*;

public class GerenciadorPedidos {
    private static final Logger LOGGER = Logger.getLogger(GerenciadorPedidos.class.getName());
    private final DatabaseManager dbManager;
    private final GerenciadorEstoque gerenciadorEstoque;

    public GerenciadorPedidos(GerenciadorEstoque gerenciadorEstoque) {
        this.dbManager = DatabaseManager.getInstance();
        this.gerenciadorEstoque = gerenciadorEstoque;
    }


    public Pedido criarPedido(Cliente cliente, List<ItemPedido> itens,
                             String formaPagamento, int numeroParcelas,
                             double valorTotal, double desconto) {
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            dbManager.iniciarTransacao(conn);

            validarPedido(cliente, itens, formaPagamento, numeroParcelas, valorTotal, desconto);
            verificarEstoqueItens(itens);

            Pedido pedido = inserirPedido(conn, cliente, formaPagamento, numeroParcelas, valorTotal, desconto);
            inserirItensPedido(conn, pedido, itens);
            atualizarEstoque(conn, itens);

            dbManager.confirmarTransacao(conn);
            LOGGER.log(Level.INFO, "Pedido criado com sucesso: {0}", pedido.getId());

            pedido.setItens(itens);
            gerarPDFPedido(pedido);

            return pedido;

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    dbManager.cancelarTransacao(conn);
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Erro ao realizar rollback", ex);
            }
            LOGGER.log(Level.SEVERE, "Erro ao criar pedido", e);
            throw new RuntimeException("Erro ao criar pedido: " + e.getMessage(), e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erro ao fechar conexão", e);
            }
        }
    }

    private void validarPedido(Cliente cliente, List<ItemPedido> itens,
                              String formaPagamento, int numeroParcelas,
                              double valorTotal, double desconto) {
        if (cliente == null || cliente.getId() == null) {
            throw new IllegalArgumentException("Cliente inválido");
        }

        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter pelo menos um item");
        }

        if (formaPagamento == null || formaPagamento.trim().isEmpty()) {
            throw new IllegalArgumentException("Forma de pagamento inválida");
        }

        if (formaPagamento.equalsIgnoreCase("Cartão de Crédito") && numeroParcelas < 1) {
            throw new IllegalArgumentException("Número de parcelas deve ser maior que zero");
        }

        if (valorTotal <= 0) {
            throw new IllegalArgumentException("Valor total deve ser maior que zero");
        }

        if (desconto < 0) {
            throw new IllegalArgumentException("Desconto não pode ser negativo");
        }

        double subtotal = itens.stream()
                .mapToDouble(item -> item.getQuantidade() * item.getValorUnitario())
                .sum();

        if (desconto > subtotal) {
            throw new IllegalArgumentException("Desconto não pode ser maior que o subtotal");
        }
    }

    private void verificarEstoqueItens(List<ItemPedido> itens) {
        for (ItemPedido item : itens) {
            Produto produto = gerenciadorEstoque.buscarProduto(item.getProduto().getId());
            if (produto == null) {
                throw new IllegalArgumentException("Produto não encontrado: " + item.getProduto().getNome());
            }
            if (produto.getQuantidade() < item.getQuantidade()) {
                throw new IllegalArgumentException(
                    String.format("Quantidade insuficiente em estoque para o produto %s. Disponível: %d, Solicitado: %d",
                        produto.getNome(), produto.getQuantidade(), item.getQuantidade()));
            }
        }
    }

    private Pedido inserirPedido(Connection conn, Cliente cliente,
                                String formaPagamento, int numeroParcelas,
                                double valorTotal, double desconto) throws SQLException {
        String sql = """
            INSERT INTO pedidos (cliente_id, data_pedido, forma_pagamento,
                               numero_parcelas, subtotal, desconto, valor_total, status)
            VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, 'PENDENTE')
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, cliente.getId());
            stmt.setString(2, formaPagamento);
            stmt.setInt(3, numeroParcelas);
            stmt.setDouble(4, valorTotal + desconto);
            stmt.setDouble(5, desconto);
            stmt.setDouble(6, valorTotal);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Pedido pedido = new Pedido();
                    pedido.setId(rs.getLong(1));
                    pedido.setCliente(cliente);
                    pedido.setFormaPagamento(formaPagamento);
                    pedido.setNumeroParcelas(numeroParcelas);
                    pedido.setValorTotal(valorTotal);
                    pedido.setDesconto(desconto);
                    pedido.setDataPedido(new Timestamp(System.currentTimeMillis()));
                    return pedido;
                }
                throw new SQLException("Falha ao criar pedido, nenhum ID obtido.");
            }
        }
    }

    private void inserirItensPedido(Connection conn, Pedido pedido, List<ItemPedido> itens) throws SQLException {
        String sql = """
            INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, valor_unitario, subtotal)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (ItemPedido item : itens) {
                stmt.setLong(1, pedido.getId());
                stmt.setLong(2, item.getProduto().getId());
                stmt.setInt(3, item.getQuantidade());
                stmt.setDouble(4, item.getValorUnitario());
                stmt.setDouble(5, item.getQuantidade() * item.getValorUnitario());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setId(rs.getLong(1));
                    }
                }
            }
        }
    }

   private void atualizarEstoque(Connection conn, List<ItemPedido> itens) throws SQLException {
    String sql = "UPDATE produtos SET quantidade = quantidade - ? WHERE id = ? AND quantidade >= ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        for (ItemPedido item : itens) {
            stmt.setInt(1, item.getQuantidade());
            stmt.setLong(2, item.getProduto().getId());
            stmt.setInt(3, item.getQuantidade()); // Verifica se há quantidade suficiente
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Quantidade insuficiente em estoque para o produto: " + 
                    item.getProduto().getNome());
            }
        }
    }
}

   public List<Pedido> listarPedidos() {
    List<Pedido> pedidos = new ArrayList<>();
    String sql = """
        SELECT p.*, 
               c.nome as cliente_nome, 
               c.cpf as cliente_cpf,
               c.endereco as cliente_endereco,
               c.contato as cliente_contato
        FROM pedidos p 
        JOIN clientes c ON p.cliente_id = c.id 
        ORDER BY p.data_pedido DESC
        """;

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                pedidos.add(criarPedidoDoResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar pedidos", e);
            throw new RuntimeException("Erro ao listar pedidos: " + e.getMessage());
        }

        return pedidos;
    }

    public Pedido buscarPedido(Long id) {
        String sql = """
            SELECT p.*, c.nome as cliente_nome, c.cpf as cliente_cpf
            FROM pedidos p
            JOIN clientes c ON p.cliente_id = c.id
            WHERE p.id = ?
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarPedidoDoResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar pedido", e);
            throw new RuntimeException("Erro ao buscar pedido: " + e.getMessage());
        }

        return null;
    }
    
    public void concluirPedido(Long pedidoId) {
    Connection conn = null;
    try {
        conn = dbManager.getConnection();
        dbManager.iniciarTransacao(conn);

        // Verifica se o pedido existe e pode ser concluído
        String sqlVerifica = "SELECT status FROM pedidos WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlVerifica)) {
            stmt.setLong(1, pedidoId);
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                throw new IllegalArgumentException("Pedido não encontrado");
            }
            
            String status = rs.getString("status");
            if (!"PENDENTE".equals(status)) {
                throw new IllegalArgumentException("Pedido não pode ser concluído: " + status);
            }
        }

        // Atualiza status do pedido
        String sqlPedido = """
            UPDATE pedidos 
            SET status = 'CONCLUÍDO',
                data_conclusao = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sqlPedido)) {
            stmt.setLong(1, pedidoId);
            stmt.executeUpdate();
        }

        dbManager.confirmarTransacao(conn);
        LOGGER.log(Level.INFO, "Pedido concluído com sucesso: {0}", pedidoId);

    } catch (SQLException e) {
        try {
            if (conn != null) {
                dbManager.cancelarTransacao(conn);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao realizar rollback", ex);
        }
        LOGGER.log(Level.SEVERE, "Erro ao concluir pedido", e);
        throw new RuntimeException("Erro ao concluir pedido: " + e.getMessage());
    } finally {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao fechar conexão", e);
        }
    }
}
    
    public void cancelarPedido(Long pedidoId, String motivo) {
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            dbManager.iniciarTransacao(conn);

            // Verifica se o pedido existe e pode ser cancelado
            String sqlVerifica = "SELECT status FROM pedidos WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlVerifica)) {
                stmt.setLong(1, pedidoId);
                ResultSet rs = stmt.executeQuery();
                
                if (!rs.next()) {
                    throw new IllegalArgumentException("Pedido não encontrado");
                }
                
                String status = rs.getString("status");
                if ("CANCELADO".equals(status)) {
                    throw new IllegalArgumentException("Pedido já está cancelado");
                }
            }

            // Atualiza status do pedido
            String sqlPedido = """
                UPDATE pedidos 
                SET status = 'CANCELADO', 
                    data_cancelamento = CURRENT_TIMESTAMP,
                    motivo_cancelamento = ?
                WHERE id = ?
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlPedido)) {
                stmt.setString(1, motivo);
                stmt.setLong(2, pedidoId);
                stmt.executeUpdate();
            }

            // Retorna itens ao estoque
            String sqlItens = "SELECT produto_id, quantidade FROM itens_pedido WHERE pedido_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlItens)) {
                stmt.setLong(1, pedidoId);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Long produtoId = rs.getLong("produto_id");
                    int quantidade = rs.getInt("quantidade");
                    
                    // Atualiza estoque
                    String sqlEstoque = """
                        UPDATE produtos 
                        SET quantidade = quantidade + ? 
                        WHERE id = ?
                    """;
                    
                    try (PreparedStatement stmtEstoque = conn.prepareStatement(sqlEstoque)) {
                        stmtEstoque.setInt(1, quantidade);
                        stmtEstoque.setLong(2, produtoId);
                        stmtEstoque.executeUpdate();
                    }
                }
            }

            dbManager.confirmarTransacao(conn);
            LOGGER.log(Level.INFO, "Pedido cancelado com sucesso: {0}", pedidoId);

        } catch (IllegalArgumentException | SQLException e) {
            try {
                if (conn != null) {
                    dbManager.cancelarTransacao(conn);
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Erro ao realizar rollback", ex);
            }
            LOGGER.log(Level.SEVERE, "Erro ao cancelar pedido", e);
            throw new RuntimeException("Erro ao cancelar pedido: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erro ao fechar conexão", e);
            }
        }
    }


   private Pedido criarPedidoDoResultSet(ResultSet rs) throws SQLException {
    Pedido pedido = new Pedido();
    pedido.setId(rs.getLong("id"));

    Cliente cliente = new Cliente();
    cliente.setId(rs.getLong("cliente_id"));
    cliente.setNome(rs.getString("cliente_nome"));
    cliente.setCpf(rs.getString("cliente_cpf"));
    cliente.setEndereco(rs.getString("cliente_endereco"));
    cliente.setContato(rs.getString("cliente_contato"));
    pedido.setCliente(cliente);

    pedido.setFormaPagamento(rs.getString("forma_pagamento"));
    pedido.setNumeroParcelas(rs.getInt("numero_parcelas"));
    pedido.setValorTotal(rs.getDouble("valor_total"));
    pedido.setDesconto(rs.getDouble("desconto"));
    
    // Converter a data/hora UTC para o fuso horário local
    Timestamp utcTimestamp = rs.getTimestamp("data_pedido");
    if (utcTimestamp != null) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(utcTimestamp.getTime());
        cal.add(Calendar.HOUR_OF_DAY, -3); // Ajusta para o fuso horário de Brasília (UTC-3)
        pedido.setDataPedido(new Timestamp(cal.getTimeInMillis()));
    }
    
    pedido.setStatus(rs.getString("status"));

    // Carregar itens do pedido...
    // [resto do código permanece igual]

    return pedido;
}

    private void gerarPDFPedido(Pedido pedido) {
    try {
        LOGGER.log(Level.INFO, "Iniciando gera\u00e7\u00e3o do PDF para o pedido {0}", pedido.getId());
        PDFGenerator.gerarRelatorioPedido(pedido);
        LOGGER.info("PDF gerado com sucesso");
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Erro ao gerar PDF do pedido", e);
        // Não interrompe o fluxo principal
    }
}
}