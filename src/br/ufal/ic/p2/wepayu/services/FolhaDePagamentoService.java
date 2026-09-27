package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import java.time.LocalDate;
import java.util.*;

public class FolhaDePagamentoService {
    private final Map<String, Empregado> empregados;
    public FolhaDePagamentoService(Map<String, Empregado> empregados) { this.empregados = empregados; }

    public double total(LocalDate data) {
        double total = 0;
        for (Empregado e : empregados.values()) if (e.deveReceber(data)) total += e.calcularPagamento(data);
        return total;
    }

    public void marcarPagamentos(LocalDate data) {
        for (Empregado e : empregados.values()) if (e.deveReceber(data) && e.calcularPagamento(data) > 0) e.setUltimaDataPagamento(data);
    }

    public String gerar(LocalDate data) {
        StringBuilder folha = new StringBuilder();
        folha.append("FOLHA DE PAGAMENTO DO DIA ").append(String.format("%04d-%02d-%02d", data.getYear(), data.getMonthValue(), data.getDayOfMonth())).append("\n====================================\n\n");
        secaoHoristas(folha, data);
        secaoAssalariados(folha, data);
        secaoComissionados(folha, data);
        folha.append("TOTAL FOLHA: ").append(moeda(total(data))).append("\n");
        return folha.toString();
    }

    private List<Empregado> ordenados() {
        List<Empregado> l = new ArrayList<>(empregados.values());
        l.sort(Comparator.comparing(Empregado::getNome));
        return l;
    }

    private void secaoHoristas(StringBuilder f, LocalDate d) {
        f.append("===============================================================================================================================\n");
        f.append("===================== HORISTAS ================================================================================================\n");
        f.append("===============================================================================================================================\n");
        f.append("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo\n");
        f.append("==================================== ===== ===== ============= ========= =============== ======================================\n");
        double horas=0, extras=0, bruto=0, descontos=0, liquido=0;
        for (Empregado e: ordenados()) if (e.ehHorista() && e.deveReceber(d)) {
            double h=e.getHorasPagamento(d), he=e.getHorasExtrasPagamento(d), b=e.calcularPagamento(d), ds=e.calcularDescontos(d), l=Math.max(0,b-ds);
            f.append(String.format(Locale.US,"%-36s %5s %5s %13s %9s %15s %s\n",e.getNome(),numero(h),numero(he),moeda(b),moeda(ds),moeda(l),e.getDescricaoPagamento()));
            horas+=h; extras+=he; bruto+=b; descontos+=ds; liquido+=l;
        }
        f.append(String.format(Locale.US,"\nTOTAL HORISTAS                       %5s %5s %13s %9s %15s\n\n",numero(horas),numero(extras),moeda(bruto),moeda(descontos),moeda(liquido)));
    }

    private void secaoAssalariados(StringBuilder f, LocalDate d) {
        f.append("===============================================================================================================================\n");
        f.append("===================== ASSALARIADOS ============================================================================================\n");
        f.append("===============================================================================================================================\n");
        f.append("Nome                                             Salario Bruto Descontos Salario Liquido Metodo\n");
        f.append("================================================ ============= ========= =============== ======================================\n");
        double bruto=0, descontos=0, liquido=0;
        for (Empregado e: ordenados()) if (e.ehAssalariado() && e.deveReceber(d)) {
            double b=e.calcularPagamento(d), ds=e.calcularDescontos(d), l=Math.max(0,b-ds);
            f.append(String.format(Locale.US,"%-48s %13s %9s %15s %s\n",e.getNome(),moeda(b),moeda(ds),moeda(l),e.getDescricaoPagamento()));
            bruto+=b; descontos+=ds; liquido+=l;
        }
        f.append(String.format(Locale.US,"\nTOTAL ASSALARIADOS                                %12s %9s %15s\n\n",moeda(bruto),moeda(descontos),moeda(liquido)));
    }

    private void secaoComissionados(StringBuilder f, LocalDate d) {
        f.append("===============================================================================================================================\n");
        f.append("===================== COMISSIONADOS ===========================================================================================\n");
        f.append("===============================================================================================================================\n");
        f.append("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo\n");
        f.append("===================== ======== ======== ======== ============= ========= =============== ======================================\n");
        double fixo=0,vendas=0,comissao=0,bruto=0,descontos=0,liquido=0;
        for (Empregado e: ordenados()) if (e.ehComissionado() && e.deveReceber(d)) {
            double fx=e.getFixoPagamento(d), v=e.getVendasPagamento(d), c=e.getComissaoPagamento(d), b=e.calcularPagamento(d), ds=e.calcularDescontos(d), l=Math.max(0,b-ds);
            f.append(String.format(Locale.US,"%-21s %8s %8s %8s %13s %9s %15s %s\n",e.getNome(),moeda(fx),moeda(v),moeda(c),moeda(b),moeda(ds),moeda(l),e.getDescricaoPagamento()));
            fixo+=fx; vendas+=v; comissao+=c; bruto+=b; descontos+=ds; liquido+=l;
        }
        f.append(String.format(Locale.US,"\nTOTAL COMISSIONADOS   %8s %8s %8s %13s %9s %15s\n\n",moeda(fixo),moeda(vendas),moeda(comissao),moeda(bruto),moeda(descontos),moeda(liquido)));
    }

    private String moeda(double v) { return String.format(Locale.US,"%.2f",v).replace('.',','); }
    private String numero(double v) {
        if (Math.abs(v-Math.rint(v))<1e-9) return Long.toString(Math.round(v));
        String s=String.format(Locale.US,"%.2f",v).replace('.',',');
        while(s.endsWith("0")) s=s.substring(0,s.length()-1);
        return s;
    }
}
