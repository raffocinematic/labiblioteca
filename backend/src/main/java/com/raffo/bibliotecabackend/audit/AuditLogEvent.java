package com.raffo.bibliotecabackend.audit;

import java.util.Map;

// usiamo un record come DTO immutabile e piccolo, serve solo a trasportare i dati dell'evento

public record AuditLogEvent(
        AuditAction action,
        String actorUsername,
        String entityType,
        Long entityId,
        Map<String, String> details
) {
}
