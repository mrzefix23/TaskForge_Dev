package com.taskforge.service;

import com.taskforge.models.User;
import com.taskforge.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour le service CustomUserDetailsService.
 * Vérifie la logique de chargement des utilisateurs pour Spring Security,
 * en simulant les réponses du UserRepository.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    /**
     * Vérifie que la méthode loadUserByUsername retourne correctement un objet UserDetails
     * lorsque l'utilisateur existe dans la base de données.
     */
    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        User user = User.builder()
                .username("testuser")
                .password("password")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("password");
    }

    /**
     * Vérifie que la méthode loadUserByUsername lève une exception UsernameNotFoundException
     * lorsque l'utilisateur n'existe pas dans la base de données.
     */
    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("nonexistent"));
    }
}