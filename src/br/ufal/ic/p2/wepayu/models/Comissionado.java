package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class Comissionado extends Empregado {
    public Comissionado(String id, String nome, String endereco, double salario, double comissao) {
        super(id, nome, endereco, salario);
        setComissao(comissao);
        setAgendaPagamento("semanal 2 5");
        setDataContrato(LocalDate.of(2005, 1, 1));
    }

    public String getTipo() { return "comissionado"; }
    public boolean ehHorista() { return false; }
    public boolean ehAssalariado() { return false; }
    public boolean ehComissionado() { return true; }
    public String agendaPadrao() { return "semanal 2 5"; }

    public double salarioBasePagamento(LocalDate data) {
        return floor2(getSalario() * 12.0 / 52.0 * 2.0);
    }

    public double calcularPagamento(LocalDate data) {
        return floor2(salarioBasePagamento(data) + getComissaoPagamento(data));
    }

    public Empregado copiar() {
        Comissionado n = new Comissionado(getId(), getNome(), getEndereco(), getSalario(), getComissao());
        copiarPara(n); return n;
    }
    protected void copiarPara(Empregado n) {
        n.setComissao(getComissao()); n.setMetodoPagamento(getMetodoPagamento()); n.setBanco(getBanco());
        n.setAgencia(getAgencia()); n.setContaCorrente(getContaCorrente()); n.setSindicalizado(isSindicalizado());
        n.setIdSindicato(getIdSindicato()); n.setTaxaSindical(getTaxaSindical()); n.setAgendaPagamento(getAgendaPagamento());
        n.setDataContrato(getDataContrato());
        for (CartaoDePonto c : getCartoes()) n.adicionarCartao(new CartaoDePonto(c.getData(), c.getHoras()));
        for (ResultadoVenda v : getVendasLista()) n.adicionarVenda(new ResultadoVenda(v.getData(), v.getValor()));
        for (TaxaServico t : getTaxasLista()) n.adicionarTaxa(new TaxaServico(t.getData(), t.getValor()));
    }
}
