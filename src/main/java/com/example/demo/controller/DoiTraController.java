package com.example.demo.controller;

import com.example.demo.entity.ChiTietPhieuXuat;
import com.example.demo.entity.DoiTraHangHoa;
import com.example.demo.entity.HangHoa;
import com.example.demo.entity.KhachHang;
import com.example.demo.entity.User;
import com.example.demo.repository.ChiTietPhieuXuatRepository;
import com.example.demo.repository.DoiTraHangHoaRepository;
import com.example.demo.repository.HangHoaRepository;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/doi-tra")
public class DoiTraController extends BaseController {
    
    @Autowired
    private DoiTraHangHoaRepository doiTraRepository;
    
    @Autowired
    private HangHoaRepository hangHoaRepository;
    
    @Autowired
    private ChiTietPhieuXuatRepository chiTietPhieuXuatRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("pageTitle", "Test đổi trả");
        model.addAttribute("tongSoDoiTra", doiTraRepository.count());
        return "doi-tra/test";
    }
    
    @Autowired
    private AuthService authService;
    
    /**
     * Trang danh sách đổi trả hàng
     */
    @GetMapping("")
    public String index(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String trangThai,
                       @RequestParam(required = false) String loaiDoiTra,
                       Model model,
                       HttpSession session) {
        
        // Kiểm tra đăng nhập
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        List<DoiTraHangHoa> danhSachDoiTra;
        
        // Tìm kiếm theo các tiêu chí
        if (search != null && !search.trim().isEmpty()) {
            danhSachDoiTra = doiTraRepository.findByKeyword(search.trim());
        } else if (trangThai != null && !trangThai.isEmpty()) {
            DoiTraHangHoa.TrangThaiDoiTra tt = DoiTraHangHoa.TrangThaiDoiTra.valueOf(trangThai);
            danhSachDoiTra = doiTraRepository.findByTrangThaiOrderByNgayDoiTraDesc(tt);
        } else if (loaiDoiTra != null && !loaiDoiTra.isEmpty()) {
            DoiTraHangHoa.LoaiDoiTra lt = DoiTraHangHoa.LoaiDoiTra.valueOf(loaiDoiTra);
            danhSachDoiTra = doiTraRepository.findByLoaiDoiTraOrderByNgayDoiTraDesc(lt);
        } else {
            danhSachDoiTra = doiTraRepository.findAllByOrderByNgayDoiTraDesc();
        }
        
        model.addAttribute("danhSachDoiTra", danhSachDoiTra);
        model.addAttribute("search", search);
        model.addAttribute("selectedTrangThai", trangThai);
        model.addAttribute("selectedLoaiDoiTra", loaiDoiTra);
        model.addAttribute("pageTitle", "Quản lý Đổi trả hàng hóa");
        
        // Thống kê
        model.addAttribute("tongSoDoiTra", doiTraRepository.count());
        model.addAttribute("choDuyet", doiTraRepository.countByTrangThai(DoiTraHangHoa.TrangThaiDoiTra.CHO_DUYET));
        model.addAttribute("daDuyet", doiTraRepository.countByTrangThai(DoiTraHangHoa.TrangThaiDoiTra.DA_DUYET));
        
        return "redirect:/hang-hoa";  // Redirect to product page temporarily until doi-tra templates are available
    }
    
    /**
     * Trang tạo đơn đổi trả mới
     */
    @GetMapping({"/form", "/new"})
    public String showCreateForm(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        // Lấy chi tiết hàng hóa đã bán trong 7 ngày qua (với serial + đơn hàng)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<ChiTietPhieuXuat> chiTietDaBanList = chiTietPhieuXuatRepository.findSoldProductDetailsInPeriod(sevenDaysAgo);
        
        // Lấy tất cả hàng hóa tồn kho cho việc đổi mới
        List<HangHoa> hangHoaTonKho = hangHoaRepository.findAll();
        
        // Lấy danh sách khách hàng để tìm kiếm
        List<KhachHang> danhSachKhachHang = khachHangRepository.findAll();
        
        model.addAttribute("doiTra", new DoiTraHangHoa());
        model.addAttribute("chiTietDaBanList", chiTietDaBanList);
        model.addAttribute("hangHoaTonKho", hangHoaTonKho);
        model.addAttribute("danhSachKhachHang", danhSachKhachHang);
        
        // Thêm enum values một cách an toàn
        List<DoiTraHangHoa.LoaiDoiTra> loaiDoiTraList = Arrays.asList(DoiTraHangHoa.LoaiDoiTra.values());
        model.addAttribute("loaiDoiTraList", loaiDoiTraList);
        
        model.addAttribute("pageTitle", "Tạo đơn đổi trả mới");
        model.addAttribute("ghiChu7Ngay", "Chỉ hiển thị hàng hóa đã bán trong vòng 7 ngày qua");
        
        return "doi-tra/form";
    }
    
    /**
     * Xử lý tạo đơn đổi trả mới
     */
    @PostMapping("/create")
    public String createDoiTra(@RequestParam(required = false) Long chiTietId,
                              @RequestParam Long hangHoaId,
                              @RequestParam DoiTraHangHoa.LoaiDoiTra loaiDoiTra,
                              @RequestParam(required = false) Long hangHoaDoiMoi,
                              @RequestParam Integer soLuong,
                              @RequestParam BigDecimal donGia,
                              @RequestParam String tenKhachHang,
                              @RequestParam(required = false) String soDienThoai,
                              @RequestParam(required = false) Long khachHangId,
                              @RequestParam(required = false) Long phieuXuatId,
                              @RequestParam String lyDo,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        try {
            // Validate hangHoaId parameter
            if (hangHoaId == null || hangHoaId <= 0) {
                redirectAttributes.addFlashAttribute("error", "ID hàng hóa không hợp lệ!");
                return "redirect:/doi-tra/new";
            }
            
            Optional<HangHoa> hangHoaOpt = hangHoaRepository.findById(hangHoaId);
            if (hangHoaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy hàng hóa!");
                return "redirect:/doi-tra/new";
            }
            
            // Kiểm tra hàng đổi mới cho trường hợp DOI_HANG
            HangHoa hangHoaDoiMoiObj = null;
            if (loaiDoiTra == DoiTraHangHoa.LoaiDoiTra.DOI_HANG) {
                if (hangHoaDoiMoi == null) {
                    redirectAttributes.addFlashAttribute("error", "Vui lòng chọn hàng hóa đổi mới!");
                    return "redirect:/doi-tra/form";
                }
                
                Optional<HangHoa> hangHoaDoiMoiOpt = hangHoaRepository.findById(hangHoaDoiMoi);
                if (hangHoaDoiMoiOpt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Không tìm thấy hàng hóa đổi mới!");
                    return "redirect:/doi-tra/form";
                }
                hangHoaDoiMoiObj = hangHoaDoiMoiOpt.get();
            }
            
            // Tạo mã đổi trả tự động
            String maDoiTra = generateMaDoiTra();
            
            // Tạo đơn đổi trả
            DoiTraHangHoa doiTra = new DoiTraHangHoa(
                maDoiTra,
                hangHoaOpt.get(),
                loaiDoiTra,
                soLuong,
                donGia,
                tenKhachHang,
                lyDo
            );
            
            // Set hàng hóa đổi mới nếu là loại đổi hàng
            if (hangHoaDoiMoiObj != null) {
                doiTra.setHangHoaDoiMoi(hangHoaDoiMoiObj);
            }
            
            if (soDienThoai != null && !soDienThoai.trim().isEmpty()) {
                doiTra.setSoDienThoai(soDienThoai.trim());
            }
            
            doiTraRepository.save(doiTra);
            
            redirectAttributes.addFlashAttribute("success", 
                "Tạo đơn đổi trả thành công! Mã đơn: " + maDoiTra);
            
            return "redirect:/doi-tra";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/doi-tra/new";
        }
    }
    
    /**
     * Xem chi tiết đơn đổi trả
     */
    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable(required = false) Long id, Model model, HttpSession session) {
        // Validate id parameter
        if (id == null || id <= 0) {
            return "redirect:/doi-tra";
        }
        
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        Optional<DoiTraHangHoa> doiTraOpt = doiTraRepository.findById(id);
        if (doiTraOpt.isEmpty()) {
            return "redirect:/doi-tra";
        }
        
        model.addAttribute("doiTra", doiTraOpt.get());
        model.addAttribute("pageTitle", "Chi tiết đơn đổi trả");
        
        return "doi-tra/detail";
    }
    
    /**
     * Duyệt/Từ chối đơn đổi trả
     */
    @PostMapping("/{id}/approve")
    public String approveDoiTra(@PathVariable(required = false) Long id,
                               @RequestParam DoiTraHangHoa.TrangThaiDoiTra trangThai,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("error", "ID đơn đổi trả không hợp lệ!");
            return "redirect:/doi-tra";
        }
        
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        // Kiểm tra quyền (chỉ ADMIN hoặc MANAGER mới được duyệt)
        if (!authService.hasPermission(currentUser, "MANAGE_PRODUCTS")) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền duyệt đơn đổi trả!");
            return "redirect:/doi-tra";
        }
        
        try {
            Optional<DoiTraHangHoa> doiTraOpt = doiTraRepository.findById(id);
            if (doiTraOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đổi trả!");
                return "redirect:/doi-tra";
            }
            
            DoiTraHangHoa doiTra = doiTraOpt.get();
            doiTra.setTrangThai(trangThai);
            doiTra.setNguoiXuLy(currentUser.getFullName());
            doiTra.setNgayXuLy(LocalDateTime.now());
            
            // Xử lý cập nhật kho hàng khi duyệt đơn
            if (trangThai == DoiTraHangHoa.TrangThaiDoiTra.DA_DUYET) {
                processInventoryUpdate(doiTra);
            }
            
            doiTraRepository.save(doiTra);
            
            String message = trangThai == DoiTraHangHoa.TrangThaiDoiTra.DA_DUYET ? 
                "Duyệt đơn đổi trả thành công!" : "Từ chối đơn đổi trả thành công!";
            redirectAttributes.addFlashAttribute("success", message);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/doi-tra";
    }
    
    /**
     * Xử lý cập nhật kho hàng khi duyệt đơn đổi trả
     */
    private void processInventoryUpdate(DoiTraHangHoa doiTra) {
        try {
            HangHoa hangHoaGoc = doiTra.getHangHoa();
            
            if (doiTra.getLoaiDoiTra() == DoiTraHangHoa.LoaiDoiTra.TRA_HANG) {
                // Trả hàng: Cộng lại vào kho
                int soLuongMoi = hangHoaGoc.getSoLuongTon() + doiTra.getSoLuong();
                hangHoaGoc.setSoLuongTon(soLuongMoi);
                hangHoaRepository.save(hangHoaGoc);
                
            } else if (doiTra.getLoaiDoiTra() == DoiTraHangHoa.LoaiDoiTra.DOI_HANG) {
                // Đổi hàng: Cộng hàng cũ vào kho + Trừ hàng mới ra kho
                int soLuongGocMoi = hangHoaGoc.getSoLuongTon() + doiTra.getSoLuong();
                hangHoaGoc.setSoLuongTon(soLuongGocMoi);
                hangHoaRepository.save(hangHoaGoc);
                
                if (doiTra.getHangHoaDoiMoi() != null) {
                    HangHoa hangHoaDoiMoi = doiTra.getHangHoaDoiMoi();
                    int soLuongDoiMoiMoi = hangHoaDoiMoi.getSoLuongTon() - doiTra.getSoLuong();
                    
                    // Kiểm tra đủ hàng để đổi
                    if (soLuongDoiMoiMoi < 0) {
                        throw new RuntimeException("Không đủ hàng trong kho để đổi! " + 
                                                 "Tồn kho: " + hangHoaDoiMoi.getSoLuongTon() + 
                                                 ", Cần: " + doiTra.getSoLuong());
                    }
                    
                    hangHoaDoiMoi.setSoLuongTon(soLuongDoiMoiMoi);
                    hangHoaRepository.save(hangHoaDoiMoi);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật kho hàng: " + e.getMessage());
        }
    }
    
    /**
     * Tạo mã đổi trả tự động
     */
    private String generateMaDoiTra() {
        Optional<String> lastMaOpt = doiTraRepository.findLastMaDoiTra();
        
        if (lastMaOpt.isPresent()) {
            String lastMa = lastMaOpt.get();
            // Tách số từ mã cuối (DT001 -> 001)
            String numberPart = lastMa.substring(2);
            int nextNumber = Integer.parseInt(numberPart) + 1;
            return "DT%03d".formatted(nextNumber);
        } else {
            return "DT001";
        }
    }
}