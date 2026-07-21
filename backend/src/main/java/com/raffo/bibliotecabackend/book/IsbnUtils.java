package com.raffo.bibliotecabackend.book;

/**
 * Questa classe ha 1 responsabilità sola: rimuovere spazi/trattini e verificare checksum ISBN-10 / ISBN-13
 */
public final class IsbnUtils {

    private IsbnUtils() {
    }

    public static String normalize(String isbn) {
        if (isbn == null) {
            return null;
        }

        return isbn.replaceAll("[\\s-]", "").toUpperCase();
    }

    public static boolean isValid(String isbn) {
        String normalizedIsbn = normalize(isbn);

        if (normalizedIsbn == null) {
            return true;
        }

        return isValidIsbn10(normalizedIsbn) || isValidIsbn13(normalizedIsbn);
    }

    private static boolean isValidIsbn10(String isbn) {
        if (!isbn.matches("\\d{9}[\\dX]")) {
            return false;
        }

        int sum = 0;

        for (int i = 0; i < 10; i++) {
            char character = isbn.charAt(i);
            int value = character == 'X' ? 10 : Character.getNumericValue(character);
            sum += value * (10 - i);
        }

        return sum % 11 == 0;
    }

    private static boolean isValidIsbn13(String isbn) {
        if (!isbn.matches("\\d{13}")) {
            return false;
        }

        int sum = 0;

        for (int i = 0; i < 13; i++) {
            int value = Character.getNumericValue(isbn.charAt(i));
            sum += i % 2 == 0 ? value : value * 3;
        }

        return sum % 10 == 0;
    }

}
