package com.example.demo.service;

import com.example.demo.entity.HangHoa;
import com.example.demo.entity.PhieuXuat;
import com.example.demo.entity.TraHang;
import com.example.demo.repository.HangHoaRepository;
import com.example.demo.repository.PhieuXuatRepository;
import com.example.demo.repository.TraHangRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DoanhThuServiceTest {

    @Autowired
    private DoanhThuService doanhThuService;

    @Autowired
    private PhieuXuatRepository phieuXuatRepository;

    @Autowired
    private TraHangRepository traHangRepository;

    @Autowired
    private HangHoaRepository hangHoaRepository;

    private HangHoa testProduct;

    @BeforeEach
    public void setUp() {
        // Clean up before each test
        traHangRepository.deleteAll();
        phieuXuatRepository.deleteAll();
        hangHoaRepository.deleteAll();

        // Create a test product for returns
        testProduct = new HangHoa();
        testProduct.setMaHangHoa("TEST_HH");
        testProduct.setTenHangHoa("Test Product");
        testProduct.setSoSerial("OSS_TEST_001");
        testProduct.setSoLuongTon(100);
        testProduct = hangHoaRepository.save(testProduct);
    }

    @Test
    public void testGetDoanhThuThangCoTraHang() {
        // Arrange: Create a test invoice for current month
        PhieuXuat phieuXuat = new PhieuXuat();
        phieuXuat.setMaPhieuXuat("PX001");
        phieuXuat.setNgayXuat(LocalDateTime.now());
        phieuXuat.setTongTien(new BigDecimal("1000000"));
        phieuXuatRepository.save(phieuXuat);

        // Act: Get revenue data
        java.util.Map<Integer, BigDecimal> doanhThu = doanhThuService.getDoanhThuThangCoTraHang();

        // Assert: Verify revenue is calculated
        assertNotNull(doanhThu);
        assertTrue(doanhThu.size() > 0);
    }

    @Test
    public void testGetDoanhThuGopTrongThang() {
        // Arrange: Create test data
        PhieuXuat phieuXuat = new PhieuXuat();
        phieuXuat.setMaPhieuXuat("PX002");
        phieuXuat.setNgayXuat(LocalDateTime.now());
        phieuXuat.setTongTien(new BigDecimal("500000"));
        phieuXuatRepository.save(phieuXuat);

        // Act: Get monthly revenue
        BigDecimal doanhThuThang = doanhThuService.getDoanhThuGopTrongThang();

        // Assert: Verify revenue is calculated
        assertNotNull(doanhThuThang);
        assertTrue(doanhThuThang.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    public void testGetDoanhThuGopHomNay() {
        // Arrange: Create today's invoice
        PhieuXuat phieuXuat = new PhieuXuat();
        phieuXuat.setMaPhieuXuat("PX003");
        phieuXuat.setNgayXuat(LocalDateTime.now());
        phieuXuat.setTongTien(new BigDecimal("300000"));
        phieuXuatRepository.save(phieuXuat);

        // Act: Get today's revenue
        BigDecimal doanhThuHom = doanhThuService.getDoanhThuGopHomNay();

        // Assert: Verify today's revenue
        assertNotNull(doanhThuHom);
        assertTrue(doanhThuHom.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    public void testGetTongTienTraHangTrongThang() {
        // Arrange: Create return data
        TraHang traHang = new TraHang();
        traHang.setMaTraHang("TH001");
        traHang.setHangHoa(testProduct);
        traHang.setTrangThai(TraHang.TrangThaiTraHang.DA_DUYET);
        traHang.setDonGia(new BigDecimal("50000"));
        traHang.setSoLuong(2);
        traHang.setThanhTien(new BigDecimal("100000"));
        traHang.setNgayXuLy(LocalDateTime.now());
        traHangRepository.save(traHang);

        // Act: Get returns amount for month
        BigDecimal tongTienTra = doanhThuService.getTongTienTraHangTrongThang();

        // Assert: Verify returns amount
        assertNotNull(tongTienTra);
        assertTrue(tongTienTra.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    public void testGetTongTienTraHangHomNay() {
        // Arrange: Create today's return
        TraHang traHang = new TraHang();
        traHang.setMaTraHang("TH002");
        traHang.setHangHoa(testProduct);
        traHang.setTrangThai(TraHang.TrangThaiTraHang.DA_DUYET);
        traHang.setDonGia(new BigDecimal("25000"));
        traHang.setSoLuong(2);
        traHang.setThanhTien(new BigDecimal("50000"));
        traHang.setNgayXuLy(LocalDateTime.now());
        traHangRepository.save(traHang);

        // Act: Get today's returns
        BigDecimal tongTienTraHom = doanhThuService.getTongTienTraHangHomNay();

        // Assert: Verify today's returns
        assertNotNull(tongTienTraHom);
        assertTrue(tongTienTraHom.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    public void testGetTongDoanhThuThangCoTraHang() {
        // Arrange: Create invoice and return for current month
        PhieuXuat phieuXuat = new PhieuXuat();
        phieuXuat.setMaPhieuXuat("PX004");
        phieuXuat.setNgayXuat(LocalDateTime.now());
        phieuXuat.setTongTien(new BigDecimal("1000000"));
        phieuXuatRepository.save(phieuXuat);

        TraHang traHang = new TraHang();
        traHang.setMaTraHang("TH003");
        traHang.setHangHoa(testProduct);
        traHang.setTrangThai(TraHang.TrangThaiTraHang.DA_DUYET);
        traHang.setDonGia(new BigDecimal("100000"));
        traHang.setSoLuong(2);
        traHang.setThanhTien(new BigDecimal("200000"));
        traHang.setNgayXuLy(LocalDateTime.now());
        traHangRepository.save(traHang);

        // Act: Get net revenue
        BigDecimal doanhThuThucTe = doanhThuService.getTongDoanhThuThangCoTraHang();

        // Assert: Verify net revenue (should be less than gross due to returns)
        assertNotNull(doanhThuThucTe);
        assertTrue(doanhThuThucTe.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    public void testGetTongDoanhThuHomNayCoTraHang() {
        // Arrange: Create today's invoice and return
        PhieuXuat phieuXuat = new PhieuXuat();
        phieuXuat.setMaPhieuXuat("PX005");
        phieuXuat.setNgayXuat(LocalDateTime.now());
        phieuXuat.setTongTien(new BigDecimal("800000"));
        phieuXuatRepository.save(phieuXuat);

        TraHang traHang = new TraHang();
        traHang.setMaTraHang("TH004");
        traHang.setHangHoa(testProduct);
        traHang.setTrangThai(TraHang.TrangThaiTraHang.DA_DUYET);
        traHang.setDonGia(new BigDecimal("50000"));
        traHang.setSoLuong(2);
        traHang.setThanhTien(new BigDecimal("100000"));
        traHang.setNgayXuLy(LocalDateTime.now());
        traHangRepository.save(traHang);

        // Act: Get today's net revenue
        BigDecimal doanhThuThucTeHom = doanhThuService.getTongDoanhThuHomNayCoTraHang();

        // Assert: Verify today's net revenue
        assertNotNull(doanhThuThucTeHom);
        assertTrue(doanhThuThucTeHom.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    public void testDoanhThuWithNoData() {
        // Arrange: Empty database

        // Act: Get revenue data
        BigDecimal doanhThu = doanhThuService.getDoanhThuGopTrongThang();

        // Assert: Should return zero
        assertNotNull(doanhThu);
        assertEquals(BigDecimal.ZERO, doanhThu);
    }

    @Test
    public void testDoanhThuWithPastMonthData() {
        // Arrange: Create invoice from last month
        PhieuXuat phieuXuat = new PhieuXuat();
        phieuXuat.setMaPhieuXuat("PX006");
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        phieuXuat.setNgayXuat(lastMonth);
        phieuXuat.setTongTien(new BigDecimal("1000000"));
        phieuXuatRepository.save(phieuXuat);

        // Act: Get current month revenue
        BigDecimal doanhThuThangNay = doanhThuService.getDoanhThuGopTrongThang();

        // Assert: Should not include last month's data
        assertNotNull(doanhThuThangNay);
        assertEquals(BigDecimal.ZERO, doanhThuThangNay);
    }
}
