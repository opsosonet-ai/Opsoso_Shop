package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "nhan_vien")
public class NhanVien {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String hoTen;
    
    private String soDienThoai;
    
    private String email;
    
    private String chucVu;
    
    private String phongBan;
    
    private Double luong;
    
    private LocalDate ngayVaoLam;
    
    private String diaChi;
    
    // Constructors
    public NhanVien() {
    }
    
    public NhanVien(String hoTen, String soDienThoai, String email, String chucVu, String phongBan) {
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.chucVu = chucVu;
        this.phongBan = phongBan;
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
    
    public String getChucVu() {
        return chucVu;
    }
    
    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }
    
    public String getPhongBan() {
        return phongBan;
    }
    
    public void setPhongBan(String phongBan) {
        this.phongBan = phongBan;
    }
    
    public Double getLuong() {
        return luong;
    }
    
    public void setLuong(Double luong) {
        this.luong = luong;
    }
    
    public LocalDate getNgayVaoLam() {
        return ngayVaoLam;
    }
    
    public void setNgayVaoLam(LocalDate ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }
    
    public String getDiaChi() {
        return diaChi;
    }
    
    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
}