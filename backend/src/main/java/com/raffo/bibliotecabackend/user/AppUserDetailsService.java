package com.raffo.bibliotecabackend.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    /*
     * Questo metodo è il punto di integrazione tra il nostro database utenti
     * e Spring Security.
     *
     * Spring Security lavora con UserDetails, non direttamente con la nostra
     * entity AppUser. Per questo qui carichiamo AppUser dal database e poi lo
     * convertiamo nel formato standard richiesto da Spring.
     */
    public AppUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        /*
         * Cerchiamo l'utente usando lo username.
         *
         * Nel flusso JWT, questo username arriverà dal subject del token.
         * Se il token contiene uno username che non esiste piu' nel database,
         * l'autenticazione deve fallire.
         */
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato_ " + username));

        /*
         * Convertiamo AppUser in UserDetails.
         *
         * username: identifica l'utente autenticato.
         * password: qui serve a Spring Security per avere un UserDetails completo;
         *           nel filtro JWT non verrà ricontrollata, perché il login è
         *           gia' stato fatto prima.
         * authorities: rappresenta i permessi/ruoli dell'utente, ad esempio ROLE_USER.
         */
        return User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(user.getRole())
                .build();
    }
}
