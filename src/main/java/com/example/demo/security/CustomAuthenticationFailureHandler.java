package com.example.demo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom Authentication Failure Handler
 * 
 * Xử lý việc log lỗi và redirect khi đăng nhập thất bại
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                       HttpServletResponse response,
                                       AuthenticationException exception) throws IOException, ServletException {
        
        log.error("❌ Authentication FAILED!");
        log.error("   Error: " + exception.getMessage());
        log.error("   Exception: " + exception.getClass().getSimpleName());
        
        String username = request.getParameter("username");
        log.error("   Attempted username: " + username);
        
        // Log the full stack trace
        log.error("Authentication exception details:", exception);
        
        // Redirect back to login page with error
        response.sendRedirect("/auth/login?error=invalid");
    }
}
