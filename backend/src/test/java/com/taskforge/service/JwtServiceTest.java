package com.taskforge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour le service utilitaire JWT (JwtService).
 * Vérifie la génération de tokens, l'extraction du nom d'utilisateur et la validation des claims (expiration).
 */
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    private static final String SECRET_KEY = "NDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQy";
    private static final long EXPIRATION_MS = 3600000; 

    /**
     * Initialise l'instance de JwtService avant chaque test.
     * Injecte manuellement la clé secrète et le temps d'expiration via ReflectionTestUtils
     * car @Value ne fonctionne pas dans un test unitaire simple sans contexte Spring.
     */
    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", EXPIRATION_MS);

        userDetails = new User("testuser", "password", new ArrayList<>());
    }

    /**
     * Vérifie que la méthode generateToken produit une chaîne de caractères non nulle et non vide.
     */
    @Test
    void generateToken_ShouldReturnValidJwtToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    /**
     * Vérifie que le nom d'utilisateur extrait du token correspond bien à celui de l'utilisateur
     * pour lequel le token a été généré.
     */
    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo(userDetails.getUsername());
    }

    /**
     * Vérifie que l'extraction d'un claim spécifique (ici l'expiration) fonctionne correctement
     * et que la date d'expiration est cohérente avec la configuration (temps actuel + délai).
     */
    @Test
    void extractClaim_ShouldReturnCorrectExpiration() {
        String token = jwtService.generateToken(userDetails);
        Long expiration = jwtService.extractClaim(token, claims -> claims.getExpiration().getTime());

        long expectedExpiration = System.currentTimeMillis() + EXPIRATION_MS;
        assertThat(expiration).isBetween(expectedExpiration - 1000, expectedExpiration + 1000);
    }

}