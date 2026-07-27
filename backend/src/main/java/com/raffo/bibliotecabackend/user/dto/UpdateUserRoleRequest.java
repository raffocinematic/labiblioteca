package com.raffo.bibliotecabackend.user.dto;

import com.raffo.bibliotecabackend.user.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull
        UserRole role
) {

}
