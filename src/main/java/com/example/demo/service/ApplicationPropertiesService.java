package com.example.demo.service;

import com.example.demo.entity.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

@Service
public class ApplicationPropertiesService {
    
    private static final Logger log = LoggerFactory.getLogger(ApplicationPropertiesService.class);
    
    @Value("${spring.datasource.url:}")
    private String currentUrl;
    
    @Value("${spring.datasource.username:}")
    private String currentUsername;
    
    @Value("${spring.datasource.password:}")
    private String currentPassword;
    
    /**
     * Lấy cấu hình database hiện tại từ application.properties
     */
    public DatabaseConfig getCurrentConfig() {
        DatabaseConfig config = new DatabaseConfig();
        
        // Parse JDBC URL to extract host, port, database
        if (currentUrl != null && currentUrl.startsWith("jdbc:mariadb://")) {
            try {
                String urlPart = currentUrl.substring("jdbc:mariadb://".length());
                String[] parts = urlPart.split("/");
                
                if (parts.length > 0) {
                    String hostPort = parts[0];
                    String[] hostPortSplit = hostPort.split(":");
                    
                    config.setHost(hostPortSplit[0]);
                    if (hostPortSplit.length > 1) {
                        config.setPort(Integer.parseInt(hostPortSplit[1]));
                    }
                }
                
                if (parts.length > 1) {
                    String dbPart = parts[1];
                    int questionMarkIndex = dbPart.indexOf('?');
                    if (questionMarkIndex > 0) {
                        config.setDatabase(dbPart.substring(0, questionMarkIndex));
                    } else {
                        config.setDatabase(dbPart);
                    }
                }
            } catch (Exception e) {
                log.error("Error parsing JDBC URL: " + e.getMessage());
            }
        }
        
        config.setUsername(currentUsername);
        config.setPassword(currentPassword);
        
        return config;
    }
    
    /**
     * Cập nhật application.properties với cấu hình mới
     * Hỗ trợ cả khi chạy từ source và từ JAR file
     */
    public boolean updateApplicationProperties(DatabaseConfig config) {
        try {
            // Xác định đường dẫn file properties
            File propertiesFile = getPropertiesFile();
            
            if (propertiesFile == null) {
                log.error("❌ Không thể xác định vị trí application.properties");
                return false;
            }
            
            // Đọc nội dung hiện tại
            String content;
            if (propertiesFile.exists()) {
                content = new String(Files.readAllBytes(propertiesFile.toPath()));
            } else {
                // Nếu file external chưa tồn tại, tạo mới từ template
                content = createDefaultPropertiesContent();
                // Tạo thư mục nếu chưa có
                File parentDir = propertiesFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
            }
            
            // Tạo JDBC URL mới
            String newJdbcUrl = config.getJdbcUrl();
            
            // Thay thế các giá trị
            content = replaceProperty(content, "spring.datasource.url", newJdbcUrl);
            content = replaceProperty(content, "spring.datasource.username", config.getUsername());
            
            // ✅ Luôn cập nhật password nếu nó được cung cấp (không null)
            // Bỏ kiểm tra isEmpty() để cho phép cập nhật password thậm chí nếu user nhập password ngắn
            if (config.getPassword() != null) {
                log.info("✅ Password được cập nhật từ form: " + config.getPassword().replaceAll(".", "*"));
                content = replaceProperty(content, "spring.datasource.password", config.getPassword());
            } else {
                log.warn("⚠️  Password không được cập nhật (giá trị null từ form)");
            }
            
            // Ghi lại file
            Files.write(propertiesFile.toPath(), content.getBytes());
            
            log.info("✅ Đã cập nhật application.properties thành công!");
            log.info("   Đường dẫn: " + propertiesFile.getAbsolutePath());
            log.info("   Nội dung cập nhật:");
            log.info("   - URL: " + newJdbcUrl);
            log.info("   - Username: " + config.getUsername());
            log.info("   - Password: " + (config.getPassword() != null ? "✅ Được cập nhật" : "❌ Không được cập nhật"));
            log.warn("⚠️  Cần khởi động lại ứng dụng để áp dụng cấu hình mới.");
            
            return true;
            
        } catch (Exception e) {
            log.error("❌ Lỗi khi cập nhật application.properties: " + e.getMessage());
            log.error("Update properties error:", e);
            return false;
        }
    }
    
