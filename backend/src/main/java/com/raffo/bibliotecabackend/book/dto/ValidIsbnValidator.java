package com.raffo.bibliotecabackend.book.dto;

import com.raffo.bibliotecabackend.book.IsbnUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Gestisce la correttezza semantica dell'ISBN
 */
public class ValidIsbnValidator implements ConstraintValidator<ValidIsbn, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            return true;
        }

        return IsbnUtils.isValid(value);
    }
}
