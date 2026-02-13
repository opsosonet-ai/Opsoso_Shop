package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tra_hang")
public class TraHang {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String maTraHang; // Mã trả hàng: TH001, TH002...
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hang_hoa_id", nullable = false)
    private HangHoa hangHoa;
    
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
    private String lyDo; // Lý do trả hàng
    
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private TrangThaiTraHang trangThai; // CHO_DUYET, DA_DUYET, TU_CHOI
    
    @Column(length = 100)
    private String nguoiXuLy; // Nhân viên xử lý
    
    @Column(name = "ngay_tra_hang")
    private LocalDateTime ngayTraHang;
    
    @Column(name = "ngay_xu_ly")
    private LocalDateTime ngayXuLy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "ngay_duyet")
    private LocalDateTime ngayDuyet; // Ngày đã duyệt (để theo dõi lịch sử)
    
    @Column(name = "da_duyet_truoc_do", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean daDuyetTruocDo = false; // Đánh dấu đã từng duyệt
    
    public enum TrangThaiTraHang {
        CHO_DUYET("Chờ duyệt"),
        DA_DUYET("Đã duyệt"),
        TU_CHOI("Từ chối");
        
        private final String displayName;
        
        TrangThaiTraHang(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public TraHang() {}
    
    public TraHang(String maTraHang, HangHoa hangHoa, Integer soLuong, 
                   BigDecimal donGia, String tenKhachHang, String lyDo) {
        this.maTraHang = maTraHang;
        this.hangHoa = hangHoa;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = donGia.multiply(new BigDecimal(soLuong));
        this.tenKhachHang = tenKhachHang;
        this.lyDo = lyDo;
        this.trangThai = TrangThaiTraHang.CHO_DUYET;
        this.ngayTraHang = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (ngayTraHang == null) {
            ngayTraHang = LocalDateTime.now();
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

    public String getMaTraHang() {
        return maTraHang;
    }

    public void setMaTraHang(String maTraHang) {
        this.maTraHang = maTraHang;
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

    public TrangThaiTraHang getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiTraHang trangThai) {
        this.trangThai = trangThai;
    }

    public String getNguoiXuLy() {
        return nguoiXuLy;
    }

    public void setNguoiXuLy(String nguoiXuLy) {
        this.nguoiXuLy = nguoiXuLy;
    }

    public LocalDateTime getNgayTraHang() {
        return ngayTraHang;
    }

    public void setNgayTraHang(LocalDateTime ngayTraHang) {
        this.ngayTraHang = ngayTraHang;
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

    public LocalDateTime getNgayDuyet() {
        return ngayDuyet;
    }

    public void setNgayDuyet(LocalDateTime ngayDuyet) {
        this.ngayDuyet = ngayDuyet;
    }

    public Boolean getDaDuyetTruocDo() {
        return daDuyetTruocDo;
    }

    public void setDaDuyetTruocDo(Boolean daDuyetTruocDo) {
        this.daDuyetTruocDo = daDuyetTruocDo;
    }
}