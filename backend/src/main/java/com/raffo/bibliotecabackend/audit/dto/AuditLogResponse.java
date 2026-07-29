package com.raffo.bibliotecabackend.audit.dto;

import com.raffo.bibliotecabackend.audit.AuditAction;
import com.raffo.bibliotecabackend.audit.AuditLog;

import java.time.Instant;
import java.util.Map;

// Non esponiamo direttamente l'entity JPA al FE, il controller restituirà questo DTO come già fa con
// BookResponse e UserResponse

public record AuditLogResponse(
        Long id,
        AuditAction action,
        String actorUsername,
        String entityType,
        Long entityId,
        Map<String, String> details,
        Instant occurredAt
) {
    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getAction(),
                auditLog.getActorUsername(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getDetails(),
                auditLog.getOccurredAt()
        );
    }
}
