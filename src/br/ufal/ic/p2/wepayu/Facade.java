package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.services.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;

public class Facade {
    private final Map<String, Empregado> empregados = new LinkedHashMap<>();
    private final Map<String, String> sindicatoParaEmpregado = new HashMap<>();
    private final Map<String, AgendaPagamento> agendas = new LinkedHashMap<>();

    private final EmpregadoService empregadoService;
    private final CartaoDePontoService cartaoDePontoService;
    private final VendaService vendaService;
    private final SindicatoService sindicatoService;
    private final FolhaDePagamentoService folhaService;
    private final AgendaPagamentoService agendaService;

    private boolean encerrado = false;
    private final Deque<Estado> undo = new ArrayDeque<>();
    private final Deque<Estado> redo = new ArrayDeque<>();

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final String ARQUIVO = "wepayu.dat";

    public Facade() {
        empregadoService = new EmpregadoService(empregados);
        cartaoDePontoService = new CartaoDePontoService(empregados);
        vendaService = new VendaService(empregados);
        sindicatoService = new SindicatoService(empregados, sindicatoParaEmpregado);
        folhaService = new FolhaDePagamentoService(empregados);
        agendaService = new AgendaPagamentoService(agendas);
        carregar();
        if (agendas.isEmpty()) criarAgendasPadrao();
    }

    private void criarAgendasPadrao() {
        agendas.clear();
        agendas.put("semanal 5", new AgendaPagamento("semanal 5"));
        agendas.put("mensal $", new AgendaPagamento("mensal $"));
        agendas.put("semanal 2 5", new AgendaPagamento("semanal 2 5"));
    }

    public void zerarSistema() {
        aberto();
        salvarUndo();
        empregados.clear();
        sindicatoParaEmpregado.clear();
        empregadoService.setProximoId(1);
        criarAgendasPadrao();
        redo.clear();
    }

    public void encerrarSistema() {
        if (encerrado) return;
        salvar();
        encerrado = true;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) {
        aberto(); validarNome(nome); validarEndereco(endereco); validarTipo(tipo);
        if (tipo.equals("comissionado")) throw new RuntimeException("Tipo nao aplicavel.");
        double s = salario(salario);
        salvarUndo();
        String id = empregadoService.criar(nome, endereco, tipo, s, null);
        redo.clear(); return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) {
        aberto(); validarNome(nome); validarEndereco(endereco); validarTipo(tipo);
        if (!tipo.equals("comissionado")) throw new RuntimeException("Tipo nao aplicavel.");
        double s = salario(salario); double c = comissao(comissao);
        salvarUndo();
        String id = empregadoService.criar(nome, endereco, tipo, s, c);
        redo.clear(); return id;
    }

    public void removerEmpregado(String emp) {
        aberto(); Empregado empregado = empregadoService.buscar(emp); salvarUndo();
        empregados.remove(empregado.getId());
        if (empregado.isSindicalizado()) sindicatoParaEmpregado.remove(empregado.getIdSindicato());
        redo.clear();
    }

    public String getAtributoEmpregado(String emp, String atributo) {
        aberto(); Empregado empregado = empregadoService.buscar(emp);
        switch (atributo) {
            case "nome": return empregado.getNome();
            case "endereco": return empregado.getEndereco();
            case "tipo": return empregado.getTipo();
            case "salario": return moeda(empregado.getSalario());
            case "comissao":
                if (!empregado.ehComissionado()) throw new RuntimeException("Empregado nao eh comissionado.");
                return moeda(empregado.getComissao());
            case "metodoPagamento": return empregado.getMetodoPagamento();
            case "banco": case "agencia": case "contaCorrente":
                if (!"banco".equals(empregado.getMetodoPagamento())) throw new RuntimeException("Empregado nao recebe em banco.");
                if (atributo.equals("banco")) return empregado.getBanco();
                if (atributo.equals("agencia")) return empregado.getAgencia();
                return empregado.getContaCorrente();
            case "sindicalizado": return Boolean.toString(empregado.isSindicalizado());
            case "idSindicato":
                if (!empregado.isSindicalizado()) throw new RuntimeException("Empregado nao eh sindicalizado.");
                return empregado.getIdSindicato();
            case "taxaSindical":
                if (!empregado.isSindicalizado()) throw new RuntimeException("Empregado nao eh sindicalizado.");
                return moeda(empregado.getTaxaSindical());
            case "agendaPagamento": return empregado.getAgendaPagamento();
            default: throw new RuntimeException("Atributo nao existe.");
        }
    }

