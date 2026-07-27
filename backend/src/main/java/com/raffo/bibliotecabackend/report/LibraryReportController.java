package com.raffo.bibliotecabackend.report;

import com.raffo.bibliotecabackend.report.dto.LibraryReportResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class LibraryReportController {

    private final LibraryReportService libraryReportService;

    public LibraryReportController(LibraryReportService libraryReportService) {
        this.libraryReportService = libraryReportService;
    }

    @GetMapping("/summary")
    public LibraryReportResponse getSummary() {
        return libraryReportService.getSummary();
    }
}
