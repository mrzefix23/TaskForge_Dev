package com.taskforge.service;

import com.taskforge.dto.AuthResponse;
import com.taskforge.dto.LoginRequest;
import com.taskforge.dto.RegisterRequest;
import com.taskforge.exceptions.EmailAlreadyExists;
import com.taskforge.exceptions.InvalidCredentialsException;
import com.taskforge.exceptions.UsernameAlreadyExists;
import com.taskforge.models.User;
import com.taskforge.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldReturnAuthResponse_WhenSuccessful() {
        RegisterRequest request = new RegisterRequest("newUser", "new@test.com", "password123");
        User savedUser = User.builder().username("newUser").email("new@test.com").password("encodedPass").build();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getUsername()).isEqualTo("newUser");
        verify(userRepository).save(any(User.class)); 
    }

    @Test
    void register_ShouldThrowUsernameAlreadyExists_WhenUsernameTaken() {
        RegisterRequest request = new RegisterRequest("existingUser", "existing@test.com", "password123");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);
        assertThrows(UsernameAlreadyExists.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowEmailAlreadyExists_WhenEmailTaken() {
        RegisterRequest request = new RegisterRequest("newUser", "existing@test.com", "password123");
        
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
        assertThrows(EmailAlreadyExists.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowPasswordTooShort_WhenPasswordInvalid() {
        RegisterRequest request = new RegisterRequest("newUser", "new@test.com", "short");
        
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenSuccessful() {
        LoginRequest request = new LoginRequest("validUser", "validPass");
        User user = User.builder().username("validUser").email("valid@test.com").password("encodedPass").build();
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(request);
        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getUsername()).isEqualTo("validUser");
    }

    @Test
    void login_ShouldThrowInvalidCredentialsException_WhenAuthenticationFails() {
        LoginRequest request = new LoginRequest("invalidUser", "invalidPass");
        doThrow(new BadCredentialsException("Bad credentials"))
            .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_ShouldThrowInvalidCredentialsException_WhenUserNotFound() {
        LoginRequest request = new LoginRequest("nonExistentUser", "somePass");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}