package br.ufal.ic.p2.wepayu;

//import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
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
        aberto(); return empregadoService.porNome(nome, indice);
    }

    public int getNumeroDeEmpregados() {
        aberto();
        return empregadoService.quantidade();
    }

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
        Empregado novo = EmpregadoFactory.criar(
                atual.getId(),
                atual.getNome(),
                atual.getEndereco(),
                tipo,
                atual.getSalario(),
                tipo.equals("comissionado") ? novaComissao : null
        );
        atual.copiarDadosPara(novo);
        if (tipo.equals("comissionado")) novo.setComissao(novaComissao);
        empregados.put(atual.getId(), novo);
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

    public String totalFolha(String data) {
        aberto();
        return moeda(folhaService.total(data(data)));
    }

    public void rodaFolha(String data, String saida) {
        aberto();
        LocalDate d = data(data);
        String conteudo = folhaService.gerar(d);

        try {
            File arquivo = new File(saida);
            File pasta = arquivo.getParentFile();
            if (pasta != null) pasta.mkdirs();

            try (FileWriter writer = new FileWriter(arquivo)) {
                writer.write(conteudo);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar folha de pagamento.", e);
        }

        // O estado de pagamento somente muda depois que o arquivo foi gravado.
        salvarUndo();
        folhaService.marcarPagamentos(d);
        redo.clear();
    }

    public void undo() { aberto(); if(undo.isEmpty()) throw new RuntimeException("Nao ha comando a desfazer."); Estado atual=estado(); redo.push(atual); restaurar(undo.pop()); }
    public void redo() { aberto(); if(redo.isEmpty()) throw new RuntimeException("Nao ha comando a refazer."); Estado atual=estado(); undo.push(atual); restaurar(redo.pop()); }

    public void criarAgendaDePagamentos(String descricao) { aberto(); salvarUndo(); agendaService.adicionar(descricao); redo.clear(); }

    private void salvarUndo() { undo.push(estado()); }

    private Estado estado() {
        Map<String, Empregado> empregadosCopiados = new LinkedHashMap<>();
        for (Map.Entry<String, Empregado> entrada : empregados.entrySet()) {
            empregadosCopiados.put(entrada.getKey(), entrada.getValue().copiar());
        }

        Map<String, AgendaPagamento> agendasCopiadas = new LinkedHashMap<>();
        for (String descricao : agendas.keySet()) {
            agendasCopiadas.put(descricao, new AgendaPagamento(descricao));
        }

        return new Estado(
                empregadosCopiados,
                new HashMap<>(sindicatoParaEmpregado),
                agendasCopiadas,
                empregadoService.getProximoId()
        );
    }

    private void restaurar(Estado estado) {
        empregados.clear();
        for (Map.Entry<String, Empregado> entrada : estado.empregados.entrySet()) {
            empregados.put(entrada.getKey(), entrada.getValue().copiar());
        }

        sindicatoParaEmpregado.clear();
        sindicatoParaEmpregado.putAll(estado.sindicato);

        agendas.clear();
        for (Map.Entry<String, AgendaPagamento> entrada : estado.agendas.entrySet()) {
            agendas.put(entrada.getKey(), new AgendaPagamento(entrada.getKey()));
        }

        empregadoService.setProximoId(estado.proximoId);
    }

    private void salvar() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ARQUIVO))) {
            out.writeObject(estado());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao encerrar sistema.", e);
        }
    }

    private void carregar() {
        File arquivo = new File(ARQUIVO);
        if (!arquivo.exists()) return;

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(arquivo))) {
            restaurar((Estado) input.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Erro ao carregar o sistema.", e);
        }
    }

    private static class Estado implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Map<String, Empregado> empregados;
        private final Map<String, String> sindicato;
        private final Map<String, AgendaPagamento> agendas;
        private final int proximoId;

        Estado(Map<String, Empregado> empregados,
               Map<String, String> sindicato,
               Map<String, AgendaPagamento> agendas,
               int proximoId) {
            this.empregados = empregados;
            this.sindicato = sindicato;
            this.agendas = agendas;
            this.proximoId = proximoId;
        }
    }

    private void aberto() {
        if (encerrado) {
            throw new RuntimeException("Nao pode dar comandos depois de encerrarSistema.");
        }
    }

    private void validarNome(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new RuntimeException("Nome nao pode ser nulo.");
        }
    }

    private void validarEndereco(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new RuntimeException("Endereco nao pode ser nulo.");
        }
    }

    private void validarTipo(String valor) {
        if (valor == null ||
                !(valor.equals("horista") || valor.equals("assalariado") || valor.equals("comissionado"))) {
            throw new RuntimeException("Tipo invalido.");
        }
    }

    private double salario(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new RuntimeException("Salario nao pode ser nulo.");
        }

        try {
            double numero = Double.parseDouble(valor.replace(',', '.'));
            if (numero < 0) {
                throw new RuntimeException("Salario deve ser nao-negativo.");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Salario deve ser numerico.", e);
        }
    }

    private double comissao(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new RuntimeException("Comissao nao pode ser nula.");
        }

        try {
            double numero = Double.parseDouble(valor.replace(',', '.'));
            if (numero < 0) {
                throw new RuntimeException("Comissao deve ser nao-negativa.");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Comissao deve ser numerica.", e);
        }
    }

    private double taxaSindical(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new RuntimeException("Taxa sindical nao pode ser nula.");
        }

        try {
            double numero = Double.parseDouble(valor.replace(',', '.'));
            if (numero < 0) {
                throw new RuntimeException("Taxa sindical deve ser nao-negativa.");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Taxa sindical deve ser numerica.", e);
        }
    }

    private double horas(String valor) {
        try {
            double numero = Double.parseDouble(valor.replace(',', '.'));
            if (numero <= 0) {
                throw new RuntimeException("Horas devem ser positivas.");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Horas devem ser positivas.", e);
        }
    }

    private double valorPositivo(String valor) {
        try {
            double numero = Double.parseDouble(valor.replace(',', '.'));
            if (numero <= 0) {
                throw new RuntimeException("Valor deve ser positivo.");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Valor deve ser positivo.", e);
        }
    }

    private LocalDate data(String valor) {
        try {
            return LocalDate.parse(valor, DF);
        } catch (Exception e) {
            throw new RuntimeException("Data invalida.", e);
        }
    }

    private LocalDate dataInicial(String valor) {
        try {
            return LocalDate.parse(valor, DF);
        } catch (Exception e) {
            throw new RuntimeException("Data inicial invalida.", e);
        }
    }

    private LocalDate dataFinal(String valor) {
        try {
            return LocalDate.parse(valor, DF);
        } catch (Exception e) {
            throw new RuntimeException("Data final invalida.", e);
        }
    }

    private void validarIntervalo(LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new RuntimeException("Data inicial nao pode ser posterior aa data final.");
        }
    }

    private String moeda(double valor) {
        return String.format(Locale.US, "%.2f", valor).replace('.', ',');
    }

    private String numero(double valor) {
        if (Math.abs(valor - Math.rint(valor)) < 1e-9) {
            return Long.toString(Math.round(valor));
        }

        String resultado = String.format(Locale.US, "%.2f", valor).replace('.', ',');
        while (resultado.endsWith("0")) {
            resultado = resultado.substring(0, resultado.length() - 1);
        }
        return resultado;
    }

}
