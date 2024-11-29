package manager;

import model.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class GerenciadorClientes {
    private static final Logger LOGGER = Logger.getLogger(GerenciadorClientes.class.getName());
    private final DatabaseManager dbManager;

    public GerenciadorClientes() {
        this.dbManager = DatabaseManager.getInstance();
    }

   private void validarDadosCliente(String nome, String cpf) {
    if (nome == null || nome.trim().isEmpty()) {
        throw new IllegalArgumentException("Nome é obrigatório");
    }
    
    if (cpf == null || cpf.trim().isEmpty()) {
        throw new IllegalArgumentException("CPF é obrigatório");
    }
    
    // Remove caracteres especiais do CPF para validação
    String cpfNumerico = cpf.replaceAll("[^0-9]", "");
    
    if (cpfNumerico.length() != 11) {
        throw new IllegalArgumentException("CPF deve conter 11 dígitos");
    }
}
   
   public Cliente adicionarCliente(String nome, String cpf, String endereco, String contato) {
    validarDadosCliente(nome, cpf);

    String sql = "INSERT INTO clientes (nome, cpf, endereco, contato) VALUES (?, ?, ?, ?)";
    
    try (Connection conn = dbManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        stmt.setString(1, nome);
        stmt.setString(2, cpf);
        stmt.setString(3, endereco);
        stmt.setString(4, contato);
        
        int affectedRows = stmt.executeUpdate();
        
        if (affectedRows == 0) {
            throw new SQLException("Falha ao criar cliente, nenhuma linha afetada.");
        }

        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                Long id = generatedKeys.getLong(1);
                return new Cliente(id, nome, cpf, endereco, contato);
            } else {
                throw new SQLException("Falha ao criar cliente, nenhum ID obtido.");
            }
        }
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Erro ao adicionar cliente", e);
        throw new RuntimeException("Erro ao adicionar cliente: " + e.getMessage());
    }
}
   
    public void atualizarCliente(Cliente cliente) {
        validarDadosCliente(cliente.getNome(), cliente.getCpf());

        String sql = "UPDATE clientes SET nome = ?, cpf = ?, endereco = ?, contato = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getCpf());
            stmt.setString(3, cliente.getEndereco());
            stmt.setString(4, cliente.getContato());
            stmt.setLong(5, cliente.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Cliente não encontrado para atualização");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar cliente", e);
            throw new RuntimeException("Erro ao atualizar cliente: " + e.getMessage());
        }
    }

    public void removerCliente(Long id) {
        String sql = "DELETE FROM clientes WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Cliente não encontrado para remoção");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao remover cliente", e);
            throw new RuntimeException("Erro ao remover cliente: " + e.getMessage());
        }
    }

    public Cliente buscarCliente(Long id) {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarClienteDoResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar cliente", e);
            throw new RuntimeException("Erro ao buscar cliente: " + e.getMessage());
        }
        return null;
    }

    public List<Cliente> listarClientes() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY nome";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                clientes.add(criarClienteDoResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar clientes", e);
            throw new RuntimeException("Erro ao listar clientes: " + e.getMessage());
        }
        return clientes;
    }

    private Cliente criarClienteDoResultSet(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getLong("id"),
            rs.getString("nome"),
            rs.getString("cpf"),
            rs.getString("endereco"),
            rs.getString("contato")
        );
    }

}