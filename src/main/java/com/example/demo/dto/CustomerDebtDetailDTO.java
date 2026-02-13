package com.example.demo.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Customer Debt Detail - prevents serialization issues
 * Only includes necessary fields to avoid LazyInitializationException
 */
public class CustomerDebtDetailDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String soPhieuXuatBan;
    private String khachHangName;
    private String khachHangDiaChi;
    private String khachHangSDT;
    private BigDecimal tongTienNo;
    private BigDecimal tongTienDaThanhToan;
    private BigDecimal tongTienConNo;
    private String trangThai;
    private LocalDate ngayHanChot;
    private String ghiChu;
    private LocalDateTime ngayTaoNo;
    private List<PaymentDetailDTO> payments;
    
    public CustomerDebtDetailDTO() {}
    
    public CustomerDebtDetailDTO(Long id, String soPhieuXuatBan, String khachHangName, 
                                String khachHangDiaChi, String khachHangSDT,
                                BigDecimal tongTienNo, BigDecimal tongTienDaThanhToan, 
                                BigDecimal tongTienConNo, String trangThai, 
                                LocalDate ngayHanChot, String ghiChu, LocalDateTime ngayTaoNo,
                                List<PaymentDetailDTO> payments) {
        this.id = id;
        this.soPhieuXuatBan = soPhieuXuatBan;
        this.khachHangName = khachHangName;
        this.khachHangDiaChi = khachHangDiaChi;
        this.khachHangSDT = khachHangSDT;
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
    
    public String getSoPhieuXuatBan() { return soPhieuXuatBan; }
    public void setSoPhieuXuatBan(String soPhieuXuatBan) { this.soPhieuXuatBan = soPhieuXuatBan; }
    
    public String getKhachHangName() { return khachHangName; }
    public void setKhachHangName(String khachHangName) { this.khachHangName = khachHangName; }
    
    public String getKhachHangDiaChi() { return khachHangDiaChi; }
    public void setKhachHangDiaChi(String khachHangDiaChi) { this.khachHangDiaChi = khachHangDiaChi; }
    
    public String getKhachHangSDT() { return khachHangSDT; }
    public void setKhachHangSDT(String khachHangSDT) { this.khachHangSDT = khachHangSDT; }
    
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
