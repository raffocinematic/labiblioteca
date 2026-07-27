import { Component, OnInit, signal } from '@angular/core';

import { LibraryReportSummary } from '../../../core/models/report.model';
import { ReportApiService } from '../../../core/services/report-api.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-admin-reports',
  imports: [PageHeaderComponent],
  templateUrl: './admin-reports.component.html',
  styleUrl: './admin-reports.component.scss'
})
export class AdminReportsComponent implements OnInit {
  protected readonly summary = signal<LibraryReportSummary | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  constructor(private readonly reportApiService: ReportApiService) {
  }

  ngOnInit(): void {
    this.loadSummary();
  }

  protected loadSummary(): void {
    this.loading.set(true);
    this.error.set(null);

    this.reportApiService.getSummary().subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossibile caricare i report.');
        this.loading.set(false);
      }
    });
  }
}
