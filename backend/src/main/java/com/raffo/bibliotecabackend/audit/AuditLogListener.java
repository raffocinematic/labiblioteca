package com.raffo.bibliotecabackend.audit;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Nota: questo listener funziona bene per eventi pubblicati dentro metodi @Transactional, come
 * BookService.create update and delete
 */
@Component
public class AuditLogListener {

    private final AuditLogService auditLogService;

    public AuditLogListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    //con questa annotation reagisce solo dopo che la transazione principale è stata confermata
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AuditLogEvent event) {
        auditLogService.record(
                event.action(),
                event.actorUsername(),
                event.entityType(),
                event.entityId(),
                event.details()
        );
    }
}
