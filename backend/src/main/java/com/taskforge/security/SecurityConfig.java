package com.taskforge.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuration de la sécurité de l'application via Spring Security.
 * Cette classe définit les règles d'authentification, d'autorisation, la gestion des sessions (stateless),
 * la configuration CORS et l'intégration du filtre JWT.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    /**
     * Configure le fournisseur d'authentification.
     * Utilise DaoAuthenticationProvider pour récupérer les détails de l'utilisateur via UserDetailsService
     * et vérifier le mot de passe avec l'encodeur défini.
     *
     * @return L'instance d'AuthenticationProvider configurée.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    /**
     * Expose le gestionnaire d'authentification (AuthenticationManager) en tant que Bean.
     * Nécessaire pour effectuer l'authentification programmatique (ex: dans AuthController).
     *
     * @param config La configuration d'authentification Spring.
     * @return L'instance d'AuthenticationManager.
     * @throws Exception En cas d'erreur de configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Définit la chaîne de filtres de sécurité (SecurityFilterChain).
     * Configure :
     * - La désactivation de CSRF (inutile pour une API REST stateless).
     * - La configuration CORS.
     * - La gestion de session en mode STATELESS (pas de session HTTP serveur).
     * - Les règles d'autorisation des requêtes HTTP (endpoints publics vs authentifiés).
     * - L'ajout du filtre JWT avant le filtre d'authentification par mot de passe.
     *
     * @param http L'objet HttpSecurity à configurer.
     * @return La chaîne de filtres construite.
     * @throws Exception En cas d'erreur de configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(
                    new AntPathRequestMatcher("/swagger-ui/**"),
                    new AntPathRequestMatcher("/swagger-ui.html"),
                    new AntPathRequestMatcher("/v3/api-docs/**"),
                    new AntPathRequestMatcher("/swagger-resources/**")
                ).permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure les règles CORS (Cross-Origin Resource Sharing).
     * Permet à l'application frontend (ou autres clients) d'accéder à l'API.
     * Actuellement configuré pour tout autoriser (*).
     *
     * @return La source de configuration CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Définit l'encodeur de mot de passe utilisé pour hacher et vérifier les mots de passe.
     * Utilise BCrypt, un algorithme de hachage robuste.
     *
     * @return L'instance de PasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
