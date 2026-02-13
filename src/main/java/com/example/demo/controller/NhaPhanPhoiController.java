package com.example.demo.controller;

import com.example.demo.entity.NhaPhanPhoi;
import com.example.demo.repository.NhaPhanPhoiRepository;
import com.example.demo.service.CodeGeneratorService;
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
@RequestMapping("/nha-phan-phoi")
public class NhaPhanPhoiController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(NhaPhanPhoiController.class);

    @Autowired
    private NhaPhanPhoiRepository nhaPhanPhoiRepository;
    
    @Autowired
    private CodeGeneratorService codeGeneratorService;

    // Hiển thị danh sách nhà phân phối
    @GetMapping({"", "/"})
    public String index(Model model, jakarta.servlet.http.HttpSession session) {
        // Thêm thông tin user vào model
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        List<NhaPhanPhoi> danhSachNhaPhanPhoi = nhaPhanPhoiRepository.findAll();
        model.addAttribute("danhSachNhaPhanPhoi", danhSachNhaPhanPhoi);
        
        // Log truy cập
        log.info("User " + getCurrentUserName(session) + " (" + getCurrentUserRole(session) + ") đã truy cập trang Nhà phân phối");
        model.addAttribute("pageTitle", "Quản lý Nhà phân phối");
        return "nha-phan-phoi/index";
    }

    // Hiển thị form thêm nhà phân phối mới
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Tạo mã nhà phân phối tự động
        String newCode = codeGeneratorService.generateNhaPhanPhoiCode();
        NhaPhanPhoi nhaPhanPhoi = new NhaPhanPhoi();
        nhaPhanPhoi.setMaNhaPhanPhoi(newCode);
        
        model.addAttribute("nhaPhanPhoi", nhaPhanPhoi);
        model.addAttribute("pageTitle", "Thêm Nhà phân phối mới");
        model.addAttribute("generatedCode", newCode);  // Gửi mã tạo ra để hiển thị
        return "nha-phan-phoi/form";
    }

    // Xử lý thêm/cập nhật nhà phân phối
    @PostMapping("/save")
    public String save(@ModelAttribute NhaPhanPhoi nhaPhanPhoi, RedirectAttributes redirectAttributes) {
        try {
            // Validate nhaPhanPhoi object
            if (nhaPhanPhoi != null) {
                nhaPhanPhoiRepository.save(nhaPhanPhoi);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Nhà phân phối '" + nhaPhanPhoi.getTenNhaPhanPhoi() + "' đã được lưu thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu nhà phân phối không hợp lệ!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi lưu nhà phân phối: " + e.getMessage());
        }
        return "redirect:/nha-phan-phoi";
    }

    // Hiển thị form chỉnh sửa nhà phân phối
    @GetMapping("/{maNhaPhanPhoi}/edit")
    public String showEditForm(@PathVariable(required = false) String maNhaPhanPhoi, Model model, RedirectAttributes redirectAttributes) {
        // Validate maNhaPhanPhoi parameter
        if (maNhaPhanPhoi == null || maNhaPhanPhoi.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã nhà phân phối không hợp lệ");
            return "redirect:/nha-phan-phoi";
        }
        
        Optional<NhaPhanPhoi> nhaPhanPhoiOptional = nhaPhanPhoiRepository.findById(maNhaPhanPhoi);
        if (nhaPhanPhoiOptional.isPresent()) {
            model.addAttribute("nhaPhanPhoi", nhaPhanPhoiOptional.get());
            model.addAttribute("pageTitle", "Chỉnh sửa Nhà phân phối");
            return "nha-phan-phoi/form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhà phân phối với mã: " + maNhaPhanPhoi);
            return "redirect:/nha-phan-phoi";
        }
    }

    // Xử lý cập nhật nhà phân phối
    @PostMapping("/{maNhaPhanPhoi}")
    public String update(@PathVariable(required = false) String maNhaPhanPhoi, @ModelAttribute NhaPhanPhoi nhaPhanPhoi, RedirectAttributes redirectAttributes) {
        // Validate maNhaPhanPhoi parameter
        if (maNhaPhanPhoi == null || maNhaPhanPhoi.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã nhà phân phối không hợp lệ");
            return "redirect:/nha-phan-phoi";
        }
        
        try {
            Optional<NhaPhanPhoi> existingNhaPhanPhoi = nhaPhanPhoiRepository.findById(maNhaPhanPhoi);
            if (existingNhaPhanPhoi.isPresent()) {
                nhaPhanPhoi.setMaNhaPhanPhoi(maNhaPhanPhoi);
                nhaPhanPhoiRepository.save(nhaPhanPhoi);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Nhà phân phối '" + nhaPhanPhoi.getTenNhaPhanPhoi() + "' đã được cập nhật thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhà phân phối với mã: " + maNhaPhanPhoi);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi cập nhật nhà phân phối: " + e.getMessage());
        }
        return "redirect:/nha-phan-phoi";
    }

    // Xóa nhà phân phối
    @GetMapping("/{maNhaPhanPhoi}/delete")
    public String delete(@PathVariable(required = false) String maNhaPhanPhoi, RedirectAttributes redirectAttributes) {
        // Validate maNhaPhanPhoi parameter
        if (maNhaPhanPhoi == null || maNhaPhanPhoi.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã nhà phân phối không hợp lệ");
            return "redirect:/nha-phan-phoi";
        }
        
        try {
            Optional<NhaPhanPhoi> nhaPhanPhoi = nhaPhanPhoiRepository.findById(maNhaPhanPhoi);
            if (nhaPhanPhoi.isPresent()) {
                nhaPhanPhoiRepository.deleteById(maNhaPhanPhoi);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Nhà phân phối '" + nhaPhanPhoi.get().getTenNhaPhanPhoi() + "' đã được xóa thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhà phân phối với mã: " + maNhaPhanPhoi);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi xóa nhà phân phối: " + e.getMessage());
        }
        return "redirect:/nha-phan-phoi";
    }

    // API: Tạo nhanh nhà phân phối (call từ form thêm hàng hóa)
    @PostMapping("/api/create-quick")
    @ResponseBody
    public NhaPhanPhoi createQuick(@RequestBody NhaPhanPhoi nhaPhanPhoi) {
        try {
            // Kiểm tra mã nhà phân phối có trùng không
            String maNhaPhanPhoi = nhaPhanPhoi.getMaNhaPhanPhoi();
            if (maNhaPhanPhoi == null || maNhaPhanPhoi.trim().isEmpty()) {
                throw new IllegalArgumentException("Mã nhà phân phối không được để trống");
            }
            
            if (nhaPhanPhoiRepository.existsById(maNhaPhanPhoi)) {
                throw new IllegalArgumentException("Mã nhà phân phối '" + maNhaPhanPhoi + "' đã tồn tại");
            }
            
            // Lưu nhà phân phối mới
            return nhaPhanPhoiRepository.save(nhaPhanPhoi);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo nhà phân phối: " + e.getMessage());
        }
    }

}