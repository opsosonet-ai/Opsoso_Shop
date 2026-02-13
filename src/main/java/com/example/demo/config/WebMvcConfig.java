package com.example.demo.config;

import com.example.demo.interceptor.DatabaseHealthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration
 * 
 * Cấu hình:
 * - DatabaseHealthInterceptor: Kiểm tra trạng thái database
 * - AuthInterceptor: DISABLED - Spring Security đã thay thế chức năng này
 * 
 * Lý do vô hiệu hóa AuthInterceptor:
 * - Spring Security xử lý authentication/authorization
 * - AuthInterceptor gây xung đột và vòng lặp redirect
 * - Spring Security filter chain được thực thi trước interceptor
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private DatabaseHealthInterceptor databaseHealthInterceptor;
    
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Interceptor kiểm tra database health (chạy đầu tiên)
        // Điều này cần thiết để cho phép settings page truy cập khi DB không khả dụng
        if (databaseHealthInterceptor != null) {
            registry.addInterceptor(databaseHealthInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(
                        "/",
                        "/auth/**",
                        "/css/**",
                        "/js/**", 
                        "/images/**",
                        "/static/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error",
                        "/settings/**"  // Settings không bị can thiệp
                    )
                    .order(1); // Order 1: Chạy đầu tiên
        }
        
        // NOTE: AuthInterceptor DISABLED vì Spring Security đã xử lý
        // Các interceptor cũ đang gây xung đột với Spring Security filter chain
        // Spring Security sẽ xử lý tất cả authentication và authorization
    }
}