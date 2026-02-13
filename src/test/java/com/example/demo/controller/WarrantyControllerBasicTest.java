package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Warranty Controller Basic Tests")
@WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
public class WarrantyControllerBasicTest {

    @Autowired
    private MockMvc mockMvc;

    private WarrantyRepository warrantyRepository;
    private HangHoaRepository hangHoaRepository;
    private KhachHangRepository khachHangRepository;
    private WarrantyClaimRepository warrantyClaimRepository;

    private Warranty testWarranty;
    private HangHoa testProduct;
    private KhachHang testCustomer;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        warrantyRepository = mock(WarrantyRepository.class);
        hangHoaRepository = mock(HangHoaRepository.class);
        khachHangRepository = mock(KhachHangRepository.class);
        warrantyClaimRepository = mock(WarrantyClaimRepository.class);

        testCustomer = new KhachHang();
        testCustomer.setId(1L);
        testCustomer.setHoTen("Nguyễn Văn A");
        testCustomer.setSoDienThoai("0123456789");

        testProduct = new HangHoa();
        testProduct.setId(1L);
        testProduct.setTenHangHoa("Laptop Dell XPS");
        testProduct.setSoSerial("SN123456");

        testWarranty = new Warranty();
        testWarranty.setId(1L);
        testWarranty.setHangHoa(testProduct);
        testWarranty.setKhachHang(testCustomer);
        testWarranty.setNgayHetHanBaoHanh(LocalDate.now().plusDays(12));
        testWarranty.setTrangThai("Còn hiệu lực");
    }

    @Test
    @DisplayName("Should display list page")
    @WithMockUser(roles = "USER")
    void testListPage() throws Exception {
        when(warrantyRepository.findAll()).thenReturn(List.of(testWarranty));

        mockMvc.perform(get("/bao-hanh"))
                .andExpect(status().isOk())
                .andExpect(view().name("bao-hanh/list"));
    }

    @Test
    @DisplayName("Should display create form")
    @WithMockUser(roles = "USER")
    void testCreateFormPage() throws Exception {
        when(hangHoaRepository.findAll()).thenReturn(List.of(testProduct));
        when(khachHangRepository.findAll()).thenReturn(List.of(testCustomer));

        mockMvc.perform(get("/bao-hanh/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("bao-hanh/form"));
    }

    @Test
    @DisplayName("Should display warranty detail")
    @WithMockUser(roles = "USER")
    void testDetailPage() throws Exception {
        when(warrantyRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testWarranty));
        when(warrantyClaimRepository.findByWarrantyId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/bao-hanh/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("bao-hanh/detail"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent warranty")
    @WithMockUser(roles = "USER")
    void testDetailPageNotFound() throws Exception {
        when(warrantyRepository.findByIdWithRelations(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/bao-hanh/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bao-hanh"));
    }

    @Test
    @DisplayName("Should display edit form")
    @WithMockUser(roles = "USER")
    void testEditPage() throws Exception {
        when(warrantyRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testWarranty));

        mockMvc.perform(get("/bao-hanh/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("bao-hanh/edit"));
    }

    @Test
    @DisplayName("Should test warranty calculation methods")
    void testWarrantyCalculations() {
        testWarranty.setNgayHetHanBaoHanh(LocalDate.now().plusDays(5));
        long daysRemaining = testWarranty.getDaysRemaining();
        
        // Should have approximately 5 days remaining (give or take 1 day for timing)
        assertTrue(daysRemaining >= 4 && daysRemaining <= 6, "Days remaining should be around 5");
    }

    @Test
    @DisplayName("Should test warranty validity check")
    void testWarrantyValidity() {
        // Not expired
        testWarranty.setNgayHetHanBaoHanh(LocalDate.now().plusDays(10));
        assertTrue(testWarranty.isStillValid(), "Warranty should still be valid");

        // Expired
        testWarranty.setNgayHetHanBaoHanh(LocalDate.now().minusDays(1));
        assertFalse(testWarranty.isStillValid(), "Warranty should be expired");
    }

    @Test
    @DisplayName("Should handle warranty with null serial")
    void testWarrantyNullSerial() {
        testProduct.setSoSerial(null);
        assertNotNull(testWarranty);
        assertEquals(testProduct, testWarranty.getHangHoa());
    }

    @Test
    @DisplayName("Should test warranty getters and setters")
    void testWarrantyGettersSetters() {
        Warranty w = new Warranty();
        w.setId(2L);
        w.setTrangThai("Hết hạn");
        
        assertEquals(2L, w.getId());
        assertEquals("Hết hạn", w.getTrangThai());
    }

    @Test
    @DisplayName("Should test warranty timeline management")
    void testWarrantyTimelines() {
        WarrantyTimeline timeline = new WarrantyTimeline();
        timeline.setBuocThucHien("Received");
        timeline.setWarranty(testWarranty);
        
        testWarranty.addTimeline(timeline);
        assertEquals(1, testWarranty.getTimelines().size());
        
        testWarranty.removeTimeline(timeline);
        assertEquals(0, testWarranty.getTimelines().size());
    }

    @Test
    @DisplayName("Should test customer - warranty relationship")
    void testCustomerWarrantyRelationship() {
        assertNotNull(testWarranty.getKhachHang());
        assertEquals("Nguyễn Văn A", testWarranty.getKhachHang().getHoTen());
    }

    @Test
    @DisplayName("Should test product - warranty relationship")
    void testProductWarrantyRelationship() {
        assertNotNull(testWarranty.getHangHoa());
        assertEquals("Laptop Dell XPS", testWarranty.getHangHoa().getTenHangHoa());
        assertEquals("SN123456", testWarranty.getHangHoa().getSoSerial());
    }
}
