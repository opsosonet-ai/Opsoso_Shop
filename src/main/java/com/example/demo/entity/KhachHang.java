package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "khach_hang")
public class KhachHang {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String hoTen;
    
    private String soDienThoai;
    
    private String email;
    
    private String diaChi;
    
    private String loaiKhachHang; // VIP, Thường, Doanh nghiệp
    
    private LocalDate ngayDangKy;
    
    private String ghiChu;
    
    private String maSoThue;
    
    @Column(name = "is_bad_debt", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isBadDebt = false; // Xác định khách hàng có nợ xấu hay không
    
    // Constructors
    public KhachHang() {
    }
    
    public KhachHang(String hoTen, String soDienThoai, String email, String diaChi) {
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getHoTen() {
        return hoTen;
    }
    
    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }
    
    public String getSoDienThoai() {
        return soDienThoai;
    }
    
    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDiaChi() {
        return diaChi;
    }
    
    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
    
    public String getLoaiKhachHang() {
        return loaiKhachHang;
    }
    
    public void setLoaiKhachHang(String loaiKhachHang) {
        this.loaiKhachHang = loaiKhachHang;
    }
    
    public LocalDate getNgayDangKy() {
        return ngayDangKy;
    }
    
    public void setNgayDangKy(LocalDate ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }
    
    public String getGhiChu() {
        return ghiChu;
    }
    
    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
    
    public String getMaSoThue() {
        return maSoThue;
    }
    
    public void setMaSoThue(String maSoThue) {
        this.maSoThue = maSoThue;
    }
    
    public Boolean getIsBadDebt() {
        return isBadDebt;
    }
    
    public void setIsBadDebt(Boolean isBadDebt) {
        this.isBadDebt = isBadDebt;
    }
}