   public String getEmpregadoPorNome(String nome, int indice) {

    aberto();

    if (nome == null || nome.isEmpty()) {
        throw new RuntimeException("Nome nao pode ser nulo.");
    }

    int contador = 0;

    for (Empregado empregado : empregados.values()) {

        if (empregado.getNome().equals(nome)) {

            contador++;

            if (contador == indice) {
                return empregado.getId();
            }
        }
    }

    throw new RuntimeException("Nao ha empregado com esse nome.");
}
    public int getNumeroDeEmpregados() { aberto(); return empregadoService.quantidade(); }

    public void alteraEmpregado(String emp, String atributo, String valor) {
        aberto(); Empregado empregado = empregadoService.buscar(emp); validarAlteracao(atributo);
        validarAlteracaoSimples(empregado, atributo, valor);
        salvarUndo(); alterarBasico(empregado, atributo, valor); redo.clear();
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) {
        aberto(); Empregado empregado = empregadoService.buscar(emp);
        if (!atributo.equals("sindicalizado")) throw new RuntimeException("Atributo nao existe.");
        double taxa = taxaSindical(taxaSindical);
        salvarUndo(); sindicatoService.alterar(empregado, valor, idSindicato, taxa); redo.clear();
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia, String contaCorrente) {
        aberto(); Empregado empregado = empregadoService.buscar(emp);
        if (!atributo.equals("metodoPagamento")) throw new RuntimeException("Atributo nao existe.");
        validarPagamento(valor1, banco, agencia, contaCorrente);
        salvarUndo(); alterarPagamento(empregado, valor1, banco, agencia, contaCorrente); redo.clear();
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String quartoParametro) {
        aberto(); Empregado empregado = empregadoService.buscar(emp);
        if (!atributo.equals("tipo")) {
            validarAlteracao(atributo);
            salvarUndo(); alterarBasico(empregado, atributo, valor); redo.clear(); return;
        }
        validarTipo(valor);
        if (valor.equals("horista") || valor.equals("assalariado")) {
            if (quartoParametro == null || quartoParametro.isEmpty()) throw new RuntimeException("Salario nao pode ser nulo.");
            double novoSalario = salario(quartoParametro);
            salvarUndo(); alterarTipo(empregado, valor, null); empregadoService.buscar(empregado.getId()).setSalario(novoSalario); redo.clear();
        } else {
            double novaComissao = comissao(quartoParametro);
            salvarUndo(); alterarTipo(empregado, valor, novaComissao); redo.clear();
        }
    }

    private void validarAlteracao(String atributo) {
        if (!(atributo.equals("nome") || atributo.equals("endereco") || atributo.equals("tipo") || atributo.equals("salario") || atributo.equals("comissao") || atributo.equals("metodoPagamento") || atributo.equals("sindicalizado") || atributo.equals("agendaPagamento")))
            throw new RuntimeException("Atributo nao existe.");
    }

    private void validarAlteracaoSimples(Empregado e, String atributo, String valor) {
        if (atributo.equals("comissao") && !e.ehComissionado()) throw new RuntimeException("Empregado nao eh comissionado.");
        if (atributo.equals("tipo")) validarTipo(valor);
        if (atributo.equals("sindicalizado") && !(valor.equals("true") || valor.equals("false"))) throw new RuntimeException("Valor deve ser true ou false.");
        if (atributo.equals("metodoPagamento")) validarPagamento(valor, null, null, null);
        if (atributo.equals("agendaPagamento") && !agendas.containsKey(valor)) throw new RuntimeException("Agenda de pagamento nao esta disponivel");
    }

