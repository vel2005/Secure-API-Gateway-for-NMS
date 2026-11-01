package com.example.secure_api_gateway.service;

import com.example.secure_api_gateway.config.Role;
import com.example.secure_api_gateway.entity.User;
import com.example.secure_api_gateway.exception.ResourceNotFoundException;
import com.example.secure_api_gateway.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        user.setPassword(
                passwordEncoder.encode(user.getPassword()));
        user.setRole("VIEWER");

        // Enable the account
        user.setEnabled(true);

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUserRole(Long userId, String newRole) {
        if (!Role.ADMIN.equals(newRole) && !Role.ENGINEER.equals(newRole) && !Role.VIEWER.equals(newRole)) {
            throw new IllegalArgumentException("Invalid role: " + newRole + ". Role must be ADMIN, ENGINEER, or VIEWER.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setRole(newRole);
        return userRepository.save(user);
    }
}
