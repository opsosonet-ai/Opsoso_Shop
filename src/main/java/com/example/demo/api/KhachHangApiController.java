package com.example.demo.api;

import com.example.demo.entity.KhachHang;
import com.example.demo.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/khach-hang")
public class KhachHangApiController {

    @Autowired
    private KhachHangRepository khachHangRepository;

    // API endpoint để cập nhật thông tin khách hàng (AJAX inline edit)
    @PostMapping("/{id}")
    public Map<String, Object> updateCustomer(@PathVariable(required = false) Long id,
                                               @RequestParam(required = false) String hoTen,
                                               @RequestParam(required = false) String soDienThoai,
                                               @RequestParam(required = false) String email,
                                               @RequestParam(required = false) String diaChi) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate id parameter
            if (id == null || id <= 0) {
                response.put("success", false);
                response.put("message", "ID khách hàng không hợp lệ");
                return response;
            }
            
            Optional<KhachHang> existingKhachHang = khachHangRepository.findById(id);
            if (existingKhachHang.isPresent()) {
                KhachHang khachHang = existingKhachHang.get();
                
                // Chỉ cập nhật các trường được gửi (không null/empty)
                if (hoTen != null && !hoTen.trim().isEmpty()) {
                    khachHang.setHoTen(hoTen);
                }
                if (soDienThoai != null && !soDienThoai.trim().isEmpty()) {
                    khachHang.setSoDienThoai(soDienThoai);
                }
                if (email != null && !email.trim().isEmpty()) {
                    khachHang.setEmail(email);
                }
                if (diaChi != null && !diaChi.trim().isEmpty()) {
                    khachHang.setDiaChi(diaChi);
                }
                
                // Ensure khachHang is not null before saving
                if (khachHang != null) {
                    khachHangRepository.save(khachHang);
                    response.put("success", true);
                    response.put("message", "Cập nhật thành công!");
                    response.put("data", khachHang);
                } else {
                    response.put("success", false);
                    response.put("message", "Lỗi: Đối tượng khách hàng không hợp lệ");
                }
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy khách hàng với ID: " + id);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }
}
