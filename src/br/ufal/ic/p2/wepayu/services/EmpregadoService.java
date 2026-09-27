package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.models.*;
import java.util.Map;

public class EmpregadoService {
    private final Map<String, Empregado> empregados;
    private int proximoId = 1;

    public EmpregadoService(Map<String, Empregado> empregados) { this.empregados = empregados; }

    public String criar(String nome, String endereco, String tipo, double salario, Double comissao) {
        String id = String.valueOf(proximoId++);
        empregados.put(id, EmpregadoFactory.criar(id, nome, endereco, tipo, salario, comissao));
        return id;
    }

    public void remover(String id) { empregados.remove(buscar(id).getId()); }

    public Empregado buscar(String id) {
        if (id == null || id.isEmpty()) throw new RuntimeException("Identificacao do empregado nao pode ser nula.");
        Empregado e = empregados.get(id);
        if (e == null) throw new EmpregadoNaoExisteException();
        return e;
    }

    public String porNome(String nome, int indice) {
        if (nome == null || nome.isEmpty()) throw new RuntimeException("Nome nao pode ser nulo.");
        int contador = 0;
        for (Empregado e : empregados.values()) {
            if (e.getNome().equals(nome) && ++contador == indice) return e.getId();
        }
        throw new RuntimeException("Empregado nao existe.");
    }

    public int quantidade() { return empregados.size(); }
    public int getProximoId() { return proximoId; }
    public void setProximoId(int id) { proximoId = id; }
    public Map<String, Empregado> getEmpregados() { return empregados; }
}
