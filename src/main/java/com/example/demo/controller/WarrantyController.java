package com.example.demo.controller;

import com.example.demo.entity.Warranty;
import com.example.demo.entity.WarrantyClaim;
import com.example.demo.entity.HangHoa;
import com.example.demo.entity.KhachHang;
import com.example.demo.entity.ChiTietPhieuXuat;
import com.example.demo.entity.WarrantyTimeline;
import com.example.demo.dto.HangHoaBanDTO;
import com.example.demo.repository.WarrantyRepository;
import com.example.demo.repository.WarrantyClaimRepository;
import com.example.demo.repository.WarrantyTimelineRepository;
import com.example.demo.repository.HangHoaRepository;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.ChiTietPhieuXuatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/bao-hanh")
public class WarrantyController extends BaseController {
    
    private static final Logger log = LoggerFactory.getLogger(WarrantyController.class);
    
    @Autowired
    private WarrantyRepository warrantyRepository;
    
    @Autowired
    private WarrantyClaimRepository warrantyClaimRepository;
    
    @Autowired
    private WarrantyTimelineRepository warrantyTimelineRepository;
    
    @Autowired
    private HangHoaRepository hangHoaRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private ChiTietPhieuXuatRepository chiTietPhieuXuatRepository;
    
    @Autowired
    private HttpServletRequest request;
    
    // ============ QUẢN LÝ BẢO HÀNH ============
    
    // Danh sách bảo hành
    @GetMapping({"", "/"})
    public String list(Model model, jakarta.servlet.http.HttpSession session) {
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        List<Warranty> danhSachBaoHanh = warrantyRepository.findAll();
        model.addAttribute("danhSachBaoHanh", danhSachBaoHanh);
        
        // Thống kê
        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);
        
        long conHieuLuc = danhSachBaoHanh.stream()
            .filter(w -> w.getNgayHetHanBaoHanh().isAfter(today) && "Còn hiệu lực".equals(w.getTrangThai()))
            .count();
        
        long hetHan = danhSachBaoHanh.stream()
            .filter(w -> w.getNgayHetHanBaoHanh().isBefore(today))
            .count();
        
        long sapHetHan = danhSachBaoHanh.stream()
            .filter(w -> w.getNgayHetHanBaoHanh().isAfter(today) && w.getNgayHetHanBaoHanh().isBefore(in30Days))
            .count();
        
        long tongCong = danhSachBaoHanh.size();
        
        model.addAttribute("conHieuLuc", conHieuLuc);
        model.addAttribute("hetHan", hetHan);
        model.addAttribute("sapHetHan", sapHetHan);
        model.addAttribute("tongCong", tongCong);
        
