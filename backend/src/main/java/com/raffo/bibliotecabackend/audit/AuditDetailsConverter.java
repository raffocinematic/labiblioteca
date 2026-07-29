package com.raffo.bibliotecabackend.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

//Converter serve a dichiarare una classe Java che converte:

//valore Java dell'entità ⇄ valore salvato nel database
@Converter
public class AuditDetailsConverter implements AttributeConverter<Map<String, String>, String> {

    // Jackson ObjectMapper: oggetto Java <--> JSON
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> details) {
        try {
            return objectMapper.writeValueAsString(details == null ? Map.of() : details);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Impossibile serializzare i dettagli audit.", e);
        }
    }

    // In Java lavoriamo con Map<String, String>, nel DB salviamo JSON testuale

    @Override
    public Map<String, String> convertToEntityAttribute(String json) {
        try {
            if (json == null || json.isBlank()) {
                return Map.of();
            }

            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Impossibile deserializzare i dettagli audit.", e);
        }
    }

}
