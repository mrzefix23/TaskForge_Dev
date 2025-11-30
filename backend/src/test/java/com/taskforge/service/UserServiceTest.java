package com.taskforge.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.taskforge.dto.UserDto;
import com.taskforge.models.User;
import com.taskforge.repositories.UserRepository;

/**
 * Tests unitaires pour le service de gestion des utilisateurs (UserService).
 * Vérifie la logique de création, de validation et de récupération des utilisateurs,
 * en utilisant des mocks pour le dépôt de données et l'encodeur de mots de passe.
 */
class UserServiceTest {

    private final UserRepository userRepo = Mockito.mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
    private final UserService userService = new UserService(userRepo, passwordEncoder);

    /**
     * Vérifie que la création d'un utilisateur réussit avec des données valides.
     * Le service doit encoder le mot de passe et sauvegarder l'entité.
     */
    @Test
    void createUser_shouldSaveUser() {
        UserDto dto = new UserDto();
        dto.setUsername("testuser");
        dto.setEmail("test@mail.com");
        dto.setPassword("secret_password");

        Mockito.when(passwordEncoder.encode("secret_password")).thenReturn("encoded");
        Mockito.when(userRepo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User user = userService.createUser(dto);

        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@mail.com");
        assertThat(user.getPassword()).isEqualTo("encoded");
    }

    /**
     * Vérifie qu'un utilisateur existant peut être récupéré correctement par son identifiant.
     */
    @Test
    void getUserById_shouldReturnUser() {
        User user = new User();
        user.setId(1L);
        Mockito.when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.getUserById(1L);
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
    }

    /**
     * Vérifie que la création échoue si le nom d'utilisateur est déjà pris.
     * Doit lever une DataIntegrityViolationException.
     */
    @Test
    void createUser_shouldThrowIfUsernameExists() {
        UserDto dto = new UserDto();
        dto.setUsername("john_doe");
        dto.setEmail("john@gmail.com");
        dto.setPassword("secret_password");

        Mockito.when(userRepo.existsByUsername("john_doe")).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            userService.createUser(dto);
        });
    }

    /**
     * Vérifie que la création échoue si l'adresse email est déjà utilisée.
     * Doit lever une DataIntegrityViolationException.
     */
    @Test
    void createUser_shouldThrowIfEmailExists() {
        UserDto dto = new UserDto();
        dto.setUsername("bob");
        dto.setEmail("bob@gmail.com");
        dto.setPassword("secret_password");

        Mockito.when(userRepo.existsByUsername("bob")).thenReturn(false);
        Mockito.when(userRepo.existsByEmail("bob@gmail.com")).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            userService.createUser(dto);
        });
    }

    /**
     * Vérifie que la méthode retourne la liste complète des utilisateurs présents en base.
     */
    @Test
    void getAllUsers_shouldReturnAllUsers() {
        User user1 = new User();
        user1.setUsername("user1");
        User user2 = new User();
        user2.setUsername("user2");
        Mockito.when(userRepo.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users).contains(user1, user2);
    }

    /**
     * Vérifie que la récupération par ID retourne null si l'utilisateur n'existe pas.
     */
    @Test
    void getUserById_shouldReturnNullIfNotFound() {
        Mockito.when(userRepo.findById(2L)).thenReturn(Optional.empty());

        User found = userService.getUserById(2L);
        assertThat(found).isNull();
    }

    /**
     * Vérifie spécifiquement que le mot de passe fourni est bien encodé via le PasswordEncoder
     * avant d'être persisté.
     */
    @Test
    void createUser_shouldEncodePassword() {
        UserDto dto = new UserDto();
        dto.setUsername("secure_user");
        dto.setEmail("secure@mail.com");
        dto.setPassword("plain_password");

        Mockito.when(passwordEncoder.encode("plain_password")).thenReturn("encoded_password");
        Mockito.when(userRepo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User user = userService.createUser(dto);

        assertThat(user.getPassword()).isEqualTo("encoded_password");
        Mockito.verify(passwordEncoder).encode("plain_password");
    }

    /**
     * Vérifie que la création échoue si le mot de passe est trop court (faible sécurité).
     * Aucune sauvegarde ne doit être effectuée.
     */
    @Test
    void createUser_shouldNotSaveUserIfPasswordIsWeak() {
        UserDto dto = new UserDto();
        dto.setUsername("weak_user");
        dto.setEmail("weak@mail.com");
        dto.setPassword("123");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(dto);
        });

        Mockito.verify(userRepo, Mockito.never()).save(any(User.class));
    }

    /**
     * Vérifie que la création échoue si le format de l'email est invalide.
     * Aucune sauvegarde ne doit être effectuée.
     */
    @Test
    void createUser_shouldNotSaveUserIfEmailIsInvalid() {
        UserDto dto = new UserDto();
        dto.setUsername("invalid_email_user");
        dto.setEmail("invalid-email");
        dto.setPassword("secure_password");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(dto);
        });

        Mockito.verify(userRepo, Mockito.never()).save(any(User.class));
    }

    /**
     * Vérifie que la création échoue si le nom d'utilisateur est vide.
     */
    @Test
    void createUser_shouldNotSaveUserIfUsernameIsEmpty() {
        UserDto dto = new UserDto();
        dto.setUsername("");
        dto.setEmail("empty_username@mail.com");
        dto.setPassword("secure_password");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(dto);
        });

        Mockito.verify(userRepo, Mockito.never()).save(any(User.class));
    }

    /**
     * Vérifie que la création échoue si l'adresse email est vide.
     */
    @Test
    void createUser_shouldNotSaveUserIfEmailIsEmpty() {
        UserDto dto = new UserDto();
        dto.setUsername("empty_email_user");
        dto.setEmail("");
        dto.setPassword("secure_password");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(dto);
        });

        Mockito.verify(userRepo, Mockito.never()).save(any(User.class));
    }
}