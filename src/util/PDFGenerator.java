package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import model.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.logging.*;

public class PDFGenerator {
    private static final Logger LOGGER = Logger.getLogger(PDFGenerator.class.getName());
    private static final String PDF_DIR = "pdf";
    
    // Configurações de fonte
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    static {
        criarDiretorioPDF();
        configurarLogger();
    }

    private static void criarDiretorioPDF() {
        File pdfDir = new File(PDF_DIR);
        if (!pdfDir.exists() && !pdfDir.mkdirs()) {
            throw new RuntimeException("Não foi possível criar o diretório PDF");
        }
    }

    private static void configurarLogger() {
        try {
            FileHandler fh = new FileHandler("logs/pdfgenerator_%g.log", 1024 * 1024, 5, true);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
        } catch (IOException | SecurityException e) {
            System.err.println("Erro ao configurar logger: " + e.getMessage());
        }
    }

    public static void gerarRelatorioPedido(Pedido pedido) {
        String nomeArquivo = String.format("%s/pedido_%d.pdf", PDF_DIR, pedido.getId());
        LOGGER.log(Level.INFO, "Gerando PDF do pedido: {0}", nomeArquivo);

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(nomeArquivo));
            document.open();

            // Cabeçalho
            adicionarCabecalhoPedido(document, pedido);
            
            // Informações do Cliente
            adicionarInformacoesCliente(document, pedido);
            
            // Itens do Pedido
            adicionarItensPedido(document, pedido);
            
            // Totais
            adicionarTotaisPedido(document, pedido);
            
            // Informações de Pagamento
            adicionarInformacoesPagamento(document, pedido);
            
            // Rodapé
            adicionarRodapePedido(document);

            document.close();
            LOGGER.info("PDF do pedido gerado com sucesso");

        } catch (DocumentException | FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar PDF do pedido", e);
            throw new RuntimeException("Erro ao gerar PDF do pedido: " + e.getMessage());
        }
    }

    private static void adicionarCabecalhoPedido(Document document, Pedido pedido) throws DocumentException {
        Paragraph titulo = new Paragraph("PEDIDO #" + pedido.getId(), TITLE_FONT);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(Chunk.NEWLINE);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Paragraph data = new Paragraph("Data: " + sdf.format(pedido.getDataPedido()), NORMAL_FONT);
        document.add(data);
        document.add(Chunk.NEWLINE);
    }

    private static void adicionarInformacoesCliente(Document document, Pedido pedido) throws DocumentException {
    document.add(new Paragraph("INFORMAÇÕES DO CLIENTE", HEADER_FONT));
    
    Cliente cliente = pedido.getCliente();
    
    // Dados básicos do cliente
    document.add(new Paragraph("Nome: " + cliente.getNome(), NORMAL_FONT));
    document.add(new Paragraph("CPF: " + cliente.getCpf(), NORMAL_FONT));
    
    // Endereço completo
    if (cliente.getEndereco() != null && !cliente.getEndereco().trim().isEmpty()) {
        document.add(new Paragraph("Endereço: " + cliente.getEndereco(), NORMAL_FONT));
    }
    
    // Contato
    if (cliente.getContato() != null && !cliente.getContato().trim().isEmpty()) {
        document.add(new Paragraph("Contato: " + cliente.getContato(), NORMAL_FONT));
    }
    
    // Data e número do pedido
    document.add(new Paragraph("Data do Pedido: " + 
        new SimpleDateFormat("dd/MM/yyyy HH:mm").format(pedido.getDataPedido()), NORMAL_FONT));
    document.add(new Paragraph("Número do Pedido: " + pedido.getId(), NORMAL_FONT));
    
    document.add(Chunk.NEWLINE);
}

    private static void adicionarItensPedido(Document document, Pedido pedido) throws DocumentException {
        document.add(new Paragraph("ITENS DO PEDIDO", HEADER_FONT));

        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100);
        float[] widths = {40f, 20f, 20f, 20f};
        tabela.setWidths(widths);

        // Cabeçalho da tabela
        adicionarCelulaHeader(tabela, "Produto");
        adicionarCelulaHeader(tabela, "Quantidade");
        adicionarCelulaHeader(tabela, "Valor Unit.");
        adicionarCelulaHeader(tabela, "Subtotal");

        // Dados dos itens
        for (ItemPedido item : pedido.getItens()) {
            adicionarCelula(tabela, item.getProduto().getNome());
            adicionarCelula(tabela, String.valueOf(item.getQuantidade()));
            adicionarCelula(tabela, String.format("R$ %.2f", item.getValorUnitario()));
            adicionarCelula(tabela, String.format("R$ %.2f", item.getQuantidade() * item.getValorUnitario()));
        }

        document.add(tabela);
        document.add(Chunk.NEWLINE);
    }
    

    private static void adicionarTotaisPedido(Document document, Pedido pedido) throws DocumentException {
    document.add(new Paragraph("RESUMO", HEADER_FONT));
    
    double subtotal = pedido.getValorTotal() + pedido.getDesconto();
    double desconto = pedido.getDesconto();
    double percentualDesconto = (desconto / subtotal) * 100;
    
    document.add(new Paragraph(String.format("Subtotal: R$ %.2f", subtotal), NORMAL_FONT));
    document.add(new Paragraph(String.format("Desconto: R$ %.2f (%.1f%%)", 
        desconto, percentualDesconto), NORMAL_FONT));
    document.add(new Paragraph(String.format("Total: R$ %.2f", 
        pedido.getValorTotal()), HEADER_FONT));
    document.add(Chunk.NEWLINE);
}

    private static void adicionarInformacoesPagamento(Document document, Pedido pedido) throws DocumentException {
        document.add(new Paragraph("PAGAMENTO", HEADER_FONT));
        document.add(new Paragraph("Forma: " + pedido.getFormaPagamento(), NORMAL_FONT));
        
        if (pedido.getNumeroParcelas() > 1) {
            double valorParcela = pedido.getValorTotal() / pedido.getNumeroParcelas();
            document.add(new Paragraph(String.format("Parcelamento: %dx de R$ %.2f", 
                pedido.getNumeroParcelas(), valorParcela), NORMAL_FONT));
        }
        document.add(Chunk.NEWLINE);
    }

    private static void adicionarRodapePedido(Document document) throws DocumentException {
        document.add(new Paragraph("----------------------------------------", SMALL_FONT));
        document.add(new Paragraph("Documento gerado eletronicamente", SMALL_FONT));
        document.add(new Paragraph(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            .format(new java.util.Date()), SMALL_FONT));
    }

    private static void adicionarCelulaHeader(PdfPTable tabela, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, HEADER_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        tabela.addCell(cell);
    }

    private static void adicionarCelula(PdfPTable tabela, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, NORMAL_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabela.addCell(cell);
    }

    // Adicione estes métodos na classe PDFGenerator


}