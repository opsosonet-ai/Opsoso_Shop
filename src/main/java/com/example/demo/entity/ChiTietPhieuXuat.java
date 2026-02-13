package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "chi_tiet_phieu_xuat")
public class ChiTietPhieuXuat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "phieu_xuat_id", nullable = false)
    private PhieuXuat phieuXuat;
    
    @ManyToOne
    @JoinColumn(name = "hang_hoa_id", nullable = true)
    private HangHoa hangHoa;
    
    @Column(nullable = false)
    private Integer soLuong;
    
    @Column(nullable = false)
    private BigDecimal donGia;
    
    @Column(nullable = false)
    private BigDecimal thanhTien;
    
    private String ghiChu;
    
    // Constructors
    public ChiTietPhieuXuat() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public PhieuXuat getPhieuXuat() {
        return phieuXuat;
    }
    
    public void setPhieuXuat(PhieuXuat phieuXuat) {
        this.phieuXuat = phieuXuat;
    }
    
    public HangHoa getHangHoa() {
        return hangHoa;
    }
    
    public void setHangHoa(HangHoa hangHoa) {
        this.hangHoa = hangHoa;
    }
    
    public Integer getSoLuong() {
        return soLuong;
    }
    
    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }
    
    public BigDecimal getDonGia() {
        return donGia;
    }
    
    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }
    
    public BigDecimal getThanhTien() {
        return thanhTien;
    }
    
    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }
    
    public String getGhiChu() {
        return ghiChu;
    }
    
    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
    
    // Helper method để tính thành tiền
    public void calculateThanhTien() {
        if (soLuong != null && donGia != null) {
            this.thanhTien = donGia.multiply(new BigDecimal(soLuong));
        }
    }
}
