package com.example.demo.service;

import com.example.demo.entity.HangHoa;
import com.example.demo.entity.TraHang;
import com.example.demo.repository.HangHoaRepository;
import com.example.demo.repository.TraHangRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TraHangServiceTest {

    @Autowired
    private TraHangService traHangService;

    @Autowired
    private TraHangRepository traHangRepository;

    @Autowired
    private HangHoaRepository hangHoaRepository;

    private TraHang testTraHang;
    private HangHoa testHangHoa;

    @BeforeEach
    public void setUp() {
        // Clean up
        traHangRepository.deleteAll();
        hangHoaRepository.deleteAll();

        // Create test product
        testHangHoa = new HangHoa();
        testHangHoa.setMaHangHoa("HH001");
        testHangHoa.setTenHangHoa("Test Product");
        testHangHoa.setSoSerial("OSS00001");  // Required field
        testHangHoa.setSoLuongTon(100);
        testHangHoa = hangHoaRepository.save(testHangHoa);

        // Create test return
        testTraHang = new TraHang();
        testTraHang.setMaTraHang("TH001");
        testTraHang.setHangHoa(testHangHoa);
        testTraHang.setSoLuong(10);
        testTraHang.setDonGia(new BigDecimal("50000"));
        testTraHang.setThanhTien(new BigDecimal("500000"));
        testTraHang.setTrangThai(TraHang.TrangThaiTraHang.CHO_DUYET);
        testTraHang = traHangRepository.save(testTraHang);
    }

    @Test
    public void testDuyetTraHang() {
        // Arrange: Return is in CHO_DUYET state
        Long testTraHangId = testTraHang.getId();
        assertNotNull(testTraHangId, "Test TraHang ID should not be null");
        
        int initialStock = testHangHoa.getSoLuongTon();

        // Act: Approve return
        traHangService.duyetTraHang(testTraHangId, "admin");

        // Assert: Status changed and inventory increased
        Optional<TraHang> updated = traHangRepository.findById(testTraHangId);
        assertTrue(updated.isPresent());
        assertEquals(TraHang.TrangThaiTraHang.DA_DUYET, updated.get().getTrangThai());

        Long testHangHoaId = testHangHoa.getId();
        assertNotNull(testHangHoaId, "Test HangHoa ID should not be null");
        HangHoa updatedProduct = hangHoaRepository.findById(testHangHoaId).get();
        assertEquals(initialStock + testTraHang.getSoLuong(), updatedProduct.getSoLuongTon());
    }

    @Test
    public void testDuyetTraHangNonExistent() {
        // Arrange: Non-existent ID

        // Act & Assert: Should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            traHangService.duyetTraHang(9999L, "admin");
        });
    }

    @Test
    public void testTuChoiTraHangAlreadyApproved() {
        // Arrange: Set return as already approved
        testTraHang.setTrangThai(TraHang.TrangThaiTraHang.DA_DUYET);
        traHangRepository.save(testTraHang);

        Long testTraHangId = testTraHang.getId();
        assertNotNull(testTraHangId, "Test TraHang ID should not be null");

        // Act & Assert: Should throw exception
        assertThrows(IllegalStateException.class, () -> {
            traHangService.duyetTraHang(testTraHangId, "admin");
        });
    }

    @Test
    public void testTuChoiTraHang() {
        // Arrange: Return is in CHO_DUYET state
        Long testTraHangId = testTraHang.getId();
        assertNotNull(testTraHangId, "Test TraHang ID should not be null");

        // Act: Reject return
        traHangService.tuChoiTraHang(testTraHangId, "admin", "Product damaged");

        // Assert: Status changed to TU_CHOI
        Optional<TraHang> updated = traHangRepository.findById(testTraHangId);
        assertTrue(updated.isPresent());
        assertEquals(TraHang.TrangThaiTraHang.TU_CHOI, updated.get().getTrangThai());
    }

    @Test
    public void testTuChoiTraHangNonExistent() {
        // Arrange: Non-existent ID

        // Act & Assert: Should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            traHangService.tuChoiTraHang(9999L, "admin", "Reason");
        });
    }

    @Test
    public void testGetTrangThaiTraHang() {
        // Arrange: Return in CHO_DUYET state

        // Act: Get status
        TraHang.TrangThaiTraHang trangThai = testTraHang.getTrangThai();

        // Assert: Verify status
        assertEquals(TraHang.TrangThaiTraHang.CHO_DUYET, trangThai);
    }

    @Test
    public void testInventoryUpdateOnApproval() {
        // Arrange: Initial inventory = 100, return quantity = 10
        int expectedInventory = 100 + 10;
        
        Long testTraHangId = testTraHang.getId();
        assertNotNull(testTraHangId, "Test TraHang ID should not be null");
        
        Long testHangHoaId = testHangHoa.getId();
        assertNotNull(testHangHoaId, "Test HangHoa ID should not be null");

        // Act: Approve return
        traHangService.duyetTraHang(testTraHangId, "admin");

        // Assert: Inventory should be 110
        HangHoa updated = hangHoaRepository.findById(testHangHoaId).get();
        assertEquals(expectedInventory, updated.getSoLuongTon());
    }

    @Test
    public void testMultipleReturnsInventory() {
        // Arrange: Create another return
        TraHang traHang2 = new TraHang();
        traHang2.setMaTraHang("TH002");
        traHang2.setHangHoa(testHangHoa);
        traHang2.setSoLuong(5);
        traHang2.setDonGia(new BigDecimal("50000"));
        traHang2.setThanhTien(new BigDecimal("250000"));
        traHang2.setTrangThai(TraHang.TrangThaiTraHang.CHO_DUYET);
        traHang2 = traHangRepository.save(traHang2);

        Long testTraHangId = testTraHang.getId();
        assertNotNull(testTraHangId, "Test TraHang ID should not be null");
        
        Long traHang2Id = traHang2.getId();
        assertNotNull(traHang2Id, "Second TraHang ID should not be null");
        
        Long testHangHoaId = testHangHoa.getId();
        assertNotNull(testHangHoaId, "Test HangHoa ID should not be null");

        // Act: Approve both returns
        traHangService.duyetTraHang(testTraHangId, "admin");
        traHangService.duyetTraHang(traHang2Id, "admin");

        // Assert: Inventory should be 100 + 10 + 5 = 115
        HangHoa updated = hangHoaRepository.findById(testHangHoaId).get();
        assertEquals(115, updated.getSoLuongTon());
    }

    @Test
    public void testApprovedReturnDetails() {
        // Arrange: Return with specific details
        Long testTraHangId = testTraHang.getId();
        assertNotNull(testTraHangId, "Test TraHang ID should not be null");

        // Act: Approve return
        traHangService.duyetTraHang(testTraHangId, "admin");

        // Assert: Verify all details are set
        Optional<TraHang> updated = traHangRepository.findById(testTraHangId);
        assertTrue(updated.isPresent());
        
        TraHang th = updated.get();
        assertEquals(TraHang.TrangThaiTraHang.DA_DUYET, th.getTrangThai());
        assertEquals("admin", th.getNguoiXuLy());
        assertTrue(th.getDaDuyetTruocDo());
        assertNotNull(th.getNgayXuLy());
        assertNotNull(th.getNgayDuyet());
    }
}
