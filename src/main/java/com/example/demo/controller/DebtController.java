package com.example.demo.controller;

import com.example.demo.dto.CustomerDebtDetailDTO;
import com.example.demo.dto.SupplierDebtDetailDTO;
import com.example.demo.entity.debt.*;
import com.example.demo.entity.enums.DebtStatus;
import com.example.demo.entity.enums.PaymentMethod;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/debt")
public class DebtController {
    
    @Autowired private SupplierDebtRepository supplierDebtRepository;
    @Autowired private SupplierDebtPaymentRepository supplierDebtPaymentRepository;
    @Autowired private CustomerDebtRepository customerDebtRepository;
    @Autowired private CustomerDebtPaymentRepository customerDebtPaymentRepository;
    @Autowired private PhieuXuatRepository phieuXuatRepository;
    
    @GetMapping("/supplier")
    public ResponseEntity<?> getAllSupplierDebts() {
        List<SupplierDebt> debts = supplierDebtRepository.findAllWithNhaPhanPhoi();
        List<SupplierDebtDetailDTO> dtos = debts.stream()
            .map(d -> new SupplierDebtDetailDTO(
                d.getId(),
                d.getSoPhieuXuatChi(),
                d.getNhaPhanPhoi() != null ? d.getNhaPhanPhoi().getTenNhaPhanPhoi() : "N/A",
                d.getNhaPhanPhoi() != null ? d.getNhaPhanPhoi().getDiaChi() : "N/A",
                d.getNhaPhanPhoi() != null ? d.getNhaPhanPhoi().getSoDienThoai() : "N/A",
                d.getTongTienNo(),
                d.getTongTienDaThanhToan(),
                d.getTongTienConNo(),
                d.getTrangThai() != null ? d.getTrangThai().toString() : "N/A",
                d.getNgayHanChot(),
                d.getGhiChu(),
                d.getNgayTaoNo(),
                new ArrayList<>() // Empty payments for list view
            ))
            .toList();
        return ResponseEntity.ok(Map.of("success", true, "count", dtos.size(), "data", dtos));
    }
    
    @GetMapping("/supplier/overdue/list")
    public ResponseEntity<?> getOverdueSupplierDebts() {
        LocalDate today = LocalDate.now();
        List<SupplierDebt> debts = supplierDebtRepository.findByNgayHanChotBefore(today);
        debts.removeIf(d -> d.getTrangThai() == DebtStatus.DA_THANH_TOAN_HET);
        return ResponseEntity.ok(Map.of("success", true, "count", debts.size(), "data", debts));
    }
    
