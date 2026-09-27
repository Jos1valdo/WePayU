package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

public class AgendaPagamento implements Serializable {
    private final String descricao;
    private final String tipo;
    private final int intervalo;
    private final int dia;
    private final boolean ultimoDiaUtil;

    public AgendaPagamento(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descricao de agenda invalida");
        }
        this.descricao = descricao.trim();
        String[] p = this.descricao.split(" ");
        if (p.length < 2) throw new IllegalArgumentException("Descricao de agenda invalida");

        if (p[0].equals("mensal")) {
            tipo = "mensal";
            intervalo = 1;
            if (p[1].equals("$")) {
                ultimoDiaUtil = true;
                dia = 0;
            } else {
                ultimoDiaUtil = false;
                dia = Integer.parseInt(p[1]);
                if (dia < 1 || dia > 31) throw new IllegalArgumentException("Descricao de agenda invalida");
            }
        } else if (p[0].equals("semanal")) {
            tipo = "semanal";
            ultimoDiaUtil = false;
            if (p.length == 2) {
                intervalo = 1;
                dia = Integer.parseInt(p[1]);
            } else {
                intervalo = Integer.parseInt(p[1]);
                int diaTemp;
                try { diaTemp = Integer.parseInt(p[2]); } catch (NumberFormatException ex) { diaTemp = diaSemana(p[2]); }
                dia = diaTemp;
            }
            if (intervalo < 1 || dia < 1 || dia > 7) throw new IllegalArgumentException("Descricao de agenda invalida");
        } else {
            throw new IllegalArgumentException("Descricao de agenda invalida");
        }
    }

    private int diaSemana(String s) {
        switch (s.toLowerCase()) {
            case "segunda": return 1;
            case "terca":
            case "terça": return 2;
            case "quarta": return 3;
            case "quinta": return 4;
            case "sexta": return 5;
            case "sabado":
            case "sábado": return 6;
            case "domingo": return 7;
            default: throw new IllegalArgumentException("Descricao de agenda invalida");
        }
    }

    public String getDescricao() { return descricao; }

    public boolean devePagar(LocalDate data) {
        if (tipo.equals("mensal")) {
            if (ultimoDiaUtil) {
                LocalDate ultimo = YearMonth.from(data).atEndOfMonth();
                while (ultimo.getDayOfWeek() == DayOfWeek.SATURDAY || ultimo.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    ultimo = ultimo.minusDays(1);
                }
                return data.equals(ultimo);
            }
            return data.getDayOfMonth() == dia;
        }

        if (data.getDayOfWeek().getValue() != dia) return false;
        LocalDate first = (intervalo == 1) ? LocalDate.of(2005, 1, 7) : LocalDate.of(2005, 1, 14);
        long semanas = java.time.temporal.ChronoUnit.WEEKS.between(first, data);
        return semanas >= 0 && semanas % intervalo == 0;
    }
}
