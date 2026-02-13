package com.example.demo.controller;

import com.example.demo.service.DatabaseHealthService;
import com.example.demo.service.AuthService;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private DatabaseHealthService databaseHealthService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping({"/login", "/dang-nhap"})
    public String loginPage(@RequestParam(required = false) String logout,
                           @RequestParam(required = false) String error,
                           @RequestParam(required = false) String expired,
                           Model model) {
        log.info("📝 GET /auth/login - Displaying login page");
        
        // Handle logout success message
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công!");
        }
        
        // Handle error messages
        if (error != null) {
            model.addAttribute("errorMessage", "Đăng nhập thất bại. Vui lòng kiểm tra tên đăng nhập và mật khẩu.");
        }
        
        if (expired != null) {
            model.addAttribute("warningMessage", "Phiên đăng nhập của bạn đã hết hạn. Vui lòng đăng nhập lại.");
        }
        
        // Kiểm tra database có sẵn sàng không
        if (!databaseHealthService.isDatabaseAvailable()) {
            return "redirect:/settings?error=database_unavailable";
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            log.info("   User already authenticated, redirecting to dashboard");
            return "redirect:/dashboard";
        }
        
        model.addAttribute("pageTitle", "Dang nhap");
        return "auth/login";
    }
    
    @GetMapping("/logout-success")
    public String logoutSuccess(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("successMessage", 
            "Ban da dang xuat thanh cong!");
        return "redirect:/auth/login";
    }
    
    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        log.info("📝 GET /auth/change-password - Displaying change password page");
        
        // Kiểm tra database có sẵn sàng không
        if (!databaseHealthService.isDatabaseAvailable()) {
            return "redirect:/settings?error=database_unavailable";
        }
        
        // Kiểm tra user đã đăng nhập chưa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            log.info("   User not authenticated, redirecting to login");
            return "redirect:/auth/login";
        }
        
        model.addAttribute("pageTitle", "Doi mat khau");
        return "auth/change-password";
    }
    
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        log.info("📝 POST /auth/change-password - Processing password change");
        
        // Kiểm tra database có sẵn sàng không
        if (!databaseHealthService.isDatabaseAvailable()) {
            redirectAttributes.addFlashAttribute("error", "Database không sẵn sàng!");
            return "redirect:/auth/change-password";
        }
        
        // Kiểm tra user đã đăng nhập chưa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            log.info("   User not authenticated, redirecting to login");
            return "redirect:/auth/login";
        }
        
        // Kiểm tra mật khẩu xác nhận
        if (!newPassword.equals(confirmPassword)) {
            log.info("   Confirm password does not match");
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/auth/change-password";
        }
        
        // Kiểm tra mật khẩu mới không được để trống
        if (newPassword == null || newPassword.isEmpty()) {
            log.info("   New password is empty");
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không được để trống!");
            return "redirect:/auth/change-password";
        }
        
        // Kiểm tra mật khẩu mới ít nhất 6 ký tự
        if (newPassword.length() < 6) {
            log.info("   New password is less than 6 characters");
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "redirect:/auth/change-password";
        }
        
        // Lấy user hiện tại từ session
        String username = authentication.getName();
        Optional<com.example.demo.entity.User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            log.info("   User not found");
            redirectAttributes.addFlashAttribute("error", "Người dùng không tồn tại!");
            return "redirect:/auth/change-password";
        }
        
        com.example.demo.entity.User user = userOpt.get();
        
        // Thay đổi mật khẩu
        if (authService.changePassword(user.getId(), oldPassword, newPassword)) {
            log.info("   Password changed successfully for user: " + username);
            redirectAttributes.addFlashAttribute("success", "Mật khẩu đã được thay đổi thành công!");
            return "redirect:/auth/change-password";
        } else {
            log.info("   Old password is incorrect");
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác!");
            return "redirect:/auth/change-password";
        }
    }
}
