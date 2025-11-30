package com.taskforge.service;

import com.taskforge.models.User;
import com.taskforge.repositories.UserRepository;
import com.taskforge.dto.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

/**
 * Service gérant la logique métier liée aux utilisateurs.
 * Permet de créer de nouveaux utilisateurs avec validation et de récupérer les informations des utilisateurs existants.
 */
@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crée un nouvel utilisateur dans le système.
     * Effectue plusieurs validations : champs obligatoires, format de l'email, longueur du mot de passe,
     * et unicité du nom d'utilisateur et de l'email.
     * Le mot de passe est haché avant d'être stocké.
     *
     * @param userDto Les informations de l'utilisateur à créer (username, email, password).
     * @return L'utilisateur créé et sauvegardé.
     * @throws IllegalArgumentException        Si les données sont invalides (null, format incorrect, mot de passe trop court).
     * @throws DataIntegrityViolationException Si le nom d'utilisateur ou l'email existe déjà.
     */
    public User createUser(UserDto userDto) {
        if(userDto.getUsername() == null || userDto.getUsername().isEmpty() || userDto.getEmail() == null || userDto.getPassword() == null) {
            throw new IllegalArgumentException("Username, email, and password must not be null");
        }
        if(userDto.getEmail().isEmpty() || !userDto.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if(userDto.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if(userRepo.existsByUsername(userDto.getUsername())) {
            throw new DataIntegrityViolationException("Username already exists");
        }
        if(userRepo.existsByEmail(userDto.getEmail())) {
            throw new DataIntegrityViolationException("Email already exists");
        }
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userRepo.save(user);
    }

    /**
     * Récupère la liste de tous les utilisateurs enregistrés.
     *
     * @return Une liste contenant tous les utilisateurs.
     */
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    /**
     * Récupère un utilisateur par son identifiant unique.
     *
     * @param id L'identifiant de l'utilisateur.
     * @return L'utilisateur trouvé, ou null s'il n'existe pas.
     */
    public User getUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }
}
