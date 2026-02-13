package com.example.demo.service;

import com.example.demo.entity.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Service
public class DatabaseCreationService {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseCreationService.class);
    
    /**
     * Kiểm tra xem database đã tồn tại hay chưa
     */
    public boolean checkDatabaseExists(DatabaseConfig config) {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            // Kết nối tới MariaDB mà không chỉ định database cụ thể
            String url = buildUrl(config, "");
            log.info("📝 Checking database existence with URL: " + url);
            log.info("   Using username: " + config.getUsername());
            
            try (Connection conn = DriverManager.getConnection(url, config.getUsername(), config.getPassword())) {
                log.info("✅ Connected successfully!");
                try (Statement stmt = conn.createStatement()) {
                    // Kiểm tra xem database oss có tồn tại không
                    String sql = "SHOW DATABASES LIKE '" + config.getDatabase() + "'";
                    log.info("   Executing: " + sql);
                    try (ResultSet rs = stmt.executeQuery(sql)) {
                        if (rs.next()) {
                            log.info("✅ Database '" + config.getDatabase() + "' exists.");
                            return true;
                        } else {
                            log.warn("⚠️  Database '" + config.getDatabase() + "' does not exist.");
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Error checking database: " + e.getMessage());
            log.error("   Error code: " + e.getClass().getSimpleName());
            log.error("Database check error:", e);
            return false;
        }
    }
    
    /**
     * Tạo database nếu chưa tồn tại
     */
    public boolean createDatabase(DatabaseConfig config) {
        try {
            // Kiểm tra xem database đã tồn tại không
            if (checkDatabaseExists(config)) {
                log.info("✅ Database already exists, skipping creation.");
                return true;
            }
            
            log.info("📝 Creating database '" + config.getDatabase() + "'...");
            Class.forName("org.mariadb.jdbc.Driver");
            
            String url = buildUrl(config, "");
            log.info("📝 Connection URL: " + url);
            log.info("   Username: " + config.getUsername());
            log.info("   Password length: " + (config.getPassword() != null ? config.getPassword().length() : 0));
            
            try (Connection conn = DriverManager.getConnection(url, config.getUsername(), config.getPassword())) {
                log.info("✅ Connected to MariaDB server!");
                try (Statement stmt = conn.createStatement()) {
                    // Tạo database với charset utf8mb4
                    String createDbSql = "CREATE DATABASE IF NOT EXISTS `" + config.getDatabase() + "` " +
                            "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
                    log.info("📝 Executing SQL: " + createDbSql);
                    stmt.executeUpdate(createDbSql);
                    log.info("✅ Database '" + config.getDatabase() + "' created successfully!");
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("❌ Error creating database: " + e.getMessage());
            if (e instanceof java.sql.SQLException sqlEx) {
                log.error("   SQL State: " + sqlEx.getSQLState());
                log.error("   Error Code: " + sqlEx.getErrorCode());
            }
            log.error("   Class: " + e.getClass().getSimpleName());
            log.error("Database creation error:", e);
            return false;
        }
    }
    
    /**
     * Xây dựng JDBC URL
     */
    private String buildUrl(DatabaseConfig config, String database) {
        StringBuilder url = new StringBuilder();
        url.append("jdbc:mariadb://");
        url.append(config.getHost());
        url.append(":");
        url.append(config.getPort());
        if (!database.isEmpty()) {
            url.append("/").append(database);
        }
        url.append("?useUnicode=true");
        url.append("&characterEncoding=utf8mb4");
        url.append("&useSSL=false");
        url.append("&allowPublicKeyRetrieval=true");
        url.append("&serverTimezone=UTC");
        url.append("&autoReconnect=true");
        url.append("&maxReconnects=3");
        return url.toString();
    }
}
