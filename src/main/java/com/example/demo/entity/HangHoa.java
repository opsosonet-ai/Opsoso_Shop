package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "hang_hoa")
public class HangHoa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String tenHangHoa;
    
    private String maHangHoa;
    
    @Column(unique = true, nullable = false)
    private String soSerial;
    
    private String loaiHangHoa;
    
    private BigDecimal giaBan;
    
    private BigDecimal giaVon;
    
    private Integer soLuongTon;
    
    private String donViTinh;
    
    private String moTa;
    
    private String thuongHieu;
    
    private String xuatXu;
    
    @Column(name = "ngay_nhap")
    private LocalDate ngayNhap;
    
    @Column(name = "ngay_xuat")
    private LocalDate ngayXuat;
    
    @Column(name = "thoi_gian_bao_hanh")
    private Integer thoiGianBaoHanh;  // Số tháng bảo hành (mặc định 12 tháng)
    
    @Column(name = "ngay_het_han_bao_hanh")
    private LocalDate ngayHetHanBaoHanh;  // Tính từ ngayNhap + thoiGianBaoHanh
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nha_phan_phoi")
    private NhaPhanPhoi nhaPhanPhoi;
    
    // Constructors
    public HangHoa() {
    }
    
    public HangHoa(String tenHangHoa, String maHangHoa, String soSerial, String loaiHangHoa, BigDecimal giaBan) {
        this.tenHangHoa = tenHangHoa;
        this.maHangHoa = maHangHoa;
        this.soSerial = soSerial;
        this.loaiHangHoa = loaiHangHoa;
        this.giaBan = giaBan;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTenHangHoa() {
        return tenHangHoa;
    }
    
    public void setTenHangHoa(String tenHangHoa) {
        this.tenHangHoa = tenHangHoa;
    }
    
    public String getMaHangHoa() {
        return maHangHoa;
    }
    
    public void setMaHangHoa(String maHangHoa) {
        this.maHangHoa = maHangHoa;
    }
    
    public String getSoSerial() {
        return soSerial;
    }
    
    public void setSoSerial(String soSerial) {
        this.soSerial = soSerial;
    }
    
    public String getLoaiHangHoa() {
        return loaiHangHoa;
    }
    
    public void setLoaiHangHoa(String loaiHangHoa) {
        this.loaiHangHoa = loaiHangHoa;
    }
    
    public BigDecimal getGiaBan() {
        return giaBan;
    }
    
    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan;
    }
    
    public BigDecimal getGiaVon() {
        return giaVon;
    }
    
    public void setGiaVon(BigDecimal giaVon) {
        this.giaVon = giaVon;
    }
    
    public Integer getSoLuongTon() {
        return soLuongTon;
    }
    
    public void setSoLuongTon(Integer soLuongTon) {
        this.soLuongTon = soLuongTon;
    }
    
    public String getDonViTinh() {
        return donViTinh;
    }
    
    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }
    
    public String getMoTa() {
        return moTa;
    }
    
    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
    
    public String getThuongHieu() {
        return thuongHieu;
    }
    
    public void setThuongHieu(String thuongHieu) {
        this.thuongHieu = thuongHieu;
    }
    
    public String getXuatXu() {
        return xuatXu;
    }
    
    public void setXuatXu(String xuatXu) {
        this.xuatXu = xuatXu;
    }
    
    public LocalDate getNgayNhap() {
        return ngayNhap;
    }
    
    public void setNgayNhap(LocalDate ngayNhap) {
        this.ngayNhap = ngayNhap;
    }
    
    public LocalDate getNgayXuat() {
        return ngayXuat;
    }
    
    public void setNgayXuat(LocalDate ngayXuat) {
        this.ngayXuat = ngayXuat;
    }
    
    public Integer getThoiGianBaoHanh() {
        return thoiGianBaoHanh;
    }
    
    public void setThoiGianBaoHanh(Integer thoiGianBaoHanh) {
        this.thoiGianBaoHanh = thoiGianBaoHanh;
    }
    
    public LocalDate getNgayHetHanBaoHanh() {
        return ngayHetHanBaoHanh;
    }
    
    public void setNgayHetHanBaoHanh(LocalDate ngayHetHanBaoHanh) {
        this.ngayHetHanBaoHanh = ngayHetHanBaoHanh;
    }

    public NhaPhanPhoi getNhaPhanPhoi() {
        return nhaPhanPhoi;
    }

    public void setNhaPhanPhoi(NhaPhanPhoi nhaPhanPhoi) {
        this.nhaPhanPhoi = nhaPhanPhoi;
    }

    @Transient
    public BigDecimal getTongGiaTriNhap() {
        if (giaVon == null || soLuongTon == null) {
            return null;
        }
        return giaVon.multiply(BigDecimal.valueOf(soLuongTon.longValue()));
    }
}
