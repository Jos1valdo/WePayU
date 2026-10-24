package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class Empregado implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String nome;
    private String endereco;
    private double salario;
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
    public double getComissao() { return 0.0; }
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

    public List<TaxaServico> getTaxasLista() {
        return Collections.unmodifiableList(taxas);
    }

    public void setComissao(double valor) {
        if (!ehComissionado()) {
            throw new IllegalStateException("Empregado nao eh comissionado.");
        }
    }

    public void setNome(String valor) { nome = valor; }
    public void setEndereco(String valor) { endereco = valor; }
    public void setSalario(double valor) { salario = valor; }
    public void setMetodoPagamento(String valor) { metodoPagamento = valor; }
    public void setBanco(String valor) { banco = valor; }
    public void setAgencia(String valor) { agencia = valor; }
    public void setContaCorrente(String valor) { contaCorrente = valor; }
    public void setSindicalizado(boolean valor) { sindicalizado = valor; }
    public void setIdSindicato(String valor) { idSindicato = valor; }
    public void setTaxaSindical(double valor) { taxaSindical = valor; }
    public void setAgendaPagamento(String valor) { agendaPagamento = valor; }
    public void setDataContrato(LocalDate valor) { dataContrato = valor; }
    public void setUltimaDataPagamento(LocalDate valor) { ultimaDataPagamento = valor; }

    public List<CartaoDePonto> getCartoes() {
        return Collections.emptyList();
    }

    public List<ResultadoVenda> getVendasLista() {
        return Collections.emptyList();
    }

    public void adicionarCartao(CartaoDePonto cartao) {
        throw new IllegalStateException("Empregado nao eh horista.");
    }

    public void adicionarVenda(ResultadoVenda venda) {
        throw new IllegalStateException("Empregado nao eh comissionado.");
    }

    public void lancarCartao(LocalDate data, double horas) {
        throw new IllegalStateException("Empregado nao eh horista.");
    }

    public void lancarVenda(LocalDate data, double valor) {
        throw new IllegalStateException("Empregado nao eh comissionado.");
    }

    public void adicionarTaxa(TaxaServico taxa) {
        taxas.add(taxa);
    }

    public void lancarTaxaServico(LocalDate data, double valor) {
        adicionarTaxa(new TaxaServico(data, valor));
    }

    public final void copiarDadosPara(Empregado destino) {
        destino.setMetodoPagamento(metodoPagamento);
        destino.setBanco(banco);
        destino.setAgencia(agencia);
        destino.setContaCorrente(contaCorrente);
        destino.setSindicalizado(sindicalizado);
        destino.setIdSindicato(idSindicato);
        destino.setTaxaSindical(taxaSindical);
        destino.setAgendaPagamento(agendaPagamento);
        destino.setDataContrato(dataContrato);
        destino.setUltimaDataPagamento(ultimaDataPagamento);

        for (TaxaServico taxa : taxas) {
            destino.adicionarTaxa(new TaxaServico(taxa.getData(), taxa.getValor()));
        }
    }

    public double getHorasPagamento(LocalDate data) {
        if (!ehHorista()) return 0;
        LocalDate inicio = inicioPeriodo(data);
        double total = 0;
        for (CartaoDePonto cartao : getCartoes()) {
            if (!cartao.getData().isBefore(inicio) && !cartao.getData().isAfter(data)) {
                total += Math.min(8, cartao.getHoras());
            }
        }
        return total;
    }

    public double getHorasExtrasPagamento(LocalDate data) {
        if (!ehHorista()) return 0;
        LocalDate inicio = inicioPeriodo(data);
        double total = 0;
        for (CartaoDePonto cartao : getCartoes()) {
            if (!cartao.getData().isBefore(inicio) && !cartao.getData().isAfter(data)) {
                total += Math.max(0, cartao.getHoras() - 8);
            }
        }
        return total;
    }

    public double getVendasPagamento(LocalDate data) {
        if (!ehComissionado()) return 0;
        LocalDate inicio = inicioPeriodo(data);
        double total = 0;
        for (ResultadoVenda venda : getVendasLista()) {
            if (!venda.getData().isBefore(inicio) && !venda.getData().isAfter(data)) {
                total += venda.getValor();
            }
        }
        return total;
    }

    public double getComissaoPagamento(LocalDate data) {
        if (!ehComissionado()) return 0;
        return floor2(getVendasPagamento(data) * getComissao());
    }

    public double getFixoPagamento(LocalDate data) {
        return salarioBasePagamento(data);
    }

    public double calcularDescontos(LocalDate data) {
        if (!sindicalizado || calcularPagamento(data) <= 0) return 0;

        LocalDate inicio = inicioPeriodo(data);
        long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, data) + 1;
        double desconto = taxaSindical * dias;

        for (TaxaServico taxa : taxas) {
            if (!taxa.getData().isBefore(inicio) && !taxa.getData().isAfter(data)) {
                desconto += taxa.getValor();
            }
        }
        return floor2(desconto);
    }

    protected LocalDate inicioPeriodo(LocalDate data) {
        AgendaPagamento agenda = new AgendaPagamento(agendaPagamento);

        if (ultimaDataPagamento != null) {
            if (ultimaDataPagamento.equals(data)) {
                return agenda.inicioPeriodo(data, dataContrato);
            }
            return ultimaDataPagamento.plusDays(1);
        }

        return agenda.inicioPeriodo(data, dataContrato);
    }

    public boolean deveReceber(LocalDate data) {
        return new AgendaPagamento(agendaPagamento).devePagar(data);
    }

    public String getDescricaoPagamento() {
        if ("emMaos".equals(metodoPagamento)) return "Em maos";
        if ("correios".equals(metodoPagamento)) return "Correios, " + endereco;
        return banco + ", Ag. " + agencia + " CC " + contaCorrente;
    }

    protected static double floor2(double valor) {
        return Math.floor(valor * 100.0 + 1e-8) / 100.0;
    }
}
