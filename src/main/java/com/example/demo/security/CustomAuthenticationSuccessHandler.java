package com.example.demo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom Authentication Success Handler
 * 
 * Xử lý việc redirect sau khi đăng nhập thành công
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        log.info("✅ Authentication SUCCESS!");
        log.info("   Username: " + authentication.getName());
        log.info("   Principal: " + authentication.getPrincipal());
        log.info("   Authorities: " + authentication.getAuthorities());
        log.info("   Authenticated: " + authentication.isAuthenticated());
        
        // Explicitly store authentication in session to ensure proper session management
        // This is required for session concurrency control to work correctly
        HttpSession session = request.getSession(true);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        
        log.info("✅ Authentication stored in session: " + session.getId());
        
        // Redirect to dashboard
        response.sendRedirect("/dashboard");
    }
}
