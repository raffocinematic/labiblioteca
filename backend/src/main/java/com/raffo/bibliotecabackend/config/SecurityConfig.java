package com.raffo.bibliotecabackend.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.raffo.bibliotecabackend.auth.JwtAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.raffo.bibliotecabackend.auth.JwtAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {

    /*
     * Filtro custom che legge il JWT dall'header Authorization: Spring Security non sa validare automaticamente i nostri JWT,
     * dobbiamo inserirgli noi un filtro nella filter chain.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    /*
     * Bean usato per codificare e verificare le password.
     *
     * In registrazione AuthService usa passwordEncoder.encode(...).
     * In login usa passwordEncoder.matches(...).
     *
     * BCrypt e' adatto alle password perché è lento di proposito:
     * rende piu' costosi gli attacchi brute-force sui database rubati.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Questa è la configurazione principale di Spring Security, qui decidiamo:
     * - quali endpoint sono pubblici;
     * - quali endpoint richiedono autenticazione;
     * - che non usiamo sessioni server-side;
     * - dove inserire il filtro JWT nella catena dei filtri.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                /*
                 * CSRF protegge applicazioni basate su session cookie.
                 * In questa API usiamo JWT nell'header Authorization e non sessioni
                 * del browser, quindi possiamo disabilitarlo.
                 */
                .csrf(csrf -> csrf.disable())

                /*
                 * Disabilitiamo il form login standard di Spring.
                 * Non vogliamo una pagina HTML di login generata da Spring:
                 * il nostro login è REST, tramite POST /api/auth/login.
                 */
                .formLogin(formLogin -> formLogin.disable())

                /*
                 * Disabilitiamo HTTP Basic.
                 * Non vogliamo autenticare ogni request con username/password
                 * nell'header Basic. Il client deve usare il JWT Bearer token.
                 */
                .httpBasic(httpBasic -> httpBasic.disable())

                /*
                 * Rendiamo l'applicazione stateless.
                 * Il backend non salva sessioni utente lato server.
                 * Ogni request deve portare il proprio JWT valido.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                /*
                 * Regole di autorizzazione.
                 *
                 * /api/auth/** resta pubblico perché login e registrazione devono
                 * essere accessibili prima di avere un token.
                 *
                 * /api/catalog/books/** diventa privato: serve un JWT valido.
                 *
                 * anyRequest().authenticated() e' una scelta prudente:
                 * qualsiasi nuovo endpoint sara' privato di default, finché
                 * non decidiamo esplicitamente di renderlo pubblico.
                 */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/catalog/books/**").authenticated()
                        .anyRequest().authenticated()
                )

                /*
                 * Inseriamo il nostro filtro JWT prima del filtro standard
                 * UsernamePasswordAuthenticationFilter.
                 *
                 * Cosi', prima che Spring Security valuti se la request è
                 * autenticata, il nostro filtro ha gia' avuto la possibilità
                 * di leggere il token e popolare il SecurityContext.
                 */
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}
