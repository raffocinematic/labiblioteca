package com.raffo.bibliotecabackend.common.exception;


/**
 * Gestione di una eccezione di questo tipo: se crei o modifichi un libro usando in ISBN già esistente,
 * non è un errore 404 ma è un conflitto dati, HTTP corretto è 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
