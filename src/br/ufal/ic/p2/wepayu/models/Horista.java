package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Horista extends Empregado {
    private final List<CartaoDePonto> cartoes = new ArrayList<>();

    public Horista(String id, String nome, String endereco, double salario) {
        super(id, nome, endereco, salario);
        setAgendaPagamento("semanal 5");
    }

    @Override
    public String getTipo() { return "horista"; }

    @Override
    public boolean ehHorista() { return true; }

    @Override
    public boolean ehAssalariado() { return false; }

    @Override
    public boolean ehComissionado() { return false; }

    @Override
    public String agendaPadrao() { return "semanal 5"; }

    @Override
    public List<CartaoDePonto> getCartoes() {
        return Collections.unmodifiableList(cartoes);
    }

    @Override
    public void adicionarCartao(CartaoDePonto cartao) {
        cartoes.add(cartao);
    }

    @Override
    public void lancarCartao(LocalDate data, double horas) {
        if (getDataContrato() == null) setDataContrato(data);
        adicionarCartao(new CartaoDePonto(data, horas));
    }

    @Override
    public double salarioBasePagamento(LocalDate data) {
        return floor2(getHorasPagamento(data) * getSalario());
    }

    @Override
    public double calcularPagamento(LocalDate data) {
        double normal = getHorasPagamento(data);
        double extra = getHorasExtrasPagamento(data);
        return floor2(normal * getSalario() + extra * getSalario() * 1.5);
    }

    @Override
    public Empregado copiar() {
        Horista novo = new Horista(getId(), getNome(), getEndereco(), getSalario());
        copiarDadosPara(novo);
        for (CartaoDePonto cartao : cartoes) {
            novo.adicionarCartao(new CartaoDePonto(cartao.getData(), cartao.getHoras()));
        }
        return novo;
    }
}
