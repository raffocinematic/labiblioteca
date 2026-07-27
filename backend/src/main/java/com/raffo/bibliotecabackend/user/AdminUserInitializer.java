package com.raffo.bibliotecabackend.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

//CommandLineRunner viene eseguito all'avvio dell'app. Questo codice crea il primo admin solo se hai
//configurato username/password e solo se non esiste già.
@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminUserInitializer(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username}") String adminUsername,
            @Value("${app.admin.password}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (adminUsername == null || adminUsername.isBlank()) {
            return;
        }

        if (adminPassword == null || adminUsername.isBlank()) {
            return;
        }

        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }

        String passwordHash = passwordEncoder.encode(adminPassword);

        AppUser admin = new AppUser(
                adminUsername,
                passwordHash,
                UserRole.ROLE_ADMIN
        );

        userRepository.save(admin);
    }
}
