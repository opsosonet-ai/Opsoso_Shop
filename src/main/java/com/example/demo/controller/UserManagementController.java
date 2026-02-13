package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * User Management Controller
 * Quản lý người dùng của hệ thống
 */
@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {
    
    private static final Logger log = LoggerFactory.getLogger(UserManagementController.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * Hiển thị danh sách tất cả users
     */
    @GetMapping
    public String listUsers(Model model) {
        log.info("Fetching all users for admin panel");
        try {
            List<User> users = userRepository.findAll();
            model.addAttribute("users", users);
            model.addAttribute("pageTitle", "Quản lý User");
            model.addAttribute("totalUsers", users.size());
            log.info("✓ Retrieved {} users", users.size());
        } catch (Exception e) {
            log.error("Error fetching users: ", e);
            model.addAttribute("errorMessage", "Lỗi khi tải danh sách users: " + e.getMessage());
        }
        return "admin/users/list";
    }
    
    /**
     * Hiển thị chi tiết user
     */
    @GetMapping("/{id}")
    public String viewUser(@PathVariable(required = false) Long id, Model model) {
        // Validate id parameter
        if (id == null || id <= 0) {
            model.addAttribute("errorMessage", "ID user không hợp lệ");
            return "redirect:/admin/users";
        }
        
        log.info("Viewing user details for ID: {}", id);
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                model.addAttribute("pageTitle", "Chi tiết User - " + user.get().getUsername());
                log.info("✓ User found: {}", user.get().getUsername());
            } else {
                log.warn("User not found with ID: {}", id);
                model.addAttribute("errorMessage", "Không tìm thấy user với ID: " + id);
                return "redirect:/admin/users";
            }
        } catch (Exception e) {
            log.error("Error viewing user: ", e);
            model.addAttribute("errorMessage", "Lỗi khi xem chi tiết user: " + e.getMessage());
        }
        return "admin/users/detail";
    }
    
    /**
     * Hiển thị form tạo user mới
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        log.info("Showing create user form");
        model.addAttribute("user", new User());
        model.addAttribute("pageTitle", "Tạo User mới");
        model.addAttribute("isNew", true);
        return "admin/users/form";
    }
    
    /**
     * Hiển thị form chỉnh sửa user
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable(required = false) Long id, Model model) {
        // Validate id parameter
        if (id == null || id <= 0) {
            model.addAttribute("errorMessage", "ID user không hợp lệ");
            return "redirect:/admin/users";
        }
        
        log.info("Showing edit user form for ID: {}", id);
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                model.addAttribute("pageTitle", "Chỉnh sửa User - " + user.get().getUsername());
                model.addAttribute("isNew", false);
                log.info("✓ Edit form loaded for user: {}", user.get().getUsername());
            } else {
                log.warn("User not found for edit with ID: {}", id);
                model.addAttribute("errorMessage", "Không tìm thấy user để chỉnh sửa");
                return "redirect:/admin/users";
            }
        } catch (Exception e) {
            log.error("Error loading edit form: ", e);
            model.addAttribute("errorMessage", "Lỗi khi tải form chỉnh sửa: " + e.getMessage());
        }
        return "admin/users/form";
    }
    
    /**
     * Lưu user (tạo hoặc cập nhật) với mã hóa mật khẩu
     */
    @PostMapping("/save")
    public String saveUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        log.info("Saving user: {}", user.getUsername());
        try {
            User savedUser;
            if (user.getId() == null) {
                // Tạo user mới - yêu cầu mật khẩu
                if (user.getPassword() == null || user.getPassword().isEmpty()) {
                    log.warn("Password is empty when creating new user");
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "❌ Mật khẩu không được để trống khi tạo user mới!");
                    return "redirect:/admin/users/new";
                }
                savedUser = userService.createUser(user);
                log.info("✓ New user created: {}", savedUser.getUsername());
            } else {
                // Cập nhật user hiện tại
                Optional<User> updatedUser = userService.updateUser(user.getId(), user);
                if (updatedUser.isPresent()) {
                    savedUser = updatedUser.get();
                    log.info("✓ User updated: {}", savedUser.getUsername());
                } else {
                    log.warn("User not found for update with ID: {}", user.getId());
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "❌ Không tìm thấy user để cập nhật");
                    return "redirect:/admin/users";
                }
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "✓ User '" + savedUser.getUsername() + "' đã được lưu thành công!");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            log.error("Validation error saving user: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Lỗi: " + e.getMessage());
            return "redirect:/admin/users/new";
        } catch (Exception e) {
            log.error("Error saving user: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Lỗi khi lưu user: " + e.getMessage());
            return "redirect:/admin/users/new";
        }
    }
    
    /**
     * Xóa user
     */
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable(required = false) Long id, RedirectAttributes redirectAttributes) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID user không hợp lệ");
            return "redirect:/admin/users";
        }
        
        log.info("Deleting user with ID: {}", id);
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                String username = user.get().getUsername();
                userRepository.deleteById(id);
                log.info("✓ User deleted: {}", username);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "✓ User '" + username + "' đã được xóa thành công!");
            } else {
                log.warn("User not found for delete with ID: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "❌ Không tìm thấy user để xóa");
            }
        } catch (Exception e) {
            log.error("Error deleting user: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Lỗi khi xóa user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    /**
     * Xóa user qua GET (confirmation page)
     */
    @GetMapping("/{id}/delete-confirm")
    public String showDeleteConfirm(@PathVariable(required = false) Long id, Model model) {
        // Validate id parameter
        if (id == null || id <= 0) {
            return "redirect:/admin/users";
        }
        
        log.info("Showing delete confirmation for user ID: {}", id);
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                model.addAttribute("pageTitle", "Xác nhận xóa User - " + user.get().getUsername());
            } else {
                log.warn("User not found for delete confirmation with ID: {}", id);
                return "redirect:/admin/users";
            }
        } catch (Exception e) {
            log.error("Error showing delete confirmation: ", e);
        }
        return "admin/users/delete-confirm";
    }
}
