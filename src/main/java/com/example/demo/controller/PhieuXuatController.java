package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Import debt entities
import com.example.demo.entity.debt.CustomerDebt;
import com.example.demo.entity.enums.DebtStatus;

@Controller
@RequestMapping("/phieu-xuat")
public class PhieuXuatController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(PhieuXuatController.class);

    @Autowired
    private PhieuXuatRepository phieuXuatRepository;
    
    @Autowired
    private HangHoaRepository hangHoaRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private StoreInfoRepository storeInfoRepository;
    
    @Autowired
    private CustomerDebtRepository customerDebtRepository;
    
    // Hiển thị danh sách phiếu xuất
    @GetMapping
    public String showList(Model model, jakarta.servlet.http.HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        List<PhieuXuat> danhSachPhieuXuat = phieuXuatRepository.findAll();
        model.addAttribute("danhSachPhieuXuat", danhSachPhieuXuat);
        model.addAttribute("pageTitle", "Danh sách phiếu xuất");
        
        return "phieu-xuat/list";
    }

    // Hiển thị trang tạo phiếu xuất
    @GetMapping("/new")
    public String showCreateForm(Model model, jakarta.servlet.http.HttpSession session) {
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        // Lấy danh sách hàng hóa có tồn kho > 0
        List<HangHoa> danhSachHangHoa = hangHoaRepository.findAll().stream()
            .filter(hh -> hh.getSoLuongTon() != null && hh.getSoLuongTon() > 0)
            .toList();
        
        // Lấy danh sách khách hàng
        List<KhachHang> danhSachKhachHang = khachHangRepository.findAll();
        
        model.addAttribute("danhSachHangHoa", danhSachHangHoa);
        model.addAttribute("danhSachKhachHang", danhSachKhachHang);
        model.addAttribute("pageTitle", "Xuất hàng hóa");
        
        return "phieu-xuat/form";
    }
    
    // Hiển thị trang chỉnh sửa phiếu xuất
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable(required = false) Long id, Model model, jakarta.servlet.http.HttpSession session) {
        // Validate id parameter
        if (id == null || id <= 0) {
            return "redirect:/phieu-xuat?error=invalid_id";
        }
        
        // Kiểm tra quyền
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }
        
        // Tìm phiếu xuất
        Optional<PhieuXuat> phieuXuatOpt = phieuXuatRepository.findById(id);
        if (phieuXuatOpt.isEmpty()) {
            return "redirect:/phieu-xuat?error=not_found";
        }
        
        PhieuXuat phieuXuat = phieuXuatOpt.get();
        
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        // Lấy danh sách hàng hóa
        List<HangHoa> danhSachHangHoa = hangHoaRepository.findAll();
        
        // Lấy danh sách khách hàng
        List<KhachHang> danhSachKhachHang = khachHangRepository.findAll();
        
        model.addAttribute("phieuXuat", phieuXuat);
        model.addAttribute("danhSachHangHoa", danhSachHangHoa);
        model.addAttribute("danhSachKhachHang", danhSachKhachHang);
        model.addAttribute("pageTitle", "Chỉnh sửa phiếu xuất: " + phieuXuat.getMaPhieuXuat());
        
        return "phieu-xuat/form";
    }

    // Xử lý tạo/cập nhật phiếu xuất
    @PostMapping("/save")
    @Transactional
    public String save(@RequestParam(required = false) Long id,
                       @RequestParam(required = false) Long khachHangId,
                       @RequestParam String ghiChu,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate ngayXuat,
                       @RequestParam(required = false, defaultValue = "TIEN_MAT") String loaiXuat,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayHanThanhToan,
                       @RequestParam List<Long> hangHoaIds,
                       @RequestParam List<Integer> soLuongs,
                       @RequestParam List<BigDecimal> donGias,
                       RedirectAttributes redirectAttributes,
                       jakarta.servlet.http.HttpSession session) {
        try {
            // Validate loại xuất
            if ("BAN_NO".equals(loaiXuat)) {
                // Kiểm tra yêu cầu khách hàng cho bán nợ
                if (khachHangId == null || khachHangId <= 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn khách hàng để bán nợ!");
                    return "redirect:/phieu-xuat/new";
                }
                // Kiểm tra yêu cầu hạn thanh toán cho bán nợ
                if (ngayHanThanhToan == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập hạn thanh toán cho bán nợ!");
                    return "redirect:/phieu-xuat/new";
                }
                // Kiểm tra hạn thanh toán phải lớn hơn ngày hiện tại
                if (ngayHanThanhToan.isBefore(LocalDate.now())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Hạn thanh toán phải lớn hơn ngày hôm nay!");
                    return "redirect:/phieu-xuat/new";
                }
            }
            
            // Tạo phiếu xuất mới hoặc lấy phiếu xuất cũ để cập nhật
            PhieuXuat phieuXuat;
            boolean isUpdate = (id != null && id > 0);
            
            if (isUpdate && id != null) {
                // Cập nhật phiếu xuất
                Optional<PhieuXuat> phieuXuatOpt = phieuXuatRepository.findById(id);
                if (phieuXuatOpt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phiếu xuất!");
                    return "redirect:/phieu-xuat";
                }
                phieuXuat = phieuXuatOpt.get();
                
                // Hoàn trả tồn kho trước khi cập nhật
                for (ChiTietPhieuXuat chiTiet : phieuXuat.getChiTietList()) {
                    HangHoa hangHoa = chiTiet.getHangHoa();
                    hangHoa.setSoLuongTon(hangHoa.getSoLuongTon() + chiTiet.getSoLuong());
                    hangHoaRepository.save(hangHoa);
                }
                
                // Xóa chi tiết cũ
                phieuXuat.getChiTietList().clear();
            } else {
                // Tạo phiếu xuất mới
                phieuXuat = new PhieuXuat();
                phieuXuat.setMaPhieuXuat(generateMaPhieuXuat());
            }
            
            // Set ngày xuất từ form, nếu không có thì lấy ngày hiện tại
            if (ngayXuat != null) {
                phieuXuat.setNgayXuat(ngayXuat.atStartOfDay());
            } else {
                phieuXuat.setNgayXuat(LocalDateTime.now());
            }
            
            phieuXuat.setNguoiXuat(getCurrentUserName(session));
            phieuXuat.setGhiChu(ghiChu);
            phieuXuat.setLoaiXuat(loaiXuat); // Set loại xuất hàng (tiền mặt hoặc bán nợ)
            
            // Set khách hàng nếu có
            if (khachHangId != null) {
                Optional<KhachHang> khachHang = khachHangRepository.findById(khachHangId);
                khachHang.ifPresent(phieuXuat::setKhachHang);
            }
            
            BigDecimal tongTien = BigDecimal.ZERO;
            
            // Tạo chi tiết phiếu xuất
            for (int i = 0; i < hangHoaIds.size(); i++) {
                Long hangHoaId = hangHoaIds.get(i);
                Integer soLuong = soLuongs.get(i);
                BigDecimal donGia = donGias.get(i);
                
                if (soLuong <= 0) continue;
                
                // Validate hangHoaId
                if (hangHoaId == null || hangHoaId <= 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "ID hàng hóa không hợp lệ!");
                    return "redirect:/phieu-xuat/new";
                }
                
                Optional<HangHoa> hangHoaOpt = hangHoaRepository.findById(hangHoaId);
                if (hangHoaOpt.isPresent()) {
                    HangHoa hangHoa = hangHoaOpt.get();
                    
                    // Kiểm tra tồn kho
                    if (hangHoa.getSoLuongTon() == null || hangHoa.getSoLuongTon() < soLuong) {
                        redirectAttributes.addFlashAttribute("errorMessage", 
                            "Hàng hóa '" + hangHoa.getTenHangHoa() + "' không đủ tồn kho!");
                        return "redirect:/phieu-xuat/new";
                    }
                    
                    // Tạo chi tiết
                    ChiTietPhieuXuat chiTiet = new ChiTietPhieuXuat();
                    chiTiet.setHangHoa(hangHoa);
                    chiTiet.setSoLuong(soLuong);
                    chiTiet.setDonGia(donGia);
                    chiTiet.calculateThanhTien();
                    
                    phieuXuat.addChiTiet(chiTiet);
                    tongTien = tongTien.add(chiTiet.getThanhTien());
                    
                    // Trừ tồn kho
                    hangHoa.setSoLuongTon(hangHoa.getSoLuongTon() - soLuong);
                    hangHoaRepository.save(hangHoa);
                }
            }
            
            phieuXuat.setTongTien(tongTien);
            phieuXuat = phieuXuatRepository.save(phieuXuat);
            
            // Nếu là bán nợ, tạo record trong bảng customer_debt
            if ("BAN_NO".equals(loaiXuat) && phieuXuat.getKhachHang() != null) {
                CustomerDebt customerDebt = new CustomerDebt();
                customerDebt.setKhachHang(phieuXuat.getKhachHang());
                customerDebt.setSoPhieuXuatBan(phieuXuat.getMaPhieuXuat());
                customerDebt.setNgayTaoNo(LocalDateTime.now());
                customerDebt.setTongTienNo(tongTien);
                customerDebt.setTongTienDaThanhToan(BigDecimal.ZERO);
                customerDebt.setTongTienConNo(tongTien);
                customerDebt.setTrangThai(DebtStatus.DANG_NO);
                
                // Set hạn thanh toán nếu có
                if (ngayHanThanhToan != null) {
                    customerDebt.setNgayHanChot(ngayHanThanhToan);
                    phieuXuat.setNgayHanThanhToan(ngayHanThanhToan.atStartOfDay());
                }
                
                customerDebt.setGhiChu(ghiChu);
                customerDebtRepository.save(customerDebt);
                
                log.info("✅ Tạo công nợ cho khách hàng: {} - Mã phiếu: {} - Số tiền: {}", 
                    phieuXuat.getKhachHang().getHoTen(), phieuXuat.getMaPhieuXuat(), tongTien);
            }
            
            if (isUpdate) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Cập nhật phiếu xuất thành công! Mã phiếu: " + phieuXuat.getMaPhieuXuat());
                return "redirect:/phieu-xuat/" + phieuXuat.getId() + "/edit";
            } else {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Xuất hàng thành công! Mã phiếu: " + phieuXuat.getMaPhieuXuat());
                redirectAttributes.addFlashAttribute("phieuXuatId", phieuXuat.getId());
                return "redirect:/phieu-xuat/new";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi xuất hàng: " + e.getMessage());
            return "redirect:/phieu-xuat/new";
        }
    }

    // Tạo mã phiếu xuất tự động
    private String generateMaPhieuXuat() {
        String prefix = "PX";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = phieuXuatRepository.count() + 1;
        return "%s%s%04d".formatted(prefix, datePart, count);
    }
    
    // API: Lấy thông tin hàng hóa theo ID
    @GetMapping("/api/hang-hoa/{id}")
    @ResponseBody
    public HangHoa getHangHoa(@PathVariable(required = false) Long id) {
        // Validate id parameter
        if (id == null || id <= 0) {
            return null;
        }
        return hangHoaRepository.findById(id).orElse(null);
    }
    
    // API: Lưu phiếu xuất với AJAX
    @PostMapping(value = "/api/save", consumes = {"application/x-www-form-urlencoded", "multipart/form-data"})
    @ResponseBody
    @Transactional
    public Map<String, Object> savePhieuXuatAjax(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long khachHangId,
            @RequestParam(required = false, defaultValue = "") String ghiChu,
            @RequestParam(value = "ngayXuat", required = false) String ngayXuatStr,
            @RequestParam(required = false, defaultValue = "TIEN_MAT") String loaiXuat,
            @RequestParam(value = "ngayHanThanhToan", required = false) String ngayHanThanhToanStr,
            @RequestParam(required = false) List<Long> hangHoaIds,
            @RequestParam(required = false) List<Integer> soLuongs,
            @RequestParam(required = false) List<BigDecimal> donGias,
            jakarta.servlet.http.HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("[PhieuXuat.api.save] Request received");
            log.info("  - hangHoaIds: " + (hangHoaIds != null ? hangHoaIds.size() : "null"));
            log.info("  - soLuongs: " + (soLuongs != null ? soLuongs.size() : "null"));
            log.info("  - donGias: " + (donGias != null ? donGias.size() : "null"));
            log.info("  - loaiXuat: " + loaiXuat);
            log.info("  - khachHangId: " + khachHangId);
            
            // Kiểm tra quyền
            if (!isLoggedIn(session)) {
                response.put("success", false);
                response.put("message", "Bạn cần đăng nhập để thực hiện chức năng này!");
                return response;
            }
            
            // Validate loại xuất
            if ("BAN_NO".equals(loaiXuat)) {
                // Kiểm tra yêu cầu khách hàng cho bán nợ
                if (khachHangId == null || khachHangId <= 0) {
                    response.put("success", false);
                    response.put("message", "Vui lòng chọn khách hàng để bán nợ!");
                    return response;
                }
                // Kiểm tra yêu cầu hạn thanh toán cho bán nợ
                if (ngayHanThanhToanStr == null || ngayHanThanhToanStr.trim().isEmpty()) {
                    response.put("success", false);
                    response.put("message", "Vui lòng nhập hạn thanh toán cho bán nợ!");
                    return response;
                }
                // Kiểm tra hạn thanh toán phải lớn hơn ngày hiện tại
                try {
                    LocalDate deadline = LocalDate.parse(ngayHanThanhToanStr);
                    if (deadline.isBefore(LocalDate.now())) {
                        response.put("success", false);
                        response.put("message", "Hạn thanh toán phải lớn hơn ngày hôm nay!");
                        return response;
                    }
                } catch (Exception e) {
                    response.put("success", false);
                    response.put("message", "Định dạng hạn thanh toán không hợp lệ!");
                    return response;
                }
            }
            
            // Validate dữ liệu
            if (hangHoaIds == null || hangHoaIds.isEmpty() || soLuongs == null || soLuongs.isEmpty() || donGias == null || donGias.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng thêm ít nhất một hàng hóa!");
                return response;
            }
            
            PhieuXuat phieuXuat;
            boolean isUpdate = (id != null && id > 0);
            
            if (isUpdate && id != null) {
                Optional<PhieuXuat> existingOpt = phieuXuatRepository.findById(id);
                if (existingOpt.isEmpty()) {
                    response.put("success", false);
                    response.put("message", "Không tìm thấy phiếu xuất để cập nhật!");
                    return response;
                }
                phieuXuat = existingOpt.get();
                
                // Hoàn trả tồn kho cho các chi tiết cũ
                if (phieuXuat.getChiTietList() != null) {
                    for (ChiTietPhieuXuat chiTiet : phieuXuat.getChiTietList()) {
                        HangHoa hangHoa = chiTiet.getHangHoa();
                        hangHoa.setSoLuongTon(hangHoa.getSoLuongTon() + chiTiet.getSoLuong());
                        hangHoaRepository.save(hangHoa);
                    }
                }
                
                // Xóa chi tiết cũ
                phieuXuat.getChiTietList().clear();
            } else {
                phieuXuat = new PhieuXuat();
                phieuXuat.setMaPhieuXuat(generateMaPhieuXuat());
            }
            
            // Parse ngày xuất
            try {
                LocalDate ngayXuatDate = LocalDate.parse(ngayXuatStr);
                phieuXuat.setNgayXuat(ngayXuatDate.atStartOfDay());
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "Định dạng ngày không hợp lệ!");
                return response;
            }
            
            phieuXuat.setNguoiXuat(getCurrentUserName(session));
            phieuXuat.setGhiChu(ghiChu);
            phieuXuat.setLoaiXuat(loaiXuat); // Set loại xuất hàng (tiền mặt hoặc bán nợ)
            
            // Set khách hàng nếu có
            if (khachHangId != null && khachHangId > 0) {
                Optional<KhachHang> khachHang = khachHangRepository.findById(khachHangId);
                khachHang.ifPresent(phieuXuat::setKhachHang);
            }
            
            BigDecimal tongTien = BigDecimal.ZERO;
            
            // Tạo chi tiết phiếu xuất
            for (int i = 0; i < hangHoaIds.size(); i++) {
                Long hangHoaId = hangHoaIds.get(i);
                Integer soLuong = soLuongs.get(i);
                BigDecimal donGia = donGias.get(i);
                
                if (soLuong <= 0) continue;
                
                // Validate hangHoaId
                if (hangHoaId == null || hangHoaId <= 0) {
                    response.put("success", false);
                    response.put("message", "ID hàng hóa không hợp lệ!");
                    return response;
                }
                
                Optional<HangHoa> hangHoaOpt = hangHoaRepository.findById(hangHoaId);
                if (hangHoaOpt.isPresent()) {
                    HangHoa hangHoa = hangHoaOpt.get();
                    
                    // Kiểm tra tồn kho
                    if (hangHoa.getSoLuongTon() == null || hangHoa.getSoLuongTon() < soLuong) {
                        response.put("success", false);
                        response.put("message", "Hàng hóa '" + hangHoa.getTenHangHoa() + "' không đủ tồn kho!");
                        return response;
                    }
                    
                    // Tạo chi tiết
                    ChiTietPhieuXuat chiTiet = new ChiTietPhieuXuat();
                    chiTiet.setHangHoa(hangHoa);
                    chiTiet.setSoLuong(soLuong);
                    chiTiet.setDonGia(donGia);
                    chiTiet.calculateThanhTien();
                    
                    phieuXuat.addChiTiet(chiTiet);
                    tongTien = tongTien.add(chiTiet.getThanhTien());
                    
                    // Trừ tồn kho
                    hangHoa.setSoLuongTon(hangHoa.getSoLuongTon() - soLuong);
                    hangHoaRepository.save(hangHoa);
                }
            }
            
            phieuXuat.setTongTien(tongTien);
            phieuXuat = phieuXuatRepository.save(phieuXuat);
            
            // Nếu là bán nợ, tạo record trong bảng customer_debt
            if ("BAN_NO".equals(loaiXuat) && phieuXuat.getKhachHang() != null) {
                try {
                    CustomerDebt customerDebt = new CustomerDebt();
                    customerDebt.setKhachHang(phieuXuat.getKhachHang());
                    customerDebt.setSoPhieuXuatBan(phieuXuat.getMaPhieuXuat());
                    customerDebt.setNgayTaoNo(LocalDateTime.now());
                    customerDebt.setTongTienNo(tongTien);
                    customerDebt.setTongTienDaThanhToan(BigDecimal.ZERO);
                    customerDebt.setTongTienConNo(tongTien);
                    customerDebt.setTrangThai(DebtStatus.DANG_NO);
                    
                    // Parse hạn thanh toán nếu có
                    if (ngayHanThanhToanStr != null && !ngayHanThanhToanStr.trim().isEmpty()) {
                        try {
                            LocalDate ngayHanThanhToan = LocalDate.parse(ngayHanThanhToanStr);
                            customerDebt.setNgayHanChot(ngayHanThanhToan);
                            phieuXuat.setNgayHanThanhToan(ngayHanThanhToan.atStartOfDay());
                        } catch (Exception e) {
                            log.warn("Không thể parse ngày hạn thanh toán: " + ngayHanThanhToanStr);
                        }
                    }
                    
                    customerDebt.setGhiChu(ghiChu);
                    customerDebtRepository.save(customerDebt);
                    
                    // Cập nhật phiếu xuất với ngày hạn thanh toán
                    phieuXuatRepository.save(phieuXuat);
                    
                    log.info("✅ [API] Tạo công nợ cho khách hàng: {} - Mã phiếu: {} - Số tiền: {}", 
                        phieuXuat.getKhachHang().getHoTen(), phieuXuat.getMaPhieuXuat(), tongTien);
                } catch (Exception e) {
                    log.error("❌ Lỗi khi tạo CustomerDebt: " + e.getMessage(), e);
                    // Không fail API, chỉ log warning
                    log.warn("⚠️ Tạo CustomerDebt thất bại nhưng vẫn lưu phiếu xuất");
                }
            }
            
            response.put("success", true);
            response.put("phieuXuatId", phieuXuat.getId());
            response.put("message", (isUpdate ? "Cập nhật" : "Tạo") + " phiếu xuất thành công! Mã: " + phieuXuat.getMaPhieuXuat());
            if ("BAN_NO".equals(loaiXuat)) {
                response.put("debtCreated", true);
                response.put("debtMessage", "Đã tạo công nợ cho khách hàng");
            }
            
        } catch (Exception e) {
            log.error("❌ ERROR in savePhieuXuatAjax: " + e.getMessage());
            log.error("Save PhieuXuat error:", e);
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return response;
    }
    
    // In phiếu xuất
    @GetMapping("/{id}/print")
    @Transactional(readOnly = true)
    public String printPhieuXuat(@PathVariable(required = false) Long id, Model model, jakarta.servlet.http.HttpSession session) {
        // Validate id parameter
        if (id == null || id <= 0) {
            return "redirect:/phieu-xuat?error=invalid_id";
        }
        
        // Kiểm tra quyền
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }
        
        // Tìm phiếu xuất
        Optional<PhieuXuat> phieuXuatOpt = phieuXuatRepository.findById(id);
        if (phieuXuatOpt.isEmpty()) {
            return "redirect:/phieu-xuat?error=not_found";
        }
        
        PhieuXuat phieuXuat = phieuXuatOpt.get();
        
        // Force load chiTietList để tránh LazyInitializationException
        if (phieuXuat.getChiTietList() != null) {
            phieuXuat.getChiTietList().size();
        }
        
        // Lấy thông tin cửa hàng
        StoreInfo storeInfo = getStoreInfo();
        
        model.addAttribute("phieuXuat", phieuXuat);
        model.addAttribute("storeInfo", storeInfo);
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("printDate", LocalDateTime.now());
        
        return "phieu-xuat/print";
    }
    
    // In phiếu xuất theo mã phiếu
    @GetMapping("/print-by-ma/{maPhieuXuat}")
    @Transactional(readOnly = true)
    public String printPhieuXuatByMa(@PathVariable String maPhieuXuat, Model model, jakarta.servlet.http.HttpSession session) {
        // Kiểm tra quyền
        if (!isLoggedIn(session)) {
            return "redirect:/auth/login";
        }
        
        // Tìm phiếu xuất theo mã
        PhieuXuat phieuXuat = phieuXuatRepository.findByMaPhieuXuat(maPhieuXuat);
        if (phieuXuat == null) {
            // Nếu không tìm thấy, redirect về trang thống kê với thông báo lỗi
            return "redirect:/hang-hoa/thong-ke?error=phieu_not_found&ma=" + maPhieuXuat;
        }
        
        // Lấy thông tin cửa hàng
        StoreInfo storeInfo = getStoreInfo();
        
        model.addAttribute("phieuXuat", phieuXuat);
        model.addAttribute("storeInfo", storeInfo);
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("printDate", LocalDateTime.now());
        
        return "phieu-xuat/print";
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
