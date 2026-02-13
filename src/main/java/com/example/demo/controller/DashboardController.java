package com.example.demo.controller;

import com.example.demo.repository.*;
import com.example.demo.service.DoanhThuService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NhanVienRepository nhanVienRepository;
    
    @Autowired
    private HangHoaRepository hangHoaRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private NhaPhanPhoiRepository nhaPhanPhoiRepository;

    @Autowired
    private DoanhThuService doanhThuService;

    @GetMapping({"", "/"})
    public String dashboard(HttpSession session, Model model) {
        // Note: Interceptor đã kiểm tra đăng nhập rồi, không cần kiểm tra lại
        
        // Thêm thông tin user vào model
        model.addAttribute("currentUser", getCurrentUserName(session));
        model.addAttribute("currentUserRole", getCurrentUserRole(session));
        
        // Thống kê tổng quan
        long tongSoUsers = userRepository.count();
        long tongSoNhanVien = nhanVienRepository.count();
        long tongSoHangHoa = hangHoaRepository.count();
        long tongSoKhachHang = khachHangRepository.count();
        long tongSoNhaPhanPhoi = nhaPhanPhoiRepository.count();
        
        // Thống kê doanh thu theo ngày trong tháng (có tính trừ trả hàng)
        Map<Integer, BigDecimal> doanhThuTheoNgay = doanhThuService.getDoanhThuThangCoTraHang();
        
        // Thống kê chi tiết
        BigDecimal doanhThuGop = doanhThuService.getDoanhThuGopTrongThang();
        BigDecimal tongTienTraHang = doanhThuService.getTongTienTraHangTrongThang();
        BigDecimal doanhThuThucTe = doanhThuService.getTongDoanhThuThangCoTraHang();
        
        // Chuẩn bị dữ liệu cho biểu đồ
        List<Integer> ngayList = new ArrayList<>();
        List<BigDecimal> doanhThuList = new ArrayList<>();
        
        LocalDate now = LocalDate.now();
        int soNgayTrongThang = now.lengthOfMonth();
        
        for (int ngay = 1; ngay <= soNgayTrongThang; ngay++) {
            ngayList.add(ngay);
            doanhThuList.add(doanhThuTheoNgay.getOrDefault(ngay, BigDecimal.ZERO));
        }
        
        // Thêm dữ liệu vào model
        model.addAttribute("pageTitle", "Tổng quan hệ thống");
        model.addAttribute("tongSoUsers", tongSoUsers);
        model.addAttribute("tongSoNhanVien", tongSoNhanVien);
        model.addAttribute("tongSoHangHoa", tongSoHangHoa);
        model.addAttribute("tongSoKhachHang", tongSoKhachHang);
        model.addAttribute("tongSoNhaPhanPhoi", tongSoNhaPhanPhoi);
        model.addAttribute("ngayList", ngayList);
        model.addAttribute("doanhThuList", doanhThuList);
        model.addAttribute("thangHienTai", now.getMonthValue());
        model.addAttribute("namHienTai", now.getYear());
        
        // Thêm thông tin doanh thu chi tiết
        model.addAttribute("doanhThuGop", doanhThuGop);
        model.addAttribute("tongTienTraHang", tongTienTraHang);
        model.addAttribute("doanhThuThucTe", doanhThuThucTe);
        
        // Log truy cập dashboard
        log.info("User " + getCurrentUserName(session) + " (" + getCurrentUserRole(session) + ") đã truy cập Dashboard");
        
        return "dashboard/index";
    }
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}
