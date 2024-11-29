package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;
import javax.swing.event.ChangeEvent;
import manager.*;

public class InterfaceUsuario extends JFrame {
    private static final String NOME_SISTEMA = "CestaFácil";
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(InterfaceUsuario.class.getName());
    private static final String VERSAO = "1.0.0";
    
    private JTabbedPane tabbedPane;
    private PainelProdutos painelProdutos;
    private PainelClientes painelClientes;
    private PainelCesta painelCesta;
    private PainelHistorico painelHistorico;
    
    private GerenciadorEstoque gerenciadorEstoque;
    private GerenciadorClientes gerenciadorClientes;
    private GerenciadorPedidos gerenciadorPedidos;
    
    private JMenuBar menuBar;
    private JLabel statusLabel;
    private JProgressBar progressBar;

  public InterfaceUsuario() {
    super("CestaFácil - v" + VERSAO);
    
    try {
        // Configurações iniciais
        configurarAparencia();
        configurarLogger();
        criarEstruturaDiretorios();
        inicializarGerenciadores();
        configurarJanela();
        
        // Carrega o ícone do sistema
        ImageIcon icon = new ImageIcon(getClass().getResource("/resources/images/logo.png"));
        if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            setIconImage(icon.getImage());
        } else {
            LOGGER.warning("Não foi possível carregar o ícone do sistema");
        }
        
        criarComponentes();
        criarMenu();
        configurarEventos();
        
        LOGGER.info("Sistema iniciado com sucesso");
        atualizarStatusBar("Sistema pronto para uso");
        
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Erro ao inicializar sistema: {0}", e.getMessage());
        JOptionPane.showMessageDialog(this,
            "Erro ao inicializar sistema: " + e.getMessage(),
            "Erro Fatal",
            JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}

    private void configurarAparencia() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    private void configurarLogger() {
        try {
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            FileHandler fh = new FileHandler("logs/sistema_%g.log", 1024 * 1024, 5, true);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
            LOGGER.setLevel(Level.ALL);
            } catch (IOException | SecurityException e) {
            System.err.println("Erro ao configurar logger: " + e.getMessage());
        }
    }

    private void criarEstruturaDiretorios() {
        String[] diretorios = {"data", "logs", "backup", "pdf"};
        for (String dir : diretorios) {
            File diretorio = new File(dir);
            if (!diretorio.exists() && !diretorio.mkdirs()) {
                throw new RuntimeException("Não foi possível criar o diretório: " + dir);
            }
        }
        LOGGER.info("Estrutura de diretórios verificada com sucesso");
    }

    private void inicializarGerenciadores() {
        try {
            gerenciadorEstoque = new GerenciadorEstoque();
            gerenciadorClientes = new GerenciadorClientes();
            gerenciadorPedidos = new GerenciadorPedidos(gerenciadorEstoque);
            LOGGER.info("Gerenciadores inicializados com sucesso");
            } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao inicializar gerenciadores: {0}", e.getMessage());
            throw new RuntimeException("Erro ao inicializar gerenciadores", e);
        }
    }

     private void configurarJanela() {
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setMinimumSize(UIConstants.WINDOW_MIN_SIZE);
    setPreferredSize(UIConstants.WINDOW_PREFERRED_SIZE);
    setLocationRelativeTo(null);
    
    // Aplicar fonte padrão em toda a interface
    UIManager.put("Button.font", UIConstants.BUTTON_FONT);
    UIManager.put("Label.font", UIConstants.LABEL_FONT);
    UIManager.put("ComboBox.font", UIConstants.INPUT_FONT);
    UIManager.put("TextField.font", UIConstants.INPUT_FONT);
    UIManager.put("Table.font", UIConstants.INPUT_FONT);
    UIManager.put("TableHeader.font", UIConstants.LABEL_FONT);
    UIManager.put("TabbedPane.font", UIConstants.LABEL_FONT);
    UIManager.put("Menu.font", UIConstants.INPUT_FONT);
    UIManager.put("MenuItem.font", UIConstants.INPUT_FONT);
}

