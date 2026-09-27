package br.ufal.ic.p2.wepayu.models;

public class EmpregadoFactory {
    public static Empregado criar(String id, String nome, String endereco, String tipo, double salario, Double comissao) {
        switch (tipo) {
            case "horista": return new Horista(id, nome, endereco, salario);
            case "assalariado": return new Assalariado(id, nome, endereco, salario);
            case "comissionado": return new Comissionado(id, nome, endereco, salario, comissao == null ? 0 : comissao);
            default: throw new IllegalArgumentException("Tipo invalido.");
        }
    }
}
