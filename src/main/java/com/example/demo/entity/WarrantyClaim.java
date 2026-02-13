package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "warranty_claim")
public class WarrantyClaim {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warranty_id", nullable = false)
    private Warranty warranty;
    
    @Column(length = 255)
    private String loaiLoi;  // "Lỗi phần cứng", "Lỗi phần mềm", "Hỏng hóc", etc.
    
    @Column(columnDefinition = "TEXT")
    private String moTaVanDe;  // Mô tả chi tiết vấn đề
    
    @Column(length = 50)
    private String trangThai;  // "Chờ xử lý", "Đang xử lý", "Hoàn thành", "Từ chối"
    
    @Column(length = 50)
    private String trangThaiXuly;  // "Sửa chữa", "Thay thế", "Hoàn tiền"
    
    @Column(nullable = false)
    private LocalDate ngayYeuCau;
    
    private LocalDate ngayNhanSuLy;
    
    private LocalDate ngayHoanThanh;
    
    @Column(columnDefinition = "TEXT")
    private String ghiChuXuly;  // Ghi chú sau khi xử lý
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_xu_ly_id")
    private NhanVien nhanVienXuLy;
    
    // Constructors
    public WarrantyClaim() {
    }
    
    public WarrantyClaim(Warranty warranty, String loaiLoi, String moTaVanDe) {
        this.warranty = warranty;
        this.loaiLoi = loaiLoi;
        this.moTaVanDe = moTaVanDe;
        this.trangThai = "Chờ xử lý";
        this.ngayYeuCau = LocalDate.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Warranty getWarranty() {
        return warranty;
    }
    
    public void setWarranty(Warranty warranty) {
        this.warranty = warranty;
    }
    
    public String getLoaiLoi() {
        return loaiLoi;
    }
    
    public void setLoaiLoi(String loaiLoi) {
        this.loaiLoi = loaiLoi;
    }
    
    public String getMoTaVanDe() {
        return moTaVanDe;
    }
    
    public void setMoTaVanDe(String moTaVanDe) {
        this.moTaVanDe = moTaVanDe;
    }
    
    public String getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    
    public String getTrangThaiXuly() {
        return trangThaiXuly;
    }
    
    public void setTrangThaiXuly(String trangThaiXuly) {
        this.trangThaiXuly = trangThaiXuly;
    }
    
    public LocalDate getNgayYeuCau() {
        return ngayYeuCau;
    }
    
    public void setNgayYeuCau(LocalDate ngayYeuCau) {
        this.ngayYeuCau = ngayYeuCau;
    }
    
    public LocalDate getNgayNhanSuLy() {
        return ngayNhanSuLy;
    }
    
    public void setNgayNhanSuLy(LocalDate ngayNhanSuLy) {
        this.ngayNhanSuLy = ngayNhanSuLy;
    }
    
    public LocalDate getNgayHoanThanh() {
        return ngayHoanThanh;
    }
    
    public void setNgayHoanThanh(LocalDate ngayHoanThanh) {
        this.ngayHoanThanh = ngayHoanThanh;
    }
    
    public String getGhiChuXuly() {
        return ghiChuXuly;
    }
    
    public void setGhiChuXuly(String ghiChuXuly) {
        this.ghiChuXuly = ghiChuXuly;
    }
    
    public NhanVien getNhanVienXuLy() {
        return nhanVienXuLy;
    }
    
    public void setNhanVienXuLy(NhanVien nhanVienXuLy) {
        this.nhanVienXuLy = nhanVienXuLy;
    }
}
