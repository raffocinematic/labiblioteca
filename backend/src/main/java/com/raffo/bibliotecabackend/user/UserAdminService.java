package com.raffo.bibliotecabackend.user;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.raffo.bibliotecabackend.common.exception.NotFoundException;
import com.raffo.bibliotecabackend.common.exception.BadRequestException;

//Questo è il service admin utenti

@Service
@Transactional(readOnly = true)
public class UserAdminService {

    private final AppUserRepository userRepository;

    public UserAdminService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<AppUser> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public AppUser updateRole(Long id, UserRole role) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utente non trovato: " +id));

        if(user.getRole() == UserRole.ROLE_ADMIN && role != UserRole.ROLE_ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.ROLE_ADMIN);

            if(adminCount <=1) {
                throw new BadRequestException("Non puoi rimuovere il ruolo all'ultimo admin.");
            }
        }
                user.changeRole(role);

                return user;
    }

    //Puoi eliminare utenti normali, puoi eliminare admin ma solo se ne resta almeno uno di admin.
    @Transactional
    public void deleteUser(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow( () -> new NotFoundException("Utente non trovato: " +id));

        if (user.getRole() == UserRole.ROLE_ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.ROLE_ADMIN);

            if(adminCount <=1) {
                throw new BadRequestException("Non puoi eliminare l'ultimo admin");
            }
        }
    }
}
