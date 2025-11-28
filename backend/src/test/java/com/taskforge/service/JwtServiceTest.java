package com.taskforge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    private static final String SECRET_KEY = "NDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQy";
    private static final long EXPIRATION_MS = 3600000; 

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", EXPIRATION_MS);

        userDetails = new User("testuser", "password", new ArrayList<>());
    }

    @Test
    void generateToken_ShouldReturnValidJwtToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo(userDetails.getUsername());
    }

    @Test
    void extractClaim_ShouldReturnCorrectExpiration() {
        String token = jwtService.generateToken(userDetails);
        Long expiration = jwtService.extractClaim(token, claims -> claims.getExpiration().getTime());

        long expectedExpiration = System.currentTimeMillis() + EXPIRATION_MS;
        assertThat(expiration).isBetween(expectedExpiration - 1000, expectedExpiration + 1000);
    }

}