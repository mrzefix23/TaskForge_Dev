package com.taskforge.service;

import com.taskforge.model.User;
import com.taskforge.repository.UserRepository;
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
