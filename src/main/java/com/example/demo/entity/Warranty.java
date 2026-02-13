package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "warranty")
public class Warranty {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chi_tiet_phieu_xuat_id", nullable = true)
    private Long chiTietPhieuXuatId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hang_hoa_id")
    private HangHoa hangHoa;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;
    
    @Column(nullable = false)
    private LocalDate ngayBan;
    
    @Column(nullable = false)
    private LocalDate ngayHetHanBaoHanh;
    
    @Column(length = 50)
    private String trangThai;  // "Còn hiệu lực", "Hết hạn", "Đã sửa chữa"
    
    private String ghiChu;
    
    @Column(updatable = false)
    private LocalDateTime ngayTao;
    
    @OneToMany(mappedBy = "warranty", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("thoiGianThucHien ASC")
    @JsonManagedReference
    private List<WarrantyTimeline> timelines = new ArrayList<>();
    
    // Constructors
    public Warranty() {
    }
    
    public Warranty(Long chiTietPhieuXuatId, HangHoa hangHoa, KhachHang khachHang, 
                    LocalDate ngayBan, LocalDate ngayHetHanBaoHanh) {
        this.chiTietPhieuXuatId = chiTietPhieuXuatId;
        this.hangHoa = hangHoa;
        this.khachHang = khachHang;
        this.ngayBan = ngayBan;
        this.ngayHetHanBaoHanh = ngayHetHanBaoHanh;
        this.trangThai = "Còn hiệu lực";
        this.ngayTao = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getChiTietPhieuXuatId() {
        return chiTietPhieuXuatId;
    }
    
    public void setChiTietPhieuXuatId(Long chiTietPhieuXuatId) {
        this.chiTietPhieuXuatId = chiTietPhieuXuatId;
    }
    
    public HangHoa getHangHoa() {
        return hangHoa;
    }
    
    public void setHangHoa(HangHoa hangHoa) {
        this.hangHoa = hangHoa;
    }
    
    public KhachHang getKhachHang() {
        return khachHang;
    }
    
    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }
    
    public LocalDate getNgayBan() {
        return ngayBan;
    }
    
    public void setNgayBan(LocalDate ngayBan) {
        this.ngayBan = ngayBan;
    }
    
    public LocalDate getNgayHetHanBaoHanh() {
        return ngayHetHanBaoHanh;
    }
    
    public void setNgayHetHanBaoHanh(LocalDate ngayHetHanBaoHanh) {
        this.ngayHetHanBaoHanh = ngayHetHanBaoHanh;
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
    
    public LocalDateTime getNgayTao() {
        return ngayTao;
    }
    
    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
    
    // Helper methods
    public boolean isStillValid() {
        return LocalDate.now().isBefore(ngayHetHanBaoHanh);
    }
    
    public long getDaysRemaining() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), ngayHetHanBaoHanh);
    }
    
    public List<WarrantyTimeline> getTimelines() {
        return timelines;
    }
    
    public void setTimelines(List<WarrantyTimeline> timelines) {
        this.timelines = timelines;
    }
    
    public void addTimeline(WarrantyTimeline timeline) {
        timelines.add(timeline);
        timeline.setWarranty(this);
    }
    
    public void removeTimeline(WarrantyTimeline timeline) {
        timelines.remove(timeline);
        timeline.setWarranty(null);
    }
}
