package com.example.demo.service;

import com.example.demo.config.DatabaseConnectionFailureAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

@DependsOnDatabaseInitialization
@Service
public class DatabaseHealthService {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthService.class);
    
    private final DataSource dataSource;
    private volatile boolean databaseAvailable = false;
    private final DatabaseConnectionFailureAnalyzer analyzer;
    
    public DatabaseHealthService(DataSource dataSource, DatabaseConnectionFailureAnalyzer analyzer) {
        this.dataSource = dataSource;
        this.analyzer = analyzer;
        checkDatabaseConnection();
    }
    
    /**
     * Kiểm tra xem database có sẵn sàng không
     */
    public boolean isDatabaseAvailable() {
        return databaseAvailable;
    }
    
    /**
     * Kiểm tra kết nối database
     */
    public boolean checkDatabaseConnection() {
        try {
            if (dataSource != null) {
                try (Connection conn = dataSource.getConnection()) {
                    boolean valid = conn.isValid(3);
                    databaseAvailable = valid;
                    if (valid) {
                        log.info("✅ Database connection successful!");
                    } else {
                        log.warn("⚠️  Database connection is not valid.");
                    }
                    return valid;
                }
            }
        } catch (Exception e) {
            log.error("❌ Database connection failed: " + e.getMessage());
            analyzer.checkDatabaseConnection(); // Hiển thị thông báo chi tiết
            databaseAvailable = false;
        }
        return false;
    }
    
    /**
     * Đánh dấu database đã sẵn sàng sau khi cấu hình
     */
    public void markDatabaseAsAvailable() {
        this.databaseAvailable = true;
    }
}
