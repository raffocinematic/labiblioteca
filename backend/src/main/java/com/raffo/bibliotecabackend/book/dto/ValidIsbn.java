package com.raffo.bibliotecabackend.book.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidIsbnValidator.class)
public @interface ValidIsbn {

    String message() default "L'ISBN deve essere un ISBN-10 o ISBN-13 valido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}