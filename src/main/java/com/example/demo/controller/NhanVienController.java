package com.example.demo.controller;

import com.example.demo.entity.NhanVien;
import com.example.demo.repository.NhanVienRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/nhan-vien")
public class NhanVienController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(NhanVienController.class);

    @Autowired
    private NhanVienRepository nhanVienRepository;

    // Hiển thị danh sách nhân viên
    @GetMapping({"", "/"})
    public String index(Model model, HttpSession session) {
        // Kiểm tra quyền truy cập (chỉ ADMIN mới xem được nhân viên)
        if (!isAdmin(session)) {
            return "redirect:/dashboard/access-denied";
        }
        
        // Thêm thông tin user vào model
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        List<NhanVien> danhSachNhanVien = nhanVienRepository.findAll();
        model.addAttribute("danhSachNhanVien", danhSachNhanVien);
        
        // Log truy cập
        log.info("Admin " + getCurrentUserName(session) + " đã truy cập trang Nhân viên");
        model.addAttribute("pageTitle", "Quản lý Nhân viên");
        return "nhan-vien/index";
    }

    // Hiển thị form thêm nhân viên mới
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("nhanVien", new NhanVien());
        model.addAttribute("pageTitle", "Thêm Nhân viên mới");
        return "nhan-vien/form";
    }

    // Xử lý thêm/cập nhật nhân viên
    @PostMapping("/save")
    public String save(@ModelAttribute NhanVien nhanVien, RedirectAttributes redirectAttributes) {
        try {
            if (nhanVien != null) {
                nhanVienRepository.save(nhanVien);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Nhân viên '" + nhanVien.getHoTen() + "' đã được lưu thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi lưu nhân viên: " + e.getMessage());
        }
        return "redirect:/nhan-vien";
    }

    // Hiển thị form chỉnh sửa nhân viên
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable(required = false) Long id, Model model, RedirectAttributes redirectAttributes) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID nhân viên không hợp lệ");
            return "redirect:/nhan-vien";
        }
        
        Optional<NhanVien> nhanVienOptional = nhanVienRepository.findById(id);
        if (nhanVienOptional.isPresent()) {
            model.addAttribute("nhanVien", nhanVienOptional.get());
            model.addAttribute("pageTitle", "Chỉnh sửa Nhân viên");
            return "nhan-vien/form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhân viên với ID: " + id);
            return "redirect:/nhan-vien";
        }
    }

    // Xử lý cập nhật nhân viên
    @PostMapping("/{id}")
    public String update(@PathVariable(required = false) Long id, @ModelAttribute NhanVien nhanVien, RedirectAttributes redirectAttributes) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID nhân viên không hợp lệ");
            return "redirect:/nhan-vien";
        }
        
        try {
            Optional<NhanVien> existingNhanVien = nhanVienRepository.findById(id);
            if (existingNhanVien.isPresent()) {
                nhanVien.setId(id);
                nhanVienRepository.save(nhanVien);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Nhân viên '" + nhanVien.getHoTen() + "' đã được cập nhật thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhân viên với ID: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi cập nhật nhân viên: " + e.getMessage());
        }
        return "redirect:/nhan-vien";
    }

    // Xóa nhân viên
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable(required = false) Long id, RedirectAttributes redirectAttributes) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID nhân viên không hợp lệ");
            return "redirect:/nhan-vien";
        }
        
        try {
            Optional<NhanVien> nhanVien = nhanVienRepository.findById(id);
            if (nhanVien.isPresent()) {
                nhanVienRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Nhân viên '" + nhanVien.get().getHoTen() + "' đã được xóa thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhân viên với ID: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi xóa nhân viên: " + e.getMessage());
        }
        return "redirect:/nhan-vien";
    }

    // Hiển thị chi tiết nhân viên
    @GetMapping("/{id}")
    public String detail(@PathVariable(required = false) Long id, Model model, RedirectAttributes redirectAttributes) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID nhân viên không hợp lệ");
            return "redirect:/nhan-vien";
        }
        
        Optional<NhanVien> nhanVienOptional = nhanVienRepository.findById(id);
        if (nhanVienOptional.isPresent()) {
            model.addAttribute("nhanVien", nhanVienOptional.get());
            model.addAttribute("pageTitle", "Chi tiết Nhân viên");
            return "nhan-vien/detail";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhân viên với ID: " + id);
            return "redirect:/nhan-vien";
        }
    }
}