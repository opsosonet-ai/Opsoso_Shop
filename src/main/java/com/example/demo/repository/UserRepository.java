package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Tìm user theo username
    Optional<User> findByUsername(String username);
    
    // Tìm user theo email
    Optional<User> findByEmail(String email);
    
    // Kiểm tra username đã tồn tại chưa
    boolean existsByUsername(String username);
    
    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);
    
    // Tìm user theo username và password
    Optional<User> findByUsernameAndPassword(String username, String password);
    
    // Tìm tất cả user đang hoạt động
    @Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.createdAt DESC")
    java.util.List<User> findAllActiveUsers();
    
    // Tìm tất cả user (bao gồm cả active và inactive) sắp xếp theo ngày tạo mới nhất
    java.util.List<User> findAllByOrderByCreatedAtDesc();
}