package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import java.time.LocalDate;
import java.util.Map;

public class SindicatoService {
    private final Map<String, Empregado> empregados;
    private final Map<String, String> sindicatoParaEmpregado;

    public SindicatoService(Map<String, Empregado> empregados, Map<String, String> sindicatoParaEmpregado) {
        this.empregados = empregados;
        this.sindicatoParaEmpregado = sindicatoParaEmpregado;
    }

    private Empregado buscar(String id) { return new EmpregadoService(empregados).buscar(id); }

    public void alterar(Empregado empregado, String valor, String id, double taxa) {
        if (!valor.equals("true") && !valor.equals("false")) throw new RuntimeException("Valor deve ser true ou false.");
        if (valor.equals("false")) {
            if (empregado.isSindicalizado()) sindicatoParaEmpregado.remove(empregado.getIdSindicato());
            empregado.setSindicalizado(false);
            return;
        }
        if (id == null || id.isEmpty()) throw new RuntimeException("Identificacao do sindicato nao pode ser nula.");
        String outro = sindicatoParaEmpregado.get(id);
        if (outro != null && !outro.equals(empregado.getId())) throw new RuntimeException("Ha outro empregado com esta identificacao de sindicato");
        if (empregado.isSindicalizado() && !id.equals(empregado.getIdSindicato())) sindicatoParaEmpregado.remove(empregado.getIdSindicato());
        empregado.setSindicalizado(true);
        empregado.setIdSindicato(id);
        empregado.setTaxaSindical(taxa);
        sindicatoParaEmpregado.put(id, empregado.getId());
    }

    public void lancarTaxa(String membro, LocalDate data, double valor) {
        if (membro == null || membro.isEmpty()) throw new RuntimeException("Identificacao do membro nao pode ser nula.");
        String id = sindicatoParaEmpregado.get(membro);
        if (id == null) throw new RuntimeException("Membro nao existe.");
        buscar(id).lancarTaxaServico(data, valor);
    }

    public double totalTaxas(String id, LocalDate inicio, LocalDate fim) {
        Empregado e = buscar(id);
        if (!e.isSindicalizado()) throw new RuntimeException("Empregado nao eh sindicalizado.");
        double total = 0;
        for (TaxaServico t : e.getTaxasLista()) if (!t.getData().isBefore(inicio) && t.getData().isBefore(fim)) total += t.getValor();
        return total;
    }
}