 private void criarComponentes() {
    // Criar painel principal com BorderLayout
    JPanel mainPanel = new JPanel(new BorderLayout());
    
    // Criar painel com logo
    JPanel painelLogo = new JPanel(new FlowLayout(FlowLayout.CENTER));
    try {
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/resources/images/logo.png"));
        if (logoIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            // Redimensionar a imagem mantendo proporções
            Image img = logoIcon.getImage();
            int targetHeight = 60; // Altura desejada
            int originalWidth = logoIcon.getIconWidth();
            int originalHeight = logoIcon.getIconHeight();
            int targetWidth = (targetHeight * originalWidth) / originalHeight;
            
            Image scaledImg = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledImg));
            painelLogo.add(logoLabel);
        }
    } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Não foi possível carregar a logo", e);
    }
    
    // Criar painéis principais
    tabbedPane = new JTabbedPane();
    painelProdutos = new PainelProdutos(gerenciadorEstoque);
    painelClientes = new PainelClientes(gerenciadorClientes);
    painelCesta = new PainelCesta(gerenciadorPedidos, gerenciadorClientes, gerenciadorEstoque);
    painelHistorico = new PainelHistorico(gerenciadorPedidos);
    
    // Adicionar abas
    tabbedPane.addTab("Produtos", new JScrollPane(painelProdutos));
    tabbedPane.addTab("Clientes", new JScrollPane(painelClientes));
    tabbedPane.addTab("Montar Cesta", new JScrollPane(painelCesta));
    tabbedPane.addTab("Histórico", new JScrollPane(painelHistorico));
    
    // Criar barra de status
    JPanel statusPanel = new JPanel(new BorderLayout());
    statusPanel.setBorder(BorderFactory.createEtchedBorder());
    
    statusLabel = new JLabel("Pronto");
    progressBar = new JProgressBar();
    progressBar.setVisible(false);
    
    JLabel versaoLabel = new JLabel("v" + VERSAO);
    versaoLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    
    statusPanel.add(statusLabel, BorderLayout.WEST);
    statusPanel.add(progressBar, BorderLayout.CENTER);
    statusPanel.add(versaoLabel, BorderLayout.EAST);
    
    // Adicionar componentes ao painel principal
    mainPanel.add(painelLogo, BorderLayout.NORTH);
    mainPanel.add(tabbedPane, BorderLayout.CENTER);
    mainPanel.add(statusPanel, BorderLayout.SOUTH);
    
    // Adicionar painel principal ao frame
    add(mainPanel);
    
    pack();
}
   private void criarMenu() {
    menuBar = new JMenuBar();
    
    // Menu Arquivo
    JMenu menuArquivo = new JMenu("Arquivo");
    menuArquivo.setMnemonic(KeyEvent.VK_A);
    
    JMenuItem menuBackup = new JMenuItem("Realizar Backup", KeyEvent.VK_B);
    menuBackup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK));
    menuBackup.addActionListener(e -> realizarBackup());
    
    JMenuItem menuGerenciarBackups = new JMenuItem("Gerenciar Backups", KeyEvent.VK_G);
    menuGerenciarBackups.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
    menuGerenciarBackups.addActionListener(e -> gerenciarBackups());
    
    JMenuItem menuAlertas = new JMenuItem("Configurar Alertas", KeyEvent.VK_C);
    menuAlertas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
    menuAlertas.addActionListener(e -> configurarAlertas());
    
    JMenuItem menuSair = new JMenuItem("Sair", KeyEvent.VK_S);
    menuSair.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
    menuSair.addActionListener(e -> confirmarSaida());
    
    menuArquivo.add(menuBackup);
    menuArquivo.add(menuGerenciarBackups);
    menuArquivo.addSeparator();
    menuArquivo.add(menuAlertas);
    menuArquivo.addSeparator();
    menuArquivo.add(menuSair);
    
    // Menu Ajuda
    JMenu menuAjuda = new JMenu("Ajuda");
    menuAjuda.setMnemonic(KeyEvent.VK_J);
    
    JMenuItem menuSobre = new JMenuItem("Sobre", KeyEvent.VK_S);
    menuSobre.addActionListener(e -> mostrarSobre());
    
    menuAjuda.add(menuSobre);
    
    menuBar.add(menuArquivo);
    menuBar.add(menuAjuda);
    
    setJMenuBar(menuBar);
}

