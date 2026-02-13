package com.example.demo.controller;

import com.example.demo.entity.HangHoa;
import com.example.demo.entity.ChiTietPhieuXuat;
import com.example.demo.entity.NhaPhanPhoi;
import com.example.demo.entity.Warranty;
import com.example.demo.entity.debt.SupplierDebt;
import com.example.demo.entity.enums.DebtStatus;
import com.example.demo.repository.HangHoaRepository;
import com.example.demo.repository.ChiTietPhieuXuatRepository;
import com.example.demo.repository.NhaPhanPhoiRepository;
import com.example.demo.repository.SupplierDebtRepository;
import com.example.demo.repository.WarrantyRepository;
import com.example.demo.service.DoanhThuService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/hang-hoa")
public class HangHoaController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(HangHoaController.class);

    @Autowired
    private HangHoaRepository hangHoaRepository;
    
    @Autowired
    private ChiTietPhieuXuatRepository chiTietPhieuXuatRepository;
    
    @Autowired
    private NhaPhanPhoiRepository nhaPhanPhoiRepository;
    
    @Autowired
    private SupplierDebtRepository supplierDebtRepository;
    
    @Autowired
    private WarrantyRepository warrantyRepository;
    
    @Autowired
    private DoanhThuService doanhThuService;

    // Hiển thị danh sách hàng hóa
    @GetMapping({"", "/"})
    public String index(Model model, HttpSession session) {
        // Thêm thông tin user vào model để hiển thị
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        List<HangHoa> danhSachHangHoa = hangHoaRepository.findBySoLuongTonGreaterThan(0);
        if (danhSachHangHoa.isEmpty()) {
            danhSachHangHoa = hangHoaRepository.findAll()
                .stream()
                .filter(h -> h.getSoLuongTon() != null && h.getSoLuongTon() > 0)
                .toList();
        }
        model.addAttribute("danhSachHangHoa", danhSachHangHoa);
        
        // Danh sách hàng hóa hết hàng (tồn kho = 0) sắp xếp theo ngày nhập gần đến xa
        List<HangHoa> danhSachHetHang = hangHoaRepository.findAll()
            .stream()
            .filter(h -> h.getSoLuongTon() != null && h.getSoLuongTon() == 0)
            .sorted((h1, h2) -> {
                // Sắp xếp theo ngày nhập, gần nhất trước
                if (h1.getNgayNhap() == null && h2.getNgayNhap() == null) return 0;
                if (h1.getNgayNhap() == null) return 1;
                if (h2.getNgayNhap() == null) return -1;
                return h2.getNgayNhap().compareTo(h1.getNgayNhap());
            })
            .toList();
        model.addAttribute("danhSachHetHang", danhSachHetHang);
        
        model.addAttribute("pageTitle", "Quản lý Hàng hóa");
        
        // Log truy cập
        log.info("User " + getCurrentUserName(session) + " (" + getCurrentUserRole(session) + ") đã truy cập trang Hàng hóa");
        
        return "hang-hoa/index";
    }

    // Hiển thị form thêm hàng hóa mới
    @GetMapping("/new")
    public String showCreateForm(Model model, HttpSession session) {
        // Kiểm tra đăng nhập
        if (!isLoggedIn(session)) {
            session.setAttribute("redirectUrl", "/hang-hoa/new");
            return "redirect:/auth/login";
        }
        
        HangHoa hangHoa = new HangHoa();
        hangHoa.setNgayNhap(java.time.LocalDate.now());
        // Không tự động tạo serial nữa, để user chọn
        model.addAttribute("hangHoa", hangHoa);
        model.addAttribute("nhaPhanPhois", nhaPhanPhoiRepository.findAll());
        model.addAttribute("pageTitle", "Thêm Hàng hóa mới");
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        return "hang-hoa/form";
    }

    // Xử lý thêm/cập nhật hàng hóa
    @PostMapping("/save")
    public String save(@ModelAttribute HangHoa hangHoa, 
                      @RequestParam(required = false) String continueAdding,
                      @RequestParam(value = "nhaPhanPhoi.maNhaPhanPhoi", required = false) String maNhaPhanPhoi,
                      // Tham số cho mua công nợ
                      @RequestParam(required = false) Boolean isDebtPurchase,
                      @RequestParam(value = "debtSupplier.maNhaPhanPhoi", required = false) String debtSupplierMa,
                      @RequestParam(required = false) LocalDate debtPurchaseDate,
                      @RequestParam(required = false) BigDecimal debtAmount,
                      @RequestParam(required = false) BigDecimal debtPaidAmount,
                      @RequestParam(required = false) LocalDate debtPaymentDeadline,
                      @RequestParam(required = false) String debtPaymentMethod,
                      @RequestParam(required = false) String debtNote,
                      RedirectAttributes redirectAttributes) {
        try {
            // Lưu trạng thái là entity mới TRƯỚC khi save
            boolean isNewEntity = (hangHoa.getId() == null);
            
            // Xử lý nhà phân phối
            if (maNhaPhanPhoi != null && !maNhaPhanPhoi.trim().isEmpty()) {
                Optional<NhaPhanPhoi> npp = nhaPhanPhoiRepository.findById(maNhaPhanPhoi);
                if (npp.isPresent()) {
                    hangHoa.setNhaPhanPhoi(npp.get());
                } else {
                    hangHoa.setNhaPhanPhoi(null);
                }
            } else {
                hangHoa.setNhaPhanPhoi(null);
            }
            
            // Kiểm tra trùng lặp số serial (chỉ khi tạo mới)
            if (isNewEntity) {
                java.util.List<HangHoa> existingSerials = hangHoaRepository.findAll();
                for (HangHoa existing : existingSerials) {
                    if (existing.getSoSerial() != null && existing.getSoSerial().equals(hangHoa.getSoSerial())) {
                        redirectAttributes.addFlashAttribute("errorMessage", 
                            "Số serial '" + hangHoa.getSoSerial() + "' đã tồn tại! Vui lòng sử dụng số serial khác.");
                        return "redirect:/hang-hoa/new";
                    }
                }
            }
            
            // Lưu hàng hóa
            hangHoaRepository.save(hangHoa);
            
            // Xử lý mua công nợ nếu có
            if (isDebtPurchase != null && isDebtPurchase && debtSupplierMa != null && !debtSupplierMa.trim().isEmpty()) {
                try {
                    // Lấy nhà phân phối
                    Optional<NhaPhanPhoi> supplierOpt = nhaPhanPhoiRepository.findById(debtSupplierMa);
                    if (supplierOpt.isPresent() && debtAmount != null && debtAmount.compareTo(BigDecimal.ZERO) > 0) {
                        NhaPhanPhoi supplier = supplierOpt.get();
                        
                        // Tạo công nợ nhà phân phối
                        SupplierDebt supplierDebt = new SupplierDebt();
                        supplierDebt.setNhaPhanPhoi(supplier);
                        supplierDebt.setSoPhieuXuatChi("PKH-" + hangHoa.getId() + "-" + System.currentTimeMillis());
                        supplierDebt.setNgayTaoNo(LocalDateTime.now());
                        supplierDebt.setTongTienNo(debtAmount);
                        
                        // Tính số tiền còn nợ
                        BigDecimal paidAmount = debtPaidAmount != null ? debtPaidAmount : BigDecimal.ZERO;
                        supplierDebt.setTongTienDaThanhToan(paidAmount);
                        supplierDebt.setTongTienConNo(debtAmount.subtract(paidAmount));
                        
                        // Xác định trạng thái
                        if (supplierDebt.getTongTienConNo().compareTo(BigDecimal.ZERO) <= 0) {
                            supplierDebt.setTrangThai(DebtStatus.DA_THANH_TOAN_HET);
                        } else {
                            supplierDebt.setTrangThai(DebtStatus.DANG_NO);
                        }
                        
                        // Thiết lập hạn thanh toán
                        if (debtPaymentDeadline != null) {
                            supplierDebt.setNgayHanChot(debtPaymentDeadline);
                        }
                        
                        // Thiết lập ghi chú
                        String fullNote = "Mua hàng hóa: " + hangHoa.getTenHangHoa() + " (Serial: " + hangHoa.getSoSerial() + ")";
                        if (debtNote != null && !debtNote.trim().isEmpty()) {
                            fullNote += " - " + debtNote;
                        }
                        supplierDebt.setGhiChu(fullNote);
                        
                        // Lưu công nợ
                        supplierDebtRepository.save(supplierDebt);
                        
                        redirectAttributes.addFlashAttribute("successMessage", 
                            "Hàng hóa '" + hangHoa.getTenHangHoa() + "' đã được lưu cùng với công nợ nhà phân phối " + 
                            supplier.getTenNhaPhanPhoi() + " (" + formatCurrency(debtAmount) + ")");
                    } else {
                        redirectAttributes.addFlashAttribute("successMessage", 
                            "Hàng hóa '" + hangHoa.getTenHangHoa() + "' đã được lưu thành công!");
                    }
                } catch (Exception e) {
                    log.error("Lỗi khi tạo công nợ nhà phân phối:", e);
                    redirectAttributes.addFlashAttribute("warningMessage", 
                        "Hàng hóa đã lưu nhưng có lỗi tạo công nợ: " + e.getMessage());
                }
            } else {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Hàng hóa '" + hangHoa.getTenHangHoa() + "' đã được lưu thành công!");
            }
            
            // Giữ lại trên trang hiện tại (form tạo mới)
            if (isNewEntity) {
                return "redirect:/hang-hoa/new";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi lưu hàng hóa: " + e.getMessage());
        }
        return "redirect:/hang-hoa/new";
    }

    // Hiển thị form chỉnh sửa hàng hóa
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable(required = false) Long id, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID hàng hóa không hợp lệ");
            return "redirect:/hang-hoa";
        }
        
        // Kiểm tra đăng nhập
        if (!isLoggedIn(session)) {
            session.setAttribute("redirectUrl", "/hang-hoa/" + id + "/edit");
            return "redirect:/auth/login";
        }
        
        Optional<HangHoa> hangHoaOptional = hangHoaRepository.findById(id);
        if (hangHoaOptional.isPresent()) {
            model.addAttribute("hangHoa", hangHoaOptional.get());
            model.addAttribute("nhaPhanPhois", nhaPhanPhoiRepository.findAll());
            model.addAttribute("pageTitle", "Chỉnh sửa Hàng hóa");
            model.addAttribute("currentUser", getCurrentUserName(session));
            model.addAttribute("currentUserRole", getCurrentUserRole(session));
            return "hang-hoa/form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hàng hóa với ID: " + id);
            return "redirect:/hang-hoa";
        }
    }

    // Route edit ngắn gọn
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        return showEditForm(id, model, redirectAttributes, session);
    }

    // Xử lý cập nhật hàng hóa
    @PostMapping("/{id}")
    public String update(@PathVariable(required = false) Long id, @ModelAttribute HangHoa hangHoa, RedirectAttributes redirectAttributes) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID hàng hóa không hợp lệ");
            return "redirect:/hang-hoa";
        }
        
        try {
            Optional<HangHoa> existingHangHoa = hangHoaRepository.findById(id);
            if (existingHangHoa.isPresent()) {
                hangHoa.setId(id);
                hangHoaRepository.save(hangHoa);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Hàng hóa '" + hangHoa.getTenHangHoa() + "' đã được cập nhật thành công!");
                return "redirect:/hang-hoa/edit/" + id;
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hàng hóa với ID: " + id);
                return "redirect:/hang-hoa";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi cập nhật hàng hóa: " + e.getMessage());
            return "redirect:/hang-hoa/edit/" + id;
        }
    }

    // Xóa hàng hóa
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable(required = false) Long id, RedirectAttributes redirectAttributes) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID hàng hóa không hợp lệ");
            return "redirect:/hang-hoa";
        }
        
        try {
            Optional<HangHoa> hangHoa = hangHoaRepository.findById(id);
            if (hangHoa.isPresent()) {
                // Check if there are any warranty records for this product
                List<Warranty> warranties = warrantyRepository.findByHangHoaId(id);
                
                if (!warranties.isEmpty()) {
                    // Delete all associated warranties first
                    warrantyRepository.deleteAll(warranties);
                    log.info("Deleted " + warranties.size() + " warranty records for product ID: " + id);
                }
                
                // Now delete the product
                hangHoaRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Hàng hóa '" + hangHoa.get().getTenHangHoa() + "' đã được xóa thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hàng hóa với ID: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi xóa hàng hóa: " + e.getMessage());
            log.error("Error deleting product ID: " + id, e);
        }
        return "redirect:/hang-hoa";
    }
    
    // Xóa nhiều hàng hóa
    @PostMapping("/delete-multiple")
    public String deleteMultiple(@RequestParam List<Long> ids, RedirectAttributes redirectAttributes) {
        try {
            int deletedCount = 0;
            StringBuilder deletedNames = new StringBuilder();
            
            for (Long id : ids) {
                // Validate each id
                if (id == null || id <= 0) {
                    continue;
                }
                
                Optional<HangHoa> hangHoa = hangHoaRepository.findById(id);
                if (hangHoa.isPresent()) {
                    // Check if there are any warranty records for this product
                    List<Warranty> warranties = warrantyRepository.findByHangHoaId(id);
                    
                    if (!warranties.isEmpty()) {
                        // Delete all associated warranties first
                        warrantyRepository.deleteAll(warranties);
                        log.info("Deleted " + warranties.size() + " warranty records for product ID: " + id);
                    }
                    
                    // Now delete the product
                    hangHoaRepository.deleteById(id);
                    deletedCount++;
                    if (deletedNames.length() > 0) {
                        deletedNames.append(", ");
                    }
                    deletedNames.append(hangHoa.get().getTenHangHoa());
                }
            }
            
            if (deletedCount > 0) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã xóa thành công " + deletedCount + " hàng hóa: " + deletedNames.toString());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hàng hóa nào để xóa");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi xóa hàng hóa: " + e.getMessage());
            log.error("Error deleting multiple products", e);
        }
        return "redirect:/hang-hoa";
    }

    // Hiển thị chi tiết hàng hóa
    @GetMapping("/{id}")
    public String detail(@PathVariable(required = false) Long id, Model model, RedirectAttributes redirectAttributes) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID hàng hóa không hợp lệ");
            return "redirect:/hang-hoa";
        }
        
        Optional<HangHoa> hangHoaOptional = hangHoaRepository.findById(id);
        if (hangHoaOptional.isPresent()) {
            model.addAttribute("hangHoa", hangHoaOptional.get());
            model.addAttribute("pageTitle", "Chi tiết Hàng hóa");
            return "hang-hoa/detail";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hàng hóa với ID: " + id);
            return "redirect:/hang-hoa";
        }
    }
    
    // Tạo số serial ngẫu nhiên theo format OSS*********** (11 ký tự A-Z hoặc 0-9)
    public String generateRandomSerial() {
        String prefix = "OSS";
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        java.util.Random random = new java.util.Random();
        StringBuilder randomChars = new StringBuilder();
        
        // Tạo 11 ký tự ngẫu nhiên (A-Z, 0-9)
        for (int i = 0; i < 11; i++) {
            randomChars.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        String newSerial = prefix + randomChars.toString();
        
        // Kiểm tra trùng lặp với database
        while (isSerialExists(newSerial)) {
            randomChars = new StringBuilder();
            for (int i = 0; i < 11; i++) {
                randomChars.append(chars.charAt(random.nextInt(chars.length())));
            }
            newSerial = prefix + randomChars.toString();
        }
        
        return newSerial;
    }
    
    // Kiểm tra serial đã tồn tại chưa
    private boolean isSerialExists(String serial) {
        if (serial == null) {
            return false;
        }
        return hangHoaRepository.findBySoSerial(serial).isPresent();
    }
    
    // API endpoint để tạo serial ngẫu nhiên
    @GetMapping("/api/generate-serial")
    @ResponseBody
    public String generateSerialApi() {
        return generateRandomSerial();
    }

    @GetMapping("/api/check-serial")
    @ResponseBody
    public Map<String, Object> checkSerial(@RequestParam String serial,
                                           @RequestParam(required = false) Long excludeId) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = false;
        if (serial != null) {
            Optional<HangHoa> existing = hangHoaRepository.findBySoSerial(serial.trim());
            if (existing.isPresent()) {
                exists = excludeId == null || !existing.get().getId().equals(excludeId);
            }
        }
        response.put("exists", exists);
        return response;
    }
    
    // API: Tìm kiếm hàng hóa theo mã
    @GetMapping("/api/search-by-code")
    @ResponseBody
    public HangHoa searchByCode(@RequestParam String code) {
        return hangHoaRepository.findByMaHangHoa(code);
    }
    
    // API: Lấy tất cả hàng hóa (cho autocomplete)
    @GetMapping("/api/all")
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> getAllHangHoa() {
        // Return simple maps instead of full entities to avoid lazy loading issues
        return hangHoaRepository.findAll().stream()
            .map(hh -> {
                Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("id", hh.getId());
                map.put("tenHangHoa", hh.getTenHangHoa());
                map.put("soSerial", hh.getSoSerial());
                map.put("giaBan", hh.getGiaBan());
                map.put("soLuongTon", hh.getSoLuongTon());
                map.put("maHangHoa", hh.getMaHangHoa());
                return map;
            })
            .toList();
    }
    
    // Hiển thị trang thống kê hàng hóa - Năm hiện tại (mặc định)
    @GetMapping("/thong-ke")
    public String thongKe(Model model, HttpSession session, @RequestParam(required = false) String error, @RequestParam(required = false) String ma) {
        // Kiểm tra đăng nhập
        if (!isLoggedIn(session)) {
            session.setAttribute("redirectUrl", "/hang-hoa/thong-ke");
            return "redirect:/auth/login";
        }

        // Xử lý thông báo lỗi từ việc in phiếu xuất
        if ("phieu_not_found".equals(error) && ma != null && !ma.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Không tìm thấy phiếu xuất với mã: " + ma);
        }

        // Lấy năm hiện tại
        int currentYear = LocalDate.now().getYear();
        LocalDate startOfYear = LocalDate.of(currentYear, 1, 1);
        LocalDate endOfYear = LocalDate.of(currentYear, 12, 31);
        
        LocalDateTime startOfYearDateTime = startOfYear.atStartOfDay();
        LocalDateTime endOfYearDateTime = endOfYear.plusDays(1).atStartOfDay().minusNanos(1);

        // Lấy danh sách chi tiết phiếu xuất và danh sách hàng nhập trong năm
        List<ChiTietPhieuXuat> danhSachBanHang = chiTietPhieuXuatRepository.findAllWithDetails()
            .stream()
            .filter(item -> item.getPhieuXuat() != null
                && item.getPhieuXuat().getNgayXuat() != null
                && !item.getPhieuXuat().getNgayXuat().isBefore(startOfYearDateTime)
                && !item.getPhieuXuat().getNgayXuat().isAfter(endOfYearDateTime))
            .toList();

        List<HangHoa> danhSachNhapHang = hangHoaRepository.findAll()
            .stream()
            .filter(hangHoa -> hangHoa.getNgayNhap() != null
                && !hangHoa.getNgayNhap().isBefore(startOfYear)
                && !hangHoa.getNgayNhap().isAfter(endOfYear))
            .toList();

        List<String> maPhieuXuatList = danhSachBanHang.stream()
            .map(ChiTietPhieuXuat::getPhieuXuat)
            .filter(Objects::nonNull)
            .map(px -> px.getMaPhieuXuat())
            .filter(code -> code != null && !code.isBlank())
            .distinct()
            .sorted()
            .toList();

        // Thêm thông tin doanh thu chính xác (có trừ trả hàng) - năm hiện tại
        BigDecimal doanhThuGop = doanhThuService.getDoanhThuGopTrongThang();
        BigDecimal tongTienTraHang = doanhThuService.getTongTienTraHangTrongThang();
        BigDecimal doanhThuThucTe = doanhThuService.getTongDoanhThuThangCoTraHang();
        
        model.addAttribute("danhSachBanHang", danhSachBanHang);
        model.addAttribute("danhSachNhapHang", danhSachNhapHang);
        model.addAttribute("maPhieuXuatList", maPhieuXuatList);
        model.addAttribute("pageTitle", "Thống kê Hàng hóa - Năm " + currentYear);
        model.addAttribute("yearStart", startOfYear);
        model.addAttribute("yearEnd", endOfYear);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("isYearRange", true);
        model.addAttribute("isFullRange", false); // ✅ Thêm để template biết
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        // Thêm thông tin doanh thu chi tiết
        model.addAttribute("doanhThuGop", doanhThuGop);
        model.addAttribute("tongTienTraHang", tongTienTraHang);
        model.addAttribute("doanhThuThucTe", doanhThuThucTe);

        return "hang-hoa/thong-ke";
    }

    // Thống kê 30 ngày gần nhất
    @GetMapping("/thong-ke/30days")
    public String thongKe30Ngay(Model model, HttpSession session, @RequestParam(required = false) String error, @RequestParam(required = false) String ma) {
        if (!isLoggedIn(session)) {
            session.setAttribute("redirectUrl", "/hang-hoa/thong-ke/30days");
            return "redirect:/auth/login";
        }

        // Xử lý thông báo lỗi từ việc in phiếu xuất
        if ("phieu_not_found".equals(error) && ma != null && !ma.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Không tìm thấy phiếu xuất với mã: " + ma);
        }

        // Giới hạn trong 30 ngày gần nhất
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgoDateTime = now.minusDays(30);
        LocalDate thirtyDaysAgoDate = thirtyDaysAgoDateTime.toLocalDate();

        // Lấy danh sách chi tiết phiếu xuất và danh sách hàng nhập trong khoảng thời gian yêu cầu
        List<ChiTietPhieuXuat> danhSachBanHang = chiTietPhieuXuatRepository.findAllWithDetails()
            .stream()
            .filter(item -> item.getPhieuXuat() != null
                && item.getPhieuXuat().getNgayXuat() != null
                && !item.getPhieuXuat().getNgayXuat().isBefore(thirtyDaysAgoDateTime))
            .toList();

        List<HangHoa> danhSachNhapHang = hangHoaRepository.findAll()
            .stream()
            .filter(hangHoa -> hangHoa.getNgayNhap() != null
                && !hangHoa.getNgayNhap().isBefore(thirtyDaysAgoDate))
            .toList();

        List<String> maPhieuXuatList = danhSachBanHang.stream()
            .map(ChiTietPhieuXuat::getPhieuXuat)
            .filter(Objects::nonNull)
            .map(px -> px.getMaPhieuXuat())
            .filter(code -> code != null && !code.isBlank())
            .distinct()
            .sorted()
            .toList();

        // Thêm thông tin doanh thu chính xác (có trừ trả hàng)
        BigDecimal doanhThuGop = doanhThuService.getDoanhThuGopTrongThang();
        BigDecimal tongTienTraHang = doanhThuService.getTongTienTraHangTrongThang();
        BigDecimal doanhThuThucTe = doanhThuService.getTongDoanhThuThangCoTraHang();
        
        model.addAttribute("danhSachBanHang", danhSachBanHang);
        model.addAttribute("danhSachNhapHang", danhSachNhapHang);
        model.addAttribute("maPhieuXuatList", maPhieuXuatList);
        model.addAttribute("pageTitle", "Thống kê Hàng hóa - 30 Ngày Gần Nhất");
        model.addAttribute("thirtyDayStart", thirtyDaysAgoDate);
        model.addAttribute("thirtyDayEnd", now.toLocalDate());
        model.addAttribute("daysLimit", 30);
        model.addAttribute("isFullRange", false);
        model.addAttribute("isYearRange", false);
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        // Thêm thông tin doanh thu chi tiết
        model.addAttribute("doanhThuGop", doanhThuGop);
        model.addAttribute("tongTienTraHang", tongTienTraHang);
        model.addAttribute("doanhThuThucTe", doanhThuThucTe);

        return "hang-hoa/thong-ke";
    }

    @GetMapping("/thong-ke/today")
    public String thongKeHomNay(Model model, HttpSession session, @RequestParam(required = false) String error, @RequestParam(required = false) String ma) {
        if (!isLoggedIn(session)) {
            session.setAttribute("redirectUrl", "/hang-hoa/thong-ke/today");
            return "redirect:/auth/login";
        }

        // Xử lý thông báo lỗi từ việc in phiếu xuất
        if ("phieu_not_found".equals(error) && ma != null && !ma.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Không tìm thấy phiếu xuất với mã: " + ma);
        }

        // Lấy dữ liệu trong ngày hiện tại (từ 0:00 đến 23:59)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay(); // 0:00:00
        LocalDateTime endOfDay = now.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1); // 23:59:59.999999999
        LocalDate today = now.toLocalDate();

        // Lấy danh sách chi tiết phiếu xuất trong ngày
        List<ChiTietPhieuXuat> danhSachBanHang = chiTietPhieuXuatRepository.findAllWithDetails()
            .stream()
            .filter(item -> item.getPhieuXuat() != null
                && item.getPhieuXuat().getNgayXuat() != null
                && !item.getPhieuXuat().getNgayXuat().isBefore(startOfDay)
                && !item.getPhieuXuat().getNgayXuat().isAfter(endOfDay))
            .toList();

        // Lấy danh sách hàng nhập trong ngày
        List<HangHoa> danhSachNhapHang = hangHoaRepository.findAll()
            .stream()
            .filter(hangHoa -> hangHoa.getNgayNhap() != null
                && hangHoa.getNgayNhap().equals(today))
            .toList();

        List<String> maPhieuXuatList = danhSachBanHang.stream()
            .map(ChiTietPhieuXuat::getPhieuXuat)
            .filter(Objects::nonNull)
            .map(px -> px.getMaPhieuXuat())
            .filter(code -> code != null && !code.isBlank())
            .distinct()
            .sorted()
            .toList();

        // Thêm thông tin doanh thu chính xác cho ngày hôm nay
        BigDecimal doanhThuGop = doanhThuService.getDoanhThuGopHomNay();
        BigDecimal tongTienTraHang = doanhThuService.getTongTienTraHangHomNay();
        BigDecimal doanhThuThucTe = doanhThuService.getTongDoanhThuHomNayCoTraHang();
        
        model.addAttribute("danhSachBanHang", danhSachBanHang);
        model.addAttribute("danhSachNhapHang", danhSachNhapHang);
        model.addAttribute("maPhieuXuatList", maPhieuXuatList);
        model.addAttribute("pageTitle", "Thống kê Hàng hóa - Hôm nay");
        model.addAttribute("thirtyDayStart", today);
        model.addAttribute("thirtyDayEnd", today);
        model.addAttribute("daysLimit", 1);
        model.addAttribute("isFullRange", false);
        model.addAttribute("isYearRange", false);
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        // Thêm thông tin doanh thu chi tiết
        model.addAttribute("doanhThuGop", doanhThuGop);
        model.addAttribute("tongTienTraHang", tongTienTraHang);
        model.addAttribute("doanhThuThucTe", doanhThuThucTe);

        return "hang-hoa/thong-ke";
    }
    
    // Phương thức helper format tiền tệ
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VND";
        return "%,.0f VND".formatted(amount);
    }

    /**
     * REST API endpoint để lấy danh sách hàng hóa tồn kho thời gian thực (JSON)
     * Được sử dụng để cập nhật bảng mà không cần reload trang
     */
    @GetMapping("/api/list")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProductsListApi() {
        try {
            // Sử dụng eager loading để tránh LazyInitializationException
            List<HangHoa> allProducts = hangHoaRepository.findAllWithSupplier();
            
            List<HangHoa> danhSachHangHoa = allProducts.stream()
                .filter(h -> h.getSoLuongTon() != null && h.getSoLuongTon() > 0)
                .toList();
            
            List<HangHoa> danhSachHetHang = allProducts.stream()
                .filter(h -> h.getSoLuongTon() != null && h.getSoLuongTon() == 0)
                .sorted((h1, h2) -> {
                    if (h1.getNgayNhap() == null && h2.getNgayNhap() == null) return 0;
                    if (h1.getNgayNhap() == null) return 1;
                    if (h2.getNgayNhap() == null) return -1;
                    return h2.getNgayNhap().compareTo(h1.getNgayNhap());
                })
                .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("inStock", danhSachHangHoa);
            response.put("outOfStock", danhSachHetHang);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách hàng hóa (API):", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    // API endpoint để tạo hàng hóa mới từ form bảo hành
    @PostMapping("/api/create")
    @ResponseBody
    public HangHoa createProductFromApi(@RequestBody HangHoa hangHoa) {
        try {
            // Validate basic info
            if (hangHoa.getTenHangHoa() == null || hangHoa.getTenHangHoa().trim().isEmpty()) {
                log.error("Lỗi: Tên hàng hóa không được để trống");
                return null;
            }
            
            // Validate soSerial - REQUIRED
            if (hangHoa.getSoSerial() == null || hangHoa.getSoSerial().trim().isEmpty()) {
                log.error("Lỗi: Số Serial không được để trống");
                return null;
            }
            
            // Clean up soSerial
            hangHoa.setSoSerial(hangHoa.getSoSerial().trim());
            
            // Check if soSerial already exists
            java.util.Optional<HangHoa> existing = hangHoaRepository.findBySoSerial(hangHoa.getSoSerial());
            if (existing.isPresent()) {
                log.error("Lỗi: Số Serial '" + hangHoa.getSoSerial() + "' đã tồn tại");
                return null;
            }
            
            // Set created date
            hangHoa.setNgayNhap(LocalDate.now());
            
            // Set default values if not provided
            if (hangHoa.getSoLuongTon() == null) {
                hangHoa.setSoLuongTon(0);
            }
            
            // Save to database
            HangHoa savedHangHoa = hangHoaRepository.save(hangHoa);
            
            log.info("Hàng hóa mới đã được tạo: " + savedHangHoa.getTenHangHoa() + " (Serial: " + savedHangHoa.getSoSerial() + ", ID: " + savedHangHoa.getId() + ")");
            
            return savedHangHoa;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Lỗi: Số Serial có thể đã tồn tại hoặc dữ liệu không hợp lệ", e);
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi tạo hàng hóa mới", e);
            return null;
        }
    }
}