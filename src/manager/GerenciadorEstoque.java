package manager;

import model.Produto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class GerenciadorEstoque {
    private static final Logger LOGGER = Logger.getLogger(GerenciadorEstoque.class.getName());
    private final DatabaseManager dbManager;

    public GerenciadorEstoque() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public Produto adicionarProduto(String nome, String categoria, String descricao, double valor, int quantidade) {
        validarDadosProduto(nome, categoria, valor, quantidade);

        String sql = "INSERT INTO produtos (nome, categoria, descricao, valor, quantidade) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, nome);
            stmt.setString(2, categoria);
            stmt.setString(3, descricao);
            stmt.setDouble(4, valor);
            stmt.setInt(5, quantidade);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar produto, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    return new Produto(id, nome, categoria, descricao, valor, quantidade);
                } else {
                    throw new SQLException("Falha ao criar produto, nenhum ID obtido.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao adicionar produto", e);
            throw new RuntimeException("Erro ao adicionar produto: " + e.getMessage());
        }
    }

    public void atualizarProduto(Produto produto) {
        validarDadosProduto(produto.getNome(), produto.getCategoria(), produto.getValor(), produto.getQuantidade());

        String sql = "UPDATE produtos SET nome = ?, categoria = ?, descricao = ?, valor = ?, quantidade = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getCategoria());
            stmt.setString(3, produto.getDescricao());
            stmt.setDouble(4, produto.getValor());
            stmt.setInt(5, produto.getQuantidade());
            stmt.setLong(6, produto.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Produto não encontrado para atualização");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar produto", e);
            throw new RuntimeException("Erro ao atualizar produto: " + e.getMessage());
        }
    }

    public void removerProduto(Long id) {
        String sql = "DELETE FROM produtos WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Produto não encontrado para remoção");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao remover produto", e);
            throw new RuntimeException("Erro ao remover produto: " + e.getMessage());
        }
    }

    public Produto buscarProduto(Long id) {
        String sql = "SELECT * FROM produtos WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarProdutoDoResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar produto", e);
            throw new RuntimeException("Erro ao buscar produto: " + e.getMessage());
        }
        return null;
    }

    public List<Produto> listarProdutos() {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produtos ORDER BY nome";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                produtos.add(criarProdutoDoResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar produtos", e);
            throw new RuntimeException("Erro ao listar produtos: " + e.getMessage());
        }
        return produtos;
    }

    private Produto criarProdutoDoResultSet(ResultSet rs) throws SQLException {
        return new Produto(
            rs.getLong("id"),
            rs.getString("nome"),
            rs.getString("categoria"),
            rs.getString("descricao"),
            rs.getDouble("valor"),
            rs.getInt("quantidade")
        );
    }

    private void validarDadosProduto(String nome, String categoria, double valor, int quantidade) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        }

        if (!categoria.equals("ALIMENTO") && !categoria.equals("HIGIENE/LIMPEZA")) {
            throw new IllegalArgumentException("Categoria inválida. Use 'ALIMENTO' ou 'LIMPEZA'");
        }

        if (valor <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }

        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa");
        }
    }
    public List<Produto> listarProdutosEstoqueBaixo() {
    String sql = "SELECT * FROM produtos WHERE quantidade <= estoque_minimo ORDER BY quantidade ASC";
    List<Produto> produtos = new ArrayList<>();
    
    try (Connection conn = dbManager.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            produtos.add(criarProdutoDoResultSet(rs));
        }
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Erro ao listar produtos com estoque baixo", e);
        throw new RuntimeException("Erro ao listar produtos com estoque baixo: " + e.getMessage());
    }
    return produtos;
}

    public boolean verificarEstoqueDisponivel(Long produtoId, int quantidade) {
        String sql = "SELECT quantidade FROM produtos WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, produtoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int estoqueAtual = rs.getInt("quantidade");
                    return estoqueAtual >= quantidade;
                }
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao verificar estoque", e);
            throw new RuntimeException("Erro ao verificar estoque: " + e.getMessage());
        }
    }

    public void atualizarQuantidadeEstoque(Long produtoId, int novaQuantidade) {
        if (novaQuantidade < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa");
        }

        String sql = "UPDATE produtos SET quantidade = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, novaQuantidade);
            stmt.setLong(2, produtoId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Produto não encontrado para atualização de estoque");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar quantidade em estoque", e);
            throw new RuntimeException("Erro ao atualizar quantidade em estoque: " + e.getMessage());
        }
    }
}