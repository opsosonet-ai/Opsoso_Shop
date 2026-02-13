package com.example.demo.controller;

import com.example.demo.entity.KhachHang;
import com.example.demo.repository.KhachHangRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/khach-hang")
public class KhachHangController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(KhachHangController.class);

    @Autowired
    private KhachHangRepository khachHangRepository;

    // Hiển thị danh sách khách hàng
    @GetMapping({"", "/"})
    public String index(Model model, jakarta.servlet.http.HttpSession session) {
        // Thêm thông tin user vào model
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        List<KhachHang> danhSachKhachHang = khachHangRepository.findAll();
        model.addAttribute("danhSachKhachHang", danhSachKhachHang);
        
        // Log truy cập
        log.info("User " + getCurrentUserName(session) + " (" + getCurrentUserRole(session) + ") đã truy cập trang Khách hàng");
        model.addAttribute("pageTitle", "Quản lý Khách hàng");
        return "khach-hang/index";
    }

    // Hiển thị form thêm khách hàng mới
    @GetMapping("/new")
    public String showCreateForm(Model model, jakarta.servlet.http.HttpSession session) {
        // Thêm thông tin user vào model
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        model.addAttribute("khachHang", new KhachHang());
        model.addAttribute("pageTitle", "Thêm Khách hàng mới");
        return "khach-hang/form";
    }

    // Xử lý thêm/cập nhật khách hàng
    @PostMapping("/save")
    public String save(@ModelAttribute KhachHang khachHang, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpSession session) {
        try {
            boolean isNewCustomer = (khachHang.getId() == null);
            
            // Set registration date for new customers
            if (isNewCustomer || khachHang.getNgayDangKy() == null) {
                khachHang.setNgayDangKy(LocalDate.now());
            } else {
                // For existing customers, preserve the original registration date
                Long customerId = khachHang.getId();
                if (customerId != null && customerId > 0) {
                    Optional<KhachHang> existingCustomer = khachHangRepository.findById(customerId);
                    if (existingCustomer.isPresent()) {
                        khachHang.setNgayDangKy(existingCustomer.get().getNgayDangKy());
                    }
                }
            }
            
            // Save the customer
            khachHangRepository.save(khachHang);
            
            log.info((isNewCustomer ? "✅ New" : "🔄 Updated") + " customer saved successfully:" +
                "\n  - ID: " + khachHang.getId() +
                "\n  - Họ tên: " + khachHang.getHoTen() +
                "\n  - Email: " + khachHang.getEmail() +
                "\n  - SĐT: " + khachHang.getSoDienThoai() +
                "\n  - Loại: " + khachHang.getLoaiKhachHang() +
                "\n  - MST: " + khachHang.getMaSoThue() +
                "\n  - Ngày ĐK: " + khachHang.getNgayDangKy()
            );
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Khách hàng '" + khachHang.getHoTen() + "' đã được " + 
                (isNewCustomer ? "thêm" : "cập nhật") + " thành công!");
        } catch (Exception e) {
            log.error("❌ Error saving customer: " + e.getMessage());
            log.error("Save customer error:", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi lưu khách hàng: " + e.getMessage());
        }
        return "redirect:/khach-hang";
    }

    // Hiển thị form chỉnh sửa khách hàng
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable(required = false) Long id, Model model, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpSession session) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID khách hàng không hợp lệ");
            return "redirect:/khach-hang";
        }
        
        // Thêm thông tin user vào model
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        log.info("🔍 Showing edit form for customer ID: " + id);
        log.info("👤 Current user: " + getCurrentUserName(session) + " (" + getCurrentUserRole(session) + ")");
        
        Optional<KhachHang> khachHangOptional = khachHangRepository.findById(id);
        if (khachHangOptional.isPresent()) {
            model.addAttribute("khachHang", khachHangOptional.get());
            model.addAttribute("pageTitle", "Chỉnh sửa Khách hàng");
            return "khach-hang/form";
        } else {
            log.error("❌ Customer not found with ID: " + id);
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khách hàng với ID: " + id);
            return "redirect:/khach-hang";
        }
    }

    // Xóa khách hàng
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable(required = false) Long id, RedirectAttributes redirectAttributes) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID khách hàng không hợp lệ");
            return "redirect:/khach-hang";
        }
        
        try {
            Optional<KhachHang> khachHang = khachHangRepository.findById(id);
            if (khachHang.isPresent()) {
                khachHangRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Khách hàng '" + khachHang.get().getHoTen() + "' đã được xóa thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khách hàng với ID: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi xóa khách hàng: " + e.getMessage());
        }
        return "redirect:/khach-hang";
    }

    // Xóa nhiều khách hàng
    @PostMapping("/delete-multiple")
    public String deleteMultiple(@RequestParam List<Long> ids, RedirectAttributes redirectAttributes) {
        try {
            int count = 0;
            for (Long id : ids) {
                // Validate each id
                if (id != null && id > 0 && khachHangRepository.existsById(id)) {
                    khachHangRepository.deleteById(id);
                    count++;
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã xóa thành công " + count + " khách hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi xóa khách hàng: " + e.getMessage());
        }
        return "redirect:/khach-hang";
    }

    // Hiển thị chi tiết khách hàng
        @GetMapping("/{id}")
    public String detail(@PathVariable(required = false) Long id, Model model, RedirectAttributes redirectAttributes) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID khách hàng không hợp lệ");
            return "redirect:/khach-hang";
        }
        
        Optional<KhachHang> khachHangOptional = khachHangRepository.findById(id);
        if (khachHangOptional.isPresent()) {
            model.addAttribute("khachHang", khachHangOptional.get());
            return "khach-hang/detail";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Khách hàng không tồn tại");
            return "redirect:/khach-hang";
        }
    }

    // API endpoint để lấy danh sách tất cả khách hàng (cho AJAX)
    @GetMapping("/api/all")
    @ResponseBody
    public List<KhachHang> getAllCustomersApi() {
        return khachHangRepository.findAll();
    }

    // API endpoint để tạo khách hàng mới từ form bảo hành
    @PostMapping("/api/create")
    @ResponseBody
    public KhachHang createCustomerFromApi(@RequestBody KhachHang khachHang) {
        try {
            // Validate basic info
            if (khachHang.getHoTen() == null || khachHang.getHoTen().trim().isEmpty()) {
                return null;
            }
            
            // Set created date
            khachHang.setNgayDangKy(LocalDate.now());
            
            // Save to database
            KhachHang savedKhachHang = khachHangRepository.save(khachHang);
            
            log.info("Khách hàng mới đã được tạo: " + savedKhachHang.getHoTen() + " (ID: " + savedKhachHang.getId() + ")");
            
            return savedKhachHang;
        } catch (Exception e) {
            log.error("Lỗi khi tạo khách hàng mới", e);
            return null;
        }
    }
}