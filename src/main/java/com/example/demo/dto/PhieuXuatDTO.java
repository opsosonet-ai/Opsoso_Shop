package com.example.demo.dto;

import java.io.Serializable;

/**
 * DTO for PhieuXuat to avoid LazyInitializationException during JSON serialization
 */
public class PhieuXuatDTO implements Serializable {
    private Long id;
    private String maPhieuXuat;
    private KhachHangDTO khachHang;
    private java.time.LocalDateTime ngayXuat;
    
    public PhieuXuatDTO() {}
    
    public PhieuXuatDTO(Long id, String maPhieuXuat, String tenKhachHang, 
                       String soDienThoai, String diaChi, java.time.LocalDateTime ngayXuat) {
        this.id = id;
        this.maPhieuXuat = maPhieuXuat;
        this.khachHang = new KhachHangDTO(tenKhachHang, soDienThoai, diaChi);
        this.ngayXuat = ngayXuat;
    }
    
    // Nested KhachHang DTO
    public static class KhachHangDTO {
        private String hoTen;
        private String soDienThoai;
        private String diaChi;
        
        public KhachHangDTO() {}
        
        public KhachHangDTO(String hoTen, String soDienThoai, String diaChi) {
            this.hoTen = hoTen;
            this.soDienThoai = soDienThoai;
            this.diaChi = diaChi;
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
        
        public String getDiaChi() {
            return diaChi;
        }
        
        public void setDiaChi(String diaChi) {
            this.diaChi = diaChi;
        }
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
    
    public KhachHangDTO getKhachHang() {
        return khachHang;
    }
    
    public void setKhachHang(KhachHangDTO khachHang) {
        this.khachHang = khachHang;
    }
    
    public java.time.LocalDateTime getNgayXuat() {
        return ngayXuat;
    }
    
    public void setNgayXuat(java.time.LocalDateTime ngayXuat) {
        this.ngayXuat = ngayXuat;
    }
}