    @PostMapping("/supplier/payment")
    public ResponseEntity<?> recordSupplierPayment(@RequestBody Map<String, Object> request) {
        try {
            Long debtId = Long.parseLong(request.get("debtId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String method = request.get("method").toString();
            String note = request.getOrDefault("note", "").toString();
            
            Optional<SupplierDebt> debtOpt = supplierDebtRepository.findById(debtId);
            if (debtOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Not found"));
            }
            
            SupplierDebt debt = debtOpt.get();
            if (amount.compareTo(debt.getTongTienConNo()) > 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Amount exceeds debt"));
            }
            
            SupplierDebtPayment payment = new SupplierDebtPayment();
            payment.setSupplierDebt(debt);
            payment.setSoPhieuThanhToan(generateCode("PTT"));
            payment.setNgayThanhToan(LocalDateTime.now());
            payment.setSoTienThanhToan(amount);
            payment.setPhuongThucThanhToan(PaymentMethod.valueOf(method));
            payment.setGhiChu(note);
            
            BigDecimal newPaid = debt.getTongTienDaThanhToan().add(amount);
            debt.setTongTienDaThanhToan(newPaid);
            debt.setTongTienConNo(debt.getTongTienNo().subtract(newPaid));
            
            if (debt.getTongTienConNo().compareTo(BigDecimal.ZERO) <= 0) {
                debt.setTrangThai(DebtStatus.DA_THANH_TOAN_HET);
            } else {
                debt.setTrangThai(DebtStatus.THANH_TOAN_TUAN_TUAN);
            }
            
            supplierDebtRepository.save(debt);
            supplierDebtPaymentRepository.save(payment);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Payment recorded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    @GetMapping("/supplier/{debtId}/payments")
    public ResponseEntity<?> getSupplierPayments(@PathVariable(required = false) Long debtId) {
        // Validate debtId parameter
        if (debtId == null || debtId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Debt ID không hợp lệ"));
        }
        
        Optional<SupplierDebt> debt = supplierDebtRepository.findById(debtId);
        if (debt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Not found"));
        }
        List<SupplierDebtPayment> payments = supplierDebtPaymentRepository.findBySupplierDebt(debt.get());
        return ResponseEntity.ok(Map.of("success", true, "count", payments.size(), "data", payments));
    }
    
    @GetMapping("/customer")
    public ResponseEntity<?> getAllCustomerDebts() {
        List<CustomerDebt> debts = customerDebtRepository.findAll();
        return ResponseEntity.ok(Map.of("success", true, "count", debts.size(), "data", debts));
    }
    
    @GetMapping("/customer/overdue/list")
    public ResponseEntity<?> getOverdueCustomerDebts() {
        LocalDate today = LocalDate.now();
        List<CustomerDebt> debts = customerDebtRepository.findByNgayHanChotBefore(today);
        debts.removeIf(d -> d.getTrangThai() == DebtStatus.DA_THANH_TOAN_HET);
        return ResponseEntity.ok(Map.of("success", true, "count", debts.size(), "data", debts));
    }
    
    @PostMapping("/customer/payment")
    public ResponseEntity<?> recordCustomerPayment(@RequestBody Map<String, Object> request) {
        try {
            Long debtId = Long.parseLong(request.get("debtId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String method = request.get("method").toString();
            String note = request.getOrDefault("note", "").toString();
            
            Optional<CustomerDebt> debtOpt = customerDebtRepository.findById(debtId);
            if (debtOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Not found"));
            }
            
            CustomerDebt debt = debtOpt.get();
            if (amount.compareTo(debt.getTongTienConNo()) > 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Amount exceeds debt"));
            }
            
            CustomerDebtPayment collection = new CustomerDebtPayment();
            collection.setCustomerDebt(debt);
            collection.setSoPhieuThuHoi(generateCode("PTH"));
            collection.setNgayThuHoi(LocalDateTime.now());
            collection.setSoTienThuHoi(amount);
            collection.setPhuongThucThuHoi(PaymentMethod.valueOf(method));
            collection.setGhiChu(note);
            
            BigDecimal newCollected = debt.getTongTienDaThanhToan().add(amount);
            debt.setTongTienDaThanhToan(newCollected);
            debt.setTongTienConNo(debt.getTongTienNo().subtract(newCollected));
            
            if (debt.getTongTienConNo().compareTo(BigDecimal.ZERO) <= 0) {
                debt.setTrangThai(DebtStatus.DA_THANH_TOAN_HET);
            } else {
                debt.setTrangThai(DebtStatus.THANH_TOAN_TUAN_TUAN);
            }
            
            customerDebtRepository.save(debt);
            customerDebtPaymentRepository.save(collection);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Collection recorded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    @GetMapping("/customer/{debtId}/collections")
    public ResponseEntity<?> getCustomerCollections(@PathVariable(required = false) Long debtId) {
        // Validate debtId parameter
        if (debtId == null || debtId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Debt ID không hợp lệ"));
        }
        
        Optional<CustomerDebt> debt = customerDebtRepository.findById(debtId);
        if (debt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Not found"));
        }
        List<CustomerDebtPayment> collections = customerDebtPaymentRepository.findByCustomerDebt(debt.get());
        return ResponseEntity.ok(Map.of("success", true, "count", collections.size(), "data", collections));
    }
    
    @GetMapping("/customer/{debtId}")
    public ResponseEntity<?> getCustomerDebtById(@PathVariable Long debtId) {
        // Use JOIN FETCH to eagerly load KhachHang to avoid LazyInitializationException
        Optional<CustomerDebt> debt = customerDebtRepository.findByIdWithKhachHang(debtId);
        if (debt.isPresent()) {
            CustomerDebt d = debt.get();
            // Convert to DTO to avoid serialization issues
            List<CustomerDebtDetailDTO.PaymentDetailDTO> paymentDTOs = 
                d.getPayments().stream()
                    .map(p -> new CustomerDebtDetailDTO.PaymentDetailDTO(
                        p.getId(),
                        p.getNgayThuHoi() != null ? p.getNgayThuHoi().toLocalDate() : LocalDate.now(),
                        p.getSoPhieuThuHoi(),
                        p.getSoTienThuHoi(),
                        p.getPhuongThucThuHoi() != null ? p.getPhuongThucThuHoi().toString() : "N/A"
                    ))
                    .toList();
            
            CustomerDebtDetailDTO dto = new CustomerDebtDetailDTO(
                d.getId(),
                d.getSoPhieuXuatBan(),
                d.getKhachHang() != null ? d.getKhachHang().getHoTen() : "N/A",
                d.getKhachHang() != null ? d.getKhachHang().getDiaChi() : "N/A",
                d.getKhachHang() != null ? d.getKhachHang().getSoDienThoai() : "N/A",
                d.getTongTienNo(),
                d.getTongTienDaThanhToan(),
                d.getTongTienConNo(),
                d.getTrangThai() != null ? d.getTrangThai().toString() : "N/A",
                d.getNgayHanChot(),
                d.getGhiChu(),
                d.getNgayTaoNo(),
                paymentDTOs
            );
            return ResponseEntity.ok(Map.of("success", true, "data", dto));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Not found"));
    }
    
    @GetMapping("/supplier/{debtId}")
    public ResponseEntity<?> getSupplierDebtById(@PathVariable Long debtId) {
        // Use JOIN FETCH to eagerly load NhaPhanPhoi to avoid LazyInitializationException
        Optional<SupplierDebt> debt = supplierDebtRepository.findByIdWithNhaPhanPhoi(debtId);
        if (debt.isPresent()) {
            SupplierDebt d = debt.get();
            // Convert to DTO to avoid serialization issues
            List<SupplierDebtDetailDTO.PaymentDetailDTO> paymentDTOs = 
                d.getPayments().stream()
                    .map(p -> new SupplierDebtDetailDTO.PaymentDetailDTO(
                        p.getId(),
                        p.getNgayThanhToan() != null ? p.getNgayThanhToan().toLocalDate() : LocalDate.now(),
                        p.getSoPhieuThanhToan(),
                        p.getSoTienThanhToan(),
                        p.getPhuongThucThanhToan() != null ? p.getPhuongThucThanhToan().toString() : "N/A"
                    ))
                    .toList();
            
            SupplierDebtDetailDTO dto = new SupplierDebtDetailDTO(
                d.getId(),
                d.getSoPhieuXuatChi(),
                d.getNhaPhanPhoi() != null ? d.getNhaPhanPhoi().getTenNhaPhanPhoi() : "N/A",
                d.getNhaPhanPhoi() != null ? d.getNhaPhanPhoi().getDiaChi() : "N/A",
                d.getNhaPhanPhoi() != null ? d.getNhaPhanPhoi().getSoDienThoai() : "N/A",
                d.getTongTienNo(),
                d.getTongTienDaThanhToan(),
                d.getTongTienConNo(),
                d.getTrangThai() != null ? d.getTrangThai().toString() : "N/A",
                d.getNgayHanChot(),
                d.getGhiChu(),
                d.getNgayTaoNo(),
                paymentDTOs
            );
            return ResponseEntity.ok(Map.of("success", true, "data", dto));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Not found"));
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        BigDecimal totalSupplier = BigDecimal.ZERO;
        for (SupplierDebt d : supplierDebtRepository.findByTrangThai(DebtStatus.DANG_NO)) {
            totalSupplier = totalSupplier.add(d.getTongTienConNo());
        }
        
        BigDecimal totalCustomer = BigDecimal.ZERO;
        for (CustomerDebt d : customerDebtRepository.findByTrangThai(DebtStatus.DANG_NO)) {
            totalCustomer = totalCustomer.add(d.getTongTienConNo());
        }
        
        LocalDate today = LocalDate.now();
        int overdueSupplier = supplierDebtRepository.findByNgayHanChotBefore(today).size();
        int overdueCustomer = customerDebtRepository.findByNgayHanChotBefore(today).size();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", Map.of(
                "totalSupplier", totalSupplier,
                "totalCustomer", totalCustomer,
                "overdueSupplier", overdueSupplier,
                "overdueCustomer", overdueCustomer
            )
        ));
    }
    
    /**
     * API để lấy chi tiết hàng hóa từ phiếu xuất theo mã phiếu xuất
     * Dùng cho modal "Chi tiết" trên trang danh sách công nợ khách hàng
     */
    @GetMapping("/customer/invoice-items/{soPhieuXuatBan}")
    public ResponseEntity<?> getInvoiceItems(@PathVariable String soPhieuXuatBan) {
        try {
            // Tìm phiếu xuất theo mã
            com.example.demo.entity.PhieuXuat phieuXuat = phieuXuatRepository.findByMaPhieuXuat(soPhieuXuatBan);
            if (phieuXuat == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Phiếu xuất không tìm thấy"));
            }
            
            // Lấy danh sách chi tiết
            List<Map<String, Object>> items = new ArrayList<>();
            if (phieuXuat.getChiTietList() != null) {
                for (com.example.demo.entity.ChiTietPhieuXuat chiTiet : phieuXuat.getChiTietList()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("hangHoaId", chiTiet.getHangHoa().getId());
                    item.put("tenHangHoa", chiTiet.getHangHoa().getTenHangHoa());
                    item.put("soLuong", chiTiet.getSoLuong());
                    item.put("donGia", chiTiet.getDonGia());
                    item.put("thanhTien", chiTiet.getThanhTien());
                    item.put("ghiChu", chiTiet.getGhiChu());
                    items.add(item);
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "maPhieuXuat", phieuXuat.getMaPhieuXuat(),
                "ngayXuat", phieuXuat.getNgayXuat().toLocalDate(),
                "tongTien", phieuXuat.getTongTien(),
                "items", items,
                "count", items.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    private String generateCode(String prefix) {
        return prefix + System.currentTimeMillis();
    }
}
