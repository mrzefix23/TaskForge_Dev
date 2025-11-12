package com.taskforge.service;

import com.taskforge.dto.UserDto;
import com.taskforge.models.User;
import com.taskforge.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class UserServiceTest {

    private final UserRepository userRepo = Mockito.mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
    private final UserService userService = new UserService(userRepo, passwordEncoder);

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

    @Test
    void getUserById_shouldReturnUser() {
        User user = new User();
        user.setId(1L);
        Mockito.when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.getUserById(1L);
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
    }

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