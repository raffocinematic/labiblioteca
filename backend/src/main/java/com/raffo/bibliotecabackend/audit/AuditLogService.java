package com.raffo.bibliotecabackend.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional(readOnly=true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    //Metodo centrale per salvare audit log
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record (
            AuditAction action,
            String actorUsername,
            String entityType,
            Long entityId,
            Map<String, String> details
    ) {
        AuditLog auditLog = new AuditLog(
                action,
                normalizeActorUsername(actorUsername),
                entityType,
                entityId,
                details
        );

        auditLogRepository.save(auditLog);
    }

    // questo serve per l'endpoint admin
    public Page<AuditLog> findAll (Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    //con questo evitiamo di salvare null nel campo actor_username, che nel DB è nullable=false
    private String normalizeActorUsername(String actorUsername) {
        if(actorUsername == null || actorUsername.isBlank()) {
            return "SYSTEM";
        }
        return actorUsername.trim();
    }
}
