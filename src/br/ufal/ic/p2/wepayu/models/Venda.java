package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.math.BigDecimal;

public class Venda implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String data;
    private final BigDecimal valor;
    public Venda(String data, BigDecimal valor) { this.data = data; this.valor = valor; }
    public String getData() { return data; }
    public BigDecimal getValor() { return valor; }
}
