package util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class FormatadorUtil {
    private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");
    private static final DecimalFormat FORMATO_MOEDA = new DecimalFormat("¤ #,##0.00");
    private static final DecimalFormat FORMATO_NUMERO = new DecimalFormat("#,##0.00");

    static {
        FORMATO_MOEDA.setCurrency(java.util.Currency.getInstance("BRL"));
        FORMATO_MOEDA.setMinimumFractionDigits(2);
        FORMATO_MOEDA.setMaximumFractionDigits(2);
    }

    public static String formatarCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    public static String removerFormatacaoCPF(String cpf) {
        if (cpf == null) return null;
        return cpf.replaceAll("\\D", "");
    }

    public static String formatarTelefone(String telefone) {
        if (telefone == null) return null;
        
        telefone = telefone.replaceAll("\\D", "");
        if (telefone.length() == 11) {
            return telefone.replaceAll("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
        } else if (telefone.length() == 10) {
            return telefone.replaceAll("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
        }
        return telefone;
    }

    public static String formatarMoeda(double valor) {
        return FORMATO_MOEDA.format(valor);
    }

    public static double converterMoedaParaDouble(String valor) throws ParseException {
        if (valor == null || valor.trim().isEmpty()) {
            return 0.0;
        }
        valor = valor.replace("R$", "").trim();
        return NumberFormat.getInstance(LOCALE_BR).parse(valor).doubleValue();
    }

    public static String formatarNumero(double numero) {
        return FORMATO_NUMERO.format(numero);
    }
}