package br.ufal.ic.p2.wepayu.models;

public enum MetodoPagamento {
    EM_MAOS("emMaos"), CORREIOS("correios"), BANCO("banco");

    private final String valor;
    MetodoPagamento(String valor) { this.valor = valor; }
    public String getValor() { return valor; }
}
