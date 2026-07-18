package com.raffo.bibliotecabackend.auth;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    /*
     * Questo filtro viene eseguito una volta per ogni request HTTP.
     *
     * Il suo compito e':
     * - leggere l'header Authorization;
     * - estrarre il JWT se presente;
     * - validarlo;
     * - popolare il SecurityContext se il token e' valido.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        /*
         * Se non c'e' un Bearer token, il filtro non autentica nulla.
         *
         * La request prosegue comunque: sara' SecurityConfig a decidere
         * se l'endpoint e' pubblico oppure se deve rispondere 401.
         */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            /*
             * Non sovrascriviamo un'autenticazione gia' presente.
             */
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    /*
                     * Punto chiave: da qui Spring Security considera autenticata
                     * questa request.
                     */
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException | AuthenticationException exception) {
            /*
             * Token scaduto, token alterato, token malformato oppure utente non trovato.
             *
             * Puliamo il contesto e restituiamo una risposta 401 coerente usando
             * il nostro AuthenticationEntryPoint.
             */
            SecurityContextHolder.clearContext();

            jwtAuthenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("Token JWT non valido.", exception)
            );
        }
    }
}