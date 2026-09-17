package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartaoPonto implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String data;
    private final BigDecimal horas;
    public CartaoPonto(String data, BigDecimal horas) { this.data = data; this.horas = horas; }
    public String getData() { return data; }
    public BigDecimal getHoras() { return horas; }
}