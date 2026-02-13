package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing User operations
 * Handles password encoding for security
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Get user by ID
     */
    public Optional<User> getUserById(Long id) {
        // Validate id parameter
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }
    
    /**
     * Create new user with password encoding
     */
    public User createUser(User user) {
        // Validate username doesn't exist
        if (user.getUsername() != null && userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        // Encode password before saving
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Update user information
     */
    public Optional<User> updateUser(Long id, User userDetails) {
        // Validate id parameter
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        
        Optional<User> optionalUser = userRepository.findById(id);
        
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            // Update basic information
            if (userDetails.getFullName() != null) {
                user.setFullName(userDetails.getFullName());
            }
            if (userDetails.getEmail() != null) {
                user.setEmail(userDetails.getEmail());
            }
            
            // Update password if provided and not empty
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                // Check if the new password is already encoded (starts with $2a$, $2b$, $2x$, $2y$)
                if (isPasswordEncoded(userDetails.getPassword())) {
                    user.setPassword(userDetails.getPassword());
                } else {
                    user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                }
            }
            
            // Validate user before saving
            if (user != null) {
                return Optional.of(userRepository.save(user));
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Update only password
     */
    public Optional<User> updatePassword(Long id, String newPassword) {
        // Validate id parameter
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        
        Optional<User> optionalUser = userRepository.findById(id);
        
        if (optionalUser.isPresent()) {
            if (newPassword == null || newPassword.isEmpty()) {
                throw new IllegalArgumentException("New password cannot be empty");
            }
            
            User user = optionalUser.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            return Optional.of(userRepository.save(user));
        }
        
        return Optional.empty();
    }
    
    /**
     * Delete user
     */
    public boolean deleteUser(Long id) {
        // Validate id parameter
        if (id == null || id <= 0) {
            return false;
        }
        
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * Check if password is already BCrypt encoded
     */
    private boolean isPasswordEncoded(String password) {
        return password != null && password.matches("^\\$2[aby]\\$.{56}$");
    }
    
    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
