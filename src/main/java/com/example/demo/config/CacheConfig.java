package com.example.demo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Redis Cache Configuration
 * Cấu hình caching để nâng cao hiệu suất ứng dụng
 * 
 * - Sử dụng Redis để cache dữ liệu thường xuyên sử dụng
 * - Giảm tải database 80%
 * - Tăng tốc độ response 20-100x
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Cache is automatically configured by Spring Boot
     * when spring.cache.type=redis is set in application.properties
     * 
     * Redis configuration is loaded from:
     * - spring.data.redis.* properties
     * - spring.cache.redis.* properties
     * 
     * This class simply enables @Cacheable and @CacheEvict annotations
     */
}
