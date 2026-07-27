package com.raffo.bibliotecabackend.user;

import com.raffo.bibliotecabackend.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.raffo.bibliotecabackend.common.exception.BadRequestException;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

/**
 * Qui testiamo la logica che legge utenti, trova per id, cambia ruolo e gestisce il caso inesistente
 */
@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    void findAllShouldReturnUsers() {
        Pageable pageable = PageRequest.of(0, 20);

        AppUser admin = new AppUser("admin", "password-hash", UserRole.ROLE_ADMIN);
        AppUser user = new AppUser("mario", "password-hash", UserRole.ROLE_USER);

        when(userRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(admin, user), pageable, 2));

        Page<AppUser> result = userAdminService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(admin, user);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(userRepository).findAll(pageable);
    }

    @Test
    void updateRoleShouldChangeRoleWhenUserExists() {
        AppUser user = new AppUser("mario", "password-hash", UserRole.ROLE_USER);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        AppUser result = userAdminService.updateRole(2L, UserRole.ROLE_ADMIN);

        assertThat(result).isSameAs(user);
        assertThat(result.getRole()).isEqualTo(UserRole.ROLE_ADMIN);

        verify(userRepository).findById(2L);
    }

    @Test
    void updateRoleShouldThrowNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAdminService.updateRole(99L, UserRole.ROLE_ADMIN))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Utente non trovato: 99");

        verify(userRepository).findById(99L);
    }

    @Test
    void updateRoleShouldThrowBadRequestWhenDemotingLastAdmin() {
        AppUser admin = new AppUser("admin", "password-hash", UserRole.ROLE_ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(UserRole.ROLE_ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userAdminService.updateRole(1L, UserRole.ROLE_USER))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Non puoi rimuovere il ruolo all'ultimo admin.");

        assertThat(admin.getRole()).isEqualTo(UserRole.ROLE_ADMIN);

        verify(userRepository).findById(1L);
        verify(userRepository).countByRole(UserRole.ROLE_ADMIN);
    }

    @Test
    void updateRoleShouldAllowDemotingAdminWhenAnotherAdminExists() {
        AppUser admin = new AppUser("admin", "password-hash", UserRole.ROLE_ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(UserRole.ROLE_ADMIN)).thenReturn(2L);

        AppUser result = userAdminService.updateRole(1L, UserRole.ROLE_USER);

        assertThat(result.getRole()).isEqualTo(UserRole.ROLE_USER);

        verify(userRepository).findById(1L);
        verify(userRepository).countByRole(UserRole.ROLE_ADMIN);
    }

    @Test
    void deleteUserShouldDeleteUserWhenExists() {
        AppUser user = new AppUser("mario", "password-hash", UserRole.ROLE_USER);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        userAdminService.deleteUser(2L);

        verify(userRepository).findById(2L);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserShouldThrowNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAdminService.deleteUser(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Utente non trovato: 99");

        verify(userRepository).findById(99L);
        verify(userRepository, never()).delete(any(AppUser.class));
    }

    @Test
    void deleteUserShouldThrowBadRequestWhenDeletingLastAdmin() {
        AppUser admin = new AppUser("admin", "password-hash", UserRole.ROLE_ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(UserRole.ROLE_ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userAdminService.deleteUser(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Non puoi eliminare l'ultimo admin.");

        verify(userRepository).findById(1L);
        verify(userRepository).countByRole(UserRole.ROLE_ADMIN);
        verify(userRepository, never()).delete(any(AppUser.class));
    }
}
