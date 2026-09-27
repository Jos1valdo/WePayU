package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class Horista extends Empregado {
    public Horista(String id, String nome, String endereco, double salario) {
        super(id, nome, endereco, salario);
        setAgendaPagamento("semanal 5");
    }

    public String getTipo() { return "horista"; }
    public boolean ehHorista() { return true; }
    public boolean ehAssalariado() { return false; }
    public boolean ehComissionado() { return false; }
    public String agendaPadrao() { return "semanal 5"; }

    public double salarioBasePagamento(LocalDate data) {
        return floor2(getHorasPagamento(data) * getSalario());
    }

    public double calcularPagamento(LocalDate data) {
        double normal = getHorasPagamento(data);
        double extra = getHorasExtrasPagamento(data);
        return floor2(normal * getSalario() + extra * getSalario() * 1.5);
    }

    public Empregado copiar() {
        Horista n = new Horista(getId(), getNome(), getEndereco(), getSalario());
        copiarPara(n);
        return n;
    }

    protected void copiarPara(Empregado n) {
        n.setComissao(getComissao()); n.setMetodoPagamento(getMetodoPagamento());
        n.setBanco(getBanco()); n.setAgencia(getAgencia()); n.setContaCorrente(getContaCorrente());
        n.setSindicalizado(isSindicalizado()); n.setIdSindicato(getIdSindicato()); n.setTaxaSindical(getTaxaSindical());
        n.setAgendaPagamento(getAgendaPagamento()); n.setDataContrato(getDataContrato()); n.setUltimaDataPagamento(getUltimaDataPagamento());
        for (CartaoDePonto c : getCartoes()) n.adicionarCartao(new CartaoDePonto(c.getData(), c.getHoras()));
        for (ResultadoVenda v : getVendasLista()) n.adicionarVenda(new ResultadoVenda(v.getData(), v.getValor()));
        for (TaxaServico t : getTaxasLista()) n.adicionarTaxa(new TaxaServico(t.getData(), t.getValor()));
    }
}
