package com.example.demo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    
    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);
    
    @Value("${spring.datasource.url}")
    private String url;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    @Primary
    DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        
        try {
            dataSource.setJdbcUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
            
            // Cấu hình cho phép khởi động khi database chưa sẵn sày
            dataSource.setInitializationFailTimeout(-1); // Không fail khi init, giá trị âm = không timeout
            dataSource.setMinimumIdle(0); // Không cần connection tối thiểu
            dataSource.setConnectionTimeout(3000); // 3 giây timeout
            dataSource.setValidationTimeout(2000); // 2 giây validation
            dataSource.setMaximumPoolSize(10);
            
            // Test connection ngay để báo lỗi sớm nhưng không crash app
            dataSource.setConnectionTestQuery("SELECT 1");
            
            log.info("🔧 DataSource configured with URL: " + url);
            log.info("👤 Username: " + username);
            log.info("⚠️  Note: Connection validation will happen on-demand, not at startup");
            log.info("🚨 If database connection fails, access: http://localhost:8080/emergency/setup");
            
        } catch (Exception e) {
            log.error("❌ Error configuring DataSource: " + e.getMessage());
            log.error("🚨 Emergency Database Setup: http://localhost:8080/emergency/setup");
            log.error("📝 Application will start anyway. Configure database using emergency setup.");
        }
        
        return dataSource;
    }
}
