package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import java.time.LocalDate;
import java.util.Map;

public class VendaService {
    private final Map<String, Empregado> empregados;
    public VendaService(Map<String, Empregado> empregados) { this.empregados = empregados; }

    private Empregado buscar(String id) { return new EmpregadoService(empregados).buscar(id); }

    public void lancar(String id, LocalDate data, double valor) {
        Empregado e = buscar(id);
        if (!e.ehComissionado()) throw new RuntimeException("Empregado nao eh comissionado.");
        e.lancarVenda(data, valor);
    }

    public double total(String id, LocalDate inicio, LocalDate fim) {
        Empregado e = buscar(id);
        if (!e.ehComissionado()) throw new RuntimeException("Empregado nao eh comissionado.");
        double total = 0;
        for (ResultadoVenda v : e.getVendasLista()) {
            if (!v.getData().isBefore(inicio) && v.getData().isBefore(fim)) total += v.getValor();
        }
        return total;
    }
}
