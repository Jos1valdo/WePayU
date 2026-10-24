package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Comissionado extends Empregado {
    private double comissao;
    private final List<ResultadoVenda> vendas = new ArrayList<>();

    public Comissionado(String id, String nome, String endereco, double salario, double comissao) {
        super(id, nome, endereco, salario);
        this.comissao = comissao;
        setAgendaPagamento("semanal 2 5");
        setDataContrato(LocalDate.of(2005, 1, 1));
    }

    @Override
    public String getTipo() { return "comissionado"; }

    @Override
    public boolean ehHorista() { return false; }

    @Override
    public boolean ehAssalariado() { return false; }

    @Override
    public boolean ehComissionado() { return true; }

    @Override
    public String agendaPadrao() { return "semanal 2 5"; }

    @Override
    public double getComissao() { return comissao; }

    @Override
    public void setComissao(double valor) { comissao = valor; }

    @Override
    public List<ResultadoVenda> getVendasLista() {
        return Collections.unmodifiableList(vendas);
    }

    @Override
    public void adicionarVenda(ResultadoVenda venda) {
        vendas.add(venda);
    }

    @Override
    public void lancarVenda(LocalDate data, double valor) {
        adicionarVenda(new ResultadoVenda(data, valor));
    }

    @Override
    public double salarioBasePagamento(LocalDate data) {
        AgendaPagamento agenda = new AgendaPagamento(getAgendaPagamento());
        if (agenda.ehMensal()) return floor2(getSalario());
        return floor2(getSalario() * 12.0 / 52.0 * agenda.getIntervalo());
    }

    @Override
    public double calcularPagamento(LocalDate data) {
        return floor2(salarioBasePagamento(data) + getComissaoPagamento(data));
    }

    @Override
    public Empregado copiar() {
        Comissionado novo = new Comissionado(getId(), getNome(), getEndereco(), getSalario(), getComissao());
        copiarDadosPara(novo);
        for (ResultadoVenda venda : vendas) {
            novo.adicionarVenda(new ResultadoVenda(venda.getData(), venda.getValor()));
        }
        return novo;
    }
}
