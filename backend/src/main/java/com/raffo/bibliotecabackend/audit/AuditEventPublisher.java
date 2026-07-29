package com.raffo.bibliotecabackend.audit;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuditEventPublisher {

    // ApplicationEventPublisher è il meccanismo standard Spring per pubblicare eventi applicativi.
    private final ApplicationEventPublisher eventPublisher;

    public AuditEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publish (
            AuditAction action,
            String entityType,
            Long entityId,
            Map<String, String> details
    ) {
        AuditLogEvent event = new AuditLogEvent(
                action,
                currentUsername(),
                entityType,
                entityId,
                details == null ? Map.of() : Map.copyOf(details) // -> evita che qualcuno modifichi la mappa dopo aver pubblicato l'evento.
        );

        eventPublisher.publishEvent(event);
    }

    // -----------------------------------------------------------------------------------------------------------

    /**
     * Qui centralizziamo la logica per capire chi ha fatto l'azione
     * @return
     */
    private String currentUsername() {
        // SecurityContextHolder contiene l'utente autenticato della request corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()) {
            return "SYSTEM";
        }

        String username = authentication.getName();

        if(username == null || username.isBlank() || "anonymousUser".equals(username)) {
            return "SYSTEM";
        }

        return username;
    }
}
