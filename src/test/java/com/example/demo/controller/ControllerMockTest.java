package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== PUBLIC PAGE TESTS ==========

    // Test: Login page is accessible without authentication
    @Test
    public void testLoginPageAccessible() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk());
    }

    // ========== AUTHENTICATION TESTS ==========

    // Test: Unauthenticated access to protected page redirects
    @Test
    public void testUnauthenticatedAccessRedirects() throws Exception {
        mockMvc.perform(get("/hang-hoa"))
                .andExpect(status().is3xxRedirection());
    }

    // ========== AUTHENTICATED USER TESTS ==========

    // Test: Authenticated user can access hang-hoa (merchandise) page
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAuthenticatedUserAccessesHangHoa() throws Exception {
        mockMvc.perform(get("/hang-hoa"))
                .andExpect(status().isOk());
    }

    // Test: Authenticated user can access new item form
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAuthenticatedUserAccessesNewItemForm() throws Exception {
        mockMvc.perform(get("/hang-hoa/new"))
                .andExpect(status().isOk());
    }

    // Test: Authenticated user can access dashboard
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAuthenticatedUserAccessesDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());
    }

    // Test: Authenticated user can access khach-hang (customer) page
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAuthenticatedUserAccessesKhachHang() throws Exception {
        mockMvc.perform(get("/khach-hang"))
                .andExpect(status().isOk());
    }

    // ========== ROLE-BASED ACCESS TESTS ==========

    // Test: User role can access basic pages
    @Test
    @WithMockUser(roles = "USER")
    public void testUserRoleAccessesBasicPages() throws Exception {
        mockMvc.perform(get("/hang-hoa"))
                .andExpect(status().isOk());
    }

    // Test: Multiple roles work (ADMIN, MANAGER)
    @Test
    @WithMockUser(roles = "MANAGER")
    public void testManagerRoleAccessesPages() throws Exception {
        mockMvc.perform(get("/hang-hoa"))
                .andExpect(status().isOk());
    }

    // ========== ERROR HANDLING TESTS ==========

    // Test: Non-existent item ID returns redirect
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testNonExistentItemHandled() throws Exception {
        mockMvc.perform(get("/hang-hoa/9999/edit"))
                .andExpect(status().is3xxRedirection());
    }
}