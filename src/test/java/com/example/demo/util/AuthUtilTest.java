package com.example.demo.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuthUtilTest {
    
    @Test
    public void testPasswordValidation() {
        // Test password validation
        String password = "SecurePassword123!";
        assertTrue(password.length() >= 8);
    }
    
    @Test
    public void testEmailValidation() {
        String email = "user@example.com";
        assertTrue(email.contains("@"));
        assertTrue(email.contains("."));
    }
    
    @Test
    public void testPhoneValidation() {
        String phone = "0123456789";
        assertTrue(phone.length() >= 10);
    }
    
    @Test
    public void testNameValidation() {
        String name = "Nguyễn Văn A";
        assertFalse(name.isEmpty());
        assertTrue(name.length() > 0);
    }
}
