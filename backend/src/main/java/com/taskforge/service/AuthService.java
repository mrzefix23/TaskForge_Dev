package com.taskforge.service;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.taskforge.dto.RegisterRequest;
import com.taskforge.dto.LoginRequest;
import com.taskforge.repositories.UserRepository;
import com.taskforge.models.User;
import com.taskforge.dto.AuthResponse;
import com.taskforge.exceptions.UsernameAlreadyExists;
import com.taskforge.exceptions.EmailAlreadyExists;
import com.taskforge.exceptions.InvalidCredentialsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;


/**
 * Service responsable de la gestion de l'authentification et de l'inscription des utilisateurs.
 * Il gère la création de nouveaux comptes, la validation des identifiants et la génération de jetons JWT.
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Enregistre un nouvel utilisateur dans le système.
     * Vérifie l'unicité du nom d'utilisateur et de l'email, valide le mot de passe,
     * hache le mot de passe et sauvegarde l'utilisateur avant de générer un token JWT.
     *
     * @param request Les informations d'inscription fournies par l'utilisateur.
     * @return Une réponse contenant les détails de l'utilisateur et le token JWT.
     * @throws UsernameAlreadyExists Si le nom d'utilisateur est déjà pris.
     * @throws EmailAlreadyExists Si l'email est déjà utilisé.
     * @throws ResponseStatusException Si le mot de passe ne respecte pas les critères de sécurité.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExists("Ce nom d'utilisateur existe déjà");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExists("Cet email existe déjà");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

        User savedUser = userRepository.save(user); // Save the user in the database

        String token = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
            .username(savedUser.getUsername())
            .email(savedUser.getEmail())
            .token(token)
            .build();
    }   

    /**
     * Authentifie un utilisateur avec ses identifiants.
     * Utilise l'AuthenticationManager pour valider les crédentials.
     * Si l'authentification réussit, un token JWT est généré.
     *
     * @param request Les informations de connexion (username et mot de passe).
     * @return Une réponse contenant les détails de l'utilisateur et le token JWT.
     * @throws InvalidCredentialsException Si l'authentification échoue (mauvais username ou mot de passe).
     */
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Le nom d'utilisateur ou le mot de passe est incorrect");
        }

        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new InvalidCredentialsException("Le nom d'utilisateur ou le mot de passe est incorrect"));
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .token(token)
            .build();
    }   
}
