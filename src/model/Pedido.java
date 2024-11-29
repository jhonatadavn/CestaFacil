package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pedido {
    private Long id;
    private Cliente cliente;
    private List<ItemPedido> itens;
    private Date dataPedido;
    private String formaPagamento;
    private int numeroParcelas;
    private double subtotal;
    private double desconto;
    private double valorTotal;
    private String status;
    private Date dataCancelamento;
    private String motivoCancelamento;

    public Pedido() {
        this.itens = new ArrayList<>();
        this.dataPedido = new Date();
        this.status = "PENDENTE";
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public Date getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(Date dataPedido) {
        this.dataPedido = dataPedido;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public int getNumeroParcelas() {
        return numeroParcelas;
    }

    public void setNumeroParcelas(int numeroParcelas) {
        this.numeroParcelas = numeroParcelas;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDesconto() {
        return desconto;
    }

    public void setDesconto(double desconto) {
        this.desconto = desconto;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDataCancelamento() {
        return dataCancelamento;
    }

    public void setDataCancelamento(Date dataCancelamento) {
        this.dataCancelamento = dataCancelamento;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    // Métodos auxiliares
    public void addItem(ItemPedido item) {
        if (this.itens == null) {
            this.itens = new ArrayList<>();
        }
        this.itens.add(item);
    }

    public void calcularTotais() {
        this.subtotal = itens.stream()
            .mapToDouble(item -> item.getQuantidade() * item.getValorUnitario())
            .sum();
        this.valorTotal = this.subtotal - this.desconto;
    }

    public boolean isCancelado() {
        return "CANCELADO".equals(this.status);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", cliente=" + cliente +
                ", dataPedido=" + dataPedido +
                ", status=" + status +
                ", valorTotal=" + valorTotal +
                '}';
    }
}