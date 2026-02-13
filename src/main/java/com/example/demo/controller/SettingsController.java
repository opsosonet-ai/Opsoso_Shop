package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.DatabaseConfig;
import com.example.demo.entity.StoreInfo;
import com.example.demo.repository.StoreInfoRepository;
import com.example.demo.service.DatabaseConfigService;
import com.example.demo.service.DatabaseCreationService;
import com.example.demo.service.DatabaseHealthService;
import com.example.demo.service.DataInitializationService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class SettingsController extends BaseController {
    
    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);
    
    @Autowired
    private DatabaseConfigService configService;
    
    @Autowired
    private DatabaseHealthService databaseHealthService;
    
    @Autowired
    private DataInitializationService dataInitializationService;
    
    @Autowired
    private DatabaseCreationService databaseCreationService;
    
    @Autowired
    private StoreInfoRepository storeInfoRepository;
    
    @GetMapping
    public String showSettings(Model model, HttpSession session) {
        // Kiểm tra lại kết nối database mỗi khi truy cập trang settings
        databaseHealthService.checkDatabaseConnection();
        
        // Nếu database không khả dụng, cho phép truy cập mà không cần auth
        if (!databaseHealthService.isDatabaseAvailable()) {
            model.addAttribute("currentUser", "Admin");
            model.addAttribute("currentUserRole", "SYSTEM");
            model.addAttribute("config", configService.getConfig());
            model.addAttribute("pageTitle", "Cài đặt hệ thống");
            model.addAttribute("databaseUnavailable", true);
            return "settings/index";
        }
        
        // 🔑 Nếu user đang ở giai đoạn cấu hình database (sau khi test connection thành công)
        // Cho phép truy cập settings mà không cần login, để họ có thể lưu cấu hình
        Boolean configuringDatabase = (Boolean) session.getAttribute("configuringDatabase");
        if (configuringDatabase != null && configuringDatabase) {
            model.addAttribute("currentUser", "Admin");
            model.addAttribute("currentUserRole", "SYSTEM");
            model.addAttribute("config", configService.getConfig());
            model.addAttribute("pageTitle", "Cài đặt hệ thống");
            model.addAttribute("databaseUnavailable", false); // Database khả dụng nhưng chưa login
            return "settings/index";
        }
        
        // Check authentication khi database đã khả dụng và không ở giai đoạn cấu hình
        if (getCurrentUser(session) == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        model.addAttribute("config", configService.getConfig());
        model.addAttribute("pageTitle", "Cài đặt hệ thống");
        
        return "settings/index";
    }
    
    @PostMapping("/save")
    public String saveConfig(@ModelAttribute DatabaseConfig config,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {
        // Cho phép lưu cấu hình mà không cần login
        // Vì đây là flow cấu hình database, người dùng cần lưu cấu hình khi DB chưa khả dụng
        // Sau khi lưu, người dùng sẽ khởi động lại app rồi login bình thường
        
        try {
            log.info("📝 Nhận được cấu hình để lưu:");
            log.info("   Host: " + config.getHost());
            log.info("   Port: " + config.getPort());
            log.info("   Database: " + config.getDatabase());
            log.info("   Username: " + config.getUsername());
            log.info("   Password: " + (config.getPassword() != null && !config.getPassword().isEmpty() ? "****" : "[EMPTY/NULL]"));
            
            configService.saveConfig(config);
            redirectAttributes.addFlashAttribute("successMessage", 
                """
                ✅ Đã lưu cấu hình thành công! 🔄 \
                BẠN PHẢI KHỞI ĐỘNG LẠI ứng dụng để cấu hình có hiệu lực.
                Tệp backup: application.properties.backup""");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu cấu hình: " + e.getMessage());
        }
        
        return "redirect:/settings";
    }
    
    @PostMapping("/test")
    @ResponseBody
    public ApiResponse<Void> testConnection(@RequestBody DatabaseConfig config, HttpSession session) {
        try {
            boolean success = configService.testConnection(config);
            if (success) {
                // Cập nhật trạng thái database sau khi kết nối thành công
                databaseHealthService.markDatabaseAsAvailable();
                // 🔑 Đánh dấu rằng user đang ở trong giai đoạn cấu hình
                session.setAttribute("configuringDatabase", true);
                return new ApiResponse<>(true, "✅ Kết nối thành công!");
            } else {
                return new ApiResponse<>(false, "❌ Không thể kết nối đến database. Vui lòng kiểm tra lại thông tin!");
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi kiểm tra kết nối: " + e.getMessage());
            log.error("Test connection error:", e);
            return new ApiResponse<>(false, "❌ Lỗi: " + e.getMessage());
        }
    }
    
    @PostMapping("/initialize")
    @ResponseBody
    public ApiResponse<Void> initializeData(@RequestBody(required = false) DatabaseConfig requestConfig) {
        try {
            // Sử dụng config từ request nếu có, nếu không thì lấy từ properties
            DatabaseConfig config = requestConfig != null ? requestConfig : configService.getConfig();
            
            log.info("📝 Thông tin kết nối để khởi tạo:");
            log.info("   Host: " + config.getHost());
            log.info("   Port: " + config.getPort());
            log.info("   Database: " + config.getDatabase());
            log.info("   Username: " + config.getUsername());
            log.info("   Password: " + (config.getPassword() != null && !config.getPassword().isEmpty() ? "****" : "[EMPTY]"));
            
            // Kiểm tra xem database đã tồn tại chưa
            if (databaseCreationService.checkDatabaseExists(config)) {
                // Database đã tồn tại
                log.info("✅ Database 'oss' đã tồn tại.");
                databaseHealthService.markDatabaseAsAvailable();
                return new ApiResponse<>(true, "✅ Database đã tồn tại. Không cần khởi tạo lại!");
            } else {
                // Database chưa tồn tại, tạo mới
                log.info("📝 Database chưa tồn tại, đang tạo...");
                boolean created = databaseCreationService.createDatabase(config);
                if (!created) {
                    log.error("❌ Tạo database thất bại!");
                    return new ApiResponse<>(false, "❌ Không thể tạo database. Kiểm tra console để xem chi tiết lỗi!");
                }
                
                // Sau khi tạo database, kiểm tra lại kết nối
                log.info("✅ Database vừa tạo, chờ 2 giây để sẵn sàng...");
                Thread.sleep(2000); // Chờ database được tạo
                if (!databaseHealthService.checkDatabaseConnection()) {
                    return new ApiResponse<>(false, "❌ Database vừa tạo không khả dụng. Vui lòng kiểm tra lại cấu hình!");
                }
                
                databaseHealthService.markDatabaseAsAvailable();
                dataInitializationService.initializeData();
                return new ApiResponse<>(true, "✅ Đã tạo database và khởi tạo dữ liệu thành công!");
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi khởi tạo: " + e.getMessage());
            log.error("Initialize error:", e);
            return new ApiResponse<>(false, "❌ Lỗi khi khởi tạo dữ liệu: " + e.getMessage());
        }
    }

    @PostMapping("/force-initialize-data")
    @ResponseBody
    public ApiResponse<Void> forceInitializeData() {
        try {
            log.info("🔄 Bắt đầu khởi tạo dữ liệu mẫu...");
            
            // Kiểm tra kết nối database trước
            if (!databaseHealthService.checkDatabaseConnection()) {
                return new ApiResponse<>(false, "❌ Không thể kết nối đến database. Vui lòng kiểm tra cấu hình!");
            }
            
            // Force khởi tạo dữ liệu
            dataInitializationService.initializeData();
            
            log.info("✅ Hoàn thành khởi tạo dữ liệu mẫu!");
            return new ApiResponse<>(true, "✅ Đã khởi tạo dữ liệu mẫu thành công!");
            
        } catch (Exception e) {
            log.error("❌ Lỗi khi khởi tạo dữ liệu: " + e.getMessage());
            log.error("Force initialize error:", e);
            return new ApiResponse<>(false, "❌ Lỗi khi khởi tạo dữ liệu: " + e.getMessage());
        }
    }
    
    // Trang cài đặt thông tin cửa hàng
    @GetMapping("/store")
    public String showStoreSettings(Model model, HttpSession session) {
        // Kiểm tra quyền truy cập
        if (!databaseHealthService.isDatabaseAvailable() || getCurrentUser(session) == null) {
            return "redirect:/auth/login";
        }
        
        // Lấy thông tin cửa hàng hiện tại
        StoreInfo storeInfo = getStoreInfo();
        
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        model.addAttribute("storeInfo", storeInfo);
        model.addAttribute("pageTitle", "Cài đặt thông tin cửa hàng");
        
        return "settings/store";
    }
    
    // Lưu thông tin cửa hàng
    @PostMapping("/store/save")
    public String saveStoreInfo(@ModelAttribute StoreInfo storeInfo,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        // Kiểm tra quyền truy cập
        if (!databaseHealthService.isDatabaseAvailable() || getCurrentUser(session) == null) {
            return "redirect:/auth/login";
        }
        
        try {
            // Lấy thông tin cửa hàng hiện tại để giữ lại ID và thời gian tạo
            StoreInfo currentInfo = getStoreInfo();
            if (currentInfo != null && currentInfo.getId() != null) {
                storeInfo.setId(currentInfo.getId());
                storeInfo.setCreatedAt(currentInfo.getCreatedAt());
            }
            
            // Validate storeInfo before saving
            if (storeInfo != null) {
                storeInfoRepository.save(storeInfo);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Đã lưu thông tin cửa hàng thành công!");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Lỗi khi lưu thông tin cửa hàng: " + e.getMessage());
        }
        
        return "redirect:/settings/store";
    }
    
    /**
     * Lấy thông tin cửa hàng (nếu chưa có sẽ tạo mặc định)
     */
    private StoreInfo getStoreInfo() {
        try {
            return storeInfoRepository.findFirstBy()
                .orElseGet(() -> createDefaultStoreInfo());
        } catch (Exception e) {
            // Nếu table chưa tồn tại, tạo thông tin mặc định
            return createDefaultStoreInfo();
        }
    }
    
    /**
     * Tạo thông tin cửa hàng mặc định
     */
    private StoreInfo createDefaultStoreInfo() {
        StoreInfo defaultStore = new StoreInfo();
        defaultStore.setStoreName("CỬA HÀNG ABC");
        defaultStore.setAddress("123 Đường ABC, Quận 1, TP.HCM");
        defaultStore.setPhone("0123-456-789");
        defaultStore.setEmail("info@cuahangabc.com");
        defaultStore.setTaxCode("1234567890");
        return defaultStore;
    }
}
