package com.raffo.bibliotecabackend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    //questo servirà al login
    Optional<AppUser> findByUsername(String username);

    //questo servirà alla registrazione per bloccare username duplicati
    boolean existsByUsername(String username);

}