package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class Assalariado extends Empregado {
    public Assalariado(String id, String nome, String endereco, double salario) {
        super(id, nome, endereco, salario);
        setAgendaPagamento("mensal $");
        setDataContrato(LocalDate.of(2005, 1, 1));
    }

    public String getTipo() { return "assalariado"; }
    public boolean ehHorista() { return false; }
    public boolean ehAssalariado() { return true; }
    public boolean ehComissionado() { return false; }
    public String agendaPadrao() { return "mensal $"; }

    public double salarioBasePagamento(LocalDate data) { return floor2(getSalario()); }
    public double calcularPagamento(LocalDate data) { return salarioBasePagamento(data); }

    public Empregado copiar() {
        Assalariado n = new Assalariado(getId(), getNome(), getEndereco(), getSalario());
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
