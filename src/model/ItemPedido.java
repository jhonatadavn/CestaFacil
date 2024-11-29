package model;

public class ItemPedido {
    private Long id;
    private Produto produto;
    private int quantidade;
    private double valorUnitario;

    // Getters
    public Long getId() {
        return id;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getValorUnitario() {
        return valorUnitario;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public void setQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        this.quantidade = quantidade;
    }

    public void setValorUnitario(double valorUnitario) {
        if (valorUnitario < 0) {
            throw new IllegalArgumentException("Valor unitário não pode ser negativo");
        }
        this.valorUnitario = valorUnitario;
    }

    // Métodos de negócio
    public double getSubtotal() {
        return quantidade * valorUnitario;
    }
}