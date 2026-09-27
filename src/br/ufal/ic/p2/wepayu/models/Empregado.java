package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Empregado implements Serializable {
    private final String id;
    private String nome;
    private String endereco;
    private double salario;
    private double comissao;
    private String metodoPagamento = "emMaos";
    private String banco;
    private String agencia;
    private String contaCorrente;
    private boolean sindicalizado;
    private String idSindicato;
    private double taxaSindical;
    private String agendaPagamento;
    private LocalDate dataContrato;
    private LocalDate ultimaDataPagamento;
    private final List<CartaoDePonto> cartoes = new ArrayList<>();
    private final List<ResultadoVenda> vendas = new ArrayList<>();
    private final List<TaxaServico> taxas = new ArrayList<>();

    protected Empregado(String id, String nome, String endereco, double salario) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.salario = salario;
    }

    public abstract String getTipo();
    public abstract boolean ehHorista();
    public abstract boolean ehAssalariado();
    public abstract boolean ehComissionado();
    public abstract Empregado copiar();
    public abstract String agendaPadrao();

    public abstract double calcularPagamento(LocalDate data);
    public abstract double salarioBasePagamento(LocalDate data);

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public double getSalario() { return salario; }
    public double getComissao() { return comissao; }
    public String getMetodoPagamento() { return metodoPagamento; }
    public String getBanco() { return banco; }
    public String getAgencia() { return agencia; }
    public String getContaCorrente() { return contaCorrente; }
    public boolean isSindicalizado() { return sindicalizado; }
    public String getIdSindicato() { return idSindicato; }
    public double getTaxaSindical() { return taxaSindical; }
    public String getAgendaPagamento() { return agendaPagamento; }
    public LocalDate getDataContrato() { return dataContrato; }
    public LocalDate getUltimaDataPagamento() { return ultimaDataPagamento; }
    public List<CartaoDePonto> getCartoes() { return cartoes; }
    public List<ResultadoVenda> getVendasLista() { return vendas; }
    public List<TaxaServico> getTaxasLista() { return taxas; }

    public void setNome(String v) { nome = v; }
    public void setEndereco(String v) { endereco = v; }
    public void setSalario(double v) { salario = v; }
    public void setComissao(double v) { comissao = v; }
    public void setMetodoPagamento(String v) { metodoPagamento = v; }
    public void setBanco(String v) { banco = v; }
    public void setAgencia(String v) { agencia = v; }
    public void setContaCorrente(String v) { contaCorrente = v; }
    public void setSindicalizado(boolean v) { sindicalizado = v; }
    public void setIdSindicato(String v) { idSindicato = v; }
    public void setTaxaSindical(double v) { taxaSindical = v; }
    public void setAgendaPagamento(String v) { agendaPagamento = v; }
    public void setDataContrato(LocalDate v) { dataContrato = v; }
    public void setUltimaDataPagamento(LocalDate v) { ultimaDataPagamento = v; }

    public void adicionarCartao(CartaoDePonto c) { cartoes.add(c); }
    public void adicionarVenda(ResultadoVenda v) { vendas.add(v); }
    public void adicionarTaxa(TaxaServico t) { taxas.add(t); }

    public void lancarCartao(LocalDate data, double horas) {
        if (dataContrato == null) dataContrato = data;
        cartoes.add(new CartaoDePonto(data, horas));
    }

    public void lancarVenda(LocalDate data, double valor) { vendas.add(new ResultadoVenda(data, valor)); }
    public void lancarTaxaServico(LocalDate data, double valor) { taxas.add(new TaxaServico(data, valor)); }

    public double getHorasPagamento(LocalDate data) {
        LocalDate inicio = inicioPeriodo(data);
        double total = 0;
        for (CartaoDePonto c : cartoes) if (!c.getData().isBefore(inicio) && !c.getData().isAfter(data)) total += Math.min(8, c.getHoras());
        return total;
    }

    public double getHorasExtrasPagamento(LocalDate data) {
        LocalDate inicio = inicioPeriodo(data);
        double total = 0;
        for (CartaoDePonto c : cartoes) if (!c.getData().isBefore(inicio) && !c.getData().isAfter(data)) total += Math.max(0, c.getHoras() - 8);
        return total;
    }

    public double getVendasPagamento(LocalDate data) {
        LocalDate inicio = inicioPeriodo(data);
        double total = 0;
        for (ResultadoVenda v : vendas) if (!v.getData().isBefore(inicio) && !v.getData().isAfter(data)) total += v.getValor();
        return total;
    }

    public double getComissaoPagamento(LocalDate data) {
        return floor2(getVendasPagamento(data) * comissao);
    }

    public double getFixoPagamento(LocalDate data) { return salarioBasePagamento(data); }

    public double calcularDescontos(LocalDate data) {
        if (!sindicalizado || calcularPagamento(data) <= 0) return 0;
        LocalDate inicio = inicioPeriodo(data);
        long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, data) + 1;
        double desconto = taxaSindical * dias;
        for (TaxaServico t : taxas) if (!t.getData().isBefore(inicio) && !t.getData().isAfter(data)) desconto += t.getValor();
        return floor2(desconto);
    }

    protected LocalDate inicioPeriodo(LocalDate data) {
        if (ultimaDataPagamento != null) {
            if (!ultimaDataPagamento.equals(data)) return ultimaDataPagamento.plusDays(1);
            LocalDate anterior;
            if (ehHorista()) {
                anterior = ultimaDataPagamento.minusDays(7);
            } else if (ehComissionado()) {
                anterior = ultimaDataPagamento.minusDays(14);
            } else {
                anterior = ultimaDataPagamento.minusMonths(1).withDayOfMonth(1);
                anterior = anterior.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());
                while (anterior.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || anterior.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) anterior = anterior.minusDays(1);
            }
            return anterior.plusDays(1);
        }
        if (dataContrato != null) return dataContrato;
        return data;
    }

    public boolean deveReceber(LocalDate data) {
        return new AgendaPagamento(agendaPagamento).devePagar(data);
    }

    public String getDescricaoPagamento() {
        if ("emMaos".equals(metodoPagamento)) return "Em maos";
        if ("correios".equals(metodoPagamento)) return "Correios, " + getEndereco();
        return "Banco do Brasil, Ag. " + agencia + " CC " + contaCorrente;
    }

    protected static double floor2(double v) {
        return Math.floor(v * 100.0 + 1e-8) / 100.0;
    }
}
