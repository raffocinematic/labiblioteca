package com.raffo.bibliotecabackend.report;

import com.raffo.bibliotecabackend.book.BookRepository;
import com.raffo.bibliotecabackend.report.dto.LibraryReportResponse;
import com.raffo.bibliotecabackend.user.AppUserRepository;
import com.raffo.bibliotecabackend.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LibraryReportService {

    private final BookRepository bookRepository;
    private final AppUserRepository userRepository;

    public LibraryReportService(BookRepository bookRepository, AppUserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public LibraryReportResponse getSummary() {
        long totalBooks = bookRepository.count();
        long totalCopies = bookRepository.sumTotalCopies();
        long availableCopies = bookRepository.sumAvailableCopies();
        long borrowedCopies = totalCopies - availableCopies;

        long totalUsers = userRepository.count();
        long adminUsers = userRepository.countByRole(UserRole.ROLE_ADMIN);
        long normalUsers = userRepository.countByRole(UserRole.ROLE_USER);

        return new LibraryReportResponse(
                totalBooks,
                totalCopies,
                availableCopies,
                borrowedCopies,
                totalUsers,
                adminUsers,
                normalUsers
        );
    }
}
