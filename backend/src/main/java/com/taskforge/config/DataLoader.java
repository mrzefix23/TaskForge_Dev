package com.taskforge.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.taskforge.models.User;
import com.taskforge.repositories.UserRepository;

@Configuration
public class DataLoader {

    /**
     * Initialise la base de données avec des données de test au démarrage de l'application.
     * Cette méthode vérifie si un utilisateur de test existe et le crée si nécessaire.
     *
     * @param userRepository Le dépôt pour accéder aux données des utilisateurs.
     * @param passwordEncoder L'encodeur pour hacher le mot de passe de l'utilisateur.
     * @return Un CommandLineRunner qui exécute la logique d'initialisation.
     */
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Vérifier si l'utilisateur de test existe déjà
            if (!userRepository.existsByUsername("user")) {
                User testUser = User.builder()
                    .username("user")
                    .email("user@test.com")
                    .password(passwordEncoder.encode("secure_password"))
                    .build();
                
                userRepository.save(testUser);
                System.out.println("✓ Utilisateur de test créé : user / secure_password");
            }
        };
    }
}