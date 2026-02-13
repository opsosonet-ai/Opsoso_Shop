package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "doi_tra_hang_hoa")
public class DoiTraHangHoa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String maDoiTra; // Mã đổi trả: DT001, DT002...
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hang_hoa_id", nullable = false)
    private HangHoa hangHoa;
    
    // Hàng hóa đổi mới (chỉ áp dụng khi loaiDoiTra = DOI_HANG)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hang_hoa_doi_moi_id")
    private HangHoa hangHoaDoiMoi;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private LoaiDoiTra loaiDoiTra; // DOI_HANG, TRA_HANG
    
    @Column(nullable = false)
    private Integer soLuong;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal donGia;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal thanhTien;
    
    @Column(length = 100)
    private String tenKhachHang;
    
    @Column(length = 20)
    private String soDienThoai;
    
    @Column(length = 500)
    private String lyDo; // Lý do đổi trả
    
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private TrangThaiDoiTra trangThai; // CHO_DUYET, DA_DUYET, TU_CHOI
    
    @Column(length = 100)
    private String nguoiXuLy; // Nhân viên xử lý
    
    @Column(name = "ngay_doi_tra")
    private LocalDateTime ngayDoiTra;
    
    @Column(name = "ngay_xu_ly")
    private LocalDateTime ngayXuLy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum LoaiDoiTra {
        DOI_HANG("Đổi hàng"),
        TRA_HANG("Trả hàng");
        
        private final String displayName;
        
        LoaiDoiTra(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum TrangThaiDoiTra {
        CHO_DUYET("Chờ duyệt"),
        DA_DUYET("Đã duyệt"),
        TU_CHOI("Từ chối");
        
        private final String displayName;
        
        TrangThaiDoiTra(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public DoiTraHangHoa() {}
    
    public DoiTraHangHoa(String maDoiTra, HangHoa hangHoa, LoaiDoiTra loaiDoiTra, Integer soLuong, 
                         BigDecimal donGia, String tenKhachHang, String lyDo) {
        this.maDoiTra = maDoiTra;
        this.hangHoa = hangHoa;
        this.loaiDoiTra = loaiDoiTra;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = donGia.multiply(new BigDecimal(soLuong));
        this.tenKhachHang = tenKhachHang;
        this.lyDo = lyDo;
        this.trangThai = TrangThaiDoiTra.CHO_DUYET;
        this.ngayDoiTra = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (ngayDoiTra == null) {
            ngayDoiTra = LocalDateTime.now();
        }
        if (thanhTien == null && donGia != null && soLuong != null) {
            thanhTien = donGia.multiply(new BigDecimal(soLuong));
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (thanhTien == null && donGia != null && soLuong != null) {
            thanhTien = donGia.multiply(new BigDecimal(soLuong));
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaDoiTra() {
        return maDoiTra;
    }

    public void setMaDoiTra(String maDoiTra) {
        this.maDoiTra = maDoiTra;
    }

    public HangHoa getHangHoa() {
        return hangHoa;
    }

    public void setHangHoa(HangHoa hangHoa) {
        this.hangHoa = hangHoa;
    }

    public HangHoa getHangHoaDoiMoi() {
        return hangHoaDoiMoi;
    }

    public void setHangHoaDoiMoi(HangHoa hangHoaDoiMoi) {
        this.hangHoaDoiMoi = hangHoaDoiMoi;
    }

    public LoaiDoiTra getLoaiDoiTra() {
        return loaiDoiTra;
    }

    public void setLoaiDoiTra(LoaiDoiTra loaiDoiTra) {
        this.loaiDoiTra = loaiDoiTra;
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

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public TrangThaiDoiTra getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiDoiTra trangThai) {
        this.trangThai = trangThai;
    }

    public String getNguoiXuLy() {
        return nguoiXuLy;
    }

    public void setNguoiXuLy(String nguoiXuLy) {
        this.nguoiXuLy = nguoiXuLy;
    }

    public LocalDateTime getNgayDoiTra() {
        return ngayDoiTra;
    }

    public void setNgayDoiTra(LocalDateTime ngayDoiTra) {
        this.ngayDoiTra = ngayDoiTra;
    }

    public LocalDateTime getNgayXuLy() {
        return ngayXuLy;
    }

    public void setNgayXuLy(LocalDateTime ngayXuLy) {
        this.ngayXuLy = ngayXuLy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}