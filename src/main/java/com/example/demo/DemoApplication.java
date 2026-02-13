package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.demo.repository")
public class DemoApplication extends SpringBootServletInitializer {
	
	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		try {
			SpringApplication.run(DemoApplication.class, args);
			log.info("✅ Đã khởi động ứng dụng!");
		} catch (Exception e) {
			log.error("\n╔════════════════════════════════════════════════════════════════════╗");
			log.error("║ ❌ LỖI KHI KHỞI ĐỘNG ỨNG DỤNG                                        ║");
			log.error("╚════════════════════════════════════════════════════════════════════╝");
			log.error("Lỗi: " + e.getMessage());
			
			// In ra nguyên nhân gốc của exception
			Throwable cause = e.getCause();
			while (cause != null) {
				log.error("Nguyên nhân: " + cause.getMessage());
				cause = cause.getCause();
			}
			
			log.error("\nVui lòng kiểm tra:");
			log.error("  1. MariaDB service đang chạy hay không");
			log.error("  2. Mật khẩu database trong application.properties có đúng không");
			log.error("  3. Database 'oss' đã tồn tại hay chưa");
			log.error("  4. Cấu hình kết nối trong application.properties\n");
			
			log.error("Stack trace:", e);
			System.exit(1);
		}
	}
}