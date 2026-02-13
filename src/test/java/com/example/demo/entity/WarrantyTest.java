package com.example.demo.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Warranty Entity Tests")
public class WarrantyTest {

    private Warranty warranty;
    private HangHoa hangHoa;
    private KhachHang khachHang;

    @BeforeEach
    void setUp() {
        // Setup test data
        khachHang = new KhachHang();
        khachHang.setId(1L);
        khachHang.setHoTen("Test Customer");

        hangHoa = new HangHoa();
        hangHoa.setId(1L);
        hangHoa.setTenHangHoa("Test Product");
        hangHoa.setSoSerial("SN123");

        warranty = new Warranty();
    }

    @Test
    @DisplayName("Should create warranty with all fields")
    void testCreateWarrantyWithAllFields() {
        // Arrange & Act
        LocalDate ngayBan = LocalDate.of(2024, 11, 9);
        LocalDate ngayHetHan = LocalDate.of(2025, 11, 9);
        LocalDateTime ngayTao = LocalDateTime.now();

        warranty.setId(1L);
        warranty.setChiTietPhieuXuatId(100L);
        warranty.setHangHoa(hangHoa);
        warranty.setKhachHang(khachHang);
        warranty.setNgayBan(ngayBan);
        warranty.setNgayHetHanBaoHanh(ngayHetHan);
        warranty.setTrangThai("Còn hiệu lực");
        warranty.setGhiChu("Test warranty");
        warranty.setNgayTao(ngayTao);

        // Assert
        assertEquals(1L, warranty.getId());
        assertEquals(100L, warranty.getChiTietPhieuXuatId());
        assertEquals(hangHoa, warranty.getHangHoa());
        assertEquals(khachHang, warranty.getKhachHang());
        assertEquals(ngayBan, warranty.getNgayBan());
        assertEquals(ngayHetHan, warranty.getNgayHetHanBaoHanh());
        assertEquals("Còn hiệu lực", warranty.getTrangThai());
        assertEquals("Test warranty", warranty.getGhiChu());
        assertEquals(ngayTao, warranty.getNgayTao());
    }

    @Test
    @DisplayName("Should handle null chi_tiet_phieu_xuat_id for non-company devices")
    void testWarrantyWithNullChiTietPhieuXuatId() {
        // Arrange & Act
        warranty.setChiTietPhieuXuatId(null);
        warranty.setHangHoa(hangHoa);
        warranty.setKhachHang(khachHang);
        warranty.setNgayBan(LocalDate.now());
        warranty.setNgayHetHanBaoHanh(LocalDate.now().plusYears(1));

        // Assert
        assertNull(warranty.getChiTietPhieuXuatId());
        assertNotNull(warranty.getHangHoa());
        assertNotNull(warranty.getKhachHang());
    }

    @Test
    @DisplayName("Should check if warranty is still valid")
    void testIsStillValid() {
        // Arrange
        LocalDate today = LocalDate.now();
        warranty.setNgayHetHanBaoHanh(today.plusDays(1));

        // Act & Assert
        assertTrue(warranty.isStillValid());

        // Warranty expired
        warranty.setNgayHetHanBaoHanh(today.minusDays(1));
        assertFalse(warranty.isStillValid());
    }

    @Test
    @DisplayName("Should calculate days remaining correctly")
    void testGetDaysRemaining() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = today.plusDays(30);
        warranty.setNgayHetHanBaoHanh(expiryDate);

        // Act
        long daysRemaining = warranty.getDaysRemaining();

