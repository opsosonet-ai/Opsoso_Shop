package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Đăng nhập người dùng
     */
    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsernameAndPassword(username, password);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isActive()) {
                // Cập nhật thời gian đăng nhập cuối
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                return user;
            }
        }
        return null;
    }
    
    /**
     * Kiểm tra quyền truy cập
     */
    public boolean hasPermission(User user, String action) {
        if (user == null || !user.isActive()) {
            return false;
        }
        
        return switch (user.getRole()) {
            case ADMIN -> true; // Admin có tất cả quyền
            case MANAGER -> !action.equals("DELETE_USER") && !action.equals("MANAGE_USERS"); // Manager không thể xóa user hoặc quản lý user
            case USER -> action.equals("VIEW") || action.equals("CREATE") || action.equals("UPDATE"); // User chỉ có quyền xem, tạo, sửa
        };
    }
    
    /**
     * Đăng ký người dùng mới
     */
    public User register(String username, String password, String fullName, String email, User.Role role) {
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        
        // Kiểm tra email đã tồn tại
        if (email != null && !email.isEmpty() && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        
        // Mã hóa mật khẩu trước khi lưu
        String encodedPassword = passwordEncoder.encode(password);
        
        User user = new User(username, encodedPassword, fullName, email, role != null ? role : User.Role.USER);
        user.setActive(true);  // Bật tài khoản mặc định
        return userRepository.save(user);
    }
    
    /**
     * Thay đổi mật khẩu
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        // Validate userId parameter
        if (userId == null || userId <= 0) {
            return false;
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // So sánh mật khẩu cũ với BCrypt hash
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                // Mã hóa mật khẩu mới
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Vô hiệu hóa/Kích hoạt user
     */
    public void toggleUserStatus(Long userId) {
        // Validate userId parameter
        if (userId == null || userId <= 0) {
            return;
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(!user.isActive());
            userRepository.save(user);
        }
    }
    
    /**
     * Xóa user khỏi hệ thống (xóa thật sự khỏi database)
     */
    public boolean deleteUser(Long userId) {
        // Validate userId parameter
        if (userId == null || userId <= 0) {
            return false;
        }
        
        try {
            if (userRepository.existsById(userId)) {
                userRepository.deleteById(userId);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Không thể xóa người dùng: " + e.getMessage());
        }
    }
}