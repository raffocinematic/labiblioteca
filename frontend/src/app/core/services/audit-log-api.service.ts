import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { AuditLog } from '../models/audit-log.model';
import { PageResponse } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class AuditLogApiService {
  private readonly auditLogsUrl = `${API_BASE_URL}/admin/audit-logs`;

  constructor(private readonly http: HttpClient) {
  }

  getAuditLogs(page = 0, size = 20): Observable<PageResponse<AuditLog>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);

    return this.http.get<PageResponse<AuditLog>>(this.auditLogsUrl, { params });
  }
}
