package com.raffo.bibliotecabackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    // questo lo teniamo perché serve ancora per il vecchio AuthService, dopo
    // aver rimosso la vecchia autenticazione valutiamo se tenerlo o toglierlo definitivamente.
   @Bean
    public PasswordEncoder passwordEncoder() {
       return new BCryptPasswordEncoder();
   }

   // Filter chain principale di Spring Security
    // Prima avevi un filtro custom JwtAuthenticatorFilter

    //Ora invece Spring Security valida automaticamente i Bearer token messi
    // da Keycloak tramite ouath2ResourceServer().

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {
       return http
               // Api stateless: non usiamo sessioni server-side
                // Ogni request deve portare il token Bearer.
               .crsf(csrf -> csrf.disable())
               .formLogin(formLogin -> formLogin.disable())
               .httpBasic(httpBasic -> httpBasic.disable())
               .sessionManagement(session ->
                       session.sessionCreationPolicy(SessionCreatonPolicy.STATELESS)
               )
               // regole di autorizzazione HTTP.
               .authorizeHttpRequests(auth -> auth
                       .requestMatchers(
                               "/swagger-ui.html",
                               "/swagger-ui/**",
                               "/v3/api-docs/**"
                       ).permitAll()

    }


}
