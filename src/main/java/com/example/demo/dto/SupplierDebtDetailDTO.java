package com.example.demo.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Supplier Debt Detail - prevents serialization issues
 * Only includes necessary fields to avoid LazyInitializationException
 */
public class SupplierDebtDetailDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String soPhieuMua;
    private String nhaPhanPhoiName;
    private String nhaPhanPhoiDiaChi;
    private String nhaPhanPhoiSDT;
    private BigDecimal tongTienNo;
    private BigDecimal tongTienDaThanhToan;
    private BigDecimal tongTienConNo;
    private String trangThai;
    private LocalDate ngayHanChot;
    private String ghiChu;
    private LocalDateTime ngayTaoNo;
    private List<PaymentDetailDTO> payments;
    
    public SupplierDebtDetailDTO() {}
    
    public SupplierDebtDetailDTO(Long id, String soPhieuMua, String nhaPhanPhoiName,
                                String nhaPhanPhoiDiaChi, String nhaPhanPhoiSDT,
                                BigDecimal tongTienNo, BigDecimal tongTienDaThanhToan,
                                BigDecimal tongTienConNo, String trangThai,
                                LocalDate ngayHanChot, String ghiChu, LocalDateTime ngayTaoNo,
                                List<PaymentDetailDTO> payments) {
        this.id = id;
        this.soPhieuMua = soPhieuMua;
        this.nhaPhanPhoiName = nhaPhanPhoiName;
        this.nhaPhanPhoiDiaChi = nhaPhanPhoiDiaChi;
        this.nhaPhanPhoiSDT = nhaPhanPhoiSDT;
        this.tongTienNo = tongTienNo;
        this.tongTienDaThanhToan = tongTienDaThanhToan;
        this.tongTienConNo = tongTienConNo;
        this.trangThai = trangThai;
        this.ngayHanChot = ngayHanChot;
        this.ghiChu = ghiChu;
        this.ngayTaoNo = ngayTaoNo;
        this.payments = payments;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSoPhieuMua() { return soPhieuMua; }
    public void setSoPhieuMua(String soPhieuMua) { this.soPhieuMua = soPhieuMua; }
    
    public String getNhaPhanPhoiName() { return nhaPhanPhoiName; }
    public void setNhaPhanPhoiName(String nhaPhanPhoiName) { this.nhaPhanPhoiName = nhaPhanPhoiName; }
    
    public String getNhaPhanPhoiDiaChi() { return nhaPhanPhoiDiaChi; }
    public void setNhaPhanPhoiDiaChi(String nhaPhanPhoiDiaChi) { this.nhaPhanPhoiDiaChi = nhaPhanPhoiDiaChi; }
    
    public String getNhaPhanPhoiSDT() { return nhaPhanPhoiSDT; }
    public void setNhaPhanPhoiSDT(String nhaPhanPhoiSDT) { this.nhaPhanPhoiSDT = nhaPhanPhoiSDT; }
    
    public BigDecimal getTongTienNo() { return tongTienNo; }
    public void setTongTienNo(BigDecimal tongTienNo) { this.tongTienNo = tongTienNo; }
    
    public BigDecimal getTongTienDaThanhToan() { return tongTienDaThanhToan; }
    public void setTongTienDaThanhToan(BigDecimal tongTienDaThanhToan) { this.tongTienDaThanhToan = tongTienDaThanhToan; }
    
    public BigDecimal getTongTienConNo() { return tongTienConNo; }
    public void setTongTienConNo(BigDecimal tongTienConNo) { this.tongTienConNo = tongTienConNo; }
    
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    
    public LocalDate getNgayHanChot() { return ngayHanChot; }
    public void setNgayHanChot(LocalDate ngayHanChot) { this.ngayHanChot = ngayHanChot; }
    
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    
    public LocalDateTime getNgayTaoNo() { return ngayTaoNo; }
    public void setNgayTaoNo(LocalDateTime ngayTaoNo) { this.ngayTaoNo = ngayTaoNo; }
    
    public List<PaymentDetailDTO> getPayments() { return payments; }
    public void setPayments(List<PaymentDetailDTO> payments) { this.payments = payments; }
    
    /**
     * Inner DTO for payment details
     */
    public static class PaymentDetailDTO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        
        private Long id;
        private LocalDate ngayTT;
        private String soPhieu;
        private BigDecimal soTienTT;
        private String phuongThucTT;
        
        public PaymentDetailDTO() {}
        
        public PaymentDetailDTO(Long id, LocalDate ngayTT, String soPhieu, BigDecimal soTienTT, String phuongThucTT) {
            this.id = id;
            this.ngayTT = ngayTT;
            this.soPhieu = soPhieu;
            this.soTienTT = soTienTT;
            this.phuongThucTT = phuongThucTT;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public LocalDate getNgayTT() { return ngayTT; }
        public void setNgayTT(LocalDate ngayTT) { this.ngayTT = ngayTT; }
        
        public String getSoPhieu() { return soPhieu; }
        public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }
        
        public BigDecimal getSoTienTT() { return soTienTT; }
        public void setSoTienTT(BigDecimal soTienTT) { this.soTienTT = soTienTT; }
        
        public String getPhuongThucTT() { return phuongThucTT; }
        public void setPhuongThucTT(String phuongThucTT) { this.phuongThucTT = phuongThucTT; }
    }
}
