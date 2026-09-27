package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.LocalDate;

public class CartaoDePonto implements Serializable {
    private final LocalDate data;
    private final double horas;

    public CartaoDePonto(LocalDate data, double horas) {
        this.data = data;
        this.horas = horas;
    }

    public LocalDate getData() { return data; }
    public double getHoras() { return horas; }
}
