package util;

import java.text.ParseException;
import java.util.regex.Pattern;

public class ValidadorUtil {
    private static final Pattern PADRAO_CPF = Pattern.compile("\\d{11}");
    private static final Pattern PADRAO_TELEFONE = Pattern.compile("\\d{10,11}");

    public static boolean validarCPF(String cpf) {
        cpf = FormatadorUtil.removerFormatacaoCPF(cpf);
        
        if (!PADRAO_CPF.matcher(cpf).matches()) {
            return false;
        }

        // Verifica se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        // Cálculo do primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito > 9) primeiroDigito = 0;

        // Cálculo do segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito > 9) segundoDigito = 0;

        // Verifica se os dígitos calculados são iguais aos dígitos informados
        return (Character.getNumericValue(cpf.charAt(9)) == primeiroDigito &&
                Character.getNumericValue(cpf.charAt(10)) == segundoDigito);
    }

    public static boolean validarTelefone(String telefone) {
        telefone = telefone.replaceAll("\\D", "");
        return PADRAO_TELEFONE.matcher(telefone).matches();
    }
    
    public static String formatarTelefone(String telefone) {
    telefone = telefone.replaceAll("\\D", "");
    if (telefone.length() == 11) {
        return String.format("(%s) %s %s-%s",
            telefone.substring(0, 2),
            telefone.substring(2, 3),
            telefone.substring(3, 7),
            telefone.substring(7));
    }
    return telefone;
}

    public static boolean validarValorMonetario(String valor) {
        try {
            double valorNumerico = FormatadorUtil.converterMoedaParaDouble(valor);
            return valorNumerico >= 0;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean validarQuantidade(int quantidade) {
        return quantidade >= 0;
    }

    public static boolean validarNome(String nome) {
        return nome != null && nome.trim().length() >= 3;
    }

    public static boolean validarCategoria(String categoria) {
        return categoria != null && 
               (categoria.equals("Alimentos") || categoria.equals("Higiene/Limpeza"));
    }
}