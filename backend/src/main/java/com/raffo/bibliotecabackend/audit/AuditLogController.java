package com.raffo.bibliotecabackend.audit;

import com.raffo.bibliotecabackend.audit.dto.AuditLogResponse;
import com.raffo.bibliotecabackend.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')") // -> limita la consultazione agli admin
public class AuditLogController {

    public final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    // Pageable evita di caricare tutto lo storico in memoria
    @GetMapping
    public PageResponse<AuditLogResponse> getAuditLogs(
            @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return PageResponse.from(
                auditLogService.findAll(pageable)
                        .map(AuditLogResponse::from)
        );
    }
}
