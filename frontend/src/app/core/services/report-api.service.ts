import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { LibraryReportSummary } from '../models/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportApiService {
  private readonly reportsUrl = `${API_BASE_URL}/admin/reports`;

  constructor(private readonly http: HttpClient) {
  }

  getSummary(): Observable<LibraryReportSummary> {
    return this.http.get<LibraryReportSummary>(`${this.reportsUrl}/summary`);
  }
}
