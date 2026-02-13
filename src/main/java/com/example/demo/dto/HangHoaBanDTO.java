package com.example.demo.dto;

import java.time.LocalDate;

/**
 * DTO để trả dữ liệu hàng hóa đã bán cùng ngày xuất
 */
public class HangHoaBanDTO {
    
    private Long id;
    private String tenHangHoa;
    private LocalDate ngayBan;  // Ngày xuất từ PhieuXuat (chuyển về LocalDate)
    private Integer soLuongTon;
    
    // Constructor
    public HangHoaBanDTO(Long id, String tenHangHoa, LocalDate ngayBan, Integer soLuongTon) {
        this.id = id;
        this.tenHangHoa = tenHangHoa;
        this.ngayBan = ngayBan;
        this.soLuongTon = soLuongTon;
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
    
    public LocalDate getNgayBan() {
        return ngayBan;
    }
    
    public void setNgayBan(LocalDate ngayBan) {
        this.ngayBan = ngayBan;
    }
    
    public Integer getSoLuongTon() {
        return soLuongTon;
    }
    
    public void setSoLuongTon(Integer soLuongTon) {
        this.soLuongTon = soLuongTon;
    }
}
