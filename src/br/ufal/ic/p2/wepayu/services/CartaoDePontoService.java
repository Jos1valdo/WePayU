package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import java.time.LocalDate;
import java.util.Map;

public class CartaoDePontoService {
    private final Map<String, Empregado> empregados;
    public CartaoDePontoService(Map<String, Empregado> empregados) { this.empregados = empregados; }

    private Empregado buscar(String id) {
        return new EmpregadoService(empregados).buscar(id);
    }

    public void lancar(String id, LocalDate data, double horas) {
        Empregado e = buscar(id);
        if (!e.ehHorista()) throw new RuntimeException("Empregado nao eh horista.");
        e.lancarCartao(data, horas);
    }

    public double horasNormais(String id, LocalDate inicio, LocalDate fim) {
        Empregado e = buscar(id);
        if (!e.ehHorista()) throw new RuntimeException("Empregado nao eh horista.");
        double total = 0;
        for (CartaoDePonto c : e.getCartoes()) {
            if (!c.getData().isBefore(inicio) && c.getData().isBefore(fim)) total += Math.min(8, c.getHoras());
        }
        return total;
    }

    public double horasExtras(String id, LocalDate inicio, LocalDate fim) {
        Empregado e = buscar(id);
        if (!e.ehHorista()) throw new RuntimeException("Empregado nao eh horista.");
        double total = 0;
        for (CartaoDePonto c : e.getCartoes()) {
            if (!c.getData().isBefore(inicio) && c.getData().isBefore(fim)) total += Math.max(0, c.getHoras() - 8);
        }
        return total;
    }
}
