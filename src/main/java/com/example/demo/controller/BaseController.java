package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

public abstract class BaseController {
    
    protected static final Logger log = LoggerFactory.getLogger(BaseController.class);
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Kiểm tra xem user đã đăng nhập hay chưa (Spring Security)
     */
    protected boolean isLoggedIn(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
            && !authentication.getName().equals("anonymousUser");
    }
    
    /**
     * Lấy thông tin user hiện tại từ Spring Security
     */
    public User getCurrentUser(HttpSession session) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                    && !authentication.getName().equals("anonymousUser")) {
                String username = authentication.getName();
                Optional<User> user = userRepository.findByUsername(username);
                
                if (user.isPresent()) {
                    return user.get();
                }
            }
        } catch (Exception e) {
            log.error("❌ Error getting current user: " + e.getMessage());
            log.error("Get current user error:", e);
        }
        return null;
    }
    
    /**
     * Kiểm tra quyền ADMIN
     */
    protected boolean isAdmin(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null && User.Role.ADMIN.equals(user.getRole());
    }
    
    /**
     * Kiểm tra quyền USER hoặc ADMIN
     */
    protected boolean isUserOrAdmin(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null && (User.Role.USER.equals(user.getRole()) || User.Role.ADMIN.equals(user.getRole()));
    }
    
    /**
     * Lấy tên hiển thị của user hiện tại
     */
    protected String getCurrentUserName(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null ? user.getFullName() : "Anonymous";
    }
    
    /**
     * Lấy role của user hiện tại
     */
    protected String getCurrentUserRole(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null ? user.getRole().toString() : "GUEST";
    }
    
    /**
     * Lấy username (tên đăng nhập) của user hiện tại
     */
    protected String getCurrentUsername(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !authentication.getName().equals("anonymousUser")) {
            return authentication.getName();
        }
        return "Anonymous";
    }
}