    private void alterarBasico(Empregado empregado, String atributo, String valor) {
        switch (atributo) {
            case "nome": validarNome(valor); empregado.setNome(valor); break;
            case "endereco": validarEndereco(valor); empregado.setEndereco(valor); break;
            case "tipo": alterarTipo(empregado, valor, null); break;
            case "salario": empregado.setSalario(salario(valor)); break;
            case "comissao": if (!empregado.ehComissionado()) throw new RuntimeException("Empregado nao eh comissionado."); empregado.setComissao(comissao(valor)); break;
            case "metodoPagamento": alterarPagamento(empregado, valor, null, null, null); break;
            case "sindicalizado":
                if (valor.equals("false")) sindicatoService.alterar(empregado, valor, null, 0);
                else throw new RuntimeException("Identificacao do sindicato nao pode ser nula.");
                break;
            case "agendaPagamento": empregado.setAgendaPagamento(valor); break;
            default: throw new RuntimeException("Atributo nao existe.");
        }
    }

    private void alterarTipo(Empregado atual, String tipo, Double novaComissao) {
        validarTipo(tipo);
        if (tipo.equals("comissionado") && novaComissao == null) {
            if (atual.ehComissionado()) return;
            throw new RuntimeException("Comissao nao pode ser nula.");
        }
        if (atual.getTipo().equals(tipo)) {
            if (tipo.equals("comissionado") && novaComissao != null) atual.setComissao(novaComissao);
            return;
        }
        Empregado novo = EmpregadoFactory.criar(atual.getId(), atual.getNome(), atual.getEndereco(), tipo, atual.getSalario(), tipo.equals("comissionado") ? novaComissao : null);
        copiarEstado(atual, novo);
        if (tipo.equals("comissionado")) novo.setComissao(novaComissao);
        empregados.put(atual.getId(), novo);
    }

    private void copiarEstado(Empregado antigo, Empregado novo) {
        novo.setComissao(antigo.getComissao()); novo.setMetodoPagamento(antigo.getMetodoPagamento());
        novo.setBanco(antigo.getBanco()); novo.setAgencia(antigo.getAgencia()); novo.setContaCorrente(antigo.getContaCorrente());
        novo.setSindicalizado(antigo.isSindicalizado()); novo.setIdSindicato(antigo.getIdSindicato()); novo.setTaxaSindical(antigo.getTaxaSindical());
        novo.setAgendaPagamento(antigo.getAgendaPagamento()); novo.setDataContrato(antigo.getDataContrato());
        for (CartaoDePonto c : antigo.getCartoes()) novo.adicionarCartao(new CartaoDePonto(c.getData(), c.getHoras()));
        for (ResultadoVenda v : antigo.getVendasLista()) novo.adicionarVenda(new ResultadoVenda(v.getData(), v.getValor()));
        for (TaxaServico t : antigo.getTaxasLista()) novo.adicionarTaxa(new TaxaServico(t.getData(), t.getValor()));
    }

    private void alterarPagamento(Empregado empregado, String metodo, String banco, String agencia, String conta) {
        if (metodo.equals("banco")) { empregado.setBanco(banco); empregado.setAgencia(agencia); empregado.setContaCorrente(conta); }
        empregado.setMetodoPagamento(metodo);
    }

    private void validarPagamento(String metodo, String banco, String agencia, String conta) {
        if (!metodo.equals("emMaos") && !metodo.equals("correios") && !metodo.equals("banco")) throw new RuntimeException("Metodo de pagamento invalido.");
        if (metodo.equals("banco")) {
            if (banco == null || banco.isEmpty()) throw new RuntimeException("Banco nao pode ser nulo.");
            if (agencia == null || agencia.isEmpty()) throw new RuntimeException("Agencia nao pode ser nulo.");
            if (conta == null || conta.isEmpty()) throw new RuntimeException("Conta corrente nao pode ser nulo.");
        }
    }

    public void lancaCartao(String emp, String data, String horas) {
        aberto(); Empregado empregado = empregadoService.buscar(emp); if (!empregado.ehHorista()) throw new RuntimeException("Empregado nao eh horista.");
        LocalDate d = data(data); double h = horas(horas); salvarUndo(); cartaoDePontoService.lancar(emp, d, h); redo.clear();
    }

