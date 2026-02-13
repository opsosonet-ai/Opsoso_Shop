package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Recent Payment/Collection Transactions
 * Used to display recent payments and collections in dashboard
 */
public class RecentTransactionDTO {
    private Long id;
    private LocalDateTime ngayThanhToan;  // Payment date
    private String loaiThanhToan;         // Type: "SUPPLIER" or "CUSTOMER"
    private String soPhieu;               // Invoice/Receipt number
    private String soPhieuGoc;            // Original invoice/receipt number
    private String tenBenLienQuan;        // Related party name
    private BigDecimal soTien;            // Amount
    private String phuongThuc;            // Payment method: TIEN_MAT, CHUYEN_KHOAN, CHI_TIEU
    private String nguoiGhiNhan;          // Person who recorded the payment
    private String ghiChu;                // Notes

    public RecentTransactionDTO() {
    }

    public RecentTransactionDTO(Long id, LocalDateTime ngayThanhToan, String loaiThanhToan,
                                 String soPhieu, String tenBenLienQuan, BigDecimal soTien,
                                 String phuongThuc) {
        this.id = id;
        this.ngayThanhToan = ngayThanhToan;
        this.loaiThanhToan = loaiThanhToan;
        this.soPhieu = soPhieu;
        this.tenBenLienQuan = tenBenLienQuan;
        this.soTien = soTien;
        this.phuongThuc = phuongThuc;
    }

    public RecentTransactionDTO(Long id, LocalDateTime ngayThanhToan, String loaiThanhToan,
                                 String soPhieu, String soPhieuGoc, String tenBenLienQuan, 
                                 BigDecimal soTien, String phuongThuc, String nguoiGhiNhan, String ghiChu) {
        this.id = id;
        this.ngayThanhToan = ngayThanhToan;
        this.loaiThanhToan = loaiThanhToan;
        this.soPhieu = soPhieu;
        this.soPhieuGoc = soPhieuGoc;
        this.tenBenLienQuan = tenBenLienQuan;
        this.soTien = soTien;
        this.phuongThuc = phuongThuc;
        this.nguoiGhiNhan = nguoiGhiNhan;
        this.ghiChu = ghiChu;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getNgayThanhToan() {
        return ngayThanhToan;
    }

    public void setNgayThanhToan(LocalDateTime ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }

    public String getLoaiThanhToan() {
        return loaiThanhToan;
    }

    public void setLoaiThanhToan(String loaiThanhToan) {
        this.loaiThanhToan = loaiThanhToan;
    }

    public String getSoPhieu() {
        return soPhieu;
    }

    public void setSoPhieu(String soPhieu) {
        this.soPhieu = soPhieu;
    }

    public String getTenBenLienQuan() {
        return tenBenLienQuan;
    }

    public void setTenBenLienQuan(String tenBenLienQuan) {
        this.tenBenLienQuan = tenBenLienQuan;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public String getSoPhieuGoc() {
        return soPhieuGoc;
    }

    public void setSoPhieuGoc(String soPhieuGoc) {
        this.soPhieuGoc = soPhieuGoc;
    }

    public String getNguoiGhiNhan() {
        return nguoiGhiNhan;
    }

    public void setNguoiGhiNhan(String nguoiGhiNhan) {
        this.nguoiGhiNhan = nguoiGhiNhan;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
