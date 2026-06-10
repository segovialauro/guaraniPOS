package com.guarani.pos.electronicinvoice.util;

public final class Mod11 {

    private Mod11() {
    }

    public static int calculate(String digits) {
        int weight = 2;
        int sum = 0;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = Character.digit(digits.charAt(i), 10);
            if (digit < 0) {
                throw new IllegalArgumentException("Solo se admiten digitos para modulo 11.");
            }
            sum += digit * weight;
            weight = weight == 11 ? 2 : weight + 1;
        }

        int remainder = sum % 11;
        int checkDigit = 11 - remainder;
        if (checkDigit == 10 || checkDigit == 11) {
            return 0;
        }
        return checkDigit;
    }
}