    public String getHorasNormaisTrabalhadas(String emp, String inicio, String fim) {
        LocalDate a=dataInicial(inicio), b=dataFinal(fim); validarIntervalo(a,b); return numero(cartaoDePontoService.horasNormais(emp,a,b));
    }
    public String getHorasExtrasTrabalhadas(String emp, String inicio, String fim) {
        LocalDate a=dataInicial(inicio), b=dataFinal(fim); validarIntervalo(a,b); return numero(cartaoDePontoService.horasExtras(emp,a,b));
    }

    public void lancaVenda(String emp, String data, String valor) {
        aberto(); Empregado e=empregadoService.buscar(emp); if(!e.ehComissionado()) throw new RuntimeException("Empregado nao eh comissionado.");
        LocalDate d=data(data); double v=valorPositivo(valor); salvarUndo(); vendaService.lancar(emp,d,v); redo.clear();
    }
    public String getVendasRealizadas(String emp, String inicio, String fim) {
        LocalDate a=dataInicial(inicio), b=dataFinal(fim); validarIntervalo(a,b); return moeda(vendaService.total(emp,a,b));
    }

    public void lancaTaxaServico(String membro, String data, String valor) {
        aberto(); LocalDate d=data(data); double v=valorPositivo(valor);
        if (membro == null || membro.isEmpty()) throw new RuntimeException("Identificacao do membro nao pode ser nula.");
        if (!sindicatoParaEmpregado.containsKey(membro)) throw new RuntimeException("Membro nao existe.");
        salvarUndo();
        sindicatoService.lancarTaxa(membro,d,v);
        redo.clear();
    }

    public String getTaxasServico(String emp, String inicio, String fim) {
        LocalDate a=dataInicial(inicio), b=dataFinal(fim); validarIntervalo(a,b); return moeda(sindicatoService.totalTaxas(emp,a,b));
    }

    public String totalFolha(String data) { aberto(); return moeda(folhaService.total(data(data))); }

    public void rodaFolha(String data, String saida) {
        aberto(); LocalDate d=data(data); salvarUndo(); redo.clear(); String conteudo=folhaService.gerar(d);
        folhaService.marcarPagamentos(d);
        try { File arquivo=new File(saida); File pasta=arquivo.getParentFile(); if(pasta!=null)pasta.mkdirs(); try(FileWriter w=new FileWriter(arquivo)){w.write(conteudo);} }
        catch(IOException e){throw new RuntimeException("Erro ao gerar folha de pagamento.");}
    }

    public void undo() { aberto(); if(undo.isEmpty()) throw new RuntimeException("Nao ha comando a desfazer."); Estado atual=estado(); redo.push(atual); restaurar(undo.pop()); }
    public void redo() { aberto(); if(redo.isEmpty()) throw new RuntimeException("Nao ha comando a refazer."); Estado atual=estado(); undo.push(atual); restaurar(redo.pop()); }

    public void criarAgendaDePagamentos(String descricao) { aberto(); salvarUndo(); agendaService.adicionar(descricao); redo.clear(); }

    private void salvarUndo() { undo.push(estado()); }

    private Estado estado() {
        Map<String,Empregado> e=new LinkedHashMap<>(); for(Map.Entry<String,Empregado> x:empregados.entrySet()) e.put(x.getKey(),x.getValue().copiar());
        Map<String,AgendaPagamento> a=new LinkedHashMap<>(); for(String k:agendas.keySet()) a.put(k,new AgendaPagamento(k));
        return new Estado(e,new HashMap<>(sindicatoParaEmpregado),a,empregadoService.getProximoId());
    }

    private void restaurar(Estado s) {
        empregados.clear(); for(Map.Entry<String,Empregado> e:s.empregados.entrySet()) empregados.put(e.getKey(),e.getValue().copiar());
        sindicatoParaEmpregado.clear(); sindicatoParaEmpregado.putAll(s.sindicato);
        agendas.clear(); for(Map.Entry<String,AgendaPagamento> e:s.agendas.entrySet()) agendas.put(e.getKey(),new AgendaPagamento(e.getKey()));
        empregadoService.setProximoId(s.proximoId);
    }

