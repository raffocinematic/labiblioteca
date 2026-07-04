package com.raffo.bibliotecabackend.auth;

import com.raffo.bibliotecabackend.auth.dto.AuthResponse;
import com.raffo.bibliotecabackend.auth.dto.LoginRequest;
import com.raffo.bibliotecabackend.auth.dto.RegisterRequest;
import com.raffo.bibliotecabackend.common.exception.ConflictException;
import com.raffo.bibliotecabackend.common.exception.UnauthorizedException;
import com.raffo.bibliotecabackend.user.AppUser;
import com.raffo.bibliotecabackend.user.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.raffo.bibliotecabackend.common.exception.BadRequestException;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) throws BadRequestException {

        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords don't match");
        }

        if(userRepository.existsByUsername(request.username())) {
            throw new ConflictException("This username" + request.username() + "is already taken");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        AppUser user = userRepository.save(new AppUser(request.username(), passwordHash));

        return new AuthResponse(jwtService.generateToken(user), user.getUsername(), user.getRole());

    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Credenziali non valide."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Credenziali non valide.");
        }

        return new AuthResponse(jwtService.generateToken(user), user.getUsername(), user.getRole());
    }
}
