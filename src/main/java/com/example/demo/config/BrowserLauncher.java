package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;

@Component
public class BrowserLauncher implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(BrowserLauncher.class);
    private final Environment environment;

    public BrowserLauncher(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        try {
            // Lấy port từ cấu hình
            String port = environment.getProperty("server.port", "8080");
            String url = "http://127.0.0.1:" + port;

            log.info("\n🌐 Ứng dụng đã sẵn sàng!");
            log.info("📍 Truy cập: " + url);

            // Thử mở trình duyệt (chỉ hoạt động trên môi trường có GUI)
            if (tryOpenBrowser(url)) {
                log.info("✅ Đã tự động mở trình duyệt!");
            } else {
                log.info("💡 Vui lòng mở trình duyệt và truy cập URL trên");
            }
            
            log.info("=".repeat(60) + "\n");
            
        } catch (Exception e) {
            String port = environment.getProperty("server.port", "8080");
            String url = "http://127.0.0.1:" + port;
            log.info("\n📍 Truy cập ứng dụng tại: " + url + "\n");
        }
    }

    /**
     * Thử mở trình duyệt, trả về true nếu thành công
     */
    private boolean tryOpenBrowser(String url) {
        try {
            // Phương pháp 1: Sử dụng Desktop API (Windows, MacOS, Linux với GUI)
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                    return true;
                }
            }

            // Phương pháp 2: Sử dụng lệnh hệ thống
            String os = System.getProperty("os.name").toLowerCase();
            Runtime runtime = Runtime.getRuntime();

            if (os.contains("win")) {
                // Windows
                runtime.exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                return true;
            } else if (os.contains("mac")) {
                // MacOS
                runtime.exec(new String[]{"open", url});
                return true;
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux - thử các trình duyệt phổ biến
                String[] browsers = {"xdg-open", "google-chrome", "firefox", "chromium-browser", "mozilla"};
                for (String browser : browsers) {
                    try {
                        runtime.exec(new String[]{browser, url});
                        return true;
                    } catch (Exception ignored) {
                        // Thử browser tiếp theo
                    }
                }
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }
}
