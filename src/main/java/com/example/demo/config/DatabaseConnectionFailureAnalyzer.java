package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Kiểm tra kết nối database khi ứng dụng khởi động
 * Được gọi từ controllers để kiểm tra connectivity
 */
@Component
@DependsOnDatabaseInitialization
public class DatabaseConnectionFailureAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionFailureAnalyzer.class);

    private final DataSource dataSource;

    public DatabaseConnectionFailureAnalyzer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Kiểm tra kết nối database
     */
    public boolean checkDatabaseConnection() {
        try {
            if (dataSource != null) {
                try (Connection conn = dataSource.getConnection()) {
                    log.info("✅ Database connection successful!");
                    return true;
                }
            }
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            // Kiểm tra xem lỗi có phải do xác thực không
            if (message.contains("access denied") || 
                message.contains("authentication failed") ||
                message.contains("password") ||
                message.contains("credential")) {
                
                log.error("\n" +
                    "╔════════════════════════════════════════════════════════════════════╗\n" +
                    "║ ❌ LỖI XÁC THỰC DATABASE                                            ║\n" +
                    "╚════════════════════════════════════════════════════════════════════╝\n" +
                    "Mật khẩu database không chính xác!\n\n" +
                    "Vui lòng kiểm tra:\n" +
                    "  1. Mật khẩu root của MariaDB trong application.properties\n" +
                    "  2. Xác nhận MariaDB đang chạy trên localhost:3306\n" +
                    "  3. Database 'oss' đã được tạo hay chưa\n\n" +
                    "Chi tiết lỗi: " + e.getMessage() + "\n"
                );
            } else if (message.contains("connection refused") || 
                       message.contains("cannot connect") ||
                       message.contains("unknown host")) {
                
                log.error("\n" +
                    "╔════════════════════════════════════════════════════════════════════╗\n" +
                    "║ ❌ KHÔNG THỂ KẾT NỐI DATABASE                                       ║\n" +
                    "╚════════════════════════════════════════════════════════════════════╝\n" +
                    "MariaDB có thể không chạy hoặc không thể truy cập được!\n\n" +
                    "Vui lòng kiểm tra:\n" +
                    "  1. MariaDB service đang chạy hay không\n" +
                    "  2. MariaDB lắng nghe trên localhost:3306\n" +
                    "  3. Không có tường lửa chặn kết nối\n\n" +
                    "Chi tiết lỗi: " + e.getMessage() + "\n"
                );
            } else {
                log.error("❌ Database connection error: " + e.getMessage());
            }
        }
        return false;
    }
}
