package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class PasswordEncoderTest {

    @Test
    public void testPasswordEncoding() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String storedHash = "$2a$12$0ogHKwuaDOkdUzFdewv9LuwKh8IYLOuQ0WlNpnTn6DR8eie59Hue.";
        String plainPassword = "admin123";
        
        boolean matches = encoder.matches(plainPassword, storedHash);
        System.out.println("Password matches: " + matches);
        
        // Generate a new hash for comparison
        String newHash = encoder.encode(plainPassword);
        System.out.println("New hash: " + newHash);
        
        assertTrue(matches, "Password should match the stored hash");
    }
}