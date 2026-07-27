package com.raffo.bibliotecabackend.user.dto;

import com.raffo.bibliotecabackend.user.AppUser;
import com.raffo.bibliotecabackend.user.UserRole;

import java.time.Instant;

//Usiamo questo perché giustamente non devi mai restituire direttamente AppUser che contiene passwordHash
public record UserResponse (
        Long id,
        String username,
        UserRole role,
        Instant createdAt
) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