        log.info("User " + getCurrentUserName(session) + " (" + getCurrentUserRole(session) + ") đã truy cập danh sách bảo hành");
        model.addAttribute("pageTitle", "Quản lý Bảo hành");
        return "bao-hanh/list";
    }
    
    // Tạo bảo hành mới
    @GetMapping("/new")
    public String create(Model model, jakarta.servlet.http.HttpSession session) {
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        // Lấy danh sách hàng hóa và khách hàng
        List<HangHoa> hangHoaList = hangHoaRepository.findAll();
        List<KhachHang> khachHangList = khachHangRepository.findAll();
        
        model.addAttribute("hangHoaList", hangHoaList);
        model.addAttribute("khachHangList", khachHangList);
        model.addAttribute("warranty", new Warranty());
        model.addAttribute("pageTitle", "Tạo Bảo hành");
        
        return "bao-hanh/form";
    }
    
    // Lưu bảo hành mới
    @PostMapping("/save")
    public String save(@ModelAttribute Warranty warranty, 
                      @RequestParam(name = "deviceSource", defaultValue = "sold") String deviceSource,
                      RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpSession session) {
        try {
            // Kiểm tra dữ liệu
            if (warranty.getNgayBan() == null || warranty.getNgayHetHanBaoHanh() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng điền đầy đủ ngày bán và ngày hết hạn!");
                return "redirect:/bao-hanh/new";
            }
            
            // Kiểm tra hàng hóa và khách hàng
            if (warranty.getHangHoa() == null || warranty.getHangHoa().getId() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn hàng hóa!");
                return "redirect:/bao-hanh/new";
            }
            
            if (warranty.getKhachHang() == null || warranty.getKhachHang().getId() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn khách hàng!");
                return "redirect:/bao-hanh/new";
            }
            
            // Xử lý theo nguồn thiết bị
            if ("sold".equals(deviceSource)) {
                // Hàng bán ra của công ty - yêu cầu chiTietPhieuXuatId
                if (warranty.getChiTietPhieuXuatId() == null || warranty.getChiTietPhieuXuatId() <= 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không tìm thấy chi tiết phiếu xuất!");
                    return "redirect:/bao-hanh/new";
                }
                
                // Kiểm tra xem hàng hóa có được bán cho khách hàng này không
                List<HangHoa> hangHoaDaSold = chiTietPhieuXuatRepository.findHangHoaBySoldToKhachHang(warranty.getKhachHang().getId());
                boolean hangHoaDuocBan = hangHoaDaSold.stream()
                    .anyMatch(h -> h.getId().equals(warranty.getHangHoa().getId()));
                
                if (!hangHoaDuocBan) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Lỗi: Hàng hóa này chưa được bán cho khách hàng! Vui lòng chọn hàng hóa đã bán cho khách hàng.");
                    return "redirect:/bao-hanh/new";
                }
            } else if ("other".equals(deviceSource)) {
                // Hàng khác (không do công ty bán) - cho phép chiTietPhieuXuatId = null
                warranty.setChiTietPhieuXuatId(null);
            }
            
            // Set trạng thái mặc định
            if (warranty.getTrangThai() == null) {
                warranty.setTrangThai("Còn hiệu lực");
            }
            
            // Set ngayTao nếu chưa có
            if (warranty.getNgayTao() == null) {
                warranty.setNgayTao(java.time.LocalDateTime.now());
            }
            
            Warranty savedWarranty = warrantyRepository.save(warranty);
            
            redirectAttributes.addFlashAttribute("successMessage", "Bảo hành đã được tạo thành công! (ID: " + savedWarranty.getId() + ")");
            log.info("User " + getCurrentUserName(session) + " tạo bảo hành: " + savedWarranty.getId() + " (deviceSource: " + deviceSource + ")");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tạo bảo hành: " + e.getMessage());
            log.error("Lỗi khi tạo bảo hành", e);
        }
        return "redirect:/bao-hanh";
    }
    
    // Chi tiết bảo hành
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Optional<Warranty> warranty = warrantyRepository.findByIdWithRelations(id);
        if (warranty.isPresent()) {
            model.addAttribute("warranty", warranty.get());
            
            // Lấy danh sách yêu cầu bảo hành
            List<WarrantyClaim> claims = warrantyClaimRepository.findByWarrantyId(id);
            model.addAttribute("relatedClaims", claims);
            
            // Kiểm tra tính hợp lệ
            model.addAttribute("isValid", warranty.get().isStillValid());
            model.addAttribute("daysRemaining", warranty.get().getDaysRemaining());
            
            model.addAttribute("pageTitle", "Chi tiết Bảo hành");
            return "bao-hanh/detail";
        } else {
            model.addAttribute("error", "Không tìm thấy bảo hành");
            return "redirect:/bao-hanh";
        }
    }
    
    // Sửa bảo hành
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Optional<Warranty> warranty = warrantyRepository.findByIdWithRelations(id);
        if (warranty.isPresent()) {
            model.addAttribute("warranty", warranty.get());
            model.addAttribute("pageTitle", "Sửa Bảo hành");
            return "bao-hanh/edit";
        } else {
            return "redirect:/bao-hanh";
        }
    }
    
    // Cập nhật bảo hành
    @PostMapping("/{id}/update")
    public String update(@PathVariable(required = false) Long id, @ModelAttribute Warranty updatedWarranty, RedirectAttributes redirectAttributes) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID bảo hành không hợp lệ");
            return "redirect:/bao-hanh";
        }
        
        try {
            Optional<Warranty> warrantyOpt = warrantyRepository.findById(id);
            if (warrantyOpt.isPresent()) {
                Warranty warranty = warrantyOpt.get();
                warranty.setTrangThai(updatedWarranty.getTrangThai());
                warranty.setGhiChu(updatedWarranty.getGhiChu());
                
                warrantyRepository.save(warranty);
                redirectAttributes.addFlashAttribute("successMessage", "Bảo hành đã được cập nhật thành công!");
                log.info("Cập nhật bảo hành: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            log.error("Lỗi khi cập nhật bảo hành", e);
        }
        return "redirect:/bao-hanh/" + id;
    }
    
    // ============ QUẢN LÝ YÊU CẦU BẢO HÀNH ============
    
    // Danh sách yêu cầu bảo hành
    @GetMapping("/yeu-cau/list")
    public String claimList(Model model, jakarta.servlet.http.HttpSession session) {
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        List<WarrantyClaim> danhSachYeuCau = warrantyClaimRepository.findAll();
        model.addAttribute("danhSachYeuCau", danhSachYeuCau);
        
        // Thống kê
        long choXuLy = warrantyClaimRepository.findByTrangThai("Chờ xử lý").size();
        long dangXuLy = warrantyClaimRepository.findByTrangThai("Đang xử lý").size();
        long hoanThanh = warrantyClaimRepository.findByTrangThai("Hoàn thành").size();
        long tongCong = danhSachYeuCau.size();
        
        model.addAttribute("choXuLy", choXuLy);
        model.addAttribute("dangXuLy", dangXuLy);
        model.addAttribute("hoanThanh", hoanThanh);
        model.addAttribute("tongCong", tongCong);
        
        log.info("User " + getCurrentUserName(session) + " (" + getCurrentUserRole(session) + ") đã truy cập yêu cầu bảo hành");
        model.addAttribute("pageTitle", "Yêu Cầu Bảo Hành");
        return "bao-hanh/yeu-cau-list";
    }
    
    // Tạo yêu cầu bảo hành mới
    @GetMapping("/{id}/yeu-cau/new")
    public String createClaim(@PathVariable Long id, Model model) {
        Optional<Warranty> warranty = warrantyRepository.findByIdWithRelations(id);
        if (warranty.isPresent()) {
            if (!warranty.get().isStillValid()) {
                model.addAttribute("error", "Bảo hành đã hết hạn!");
                return "redirect:/bao-hanh/" + id;
            }
            
            model.addAttribute("warranty", warranty.get());
            model.addAttribute("claim", new WarrantyClaim());
            model.addAttribute("pageTitle", "Tạo Yêu Cầu Bảo Hành");
            return "bao-hanh/yeu-cau-form";
        } else {
            return "redirect:/bao-hanh";
        }
    }
    
    // Lưu yêu cầu bảo hành
    @PostMapping("/yeu-cau/save")
    public String saveClaim(@ModelAttribute WarrantyClaim claim, RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra bảo hành có còn hạn không
            if (!claim.getWarranty().isStillValid()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bảo hành đã hết hạn!");
                return "redirect:/bao-hanh/" + claim.getWarranty().getId();
            }
            
            claim.setNgayYeuCau(LocalDate.now());
            claim.setTrangThai("Chờ xử lý");
            warrantyClaimRepository.save(claim);
            
            redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu bảo hành đã được tạo thành công!");
            log.info("Tạo yêu cầu bảo hành: " + claim.getId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tạo yêu cầu: " + e.getMessage());
            log.error("Lỗi khi tạo yêu cầu bảo hành", e);
        }
        return "redirect:/bao-hanh/yeu-cau/list";
    }
    
    // Xử lý yêu cầu bảo hành
    @GetMapping("/yeu-cau/{id}/xu-ly")
    public String processClaim(@PathVariable Long id, Model model) {
        Optional<WarrantyClaim> claim = warrantyClaimRepository.findByIdWithRelations(id);
        if (claim.isPresent()) {
            model.addAttribute("claim", claim.get());
            model.addAttribute("pageTitle", "Xử Lý Yêu Cầu Bảo Hành");
            return "bao-hanh/yeu-cau-process";
        } else {
            return "redirect:/bao-hanh/yeu-cau/list";
        }
    }
    
    // Cập nhật xử lý yêu cầu bảo hành
    @PostMapping("/yeu-cau/{id}/update")
    public String updateClaim(@PathVariable Long id, @ModelAttribute WarrantyClaim claimData, RedirectAttributes redirectAttributes) {
        Optional<WarrantyClaim> claimOpt = warrantyClaimRepository.findByIdWithRelations(id);
        if (claimOpt.isPresent()) {
            WarrantyClaim claim = claimOpt.get();
            claim.setTrangThaiXuly(claimData.getTrangThaiXuly());
            claim.setGhiChuXuly(claimData.getGhiChuXuly());
            if ("Hoàn thành".equals(claimData.getTrangThaiXuly())) {
                claim.setNgayHoanThanh(LocalDate.now());
            }
            warrantyClaimRepository.save(claim);
            redirectAttributes.addFlashAttribute("message", "Cập nhật xử lý thành công");
            return "redirect:/bao-hanh/yeu-cau/list";
        }
        return "redirect:/bao-hanh/yeu-cau/list";
    }

    // ============ HOÀN THÀNH BẢO HÀNH ============

    // Form hoàn thành bảo hành
    @GetMapping("/{id}/hoan-thanh")
    public String completeForm(@PathVariable Long id, Model model) {
        Optional<Warranty> warranty = warrantyRepository.findByIdWithRelations(id);
        if (warranty.isPresent()) {
            model.addAttribute("warranty", warranty.get());
            model.addAttribute("pageTitle", "Hoàn Thành Bảo Hành");
            return "bao-hanh/complete";
        }
        return "redirect:/bao-hanh";
    }

    // Hoàn thành bảo hành
    @PostMapping("/{id}/hoan-thanh")
    public String complete(@PathVariable(required = false) Long id, 
                          @RequestParam(required = false) String ghiChuHoanThanh,
                          RedirectAttributes redirectAttributes) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID bảo hành không hợp lệ");
            return "redirect:/bao-hanh";
        }
        
        try {
            Optional<Warranty> warranty = warrantyRepository.findById(id);
            if (warranty.isPresent()) {
                Warranty w = warranty.get();
                
                // Kiểm tra trạng thái hiện tại
                if ("Hoàn thành".equals(w.getTrangThai())) {
                    redirectAttributes.addFlashAttribute("warningMessage", "Bảo hành này đã được hoàn thành trước đó!");
                    return "redirect:/bao-hanh/" + id;
                }
                
                // Cập nhật trạng thái thành "Hoàn thành"
                w.setTrangThai("Hoàn thành");
                
                // Nếu có ghi chú hoàn thành, thêm vào ghi chú cũ
                if (ghiChuHoanThanh != null && !ghiChuHoanThanh.trim().isEmpty()) {
                    String ghiChuCu = w.getGhiChu() != null ? w.getGhiChu() : "";
                    w.setGhiChu(ghiChuCu + "\n[Hoàn thành - " + java.time.LocalDate.now() + "]: " + ghiChuHoanThanh);
                }
                
                // Lưu timeline: "Trả thiết bị cho khách hàng"
                WarrantyTimeline finalTimeline = new WarrantyTimeline();
                finalTimeline.setWarranty(w);
                finalTimeline.setBuocThucHien("Trả thiết bị cho khách hàng");
                finalTimeline.setThoiGianThucHien(java.time.LocalDateTime.now());
                finalTimeline.setGhiChu(ghiChuHoanThanh);
                finalTimeline.setNguoiThucHien(getCurrentUserName(request.getSession()));
                warrantyTimelineRepository.save(finalTimeline);
                
                // Lưu warranty
                warrantyRepository.save(w);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Bảo hành đã được hoàn thành thành công!");
                log.info("Hoàn thành bảo hành: " + id + " bởi " + getCurrentUserName(request.getSession()));
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bảo hành!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi hoàn thành: " + e.getMessage());
            log.error("Lỗi khi hoàn thành bảo hành", e);
        }
        return "redirect:/bao-hanh/" + id;
    }

    // API: Hoàn thành bảo hành (via AJAX)
    @PostMapping("/api/{id}/hoan-thanh")
    @ResponseBody
    public ResponseEntity<?> completeApi(@PathVariable(required = false) Long id,
                                        @RequestParam(required = false) String ghiChuHoanThanh,
                                        jakarta.servlet.http.HttpSession session) {
        // Validate id parameter
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body(
                java.util.Map.of("success", false, "message", "ID bảo hành không hợp lệ")
            );
        }
        
        try {
            Optional<Warranty> warrantyOpt = warrantyRepository.findByIdWithRelations(id);
            if (warrantyOpt.isPresent()) {
                Warranty warranty = warrantyOpt.get();
                
                if ("Hoàn thành".equals(warranty.getTrangThai())) {
                    return ResponseEntity.badRequest().body(
                        java.util.Map.of("success", false, "message", "Bảo hành đã được hoàn thành trước đó")
                    );
                }
                
                // Cập nhật trạng thái
                warranty.setTrangThai("Hoàn thành");
                if (ghiChuHoanThanh != null && !ghiChuHoanThanh.trim().isEmpty()) {
                    String ghiChuCu = warranty.getGhiChu() != null ? warranty.getGhiChu() : "";
                    warranty.setGhiChu(ghiChuCu + "\n[Hoàn thành - " + java.time.LocalDate.now() + "]: " + ghiChuHoanThanh);
                }
                
                // Lưu timeline
                WarrantyTimeline finalTimeline = new WarrantyTimeline();
                finalTimeline.setWarranty(warranty);
                finalTimeline.setBuocThucHien("Trả thiết bị cho khách hàng");
                finalTimeline.setThoiGianThucHien(java.time.LocalDateTime.now());
                finalTimeline.setGhiChu(ghiChuHoanThanh);
                finalTimeline.setNguoiThucHien(getCurrentUserName(session));
                warrantyTimelineRepository.save(finalTimeline);
                
                // Lưu warranty
                warrantyRepository.save(warranty);
                
                log.info("Hoàn thành bảo hành via API: " + id);
                return ResponseEntity.ok(
                    java.util.Map.of("success", true, "message", "Bảo hành đã được hoàn thành")
                );
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi hoàn thành bảo hành via API", e);
            return ResponseEntity.badRequest().body(
                java.util.Map.of("success", false, "message", "Lỗi: " + e.getMessage())
            );
        }
    }

    // API: Lấy danh sách bảo hành còn hiệu lực
    @GetMapping("/api/valid")
    @ResponseBody
    public ResponseEntity<List<Warranty>> getValidWarranties() {
        LocalDate today = LocalDate.now();
        List<Warranty> validWarranties = warrantyRepository.findByNgayHetHanBaoHanhGreaterThanEqual(today);
        return ResponseEntity.ok(validWarranties);
    }
    
    // API: Lấy danh sách yêu cầu chờ xử lý
    @GetMapping("/api/pending-claims")
    @ResponseBody
    public ResponseEntity<List<WarrantyClaim>> getPendingClaims() {
        return ResponseEntity.ok(warrantyClaimRepository.findByTrangThai("Chờ xử lý"));
    }
    
    // API: Lấy danh sách hàng hóa đã bán cho khách hàng
    @GetMapping("/api/hang-hoa-by-khach-hang/{khachHangId}")
    @ResponseBody
    public ResponseEntity<List<HangHoa>> getHangHoaByKhachHang(@PathVariable Long khachHangId) {
        try {
            List<HangHoa> hangHoaList = chiTietPhieuXuatRepository.findHangHoaBySoldToKhachHang(khachHangId);
            return ResponseEntity.ok(hangHoaList);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách hàng hóa cho khách hàng: " + khachHangId, e);
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }
    
    // API: Lấy danh sách hàng hóa + ngày bán cho khách hàng
    @GetMapping("/api/hang-hoa-with-ngay-ban/{khachHangId}")
    @ResponseBody
    public ResponseEntity<List<HangHoaBanDTO>> getHangHoaWithNgayBan(@PathVariable Long khachHangId) {
        try {
            List<HangHoaBanDTO> hangHoaList = chiTietPhieuXuatRepository.findHangHoaBanWithNgayBan(khachHangId);
            return ResponseEntity.ok(hangHoaList);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách hàng hóa + ngày bán cho khách hàng: " + khachHangId, e);
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }
    
    // API: Lấy chiTietPhieuXuatId từ khachHangId + hangHoaId
    @GetMapping("/api/chi-tiet-phieu-xuat/{khachHangId}/{hangHoaId}")
    @ResponseBody
    public ResponseEntity<Long> getChiTietPhieuXuatId(@PathVariable Long khachHangId, @PathVariable Long hangHoaId) {
        try {
            // Query ChiTietPhieuXuat theo khachHangId và hangHoaId
            List<ChiTietPhieuXuat> chiTietList = chiTietPhieuXuatRepository.findByKhachHangId(khachHangId);
            
            // Lọc theo hangHoaId
            ChiTietPhieuXuat chiTiet = chiTietList.stream()
                .filter(ct -> ct.getHangHoa() != null && ct.getHangHoa().getId().equals(hangHoaId))
                .findFirst()
                .orElse(null);
            
            if (chiTiet != null) {
                return ResponseEntity.ok(chiTiet.getId());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy chiTietPhieuXuatId", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // API: Lấy danh sách khách hàng
    @GetMapping("/api/khach-hang-list")
    @ResponseBody
    public ResponseEntity<List<KhachHang>> getKhachHangList() {
        try {
            List<KhachHang> khachHangList = khachHangRepository.findAll();
            return ResponseEntity.ok(khachHangList);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách khách hàng", e);
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }
    
    // ============ QUẢN LÝ QUY TRÌNH THỜI GIAN BẢO HÀNH (TIMELINE) ============
    
    // Hiển thị timeline chi tiết trên trang chi tiết bảo hành
    // (được gọi từ detail.html thông qua fetch API)
    @Transactional(readOnly = true)
    @GetMapping("/{id}/timeline-list")
    @ResponseBody
    public ResponseEntity<?> getTimelineList(@PathVariable Long id) {
        try {
            Optional<Warranty> warranty = warrantyRepository.findByIdWithRelations(id);
            if (warranty.isPresent()) {
                List<WarrantyTimeline> timelines = warranty.get().getTimelines();
                // Ensure timelines are initialized
                if (timelines != null) {
                    timelines.size(); // Force load
                }
                return ResponseEntity.ok(timelines != null ? timelines : new java.util.ArrayList<>());
            }
            return ResponseEntity.ok(new java.util.ArrayList<>());
        } catch (Exception e) {
            log.error("Lỗi khi lấy timeline list: " + id, e);
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }
    
    // Form thêm bước thực hiện trong quy trình
    @GetMapping("/{id}/timeline/add")
    public String addTimeline(@PathVariable Long id, Model model) {
        Optional<Warranty> warranty = warrantyRepository.findByIdWithRelations(id);
        if (warranty.isPresent()) {
            model.addAttribute("warranty", warranty.get());
            model.addAttribute("pageTitle", "Thêm Bước Quy Trình Bảo Hành");
            model.addAttribute("buocOptions", java.util.Arrays.asList(
                "Nhận thiết bị",
                "Gửi nhà phân phối/hãng",
                "Lấy về từ nhà phân phối/hãng",
                "Trả thiết bị cho khách hàng"
            ));
            return "bao-hanh/timeline-add";
        }
        return "redirect:/bao-hanh";
    }
    
    /**
     * Định nghĩa thứ tự các bước trong quy trình bảo hành
     */
    private static final java.util.List<String> WORKFLOW_STEPS = java.util.Arrays.asList(
        "Nhận thiết bị",
        "Gửi nhà phân phối - bên bán",
        "Lấy về từ nhà phân phối - bên bán",
        "Trả thiết bị cho khách hàng"
    );
    
    /**
     * Kiểm tra xem bước tiếp theo có hợp lệ không
     * Phải tuân theo thứ tự: 1 -> 2 -> 3 -> 4
     */
    private String validateNextStep(Warranty warranty, String nextStep) {
        List<WarrantyTimeline> completedSteps = warranty.getTimelines();
        
        // Nếu chưa có bước nào, bước tiếp theo phải là bước đầu tiên
        if (completedSteps.isEmpty()) {
            if (!nextStep.equals(WORKFLOW_STEPS.get(0))) {
                return "Bước đầu tiên phải là: " + WORKFLOW_STEPS.get(0);
            }
            return null; // Valid
        }
        
        // Lấy bước cuối cùng đã hoàn thành
        String lastCompletedStep = completedSteps.get(completedSteps.size() - 1).getBuocThucHien();
        int lastStepIndex = WORKFLOW_STEPS.indexOf(lastCompletedStep);
        
        if (lastStepIndex == -1) {
            return "Bước trước đó không hợp lệ trong hệ thống";
        }
        
        // Nếu đã hoàn thành tất cả các bước
        if (lastStepIndex == WORKFLOW_STEPS.size() - 1) {
            return "Quy trình bảo hành đã hoàn thành! Không thể thêm bước mới.";
        }
        
        // Bước tiếp theo phải là bước sau bước cuối cùng
        String expectedNextStep = WORKFLOW_STEPS.get(lastStepIndex + 1);
        if (!nextStep.equals(expectedNextStep)) {
            return "Bước tiếp theo phải là: " + expectedNextStep;
        }
        
        return null; // Valid
    }
    
    // Lưu bước thực hiện trong quy trình
    @PostMapping("/{id}/timeline/save")
    public String saveTimeline(@PathVariable(required = false) Long id, 
                              @RequestParam String buocThucHien,
                              @RequestParam(required = false) String ghiChu,
                              @RequestParam(required = false) String nguoiThucHien,
                              RedirectAttributes redirectAttributes) {
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID bảo hành không hợp lệ");
            return "redirect:/bao-hanh";
        }
        
        try {
            Optional<Warranty> warranty = warrantyRepository.findByIdWithRelations(id);
            if (warranty.isPresent()) {
                // Kiểm tra xem bước này có hợp lệ không
                String validationError = validateNextStep(warranty.get(), buocThucHien);
                if (validationError != null) {
                    redirectAttributes.addFlashAttribute("errorMessage", validationError);
                    return "redirect:/bao-hanh/" + id + "/timeline/add";
                }
                
                // Tạo timeline mới
                WarrantyTimeline timeline = new WarrantyTimeline();
                timeline.setWarranty(warranty.get());
                timeline.setBuocThucHien(buocThucHien);
                timeline.setThoiGianThucHien(java.time.LocalDateTime.now());
                timeline.setGhiChu(ghiChu);
                timeline.setNguoiThucHien(nguoiThucHien);
                
                // Lưu timeline vào database
                warrantyTimelineRepository.save(timeline);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Bước '" + buocThucHien + "' đã được thêm thành công!");
                
                // Nếu đã hoàn thành bước cuối cùng, cập nhật trạng thái bảo hành
                if (buocThucHien.equals(WORKFLOW_STEPS.get(WORKFLOW_STEPS.size() - 1))) {
                    Warranty w = warranty.get();
                    w.setTrangThai("Hoàn thành");
                    warrantyRepository.save(w);
                    redirectAttributes.addFlashAttribute("infoMessage", 
                        "Quy trình bảo hành đã hoàn thành!");
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi lưu timeline", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu: " + e.getMessage());
        }
        return "redirect:/bao-hanh/" + id;
    }
    
    /**
     * API để lấy danh sách bước tiếp theo có sẵn
     */
    @GetMapping("/{id}/next-steps")
    @ResponseBody
    public ResponseEntity<?> getNextSteps(@PathVariable Long id) {
        try {
            Optional<Warranty> warranty = warrantyRepository.findByIdWithRelations(id);
            if (warranty.isPresent()) {
                List<WarrantyTimeline> completedSteps = warranty.get().getTimelines();
                
                // Nếu chưa có bước nào
                if (completedSteps.isEmpty()) {
                    return ResponseEntity.ok(java.util.Collections.singletonList(WORKFLOW_STEPS.get(0)));
                }
                
                String lastCompletedStep = completedSteps.get(completedSteps.size() - 1).getBuocThucHien();
                int lastStepIndex = WORKFLOW_STEPS.indexOf(lastCompletedStep);
                
                // Nếu đã hoàn thành tất cả các bước
                if (lastStepIndex == WORKFLOW_STEPS.size() - 1) {
                    return ResponseEntity.ok(java.util.Collections.emptyList());
                }
                
                // Trả về bước tiếp theo
                String nextStep = WORKFLOW_STEPS.get(lastStepIndex + 1);
                return ResponseEntity.ok(java.util.Collections.singletonList(nextStep));
            }
            return ResponseEntity.ok(java.util.Collections.emptyList());
        } catch (Exception e) {
            log.error("Lỗi khi lấy next steps: " + id, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