private void configurarAlertas() {
    String input = JOptionPane.showInputDialog(this,
        "Digite a quantidade mínima para alertas de estoque:",
        "Configurar Alertas",
        JOptionPane.QUESTION_MESSAGE);
        
    if (input != null && !input.trim().isEmpty()) {
        try {
            int quantidade = Integer.parseInt(input);
            if (quantidade >= 0) {
                // Atualiza o estoque mínimo no banco de dados
                try (Connection conn = DatabaseManager.getInstance().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE produtos SET estoque_minimo = ?")) {
                    stmt.setInt(1, quantidade);
                    stmt.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this,
                        "Configuração de alertas atualizada com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                        
                    // Atualiza os painéis que mostram alertas
                    painelProdutos.atualizarDados();
                    
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Erro ao atualizar configuração de alertas", e);
                    JOptionPane.showMessageDialog(this,
                        "Erro ao atualizar configuração de alertas: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "A quantidade mínima deve ser maior ou igual a zero",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Digite um número válido",
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
   
private void gerenciarBackups() {
    try {
        List<String> backups = DatabaseManager.getInstance().listarBackups();
        if (backups.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Nenhum backup encontrado",
                "Backups",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] opcoes = backups.toArray(String[]::new);
        String selectedBackup = (String) JOptionPane.showInputDialog(this,
            "Selecione um backup para restaurar:",
            "Restaurar Backup",
            JOptionPane.QUESTION_MESSAGE,
            null,
            opcoes,
            opcoes[0]);

        if (selectedBackup != null) {
            int confirm = JOptionPane.showConfirmDialog(this, """
                                                              Tem certeza que deseja restaurar este backup?
                                                              O banco de dados atual ser\u00e1 substitu\u00eddo.""",
                "Confirmar Restauração",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                String nomeArquivo = selectedBackup.substring(selectedBackup.indexOf(" - ") + 3);
                DatabaseManager.getInstance().restaurarBackup(nomeArquivo);
                
                JOptionPane.showMessageDialog(this, """
                                                    Backup restaurado com sucesso!
                                                    O sistema ser\u00e1 reiniciado para aplicar as altera\u00e7\u00f5es.""",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Reinicia a aplicação
                dispose();
                new InterfaceUsuario().setVisible(true);
            }
        }
    } catch (HeadlessException e) {
        LOGGER.log(Level.SEVERE, "Erro ao gerenciar backups", e);
        JOptionPane.showMessageDialog(this,
            "Erro ao gerenciar backups: " + e.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE);
    }
}

    private void configurarEventos() {
        // Evento de mudança de aba
        tabbedPane.addChangeListener((ChangeEvent e) -> {
            int index = tabbedPane.getSelectedIndex();
            switch (index) {
                case 0 -> {
                    painelProdutos.atualizarDados();
                    atualizarStatusBar("Gerenciando produtos");
                }
                case 1 -> {
                    painelClientes.atualizarDados();
                    atualizarStatusBar("Gerenciando clientes");
                }
                case 2 -> {
                    try {
                        painelCesta.atualizarDados();
                        atualizarStatusBar("Montando cesta");
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Erro ao atualizar painel cesta: {0}", ex.getMessage());
                        atualizarStatusBar("Erro ao atualizar painel cesta");
                    }
                }
                case 3 -> {
                    try {
                        painelHistorico.atualizarDados();
                        atualizarStatusBar("Visualizando histórico de pedidos");
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Erro ao atualizar hist\u00f3rico: {0}", ex.getMessage());
                        atualizarStatusBar("Erro ao atualizar histórico");
                    }
                }
                default -> atualizarStatusBar("Sistema pronto");
            }
        });
        
        // Evento de fechamento da janela
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSaida();
            }
        });
    }

    private void realizarBackup() {
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        atualizarStatusBar("Realizando backup...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                DatabaseManager.getInstance().realizarBackup();
                return null;
            }
            
            @Override
            protected void done() {
                progressBar.setVisible(false);
                try {
                    get();
                    atualizarStatusBar("Backup realizado com sucesso");
                    JOptionPane.showMessageDialog(InterfaceUsuario.this,
                        "Backup realizado com sucesso!",
                        "Backup",
                        JOptionPane.INFORMATION_MESSAGE);
                    } catch (HeadlessException | InterruptedException | ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Erro ao realizar backup: {0}", e.getMessage());
                    atualizarStatusBar("Erro ao realizar backup");
                    JOptionPane.showMessageDialog(InterfaceUsuario.this,
                        "Erro ao realizar backup: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }

     private void mostrarSobre() {
        JOptionPane.showMessageDialog(this,
            """
            CestaFácil
            Sistema de Gerenciamento de Cestas Básicas
            Desenvolvedores: Anya, Douglas, Jhonatã, Miguel
            Versão """ + VERSAO + "\n\n" +
            "©" + LocalDate.now().getYear(),
            "Sobre " + NOME_SISTEMA,
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmarSaida() {
        int opcao = JOptionPane.showConfirmDialog(this,
            "Deseja realmente sair do sistema?",
            "Confirmação",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (opcao == JOptionPane.YES_OPTION) {
            LOGGER.info("Encerrando aplicação");
            dispose();
            System.exit(0);
        }
    }

    private void atualizarStatusBar(String mensagem) {
        statusLabel.setText(mensagem);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new InterfaceUsuario().setVisible(true);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao iniciar aplica\u00e7\u00e3o: {0}", e.getMessage());
                JOptionPane.showMessageDialog(null,
                    "Erro ao iniciar aplicação: " + e.getMessage(),
                    "Erro Fatal",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
        
       
    }
}