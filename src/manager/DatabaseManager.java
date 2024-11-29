package manager;

import java.sql.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DB_URL = "jdbc:sqlite:data/cestas_basicas.db";
    private static final String DRIVER = "org.sqlite.JDBC";
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String BACKUP_DIR = "data/backup/";
    
    static {
        try {
            // Configuração do logger
            FileHandler fh = new FileHandler("data/database.log", true);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
        } catch (IOException | SecurityException e) {
            System.err.println("Erro ao configurar logger: " + e.getMessage());
        }
    }

    private DatabaseManager() {
        try {
            Class.forName(DRIVER);
            inicializarBancoDados();
        } catch (ClassNotFoundException e) {
            String erro = "Driver SQLite não encontrado. Adicione a dependência sqlite-jdbc ao projeto.";
            LOGGER.severe(erro);
            throw new RuntimeException(erro, e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void inicializarBancoDados() {
        criarDiretorios();
        
        try (Connection conn = getConnection()) {
            criarTabelas(conn);
            LOGGER.info("Banco de dados inicializado com sucesso");
        } catch (SQLException e) {
            String erro = "Erro ao inicializar banco de dados";
            LOGGER.log(Level.SEVERE, "{0}: {1}", new Object[]{erro, e.getMessage()});
            throw new RuntimeException(erro, e);
        }
    }

    private void criarDiretorios() {
        File dataDir = new File("data");
        File backupDir = new File(BACKUP_DIR);
        
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            throw new RuntimeException("Não foi possível criar o diretório data");
        }
        
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            throw new RuntimeException("Não foi possível criar o diretório de backup");
        }
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        conn.setAutoCommit(true); // Configuração padrão
        return conn;
    }

   private void criarTabelas(Connection conn) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
        // Tabela Clientes
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS clientes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                cpf TEXT UNIQUE NOT NULL,
                email TEXT,
                contato TEXT,
                endereco TEXT,
                data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Tabela Produtos
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS produtos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                descricao TEXT,
                categoria TEXT NOT NULL CHECK (categoria IN ('ALIMENTO', 'HIGIENE/LIMPEZA')),
                valor REAL NOT NULL CHECK (valor >= 0),
                quantidade INTEGER NOT NULL CHECK (quantidade >= 0),
                estoque_minimo INTEGER NOT NULL DEFAULT 5,
                data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Tabela Pedidos
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS pedidos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente_id INTEGER NOT NULL,
                data_pedido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                forma_pagamento TEXT NOT NULL,
                numero_parcelas INTEGER DEFAULT 1,
                subtotal REAL NOT NULL,
                desconto REAL DEFAULT 0,
                valor_total REAL NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDENTE',
                data_cancelamento DATETIME,
                data_conclusao DATETIME,
                motivo_cancelamento TEXT,
                FOREIGN KEY (cliente_id) REFERENCES clientes(id)
            )
        """);

        // Tabela Itens do Pedido
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS itens_pedido (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pedido_id INTEGER NOT NULL,
                produto_id INTEGER NOT NULL,
                quantidade INTEGER NOT NULL,
                valor_unitario REAL NOT NULL,
                subtotal REAL NOT NULL,
                FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
                FOREIGN KEY (produto_id) REFERENCES produtos(id)
            )
        """);

        criarIndices(stmt);
    }
}

    private void criarIndices(Statement stmt) throws SQLException {
        // Índices para melhorar a performance
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_clientes_cpf ON clientes(cpf)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_produtos_categoria ON produtos(categoria)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_pedidos_cliente ON pedidos(cliente_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_pedidos_data ON pedidos(data_pedido)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_itens_pedido ON itens_pedido(pedido_id, produto_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_pedidos_cliente_data ON pedidos(cliente_id, data_pedido)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_itens_pedido_produto ON itens_pedido(pedido_id, produto_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_produtos_categoria_nome ON produtos(categoria, nome)");
    }

    public void realizarBackup() {
        String backupFile = BACKUP_DIR + "backup_" + System.currentTimeMillis() + ".db";
        String sql = "backup to " + backupFile;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "Backup realizado com sucesso: {0}", backupFile);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao realizar backup: {0}", e.getMessage());
            throw new RuntimeException("Erro ao realizar backup", e);
        }
    }
    
    public List<String> listarBackups() {
    List<String> backups = new ArrayList<>();
    File backupDir = new File(BACKUP_DIR);
    
    if (backupDir.exists() && backupDir.isDirectory()) {
        File[] arquivos = backupDir.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".db"));
        if (arquivos != null) {
            for (File arquivo : arquivos) {
                String nomeArquivo = arquivo.getName();
                try {
                    long timestamp = Long.parseLong(nomeArquivo.replace("backup_", "").replace(".db", ""));
                    String dataFormatada = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                        .format(new Date(timestamp));
                    backups.add(dataFormatada + " - " + arquivo.getName());
                } catch (NumberFormatException e) {
                    backups.add(arquivo.getName());
                }
            }
        }
    }
    return backups;
}

public void restaurarBackup(String nomeArquivo) {
    File backupFile = new File(BACKUP_DIR + nomeArquivo);
    if (!backupFile.exists()) {
        throw new RuntimeException("Arquivo de backup não encontrado");
    }

    // Fecha todas as conexões existentes
    try {
        DriverManager.getConnection(DB_URL).close();
    } catch (SQLException e) {
        LOGGER.log(Level.WARNING, "Erro ao fechar conexões existentes", e);
    }

    // Cria backup do banco atual antes de restaurar
    realizarBackup();

    // Restaura o backup selecionado
    String sql = "restore from " + backupFile.getAbsolutePath();
    
    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement()) {
        stmt.execute(sql);
        LOGGER.log(Level.INFO, "Backup restaurado com sucesso: {0}", nomeArquivo);
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Erro ao restaurar backup", e);
        throw new RuntimeException("Erro ao restaurar backup: " + e.getMessage());
    }
}

    public void fecharRecursos(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erro ao fechar recursos", e);
        }
    }

    // Métodos para gerenciamento de transações
    public void iniciarTransacao(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(false);
            LOGGER.fine("Transação iniciada");
        }
    }

    public void confirmarTransacao(Connection conn) throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true);
            LOGGER.fine("Transação confirmada");
        }
    }

    public void cancelarTransacao(Connection conn) throws SQLException {
        if (conn != null) {
            conn.rollback();
            conn.setAutoCommit(true);
            LOGGER.fine("Transação cancelada");
        }
    }
}