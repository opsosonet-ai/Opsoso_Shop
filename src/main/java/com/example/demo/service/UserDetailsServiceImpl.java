package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Custom UserDetailsService Implementation
 * 
 * Được sử dụng bởi Spring Security để:
 * - Load thông tin user từ database
 * - Verify credentials khi login
 * - Lấy roles/authorities của user
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Load user by username
     * 
     * @param username - tên đăng nhập
     * @return UserDetails object cho Spring Security
     * @throws UsernameNotFoundException - nếu user không tìm thấy
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User không tìm thấy: " + username);
        }
        
        User user = userOpt.get();
        
        if (!user.isActive()) {
            throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa: " + username);
        }
        
        // Convert User entity to Spring Security UserDetails
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),  // BCrypt hash stored in database
            user.isActive(),     // enabled
            true,                // accountNonExpired
            true,                // credentialsNonExpired
            true,                // accountNonLocked
            getAuthorities(user)  // authorities/roles
        );
    }

    /**
     * Get authorities (roles) từ User entity
     * 
     * @param user - User entity
     * @return Collection of GrantedAuthority
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Thêm role chính
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }
        
        // Thêm quyền mặc định theo role
        switch (user.getRole()) {
            case ADMIN:
                // Admin có tất cả quyền
                authorities.add(new SimpleGrantedAuthority("PERMISSION_ALL"));
                break;
            case MANAGER:
                // Manager có quyền quản lý
                authorities.add(new SimpleGrantedAuthority("PERMISSION_APPROVE"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE"));
                break;
            case USER:
                // User thường có quyền cơ bản
                authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_CREATE"));
                break;
        }
        
        return authorities;
    }

    /**
     * Register user mới với password encoding
     * 
     * Không được gọi trực tiếp - dùng qua AuthService
     */
    public User registerUser(String username, String password, String fullName, 
                            String email, User.Role role) {
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        
        // Kiểm tra email đã tồn tại
        if (email != null && !email.isEmpty() && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        
        // Encode password trước khi lưu
        String encodedPassword = passwordEncoder.encode(password);
        
        User user = new User(username, encodedPassword, fullName, email, 
                            role != null ? role : User.Role.USER);
        user.setActive(true);
        return userRepository.save(user);
    }

    /**
     * Verify password
     * 
     * @param rawPassword - password người dùng nhập
     * @param encodedPassword - BCrypt hash từ database
     * @return true nếu match
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Change password với encoding
     * 
     * @param userId - User ID
     * @param oldPassword - mật khẩu cũ (plaintext)
     * @param newPassword - mật khẩu mới (plaintext)
     * @return true nếu thành công
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        // Validate userId parameter
        if (userId == null || userId <= 0) {
            return false;
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Verify old password
        if (!verifyPassword(oldPassword, user.getPassword())) {
            return false;
        }
        
        // Encode và set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}