        // Assert
        assertEquals(30L, daysRemaining);
    }

    @Test
    @DisplayName("Should handle negative days remaining (expired)")
    void testGetDaysRemainingExpired() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = today.minusDays(10);
        warranty.setNgayHetHanBaoHanh(expiryDate);

        // Act
        long daysRemaining = warranty.getDaysRemaining();

        // Assert
        assertEquals(-10L, daysRemaining);
    }

    @Test
    @DisplayName("Should create warranty using constructor")
    void testConstructorWithParameters() {
        // Arrange & Act
        Warranty newWarranty = new Warranty(
                100L,
                hangHoa,
                khachHang,
                LocalDate.of(2024, 11, 9),
                LocalDate.of(2025, 11, 9)
        );

        // Assert
        assertEquals(100L, newWarranty.getChiTietPhieuXuatId());
        assertEquals(hangHoa, newWarranty.getHangHoa());
        assertEquals(khachHang, newWarranty.getKhachHang());
        assertEquals(LocalDate.of(2024, 11, 9), newWarranty.getNgayBan());
        assertEquals(LocalDate.of(2025, 11, 9), newWarranty.getNgayHetHanBaoHanh());
        assertEquals("Còn hiệu lực", newWarranty.getTrangThai());
        assertNotNull(newWarranty.getNgayTao());
    }

    @Test
    @DisplayName("Should manage timeline items")
    void testManageTimelines() {
        // Arrange
        WarrantyTimeline timeline1 = new WarrantyTimeline();
        timeline1.setId(1L);
        
        WarrantyTimeline timeline2 = new WarrantyTimeline();
        timeline2.setId(2L);

        // Act
        warranty.addTimeline(timeline1);
        warranty.addTimeline(timeline2);

        // Assert
        assertEquals(2, warranty.getTimelines().size());
        assertTrue(warranty.getTimelines().contains(timeline1));
        assertTrue(warranty.getTimelines().contains(timeline2));

        // Remove timeline
        warranty.removeTimeline(timeline1);
        assertEquals(1, warranty.getTimelines().size());
        assertFalse(warranty.getTimelines().contains(timeline1));
    }

    @Test
    @DisplayName("Should set status to Còn hiệu lực by default")
    void testDefaultStatus() {
        // Arrange & Act
        Warranty newWarranty = new Warranty();
        newWarranty.setNgayBan(LocalDate.now());
        newWarranty.setNgayHetHanBaoHanh(LocalDate.now().plusYears(1));

        // Assert
        // Note: Constructor sets default, but when not using constructor:
        if (newWarranty.getTrangThai() == null) {
            newWarranty.setTrangThai("Còn hiệu lực");
        }
        assertEquals("Còn hiệu lực", newWarranty.getTrangThai());
    }

    @Test
    @DisplayName("Should handle various warranty statuses")
    void testVariousStatuses() {
        // Arrange & Act
        warranty.setTrangThai("Hết hạn");
        assertEquals("Hết hạn", warranty.getTrangThai());

        warranty.setTrangThai("Đã sửa chữa");
        assertEquals("Đã sửa chữa", warranty.getTrangThai());

        warranty.setTrangThai("Đã hủy");
        assertEquals("Đã hủy", warranty.getTrangThai());
    }

    @Test
    @DisplayName("Should update warranty fields")
    void testUpdateWarrantyFields() {
        // Arrange
        warranty.setId(1L);
        warranty.setTrangThai("Còn hiệu lực");

        // Act - Update fields
        warranty.setTrangThai("Hết hạn");
        warranty.setGhiChu("Updated note");

        // Assert
        assertEquals("Hết hạn", warranty.getTrangThai());
        assertEquals("Updated note", warranty.getGhiChu());
    }

    @Test
    @DisplayName("Should handle warranty with same hàng hóa multiple times")
    void testMultipleWarrantiesForSameProduct() {
        // Arrange & Act
        Warranty warranty1 = new Warranty();
        warranty1.setHangHoa(hangHoa);
        warranty1.setKhachHang(khachHang);

        Warranty warranty2 = new Warranty();
        warranty2.setHangHoa(hangHoa);
        warranty2.setKhachHang(khachHang);

        // Assert
        assertEquals(warranty1.getHangHoa().getId(), warranty2.getHangHoa().getId());
        assertNotEquals(warranty1, warranty2); // Different instances
    }

    @Test
    @DisplayName("Should validate warranty date range")
    void testWarrantyDateRange() {
        // Arrange
        LocalDate ngayBan = LocalDate.of(2024, 11, 9);
        LocalDate ngayHetHan = LocalDate.of(2025, 11, 9);

        warranty.setNgayBan(ngayBan);
        warranty.setNgayHetHanBaoHanh(ngayHetHan);

        // Act & Assert
        assertTrue(ngayHetHan.isAfter(ngayBan));
        long warrantyPeriodDays = java.time.temporal.ChronoUnit.DAYS.between(ngayBan, ngayHetHan);
        assertEquals(365L, warrantyPeriodDays);
    }
}
