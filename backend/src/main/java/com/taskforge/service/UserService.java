package com.taskforge.service;

import com.taskforge.models.User;
import com.taskforge.repositories.UserRepository;
import com.taskforge.dto.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

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

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }
}
