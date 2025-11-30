package com.taskforge.models;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.Collections;

/**
 * Entité représentant un utilisateur de l'application.
 * Implémente l'interface UserDetails de Spring Security pour l'authentification et l'autorisation.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Schema(description = "Identifiant unique de l'utilisateur", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Nom d'utilisateur unique", example = "johndoe")
    @Column(nullable = false, unique = true)
    private String username;

    @Schema(description = "Mot de passe de l'utilisateur", example = "password123")
    @Column(nullable = false)
    private String password;

    @Schema(description = "Adresse email unique de l'utilisateur", example = "johndoe@example.com")
    @Column(nullable = false, unique = true)
    private String email;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // No roles or authorities for now
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
