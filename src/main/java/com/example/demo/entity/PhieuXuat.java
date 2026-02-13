package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "phieu_xuat")
public class PhieuXuat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String maPhieuXuat;
    
    @Column(nullable = false)
    private LocalDateTime ngayXuat;
    
    @ManyToOne
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;
    
    private String nguoiXuat; // Tên nhân viên xuất hàng
    
    private BigDecimal tongTien;
    
    private String ghiChu;
    
    private String trangThai; // Đã xuất, Đã hủy
    
    @Column(nullable = false)
    private String loaiXuat = "TIEN_MAT"; // TIEN_MAT (cash) hoặc BAN_NO (credit)
    
    private LocalDateTime ngayHanThanhToan; // Hạn thanh toán cho bán nợ
    
    @OneToMany(mappedBy = "phieuXuat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietPhieuXuat> chiTietList = new ArrayList<>();
    
    // Constructors
    public PhieuXuat() {
        this.ngayXuat = LocalDateTime.now();
        this.trangThai = "Đã xuất";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMaPhieuXuat() {
        return maPhieuXuat;
    }
    
    public void setMaPhieuXuat(String maPhieuXuat) {
        this.maPhieuXuat = maPhieuXuat;
    }
    
    public LocalDateTime getNgayXuat() {
        return ngayXuat;
    }
    
    public void setNgayXuat(LocalDateTime ngayXuat) {
        this.ngayXuat = ngayXuat;
    }
    
    public KhachHang getKhachHang() {
        return khachHang;
    }
    
    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }
    
    public String getNguoiXuat() {
        return nguoiXuat;
    }
    
    public void setNguoiXuat(String nguoiXuat) {
        this.nguoiXuat = nguoiXuat;
    }
    
    public BigDecimal getTongTien() {
        return tongTien;
    }
    
    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }
    
    public String getGhiChu() {
        return ghiChu;
    }
    
    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
    
    public String getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    
    public List<ChiTietPhieuXuat> getChiTietList() {
        return chiTietList;
    }
    
    public void setChiTietList(List<ChiTietPhieuXuat> chiTietList) {
        this.chiTietList = chiTietList;
    }
    
    public void addChiTiet(ChiTietPhieuXuat chiTiet) {
        chiTietList.add(chiTiet);
        chiTiet.setPhieuXuat(this);
    }
    
    public void removeChiTiet(ChiTietPhieuXuat chiTiet) {
        chiTietList.remove(chiTiet);
        chiTiet.setPhieuXuat(null);
    }
    
    public String getLoaiXuat() {
        return loaiXuat;
    }
    
    public void setLoaiXuat(String loaiXuat) {
        this.loaiXuat = loaiXuat;
    }
    
    public LocalDateTime getNgayHanThanhToan() {
        return ngayHanThanhToan;
    }
    
    public void setNgayHanThanhToan(LocalDateTime ngayHanThanhToan) {
        this.ngayHanThanhToan = ngayHanThanhToan;
    }
    
    public boolean isBanNo() {
        return "BAN_NO".equals(this.loaiXuat);
    }
    
    // Method để chuyển tổng tiền thành chữ
    public String getTongTienBangChu() {
        return com.example.demo.util.NumberToWordsUtil.convertToWords(this.tongTien);
    }
}
