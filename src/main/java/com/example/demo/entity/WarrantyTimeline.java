package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Entity để tracking quy trình thời gian bảo hành
 * Các bước: Nhận thiết bị -> Gửi nhà phân phối/hãng -> Lấy về -> Trả khách hàng
 */
@Entity
@Table(name = "warranty_timeline")
public class WarrantyTimeline {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warranty_id", nullable = false)
    @JsonBackReference
    private Warranty warranty;
    
    @Column(length = 50, nullable = false)
    private String buocThucHien;  // "Nhận thiết bị", "Gửi nhà phân phối", "Lấy về", "Trả khách hàng"
    
    @Column(nullable = false)
    private LocalDateTime thoiGianThucHien;
    
    @Column(columnDefinition = "TEXT")
    private String ghiChu;
    
    @Column(length = 100)
    private String nguoiThucHien;  // Tên nhân viên hoặc người xử lý
    
    @Column(updatable = false)
    private LocalDateTime ngayTao;
    
    // Constructor mặc định
    public WarrantyTimeline() {
        this.ngayTao = LocalDateTime.now();
    }
    
    // Constructor có tham số
    public WarrantyTimeline(Warranty warranty, String buocThucHien, LocalDateTime thoiGianThucHien, 
                           String ghiChu, String nguoiThucHien) {
        this.warranty = warranty;
        this.buocThucHien = buocThucHien;
        this.thoiGianThucHien = thoiGianThucHien;
        this.ghiChu = ghiChu;
        this.nguoiThucHien = nguoiThucHien;
        this.ngayTao = LocalDateTime.now();
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
    
    public String getBuocThucHien() {
        return buocThucHien;
    }
    
    public void setBuocThucHien(String buocThucHien) {
        this.buocThucHien = buocThucHien;
    }
    
    public LocalDateTime getThoiGianThucHien() {
        return thoiGianThucHien;
    }
    
    public void setThoiGianThucHien(LocalDateTime thoiGianThucHien) {
        this.thoiGianThucHien = thoiGianThucHien;
    }
    
    public String getGhiChu() {
        return ghiChu;
    }
    
    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
    
    public String getNguoiThucHien() {
        return nguoiThucHien;
    }
    
    public void setNguoiThucHien(String nguoiThucHien) {
        this.nguoiThucHien = nguoiThucHien;
    }
    
    public LocalDateTime getNgayTao() {
        return ngayTao;
    }
    
    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}
