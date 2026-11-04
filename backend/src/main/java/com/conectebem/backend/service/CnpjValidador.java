package com.conectebem.backend.service;

/**
 * Utilitário de normalização e validação de CNPJ (14 dígitos com dígitos verificadores).
 */
final class CnpjValidador {

    private CnpjValidador() {
    }

    /** Remove tudo que não for dígito (aceita "11.222.333/0001-81" ou "11222333000181"). */
    static String somenteDigitos(String cnpj) {
        return cnpj == null ? "" : cnpj.replaceAll("\\D", "");
    }

    /** Valida um CNPJ já normalizado (somente dígitos). */
    static boolean valido(String digitos) {
        if (digitos == null || !digitos.matches("\\d{14}")) {
            return false;
        }
        // Rejeita sequências repetidas (00000000000000, 11111111111111...)
        if (digitos.chars().distinct().count() == 1) {
            return false;
        }
        return calcularDigito(digitos, 12) == digitos.charAt(12) - '0'
                && calcularDigito(digitos, 13) == digitos.charAt(13) - '0';
    }

    private static int calcularDigito(String digitos, int tamanho) {
        int soma = 0;
        int peso = tamanho - 7;
        for (int i = 0; i < tamanho; i++) {
            soma += (digitos.charAt(i) - '0') * peso;
            peso--;
            if (peso < 2) {
                peso = 9;
            }
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
