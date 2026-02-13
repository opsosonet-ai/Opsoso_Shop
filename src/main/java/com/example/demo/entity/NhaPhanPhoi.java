package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "nha_phan_phoi")
public class NhaPhanPhoi {
    
    @Id
    @Column(name = "ma_nha_phan_phoi", nullable = false, unique = true, length = 50)
    private String maNhaPhanPhoi;
    
    @Column(nullable = false)
    private String tenNhaPhanPhoi;
    
    @Column(name = "id_cu")
    private Long idCu;
    
    private String soDienThoai;
    
    private String email;
    
    private String diaChi;
    
    private String nguoiLienHe;
    
    private String maSoThue;
    
    private String trangThai; // Hoạt động, Tạm dừng
    
    private String ghiChu;
    
    private String linhVuc; // Thực phẩm, Điện tử, May mặc, etc.
    
    // Constructors
    public NhaPhanPhoi() {
    }
    
    public NhaPhanPhoi(String maNhaPhanPhoi, String tenNhaPhanPhoi, String soDienThoai, String email) {
        this.maNhaPhanPhoi = maNhaPhanPhoi;
        this.tenNhaPhanPhoi = tenNhaPhanPhoi;
        this.soDienThoai = soDienThoai;
        this.email = email;
    }
    
    // Getters and Setters
    public String getMaNhaPhanPhoi() {
        return maNhaPhanPhoi;
    }
    
    public void setMaNhaPhanPhoi(String maNhaPhanPhoi) {
        this.maNhaPhanPhoi = maNhaPhanPhoi;
    }
    
    public Long getIdCu() {
        return idCu;
    }
    
    public void setIdCu(Long idCu) {
        this.idCu = idCu;
    }
    
    public String getTenNhaPhanPhoi() {
        return tenNhaPhanPhoi;
    }
    
    public void setTenNhaPhanPhoi(String tenNhaPhanPhoi) {
        this.tenNhaPhanPhoi = tenNhaPhanPhoi;
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
    
    public String getNguoiLienHe() {
        return nguoiLienHe;
    }
    
    public void setNguoiLienHe(String nguoiLienHe) {
        this.nguoiLienHe = nguoiLienHe;
    }
    
    public String getMaSoThue() {
        return maSoThue;
    }
    
    public void setMaSoThue(String maSoThue) {
        this.maSoThue = maSoThue;
    }
    
    public String getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    
    public String getGhiChu() {
        return ghiChu;
    }
    
    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
    
    public String getLinhVuc() {
        return linhVuc;
    }
    
    public void setLinhVuc(String linhVuc) {
        this.linhVuc = linhVuc;
    }
}