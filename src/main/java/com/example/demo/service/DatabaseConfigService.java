package com.example.demo.service;

import com.example.demo.entity.DatabaseConfig;
import com.example.demo.repository.DatabaseConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;

@Service
public class DatabaseConfigService {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfigService.class);
    
    @Autowired
    private DatabaseConfigRepository repository;
    
    @Autowired
    private ApplicationPropertiesService propertiesService;
    
    public DatabaseConfig getConfig() {
        // Ưu tiên lấy từ database, nếu không có thì lấy từ application.properties
        try {
            return repository.findAll().stream()
                    .findFirst()
                    .orElse(propertiesService.getCurrentConfig());
        } catch (Exception e) {
            // Nếu không kết nối được database, lấy từ application.properties
            log.warn("⚠️  Cannot get config from database, using application.properties");
            return propertiesService.getCurrentConfig();
        }
    }
    
    public DatabaseConfig saveConfig(DatabaseConfig config) {
        // Validate config parameter
        if (config == null) {
            throw new IllegalArgumentException("DatabaseConfig không thể null");
        }
        
        // Backup application.properties trước khi thay đổi
        propertiesService.backupApplicationProperties();
        
        // Cập nhật vào application.properties
        boolean updated = propertiesService.updateApplicationProperties(config);
        
        if (!updated) {
            throw new RuntimeException("Không thể cập nhật file application.properties");
        }
        
        // Thử lưu vào database nếu có thể kết nối
        try {
            repository.deleteAll();
            return repository.save(config);
        } catch (Exception e) {
            log.warn("⚠️  Cannot save to database, config saved to application.properties only");
            // Trả về config đã lưu trong file
            return config;
        }
    }
    
    public boolean testConnection(DatabaseConfig config) {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            String url = config.getJdbcUrl();
            try (Connection conn = DriverManager.getConnection(url, config.getUsername(), config.getPassword())) {
                return conn.isValid(5); // 5 seconds timeout
            }
        } catch (Exception e) {
            log.error("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
