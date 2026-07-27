package com.raffo.bibliotecabackend.report.dto;

public record LibraryReportResponse (
        long totalBooks,
        long totalCopies,
        long availableCopies,
        long borrowedCopies,
        long totalUsers,
        long adminUsers,
        long normalUsers
)   {
}