    /**
     * Lấy file properties - ưu tiên file external, fallback sang source
     */
    private File getPropertiesFile() {
        // 1. Kiểm tra thư mục hiện tại (nơi chạy JAR) - ./config/application.properties
        File configDir = new File("config");
        File externalConfig = new File(configDir, "application.properties");
        if (externalConfig.exists() || configDir.exists() || configDir.mkdirs()) {
            return externalConfig;
        }
        
        // 2. Kiểm tra thư mục hiện tại - ./application.properties
        File currentDirConfig = new File("application.properties");
        if (currentDirConfig.exists() || currentDirConfig.getParentFile() == null || currentDirConfig.getParentFile().exists()) {
            return currentDirConfig;
        }
        
        // 3. Fallback: source code location (chỉ hoạt động khi dev)
        File sourceConfig = new File("src/main/resources/application.properties");
        if (sourceConfig.exists()) {
            return sourceConfig;
        }
        
        // 4. Mặc định: tạo mới trong thư mục config
        configDir.mkdirs();
        return externalConfig;
    }
    
    /**
     * Tạo nội dung mặc định cho application.properties
     */
    private String createDefaultPropertiesContent() {
        return """
               # Server Configuration
               server.address=0.0.0.0
               server.port=8080
               
               # MariaDB Database Configuration
               spring.datasource.url=jdbc:mariadb://localhost:3306/oss?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true
               spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
               spring.datasource.username=root
               spring.datasource.password=Vph38302@
               
               # Connection Pool
               spring.datasource.hikari.minimum-idle=0
               spring.datasource.hikari.maximum-pool-size=10
               spring.datasource.hikari.connection-timeout=3000
               spring.datasource.hikari.initialization-fail-timeout=-1
               
               # JPA Configuration
               spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect
               spring.jpa.hibernate.ddl-auto=update
               spring.jpa.show-sql=false
               spring.jpa.properties.hibernate.format_sql=true
               spring.jpa.properties.hibernate.use_sql_comments=true
               spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false
               spring.jpa.properties.hibernate.hbm2ddl.auto=none
               
               # Allow app to start even if database is not available
               spring.sql.init.continue-on-error=true
               spring.data.jpa.repositories.bootstrap-mode=deferred
               """;
    }
    
    /**
     * Thay thế giá trị của một property trong nội dung file
     */
    private String replaceProperty(String content, String propertyKey, String newValue) {
        // Pattern: propertyKey=oldValue
        String pattern = "(?m)^" + propertyKey.replace(".", "\\.") + "=.*$";
        String replacement = propertyKey + "=" + newValue;
        return content.replaceAll(pattern, replacement);
    }
    
    /**
     * Backup application.properties trước khi thay đổi
     */
    public boolean backupApplicationProperties() {
        try {
            File propertiesFile = getPropertiesFile();
            if (propertiesFile == null || !propertiesFile.exists()) {
                log.warn("⚠️  Không có file properties để backup");
                return false;
            }
            
            String backupPath = propertiesFile.getAbsolutePath() + ".backup";
            
            Files.copy(
                propertiesFile.toPath(), 
                Path.of(backupPath), 
                StandardCopyOption.REPLACE_EXISTING
            );
            
            log.info("✅ Đã tạo backup: " + backupPath);
            return true;
            
        } catch (Exception e) {
            log.error("❌ Lỗi khi tạo backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cập nhật cấu hình database trong application.properties
     */
    public boolean updateDatabaseConfiguration(String host, int port, String database, String username, String password) {
        try {
            Path propertiesPath = Path.of("src/main/resources/application.properties");
            
            // Tạo backup trước khi cập nhật
            backupApplicationProperties();
            
            // Đọc file properties hiện tại
            Properties properties = new Properties();
            if (Files.exists(propertiesPath)) {
                try (InputStream input = Files.newInputStream(propertiesPath)) {
                    properties.load(input);
                }
            }
            
            // Cập nhật cấu hình database
            String jdbcUrl = "jdbc:mariadb://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true".formatted(
                    host, port, database);
            
            properties.setProperty("spring.datasource.url", jdbcUrl);
            properties.setProperty("spring.datasource.username", username);
            properties.setProperty("spring.datasource.password", password);
            
            // Ghi lại file properties
            try (OutputStream output = Files.newOutputStream(propertiesPath)) {
                properties.store(output, "Updated by Emergency Configuration - " + java.time.LocalDateTime.now());
            }
            
            log.info("✅ Đã cập nhật cấu hình database:");
            log.info("   Host: " + host + ":" + port);
            log.info("   Database: " + database);
            log.info("   Username: " + username);
            
            return true;
            
        } catch (Exception e) {
            log.error("❌ Lỗi khi cập nhật cấu hình database: " + e.getMessage());
            return false;
        }
    }
}
