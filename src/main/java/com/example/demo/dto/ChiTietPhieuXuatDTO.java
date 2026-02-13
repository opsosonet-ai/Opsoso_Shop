package com.example.demo.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for ChiTietPhieuXuat to avoid LazyInitializationException during JSON serialization
 */
public class ChiTietPhieuXuatDTO implements Serializable {
    private Long id;
    private HangHoaDTO hangHoa;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    
    public ChiTietPhieuXuatDTO() {}
    
    public ChiTietPhieuXuatDTO(Long id, Long hangHoaId, String tenHangHoa, 
                              String maHangHoa, Integer soLuong, BigDecimal donGia, BigDecimal thanhTien) {
        this.id = id;
        this.hangHoa = new HangHoaDTO(hangHoaId, tenHangHoa, maHangHoa);
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }
    
    // Nested HangHoa DTO
    public static class HangHoaDTO {
        private Long id;
        private String tenHangHoa;
        private String maHangHoa;
        
        public HangHoaDTO() {}
        
        public HangHoaDTO(Long id, String tenHangHoa, String maHangHoa) {
            this.id = id;
            this.tenHangHoa = tenHangHoa;
            this.maHangHoa = maHangHoa;
        }
        
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
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public HangHoaDTO getHangHoa() {
        return hangHoa;
    }
    
    public void setHangHoa(HangHoaDTO hangHoa) {
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
}
