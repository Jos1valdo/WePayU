package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class Assalariado extends Empregado {
    public Assalariado(String id, String nome, String endereco, double salario) {
        super(id, nome, endereco, salario);
        setAgendaPagamento("mensal $");
        setDataContrato(LocalDate.of(2005, 1, 1));
    }

    @Override
    public String getTipo() { return "assalariado"; }

    @Override
    public boolean ehHorista() { return false; }

    @Override
    public boolean ehAssalariado() { return true; }

    @Override
    public boolean ehComissionado() { return false; }

    @Override
    public String agendaPadrao() { return "mensal $"; }

    @Override
    public double salarioBasePagamento(LocalDate data) {
        AgendaPagamento agenda = new AgendaPagamento(getAgendaPagamento());
        if (agenda.ehMensal()) return floor2(getSalario());
        return floor2(getSalario() * 12.0 / 52.0 * agenda.getIntervalo());
    }

    @Override
    public double calcularPagamento(LocalDate data) {
        return salarioBasePagamento(data);
    }

    @Override
    public Empregado copiar() {
        Assalariado novo = new Assalariado(getId(), getNome(), getEndereco(), getSalario());
        copiarDadosPara(novo);
        return novo;
    }
}
