package com.taskforge.security;

import com.taskforge.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le filtre de sécurité JWT (JwtFilter).
 * Vérifie le comportement du filtre dans différents scénarios : absence de token,
 * token invalide, token valide, et utilisateur déjà authentifié.
 */
@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    /**
     * Nettoie le contexte de sécurité avant chaque test pour garantir l'isolation.
     */
    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Nettoie le contexte de sécurité après chaque test.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Vérifie que le filtre passe la main sans authentifier si l'en-tête Authorization est manquant.
     */
    @Test
    void shouldSkipFilter_WhenNoAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService);
    }

    /**
     * Vérifie que le filtre passe la main sans authentifier si l'en-tête Authorization ne commence pas par "Bearer ".
     */
    @Test
    void shouldSkipFilter_WhenInvalidAuthorizationHeader() throws ServletException, IOException {

        when(request.getHeader("Authorization")).thenReturn("Basic 12345");

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService);
    }

    /**
     * Vérifie que le filtre authentifie correctement l'utilisateur lorsqu'un token JWT valide est fourni.
     * Le contexte de sécurité doit être mis à jour avec les détails de l'utilisateur.
     */
    @Test
    void shouldAuthenticateUser_WhenValidToken() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "testUser";
        UserDetails userDetails = new User(username, "password", new ArrayList<>());

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, atLeast(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(username);

    }

    /**
     * Vérifie que le filtre ne tente pas de ré-authentifier si l'utilisateur est déjà authentifié dans le contexte.
     */
    @Test
    void shouldSkipFilter_WhenUserAlreadyAuthenticated() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "testuser";
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        var authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(username);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, atLeast(1)).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    /**
     * Vérifie que le filtre gère correctement les exceptions (ex: token invalide)
     * en renvoyant un statut 401 Unauthorized et en arrêtant la chaîne de filtres.
     */
    @Test
    void shouldHandleException_WhenTokenInvalid() throws ServletException, IOException {
        String token = "invalid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Invalid JWT"));

        jwtFilter.doFilterInternal(request, response, filterChain);
        
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}