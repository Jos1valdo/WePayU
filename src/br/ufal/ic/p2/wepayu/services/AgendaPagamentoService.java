package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.AgendaPagamento;
import java.util.Map;

public class AgendaPagamentoService {
    private final Map<String, AgendaPagamento> agendas;
    public AgendaPagamentoService(Map<String, AgendaPagamento> agendas) { this.agendas = agendas; }

    public void adicionar(String descricao) {
        if (descricao == null) throw new RuntimeException("Descricao de agenda invalida");
        AgendaPagamento agenda;
        try { agenda = new AgendaPagamento(descricao); }
        catch (Exception e) { throw new RuntimeException("Descricao de agenda invalida"); }
        if (agendas.containsKey(descricao)) throw new RuntimeException("Agenda de pagamentos ja existe");
        agendas.put(descricao, agenda);
    }

    public boolean existe(String descricao) { return agendas.containsKey(descricao); }
    public Map<String, AgendaPagamento> getAgendas() { return agendas; }
}
