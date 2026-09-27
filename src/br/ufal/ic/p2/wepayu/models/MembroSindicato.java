package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;

public class MembroSindicato implements Serializable {
    private String id;
    private double taxaSindical;

    public MembroSindicato(String id, double taxaSindical) {
        this.id = id;
        this.taxaSindical = taxaSindical;
    }

    public String getId() { return id; }
    public double getTaxaSindical() { return taxaSindical; }
    public void setId(String id) { this.id = id; }
    public void setTaxaSindical(double taxaSindical) { this.taxaSindical = taxaSindical; }
}
