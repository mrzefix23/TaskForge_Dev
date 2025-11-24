package com.taskforge.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.taskforge.models.User;
import com.taskforge.repositories.UserRepository;

@Configuration
public class DataLoader {

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