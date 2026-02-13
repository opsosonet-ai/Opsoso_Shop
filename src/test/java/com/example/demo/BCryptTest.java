package com.example.demo;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class BCryptTest {
    public static void main(String[] args) {
        String hashedPassword = "$2a$12$0ogHKwuaDOkdUzFdewv9LuwKh8IYLOuQ0WlNpnTn6DR8eie59Hue.";
        String plainPassword = "admin123";
        
        boolean matches = BCrypt.checkpw(plainPassword, hashedPassword);
        System.out.println("Password matches: " + matches);
        
        // Generate a new hash for comparison
        String newHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
        System.out.println("New hash: " + newHash);
    }
}