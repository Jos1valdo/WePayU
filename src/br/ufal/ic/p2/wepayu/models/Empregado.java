package br.ufal.ic.p2.wepayu.models;

//import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Empregado implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final int id;
    private String nome;
    private String endereco;
    private String tipo;
    private BigDecimal salario;
    private BigDecimal comissao;
    
    private String metodoPagamento = "emMaos";
    private String banco;
    private String agencia;
    private String contaCorrente;
    
    private boolean sindicalizado;
    private String idSindicato;
    private BigDecimal taxaSindical;
    
    private String agendaPagamento;
    private String dataContrato;
    
    private final List<CartaoPonto> cartoes = new ArrayList<>();
    private final List<Venda> vendas = new ArrayList<>();
    private final List<TaxaServico> taxasServico = new ArrayList<>();

    public Empregado(int id, String nome, String endereco, String tipo, BigDecimal salario,
            BigDecimal comissao, String agendaPagamento) {
    		this.id = id;
    		this.nome = nome;
    		this.endereco = endereco;
    		this.tipo = tipo;
    		this.salario = salario;
    		this.comissao = comissao;
    		this.agendaPagamento = agendaPagamento;
    }
    
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getTipo() { return tipo; }
    public BigDecimal getSalario() { return salario; }
    public BigDecimal getComissao() { return comissao; }
    public String getMetodoPagamento() { return metodoPagamento; }
    public String getBanco() { return banco; }
    public String getAgencia() { return agencia; }
    public String getContaCorrente() { return contaCorrente; }
    public boolean isSindicalizado() { return sindicalizado; }
    public String getIdSindcato() { return idSindicato; }
    public BigDecimal getTaxaSindical() { return taxaSindical; }
    public String getAgendaPagamento() { return agendaPagamento; }
    public String getDataContrato() { return dataContrato; }
    public List<CartaoPonto> getCartoes() { return cartoes; }
    public List<Venda> getVendas() { return vendas; }
    public List<TaxaServico> getTaxasServico() { return taxasServico; }
    
    
    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }
    public void setComissao(BigDecimal comissao) { this.comissao = comissao; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public void setBanco(String banco) { this.banco = banco; }
    public void setAgencia(String agencia) { this.agencia = agencia; }
    public void setContaCorrente(String contaCorrente) { this.contaCorrente = contaCorrente; }
    public void setSindicalizado(boolean sindicalizado) { this.sindicalizado = sindicalizado; }
    public void setIdSindicato(String idSindicato) { this.idSindicato = idSindicato; }
    public void setTaxaSindical(BigDecimal taxaSindical) { this.taxaSindical = taxaSindical; }
    public void setAgendaPagamento(String agendaPagamento) { this.agendaPagamento = agendaPagamento; }
    public void setDataContrato(String dataContrato) { this.dataContrato = dataContrato; }
    

}
