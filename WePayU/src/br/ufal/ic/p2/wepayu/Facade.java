package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.models.Empregado;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Facade {

    List<Empregado> ListaEmpregado;

    private Map<String, Empregado> empregados;
    private int proximoId;

    public Facade() {
        this.empregados = new HashMap<>();
        this.proximoId = 1; // Gerando ID para empregados
    }

    public void zerarSistema() {
        this.empregados.clear();
        this.proximoId = 1;
    }
    public String getAtributoEmpregado(String emp, String atributo) throws Exception {
        for (Empregado em : this.ListaEmpregado);

        throw new Exception("Empregado não existe.");
    }

    public void encerrarSistema() {

    }
}
