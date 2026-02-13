package com.example.demo.controller;

import com.example.demo.entity.ChiTietPhieuXuat;
import com.example.demo.entity.TraHang;
import com.example.demo.entity.HangHoa;
import com.example.demo.entity.KhachHang;
import com.example.demo.entity.User;
import com.example.demo.dto.PhieuXuatDTO;
import com.example.demo.dto.ChiTietPhieuXuatDTO;
import com.example.demo.repository.ChiTietPhieuXuatRepository;
import com.example.demo.repository.TraHangRepository;
import com.example.demo.repository.HangHoaRepository;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.service.TraHangService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tra-hang")
public class TraHangController extends BaseController {
    
    @Autowired
    private TraHangRepository traHangRepository;
    
    @Autowired
    private HangHoaRepository hangHoaRepository;
    
    @Autowired
    private ChiTietPhieuXuatRepository chiTietPhieuXuatRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private TraHangService traHangService;
    
    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("pageTitle", "Test trả hàng");
        model.addAttribute("tongSoTraHang", traHangRepository.count());
        return "tra-hang/test";
    }
    
    @GetMapping
    @Transactional(readOnly = true)
    public String danhSach(Model model, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // Use eager loading to prevent LazyInitializationException
        List<TraHang> danhSachTraHang = traHangRepository.findAllWithEagerHangHoa();
        
        // Thống kê trạng thái
        long choDuyet = traHangRepository.countByTrangThai(TraHang.TrangThaiTraHang.CHO_DUYET);
        long daDuyet = traHangRepository.countByTrangThai(TraHang.TrangThaiTraHang.DA_DUYET);
        long tuChoi = traHangRepository.countByTrangThai(TraHang.TrangThaiTraHang.TU_CHOI);
        
        model.addAttribute("pageTitle", "Quản lý trả hàng");
        model.addAttribute("danhSachTraHang", danhSachTraHang);
        model.addAttribute("choDuyet", choDuyet);
        model.addAttribute("daDuyet", daDuyet);
        model.addAttribute("tuChoi", tuChoi);
        model.addAttribute("tongSo", danhSachTraHang.size());
        
        return "tra-hang/danh-sach";
    }
    
    @GetMapping("/them")
    public String themMoi(Model model, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // Tạo mã trả hàng tự động
        String maTraHang = taoMaTraHang();
        
        // Lấy danh sách hàng hóa và khách hàng
        List<HangHoa> danhSachHangHoa = hangHoaRepository.findAll();
        List<KhachHang> danhSachKhachHang = khachHangRepository.findAll();
        
        // Lấy danh sách chi tiết phiếu xuất để hiển thị thông tin đầy đủ
        List<ChiTietPhieuXuat> danhSachChiTietPhieuXuat = chiTietPhieuXuatRepository.findAllWithDetails();
        
        model.addAttribute("pageTitle", "Thêm mới trả hàng");
        model.addAttribute("traHang", new TraHang());
        model.addAttribute("maTraHang", maTraHang);
        model.addAttribute("danhSachHangHoa", danhSachHangHoa);
        model.addAttribute("danhSachKhachHang", danhSachKhachHang);
        model.addAttribute("danhSachChiTietPhieuXuat", danhSachChiTietPhieuXuat);
        model.addAttribute("danhSachTrangThai", TraHang.TrangThaiTraHang.values());
        
        return "tra-hang/them";
    }
    
    @PostMapping("/them")
    public String luuTraHang(@ModelAttribute TraHang traHang, 
                            @RequestParam(required = false) Long hangHoaId,
                            @RequestParam(required = false, name = "products") String productsJson,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // Debug: in ra giá trị nhận được
        System.out.println("🔍 Received traHang: " + traHang);
        System.out.println("🔍 Received hangHoaId: " + hangHoaId);
        System.out.println("🔍 Received productsJson: " + productsJson);
        System.out.println("🔍 productsJson is null: " + (productsJson == null));
        System.out.println("🔍 productsJson is empty: " + (productsJson != null && productsJson.trim().isEmpty()));

        try {
            // Nếu không có hangHoaId và productsJson, trả về lỗi
            if ((hangHoaId == null || hangHoaId <= 0) && (productsJson == null || productsJson.trim().isEmpty())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất một sản phẩm để trả hàng!");
                return "redirect:/tra-hang/them";
            }

            // Nếu chỉ có hangHoaId (cách cũ - 1 sản phẩm)
            if (hangHoaId != null && hangHoaId > 0) {
                Optional<HangHoa> hangHoaOpt = hangHoaRepository.findById(hangHoaId);
                if (hangHoaOpt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hàng hóa!");
                    return "redirect:/tra-hang/them";
                }
                
                HangHoa hangHoa = hangHoaOpt.get();
                traHang.setHangHoa(hangHoa);
                
                // Tạo mã trả hàng nếu chưa có
                if (traHang.getMaTraHang() == null || traHang.getMaTraHang().isEmpty()) {
                    traHang.setMaTraHang(taoMaTraHang());
                }
                
                // Tính thành tiền
                if (traHang.getDonGia() != null && traHang.getSoLuong() != null) {
                    BigDecimal thanhTien = traHang.getDonGia().multiply(new BigDecimal(traHang.getSoLuong()));
                    traHang.setThanhTien(thanhTien);
                }
                
                // Set người xử lý
                traHang.setNguoiXuLy(user.getUsername());
                traHang.setNgayTraHang(LocalDateTime.now());
                
                // Lưu trả hàng
                traHangRepository.save(traHang);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã thêm mới trả hàng thành công! Mã: " + traHang.getMaTraHang());
                
            } else if (productsJson != null && !productsJson.trim().isEmpty()) {
                // Xử lý nhiều sản phẩm từ JSON
                System.out.println("📦 Processing multiple products from JSON");
                try {
                    // Parse JSON array
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.List<java.util.Map<String, Object>> products = objectMapper.readValue(productsJson, 
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>(){});
                    
                    System.out.println("✅ Parsed " + products.size() + " products from JSON");
                    
                    if (products.isEmpty()) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Danh sách sản phẩm trống!");
                        return "redirect:/tra-hang/them";
                    }
                    
                    // Lưu từng sản phẩm
                    for (java.util.Map<String, Object> productData : products) {
                        TraHang currentTraHang = new TraHang();
                        
                        // Copy thông tin từ form gốc
                        currentTraHang.setTenKhachHang(traHang.getTenKhachHang());
                        currentTraHang.setSoDienThoai(traHang.getSoDienThoai());
                        currentTraHang.setLyDo(traHang.getLyDo());
                        currentTraHang.setTrangThai(traHang.getTrangThai() != null ? traHang.getTrangThai() : TraHang.TrangThaiTraHang.CHO_DUYET);
                        
                        // Lấy hàng hóa từ JSON - handle both Number and String types
                        Object hangHoaIdObj = productData.get("hangHoaId");
                        Long hangHoaIdFromJson;
                        if (hangHoaIdObj instanceof Number) {
                            hangHoaIdFromJson = ((Number) hangHoaIdObj).longValue();
                        } else {
                            hangHoaIdFromJson = Long.parseLong(hangHoaIdObj.toString());
                        }
                        
                        System.out.println("📦 Processing product with hangHoaId: " + hangHoaIdFromJson + " (type: " + hangHoaIdObj.getClass().getSimpleName() + ")");
                        
                        Optional<HangHoa> hangHoaOpt = hangHoaRepository.findById(hangHoaIdFromJson);
                        
                        if (hangHoaOpt.isEmpty()) {
                            System.out.println("⚠️  Hàng hóa ID " + hangHoaIdFromJson + " không tìm thấy, bỏ qua");
                            continue;
                        }
                        
                        currentTraHang.setHangHoa(hangHoaOpt.get());
                        
                        // Handle soLuong - can be Number or String
                        Object soLuongObj = productData.get("soLuong");
                        int soLuong = soLuongObj instanceof Number ? 
                            ((Number) soLuongObj).intValue() : 
                            Integer.parseInt(soLuongObj.toString());
                        currentTraHang.setSoLuong(soLuong);
                        
                        // Handle donGia
                        Object donGiaObj = productData.get("donGia");
                        BigDecimal donGia = donGiaObj instanceof BigDecimal ? 
                            (BigDecimal) donGiaObj : 
                            new BigDecimal(donGiaObj.toString());
                        currentTraHang.setDonGia(donGia);
                        
                        // Handle thanhTien
                        Object thanhTienObj = productData.get("thanhTien");
                        BigDecimal thanhTien = thanhTienObj instanceof BigDecimal ? 
                            (BigDecimal) thanhTienObj : 
                            new BigDecimal(thanhTienObj.toString());
                        currentTraHang.setThanhTien(thanhTien);
                        
                        // Tạo mã trả hàng
                        currentTraHang.setMaTraHang(taoMaTraHang());
                        
                        // Set người xử lý
                        currentTraHang.setNguoiXuLy(user.getUsername());
                        currentTraHang.setNgayTraHang(LocalDateTime.now());
                        
                        // Lưu
                        traHangRepository.save(currentTraHang);
                        System.out.println("✅ Saved trả hàng: " + currentTraHang.getMaTraHang() + " - " + hangHoaOpt.get().getTenHangHoa());
                    }
                    
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Đã thêm " + products.size() + " trả hàng thành công!");
                    
                } catch (Exception e) {
                    System.err.println("❌ Error parsing products JSON: " + e.getMessage());
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Lỗi xử lý dữ liệu sản phẩm: " + e.getMessage());
                    return "redirect:/tra-hang/them";
                }
            }
            
            return "redirect:/tra-hang";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/tra-hang/them";
        }
    }
    
    @GetMapping("/chi-tiet/{id}")
    @Transactional(readOnly = true)
    public String chiTiet(@PathVariable(required = false) Long id, Model model, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // Validate id parameter
        if (id == null || id <= 0) {
            return "redirect:/tra-hang";
        }

        Optional<TraHang> traHangOpt = traHangRepository.findByIdWithEagerHangHoa(id);
        if (traHangOpt.isEmpty()) {
            return "redirect:/tra-hang";
        }        TraHang traHang = traHangOpt.get();
        
        model.addAttribute("pageTitle", "Chi tiết trả hàng - " + traHang.getMaTraHang());
        model.addAttribute("traHang", traHang);
        
        return "tra-hang/chi-tiet";
    }
    
    @GetMapping("/sua/{id}")
    @Transactional(readOnly = true)
    public String suaTraHang(@PathVariable(required = false) Long id, Model model, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // Validate id parameter
        if (id == null || id <= 0) {
            return "redirect:/tra-hang";
        }

        Optional<TraHang> traHangOpt = traHangRepository.findById(id);
        if (traHangOpt.isEmpty()) {
            return "redirect:/tra-hang";
        }
        
        TraHang traHang = traHangOpt.get();
        List<HangHoa> danhSachHangHoa = hangHoaRepository.findAll();
        List<KhachHang> danhSachKhachHang = khachHangRepository.findAll();
        
        model.addAttribute("pageTitle", "Sửa trả hàng - " + traHang.getMaTraHang());
        model.addAttribute("traHang", traHang);
        model.addAttribute("danhSachHangHoa", danhSachHangHoa);
        model.addAttribute("danhSachKhachHang", danhSachKhachHang);
        model.addAttribute("danhSachTrangThai", TraHang.TrangThaiTraHang.values());
        
        return "tra-hang/sua";
    }
    
    @PostMapping("/sua/{id}")
    public String capNhatTraHang(@PathVariable(required = false) Long id,
                                @ModelAttribute TraHang traHang,
                                @RequestParam(required = false) Long hangHoaId,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID trả hàng không hợp lệ");
            return "redirect:/tra-hang";
        }

        try {
            Optional<TraHang> traHangOpt = traHangRepository.findById(id);
            if (traHangOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy trả hàng!");
                return "redirect:/tra-hang";
            }
            
            TraHang traHangCu = traHangOpt.get();
            
            // Validate hangHoaId parameter
            if (hangHoaId == null || hangHoaId <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "ID hàng hóa không hợp lệ!");
                return "redirect:/tra-hang/sua/" + id;
            }
            
            // Kiểm tra hàng hóa
            Optional<HangHoa> hangHoaOpt = hangHoaRepository.findById(hangHoaId);
            if (hangHoaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hàng hóa!");
                return "redirect:/tra-hang/sua/" + id;
            }            // Cập nhật thông tin
            traHangCu.setHangHoa(hangHoaOpt.get());
            traHangCu.setSoLuong(traHang.getSoLuong());
            traHangCu.setDonGia(traHang.getDonGia());
            traHangCu.setTenKhachHang(traHang.getTenKhachHang());
            traHangCu.setSoDienThoai(traHang.getSoDienThoai());
            traHangCu.setLyDo(traHang.getLyDo());
            traHangCu.setTrangThai(traHang.getTrangThai());
            
            // Tính lại thành tiền
            if (traHangCu.getDonGia() != null && traHangCu.getSoLuong() != null) {
                BigDecimal thanhTien = traHangCu.getDonGia().multiply(new BigDecimal(traHangCu.getSoLuong()));
                traHangCu.setThanhTien(thanhTien);
            }
            
            // Cập nhật ngày xử lý nếu trạng thái thay đổi
            if (traHang.getTrangThai() != traHangCu.getTrangThai() && 
                traHang.getTrangThai() != TraHang.TrangThaiTraHang.CHO_DUYET) {
                traHangCu.setNgayXuLy(LocalDateTime.now());
                traHangCu.setNguoiXuLy(user.getUsername());
            }
            
            traHangRepository.save(traHangCu);
            
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trả hàng thành công!");
            return "redirect:/tra-hang/chi-tiet/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/tra-hang/sua/" + id;
        }
    }
    
    @PostMapping("/xoa/{id}")
    public String xoaTraHang(@PathVariable(required = false) Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // Validate id parameter
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID trả hàng không hợp lệ");
            return "redirect:/tra-hang";
        }

        try {
            Optional<TraHang> traHangOpt = traHangRepository.findById(id);
            if (traHangOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy trả hàng!");
                return "redirect:/tra-hang";
            }
            
            TraHang traHang = traHangOpt.get();
            if (traHang != null) {
                traHangRepository.delete(traHang);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã xóa trả hàng " + traHang.getMaTraHang() + " thành công!");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/tra-hang";
    }
    
    @PostMapping("/duyet/{id}")
    public String duyetTraHang(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        try {
            traHangService.duyetTraHang(id, user.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã duyệt trả hàng thành công! Hàng hóa đã được nhập lại vào kho.");
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/tra-hang";
    }
    
    @PostMapping("/tu-choi/{id}")
    public String tuChoiTraHang(@PathVariable Long id, 
                               @RequestParam(required = false) String lyDoTuChoi,
                               RedirectAttributes redirectAttributes, 
                               HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        try {
            traHangService.tuChoiTraHang(id, user.getUsername(), lyDoTuChoi);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã từ chối trả hàng thành công!");
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/tra-hang";
    }
    
    @PostMapping("/hoan-tac/{id}")
    public String hoanTacDuyetTraHang(@PathVariable Long id, 
                                     RedirectAttributes redirectAttributes, 
                                     HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        try {
            traHangService.hoanTacDuyetTraHang(id, user.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã hoàn tác duyệt trả hàng thành công! Hàng hóa đã được trừ khỏi kho.");
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/tra-hang";
    }
    
    // API endpoints for AJAX
    @GetMapping("/api/search-khach-hang")
    @ResponseBody
    public List<KhachHang> searchKhachHang(@RequestParam("q") String query) {
        return khachHangRepository.timKiemKhachHang(query, query);
    }
    
    @GetMapping("/api/all-phieu-xuat")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<PhieuXuatDTO> getAllPhieuXuat() {
        return chiTietPhieuXuatRepository.findAllPhieuXuatWithDetails();
    }
    
    @GetMapping("/api/phieu-xuat/{phieuXuatId}/chi-tiet")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<ChiTietPhieuXuatDTO> getPhieuXuatDetails(@PathVariable Long phieuXuatId) {
        List<ChiTietPhieuXuat> details = chiTietPhieuXuatRepository.findByPhieuXuatId(phieuXuatId);
        return details.stream().map(ct -> new ChiTietPhieuXuatDTO(
            ct.getId(),
            ct.getHangHoa().getId(),
            ct.getHangHoa().getTenHangHoa(),
            ct.getHangHoa().getMaHangHoa(),
            ct.getSoLuong(),
            ct.getDonGia(),
            ct.getThanhTien()
        )).toList();
    }
    
    @GetMapping("/api/hang-hoa/{id}")
    @ResponseBody
    public HangHoa getHangHoaInfo(@PathVariable(required = false) Long id) {
        // Validate id parameter
        if (id == null || id <= 0) {
            return null;
        }
        
        return hangHoaRepository.findById(id).orElse(null);
    }
    
    @GetMapping("/api/hang-hoa/{id}/chi-tiet-phieu-xuat")
    @ResponseBody
    public List<ChiTietPhieuXuat> getChiTietPhieuXuatByHangHoa(@PathVariable Long id) {
        return chiTietPhieuXuatRepository.findByHangHoaId(id);
    }
    
    @GetMapping("/api/hang-hoa/{id}/khach-hang-da-mua")
    @ResponseBody  
    public List<Object[]> getKhachHangDaMuaHangHoa(@PathVariable Long id) {
        // Lấy danh sách khách hàng đã mua hàng hóa này từ phiếu xuất
        return chiTietPhieuXuatRepository.findKhachHangDaMuaHangHoa(id);
    }
    
    // Private methods
    private String taoMaTraHang() {
        // Generate 9-character code using digits 1-9 and letters A-Z (base 35)
        // Increments in order: 1,2,3,4,5,6,7,8,9,A,B,C,...,Z,11,12,...
        
        // Get the last mã trả hàng and extract the sequence number
        String lastCode = traHangRepository.findLastMaTraHang();
        long nextNumber = 1;
        
        if (lastCode != null && lastCode.length() >= 11 && lastCode.startsWith("TH")) {
            String codeDigits = lastCode.substring(2); // Remove "TH" prefix
            try {
                nextNumber = convertFromBase35(codeDigits) + 1;
                System.out.println("📝 Last code: " + lastCode + " -> Parsed number: " + (nextNumber - 1) + " -> Next: " + nextNumber);
            } catch (Exception e) {
                System.err.println("⚠️  Could not parse last code, starting from 1: " + e.getMessage());
                nextNumber = 1;
            }
        } else {
            System.out.println("📝 No previous code found, starting from 1");
        }
        
        // Convert to base-35 string (9 characters)
        String code = convertToBase35(nextNumber, 9);
        
        System.out.println("✅ Generated code: TH" + code + " (sequence: " + nextNumber + ")");
        
        return "TH" + code;
    }
    
    private String convertToBase35(long number, int length) {
        // Character set: 1-9 (9 chars) + A-Z (26 chars) = 35 total
        // This gives natural ordering: 1,2,3,...,9,A,B,...,Z,11,12,...
        String chars = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder result = new StringBuilder();
        
        // Convert number to base-35
        if (number == 0) {
            for (int i = 0; i < length; i++) {
                result.append('1');
            }
            return result.toString();
        }
        
        while (number > 0) {
            result.insert(0, chars.charAt((int)((number - 1) % 35)));
            number = (number - 1) / 35;
        }
        
        // Pad with leading '1's to reach desired length
        while (result.length() < length) {
            result.insert(0, '1');
        }
        
        return result.toString();
    }
    
    private long convertFromBase35(String code) {
        // Reverse of convertToBase35 - convert base-35 string back to number
        String chars = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        long result = 0;
        
        for (char c : code.toCharArray()) {
            int digit = chars.indexOf(c);
            if (digit < 0) {
                throw new IllegalArgumentException("Invalid character in code: " + c);
            }
            result = result * 35 + digit + 1;
        }
        
        return result;
    }
}