package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;

public class AgendaPagamento implements Serializable {
    private static final long serialVersionUID = 1L;

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
        String[] partes = this.descricao.split("\\s+");

        if ("mensal".equals(partes[0])) {
            validarQuantidade(partes, 2);
            tipo = "mensal";
            intervalo = 1;

            if ("$".equals(partes[1])) {
                ultimoDiaUtil = true;
                dia = 0;
                return;
            }

            ultimoDiaUtil = false;
            dia = inteiro(partes[1]);
            if (dia < 1 || dia > 28) {
                throw new IllegalArgumentException("Descricao de agenda invalida");
            }
            return;
        }

        if ("semanal".equals(partes[0])) {
            if (partes.length != 2 && partes.length != 3) {
                throw new IllegalArgumentException("Descricao de agenda invalida");
            }

            tipo = "semanal";
            ultimoDiaUtil = false;

            if (partes.length == 2) {
                intervalo = 1;
                dia = diaSemana(partes[1]);
            } else {
                intervalo = inteiro(partes[1]);
                dia = diaSemana(partes[2]);
                if (intervalo < 1 || intervalo > 52) {
                    throw new IllegalArgumentException("Descricao de agenda invalida");
                }
            }
            return;
        }

        throw new IllegalArgumentException("Descricao de agenda invalida");
    }

    private static void validarQuantidade(String[] partes, int quantidade) {
        if (partes.length != quantidade) {
            throw new IllegalArgumentException("Descricao de agenda invalida");
        }
    }

    private static int inteiro(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Descricao de agenda invalida", e);
        }
    }

    private static int diaSemana(String valor) {
        try {
            int numero = Integer.parseInt(valor);
            if (numero < 1 || numero > 7) {
                throw new IllegalArgumentException("Descricao de agenda invalida");
            }
            return numero;
        } catch (NumberFormatException ignored) {
            switch (valor.toLowerCase()) {
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
    }

    public String getDescricao() {
        return descricao;
    }

    public int getIntervalo() {
        return intervalo;
    }

    public boolean ehMensal() {
        return "mensal".equals(tipo);
    }

    public boolean ehSemanal() {
        return "semanal".equals(tipo);
    }

    public boolean devePagar(LocalDate data) {
        if (ehMensal()) {
            if (ultimoDiaUtil) {
                return data.equals(ultimoDiaUtil(data));
            }
            return data.getDayOfMonth() == dia;
        }

        if (data.getDayOfWeek().getValue() != dia) {
            return false;
        }

        int semana = data.get(WeekFields.ISO.weekOfWeekBasedYear());
        return semana % intervalo == 0;
    }

    public LocalDate inicioPeriodo(LocalDate data, LocalDate dataContrato) {
        LocalDate inicio;
        LocalDate anterior = pagamentoAnterior(data);

        if (anterior == null) {
            inicio = data;
        } else {
            inicio = anterior.plusDays(1);
        }

        if (dataContrato != null && inicio.isBefore(dataContrato)) {
            inicio = dataContrato;
        }
        return inicio;
    }

    private LocalDate pagamentoAnterior(LocalDate data) {
        if (ehMensal()) {
            LocalDate anterior = data.minusMonths(1);
            if (ultimoDiaUtil) {
                return ultimoDiaUtil(anterior);
            }
            int ultimoDia = YearMonth.from(anterior).lengthOfMonth();
            return anterior.withDayOfMonth(Math.min(dia, ultimoDia));
        }

        LocalDate candidato = data.minusDays(1);
        for (int i = 0; i < 370; i++, candidato = candidato.minusDays(1)) {
            if (devePagar(candidato)) {
                return candidato;
            }
        }
        return null;
    }

    private static LocalDate ultimoDiaUtil(LocalDate data) {
        LocalDate ultimo = YearMonth.from(data).atEndOfMonth();
        while (ultimo.getDayOfWeek() == DayOfWeek.SATURDAY || ultimo.getDayOfWeek() == DayOfWeek.SUNDAY) {
            ultimo = ultimo.minusDays(1);
        }
        return ultimo;
    }
}
