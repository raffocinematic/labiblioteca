import { Component, OnInit, signal } from '@angular/core';

import { AuditLog } from '../../../core/models/audit-log.model';
import { AuditLogApiService } from '../../../core/services/audit-log-api.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-admin-audit-logs',
  imports: [PageHeaderComponent],
  templateUrl: './admin-audit-logs.component.html',
  styleUrl: './admin-audit-logs.component.scss'
})
export class AdminAuditLogsComponent implements OnInit {
  protected readonly auditLogs = signal<AuditLog[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly totalPages = signal(0);

  constructor(private readonly auditLogApiService: AuditLogApiService) {
  }

  ngOnInit(): void {
    this.loadAuditLogs();
  }

  protected loadAuditLogs(page = this.page()): void {
    this.loading.set(true);
    this.error.set(null);

    this.auditLogApiService.getAuditLogs(page, this.size()).subscribe({
      next: (response) => {
        this.auditLogs.set(response.content);
        this.page.set(response.page);
        this.totalPages.set(response.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossibile caricare gli audit log.');
        this.loading.set(false);
      }
    });
  }

  protected previousPage(): void {
    if (this.page() === 0) {
      return;
    }

    this.loadAuditLogs(this.page() - 1);
  }

  protected nextPage(): void {
    if (this.page() + 1 >= this.totalPages()) {
      return;
    }

    this.loadAuditLogs(this.page() + 1);
  }

  protected formatOccurredAt(occurredAt: string): string {
    return new Intl.DateTimeFormat('it-IT', {
      dateStyle: 'short',
      timeStyle: 'medium'
    }).format(new Date(occurredAt));
  }

  protected formatEntity(auditLog: AuditLog): string {
    if (auditLog.entityType === null) {
      return '-';
    }

    if (auditLog.entityId === null) {
      return auditLog.entityType;
    }

    return `${auditLog.entityType} #${auditLog.entityId}`;
  }

  protected detailEntries(details: Record<string, string>): [string, string][] {
    return Object.entries(details);
  }
}
