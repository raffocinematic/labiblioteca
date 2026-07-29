package com.raffo.bibliotecabackend.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Map;

@Entity
@Table ( name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "actor_username", nullable = false, length = 100)
    private String actorUsername;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Convert(converter = AuditDetailsConverter.class)
    @Column(name = "details_json", nullable = false, columnDefinition = "TEXT")
    private Map<String, String> details = Map.of();

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt = Instant.now();

    protected AuditLog() {
    }

    public AuditLog(
            AuditAction action,
            String actorUsername,
            String entityType,
            Long entityId,
            Map<String, String> details
    ) {
        this.action = action;
        this.actorUsername = actorUsername;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details == null ? Map.of() : details;
    }

    // non mettiamo setter: audit log non dovrebbe essere modificato dopo la creazione.

    public Long getId() {
        return id;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