    private void salvar() { try(ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(ARQUIVO))){out.writeObject(estado());} catch(IOException e){throw new RuntimeException("Erro ao encerrar sistema.");} }
    private void carregar() {
        File f=new File(ARQUIVO); if(!f.exists())return;
        try(ObjectInputStream in=new ObjectInputStream(new FileInputStream(f))){restaurar((Estado)in.readObject());}catch(Exception ignored){empregados.clear(); sindicatoParaEmpregado.clear(); agendas.clear(); empregadoService.setProximoId(1);}
    }

    private static class Estado implements Serializable {
        Map<String,Empregado> empregados; Map<String,String> sindicato; Map<String,AgendaPagamento> agendas; int proximoId;
        Estado(Map<String,Empregado> e,Map<String,String>s,Map<String,AgendaPagamento>a,int p){empregados=e;sindicato=s;agendas=a;proximoId=p;}
    }

    private void aberto(){if(encerrado)throw new RuntimeException("Nao pode dar comandos depois de encerrarSistema.");}
    private void validarNome(String v){if(v==null||v.isEmpty())throw new RuntimeException("Nome nao pode ser nulo.");}
    private void validarEndereco(String v){if(v==null||v.isEmpty())throw new RuntimeException("Endereco nao pode ser nulo.");}
    private void validarTipo(String v){if(v==null||!(v.equals("horista")||v.equals("assalariado")||v.equals("comissionado")))throw new RuntimeException("Tipo invalido.");}
    private double salario(String v){if(v==null||v.isEmpty())throw new RuntimeException("Salario nao pode ser nulo.");try{double x=Double.parseDouble(v.replace(',','.'));if(x<0)throw new RuntimeException("Salario deve ser nao-negativo.");return x;}catch(NumberFormatException e){throw new RuntimeException("Salario deve ser numerico.");}}
    private double comissao(String v){if(v==null||v.isEmpty())throw new RuntimeException("Comissao nao pode ser nula.");try{double x=Double.parseDouble(v.replace(',','.'));if(x<0)throw new RuntimeException("Comissao deve ser nao-negativa.");return x;}catch(NumberFormatException e){throw new RuntimeException("Comissao deve ser numerica.");}}
    private double taxaSindical(String v){if(v==null||v.isEmpty())throw new RuntimeException("Taxa sindical nao pode ser nula.");try{double x=Double.parseDouble(v.replace(',','.'));if(x<0)throw new RuntimeException("Taxa sindical deve ser nao-negativa.");return x;}catch(NumberFormatException e){throw new RuntimeException("Taxa sindical deve ser numerica.");}}
    private double horas(String v){try{double x=Double.parseDouble(v.replace(',','.'));if(x<=0)throw new RuntimeException("Horas devem ser positivas.");return x;}catch(NumberFormatException e){throw new RuntimeException("Horas devem ser positivas.");}}
    private double valorPositivo(String v){try{double x=Double.parseDouble(v.replace(',','.'));if(x<=0)throw new RuntimeException("Valor deve ser positivo.");return x;}catch(NumberFormatException e){throw new RuntimeException("Valor deve ser positivo.");}}
    private LocalDate data(String v){try{return LocalDate.parse(v,DF);}catch(Exception e){throw new RuntimeException("Data invalida.");}}
    private LocalDate dataInicial(String v){try{return LocalDate.parse(v,DF);}catch(Exception e){throw new RuntimeException("Data inicial invalida.");}}
    private LocalDate dataFinal(String v){try{return LocalDate.parse(v,DF);}catch(Exception e){throw new RuntimeException("Data final invalida.");}}
    private void validarIntervalo(LocalDate a,LocalDate b){if(a.isAfter(b))throw new RuntimeException("Data inicial nao pode ser posterior aa data final.");}
    private String moeda(double v){return String.format(Locale.US,"%.2f",v).replace('.',',');}
    private String numero(double v){if(Math.abs(v-Math.rint(v))<1e-9)return Long.toString(Math.round(v));String s=String.format(Locale.US,"%.2f",v).replace('.',',');while(s.endsWith("0"))s=s.substring(0,s.length()-1);return s;}
}
