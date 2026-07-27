package com.raffo.bibliotecabackend.user;

import com.raffo.bibliotecabackend.auth.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffo.bibliotecabackend.common.exception.NotFoundException;
import com.raffo.bibliotecabackend.user.dto.UpdateUserRoleRequest;
import org.springframework.http.MediaType;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import com.raffo.bibliotecabackend.common.exception.BadRequestException;

@SpringBootTest
@AutoConfigureMockMvc
class UserAdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserAdminService userAdminService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getUsersWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsersWithUserRoleShouldReturn403() throws Exception {
        authenticateAs("user-token", "mario", "ROLE_USER");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsersWithAdminRoleShouldReturn200() throws Exception {
        authenticateAs("admin-token", "admin", "ROLE_ADMIN");

        AppUser admin = new AppUser(
                "admin",
                "password-hash",
                UserRole.ROLE_ADMIN
        );

        when(userAdminService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(admin)));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].username").value("admin"))
                .andExpect(jsonPath("$.content[0].role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist());
    }

    @Test
    void updateUserRoleWithUserRoleShouldReturn403() throws Exception {
        authenticateAs("user-token", "mario", "ROLE_USER");

        UpdateUserRoleRequest request = new UpdateUserRoleRequest(UserRole.ROLE_ADMIN);

        mockMvc.perform(patch("/api/admin/users/2/role")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserRoleWithAdminRoleShouldReturn200() throws Exception {
        authenticateAs("admin-token", "admin", "ROLE_ADMIN");

        UpdateUserRoleRequest request = new UpdateUserRoleRequest(UserRole.ROLE_ADMIN);

        AppUser updatedUser = new AppUser(
                "mario",
                "password-hash",
                UserRole.ROLE_ADMIN
        );

        when(userAdminService.updateRole(eq(2L), eq(UserRole.ROLE_ADMIN)))
                .thenReturn(updatedUser);

        mockMvc.perform(patch("/api/admin/users/2/role")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("mario"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void updateUserRoleForMissingUserShouldReturn404() throws Exception {
        authenticateAs("admin-token", "admin", "ROLE_ADMIN");

        UpdateUserRoleRequest request = new UpdateUserRoleRequest(UserRole.ROLE_USER);

        when(userAdminService.updateRole(eq(99L), eq(UserRole.ROLE_USER)))
                .thenThrow(new NotFoundException("Utente non trovato: 99"));

        mockMvc.perform(patch("/api/admin/users/99/role")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Utente non trovato: 99"));
    }

    @Test
    void updateUserRoleWithoutRoleShouldReturn400() throws Exception {
        authenticateAs("admin-token", "admin", "ROLE_ADMIN");

        mockMvc.perform(patch("/api/admin/users/2/role")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserRoleWhenDemotingLastAdminShouldReturn400() throws Exception {
        authenticateAs("admin-token", "admin", "ROLE_ADMIN");

        UpdateUserRoleRequest request = new UpdateUserRoleRequest(UserRole.ROLE_USER);

        when(userAdminService.updateRole(eq(1L), eq(UserRole.ROLE_USER)))
                .thenThrow(new BadRequestException("Non puoi rimuovere il ruolo all'ultimo admin."));

        mockMvc.perform(patch("/api/admin/users/1/role")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Non puoi rimuovere il ruolo all'ultimo admin."));
    }

    // ---------------------------------------------------------------------------------------------------------------

    private void authenticateAs(String token, String username, String role) {
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password-hash")
                .authorities(role)
                .build();

        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);
    }
}
