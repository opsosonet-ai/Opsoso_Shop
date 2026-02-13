package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Service - Không tự động chạy khi khởi động (CommandLineRunner được vô hiệu hóa)
@Service
public class DataInitializationService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializationService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private NhanVienRepository nhanVienRepository;
    @Autowired private HangHoaRepository hangHoaRepository;
    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private NhaPhanPhoiRepository nhaPhanPhoiRepository;
    @Autowired private StoreInfoRepository storeInfoRepository;
    @Autowired private PhieuXuatRepository phieuXuatRepository;
    @Autowired private ChiTietPhieuXuatRepository chiTietPhieuXuatRepository;
    @Autowired private TraHangRepository traHangRepository;
    @Autowired private DoiTraHangHoaRepository doiTraHangHoaRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Vô hiệu hóa: Không tự động khởi tạo khi ứng dụng khởi động
        // Khởi tạo chỉ khi user nhấn button "Khởi tạo dữ liệu" trên trang settings
        log.warn("⚠️  Data initialization is disabled at startup. Use Settings page to initialize data.");
    }

    // Phương thức công khai để khởi tạo dữ liệu theo yêu cầu
    public void initializeData() throws Exception {
        initializeUsers();
        initializeNhanVien();
        initializeHangHoa();
        initializeKhachHang();
        initializeNhaPhanPhoi();
        initializeStoreInfo();
        initializePhieuXuat();
        initializeTraHang();
        initializeDoiTraHangHoa();
    }


    private void initializeUsers() {
        if (userRepository.count() == 0) {
            // Tạo tài khoản Root/Admin chính
            User root = new User("root", passwordEncoder.encode("root123"), "Administrator", "root@company.com", User.Role.ADMIN);
            userRepository.save(root);
            
            // Tạo tài khoản Admin mặc định
            User admin = new User("admin", passwordEncoder.encode("admin123"), "Quản trị viên", "admin@company.com", User.Role.ADMIN);
            userRepository.save(admin);
            
            // Tạo tài khoản Manager
            User manager = new User("manager", passwordEncoder.encode("manager123"), "Nguyễn Văn Manager", "manager@company.com", User.Role.MANAGER);
            userRepository.save(manager);
            
            // Tạo tài khoản User thường
            User user = new User("user", passwordEncoder.encode("user123"), "Trần Thị User", "user@company.com", User.Role.USER);
            userRepository.save(user);
            
            log.info("✅ Đã tạo tài khoản người dùng:");
            log.info("   - root/root123 (Administrator - Tài khoản chính)");
            log.info("   - admin/admin123 (Quản trị viên)");
            log.info("   - manager/manager123 (Quản lý)");
            log.info("   - user/user123 (Nhân viên)");
        }
    }

    private void initializeNhanVien() {
        if (nhanVienRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu Nhân viên...");
            
            NhanVien nv1 = new NhanVien("Nguyễn Văn Minh", "0901234567", "minh.nv@company.com", "Giám đốc", "Ban Giám đốc");
            nv1.setLuong(50000000.0);
            nv1.setNgayVaoLam(LocalDate.of(2020, 1, 15));
            nv1.setDiaChi("123 Lê Lợi, Q1, TP.HCM");
            
            NhanVien nv2 = new NhanVien("Trần Thị Hoa", "0901234568", "hoa.tt@company.com", "Trưởng phòng", "Phòng Kế toán");
            nv2.setLuong(25000000.0);
            nv2.setNgayVaoLam(LocalDate.of(2021, 3, 10));
            nv2.setDiaChi("456 Nguyễn Huệ, Q1, TP.HCM");

            NhanVien nv3 = new NhanVien("Lê Văn Đức", "0901234569", "duc.lv@company.com", "Nhân viên", "Phòng IT");
            nv3.setLuong(15000000.0);
            nv3.setNgayVaoLam(LocalDate.of(2022, 6, 1));
            nv3.setDiaChi("789 Pasteur, Q3, TP.HCM");

            nhanVienRepository.save(nv1);
            nhanVienRepository.save(nv2);
            nhanVienRepository.save(nv3);
            
            log.info("✅ Đã tạo " + nhanVienRepository.count() + " nhân viên mẫu.");
        }
    }

    private void initializeHangHoa() {
        if (hangHoaRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu Hàng hóa...");
            
            HangHoa hh1 = new HangHoa("Laptop Dell XPS 13", "LAP001", "SN2024001", "Điện tử", new BigDecimal("25000000"));
            hh1.setGiaVon(new BigDecimal("20000000"));
            hh1.setSoLuongTon(10);
            hh1.setDonViTinh("Chiếc");
            hh1.setThuongHieu("Dell");
            hh1.setXuatXu("Mỹ");
            hh1.setMoTa("Laptop siêu mỏng, hiệu năng cao");
            hh1.setNgayNhap(LocalDate.of(2024, 1, 5));

            HangHoa hh2 = new HangHoa("Điện thoại iPhone 15", "PHO001", "SN2024002", "Điện tử", new BigDecimal("30000000"));
            hh2.setGiaVon(new BigDecimal("25000000"));
            hh2.setSoLuongTon(20);
            hh2.setDonViTinh("Chiếc");
            hh2.setThuongHieu("Apple");
            hh2.setXuatXu("Mỹ");
            hh2.setMoTa("Smartphone cao cấp");
            hh2.setNgayNhap(LocalDate.of(2024, 2, 12));

            HangHoa hh3 = new HangHoa("Bàn làm việc gỗ", "BAN001", "SN2024003", "Nội thất", new BigDecimal("5000000"));
            hh3.setGiaVon(new BigDecimal("3500000"));
            hh3.setSoLuongTon(5);
            hh3.setDonViTinh("Chiếc");
            hh3.setThuongHieu("Hòa Phát");
            hh3.setXuatXu("Việt Nam");
            hh3.setMoTa("Bàn làm việc chất liệu gỗ tự nhiên");
            hh3.setNgayNhap(LocalDate.of(2024, 3, 8));

            // Thêm sản phẩm với đơn vị tính mới
            HangHoa hh4 = new HangHoa("Microsoft Office 365", "SW001", "SN2024004", "Phần mềm", new BigDecimal("2000000"));
            hh4.setGiaVon(new BigDecimal("1500000"));
            hh4.setSoLuongTon(100);
            hh4.setDonViTinh("Phần mềm");
            hh4.setThuongHieu("Microsoft");
            hh4.setXuatXu("Mỹ");
            hh4.setMoTa("Bộ office chuyên nghiệp");
            hh4.setNgayNhap(LocalDate.of(2024, 1, 10));

            HangHoa hh5 = new HangHoa("AutoCAD License", "LIC001", "SN2024005", "Phần mềm", new BigDecimal("15000000"));
            hh5.setGiaVon(new BigDecimal("12000000"));
            hh5.setSoLuongTon(50);
            hh5.setDonViTinh("Licence");
            hh5.setThuongHieu("Autodesk");
            hh5.setXuatXu("Mỹ");
            hh5.setMoTa("Phần mềm thiết kế CAD chuyên nghiệp");
            hh5.setNgayNhap(LocalDate.of(2024, 2, 5));

            HangHoa hh6 = new HangHoa("Cây Mai Vàng", "CAY001", "SN2024006", "Cây cảnh", new BigDecimal("3000000"));
            hh6.setGiaVon(new BigDecimal("2000000"));
            hh6.setSoLuongTon(15);
            hh6.setDonViTinh("Cây");
            hh6.setThuongHieu("Vườn Mai Đồng Tháp");
            hh6.setXuatXu("Việt Nam");
            hh6.setMoTa("Cây mai vàng cao 1.5m, dáng đẹp");
            hh6.setNgayNhap(LocalDate.of(2024, 1, 20));

            HangHoa hh7 = new HangHoa("Vải Cotton Cao Cấp", "VAI001", "SN2024007", "Vải may", new BigDecimal("500000"));
            hh7.setGiaVon(new BigDecimal("350000"));
            hh7.setSoLuongTon(200);
            hh7.setDonViTinh("Cuộn");
            hh7.setThuongHieu("Viet Tien");
            hh7.setXuatXu("Việt Nam");
            hh7.setMoTa("Vải cotton cao cấp, cuộn 50m");
            hh7.setNgayNhap(LocalDate.of(2024, 3, 1));

            HangHoa hh8 = new HangHoa("Sợi Tơ Tằm", "SOI001", "SN2024008", "Nguyên liệu dệt", new BigDecimal("800000"));
            hh8.setGiaVon(new BigDecimal("600000"));
            hh8.setSoLuongTon(500);
            hh8.setDonViTinh("Sợi");
            hh8.setThuongHieu("Silk Vietnam");
            hh8.setXuatXu("Việt Nam");
            hh8.setMoTa("Sợi tơ tằm tự nhiên cao cấp");
            hh8.setNgayNhap(LocalDate.of(2024, 2, 15));

            hangHoaRepository.save(hh1);
            hangHoaRepository.save(hh2);
            hangHoaRepository.save(hh3);
            hangHoaRepository.save(hh4);
            hangHoaRepository.save(hh5);
            hangHoaRepository.save(hh6);
            hangHoaRepository.save(hh7);
            hangHoaRepository.save(hh8);
            
            log.info("✅ Đã tạo " + hangHoaRepository.count() + " hàng hóa mẫu.");
        }
    }

    private void initializeKhachHang() {
        if (khachHangRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu Khách hàng...");
            
            KhachHang kh1 = new KhachHang("Công ty TNHH ABC", "0987654321", "abc@company.com", "123 Lý Tự Trọng, Q1, TP.HCM");
            kh1.setLoaiKhachHang("Doanh nghiệp");
            kh1.setNgayDangKy(LocalDate.of(2023, 1, 15));
            kh1.setMaSoThue("0123456789");
            kh1.setGhiChu("Khách hàng VIP");

            KhachHang kh2 = new KhachHang("Nguyễn Thị Lan", "0912345678", "lan.nt@gmail.com", "456 Hai Bà Trưng, Q3, TP.HCM");
            kh2.setLoaiKhachHang("Cá nhân");
            kh2.setNgayDangKy(LocalDate.of(2023, 5, 20));
            kh2.setGhiChu("Khách hàng thường xuyên");

            KhachHang kh3 = new KhachHang("Phạm Văn Tùng", "0901111222", "tung.pv@yahoo.com", "789 Võ Văn Tần, Q3, TP.HCM");
            kh3.setLoaiKhachHang("VIP");
            kh3.setNgayDangKy(LocalDate.of(2022, 12, 1));

            khachHangRepository.save(kh1);
            khachHangRepository.save(kh2);
            khachHangRepository.save(kh3);
            
            log.info("✅ Đã tạo " + khachHangRepository.count() + " khách hàng mẫu.");
        }
    }

    private void initializeNhaPhanPhoi() {
        if (nhaPhanPhoiRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu Nhà phân phối...");
            
            NhaPhanPhoi npp1 = new NhaPhanPhoi("Công ty Điện máy Xanh", "NPP001", "02812345678", "contact@dienmayxanh.com");
            npp1.setDiaChi("100 Nguyễn Văn Cừ, Q5, TP.HCM");
            npp1.setNguoiLienHe("Nguyễn Văn A");
            npp1.setMaSoThue("0987654321");
            npp1.setTrangThai("Hoạt động");
            npp1.setLinhVuc("Điện máy");
            npp1.setGhiChu("Nhà phân phối chính");

            NhaPhanPhoi npp2 = new NhaPhanPhoi("Công ty Nội thất Hòa Phát", "NPP002", "02423456789", "sales@hoaphat.com");
            npp2.setDiaChi("200 Lê Duẩn, Hai Bà Trưng, Hà Nội");
            npp2.setNguoiLienHe("Trần Thị B");
            npp2.setMaSoThue("0123456788");
            npp2.setTrangThai("Hoạt động");
            npp2.setLinhVuc("Nội thất");
            npp2.setGhiChu("Đối tác lâu năm");

            nhaPhanPhoiRepository.save(npp1);
            nhaPhanPhoiRepository.save(npp2);
            
            log.info("✅ Đã tạo " + nhaPhanPhoiRepository.count() + " nhà phân phối mẫu.");
        }
    }

    private void initializeStoreInfo() {
        if (storeInfoRepository.count() == 0) {
            StoreInfo store = new StoreInfo();
            store.setStoreName("Cửa hàng DEMO");
            store.setAddress("123 Đường ABC, Phường XYZ, Quận 1, TP.HCM");
            store.setPhone("0123-456-789");
            store.setEmail("info@cuahang-demo.com");
            store.setTaxCode("0123456789");
            store.setBusinessLicense("0123456789-001");
            store.setLogoPath("");
            
            storeInfoRepository.save(store);
            log.info("✅ Đã tạo thông tin cửa hàng mẫu.");
        }
        
        log.info("🎉 Hoàn tất khởi tạo dữ liệu mẫu cho tất cả module!");
    }
    
    private void initializePhieuXuat() {
        if (phieuXuatRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu Phiếu xuất...");
            
            // Lấy danh sách khách hàng đã có
            java.util.List<KhachHang> khachHangs = khachHangRepository.findAll();
            if (khachHangs.isEmpty()) {
                log.info("⚠️ Không có khách hàng nào để tạo phiếu xuất.");
                return;
            }
            
            // Tạo phiếu xuất mẫu
            PhieuXuat px1 = new PhieuXuat();
            px1.setMaPhieuXuat("PX001");
            px1.setNgayXuat(LocalDateTime.now().minusDays(5));
            px1.setKhachHang(khachHangs.get(0)); // Công ty TNHH ABC
            px1.setTongTien(new BigDecimal("55000000"));
            px1.setGhiChu("Đơn hàng lớn");
            px1.setNguoiXuat("Admin");
            phieuXuatRepository.save(px1);
            
            PhieuXuat px2 = new PhieuXuat();
            px2.setMaPhieuXuat("PX002");
            px2.setNgayXuat(LocalDateTime.now().minusDays(3));
            if (khachHangs.size() > 1) {
                px2.setKhachHang(khachHangs.get(1)); // Nguyễn Thị Lan
            } else {
                px2.setKhachHang(khachHangs.get(0));
            }
            px2.setTongTien(new BigDecimal("30000000"));
            px2.setGhiChu("Khách hàng VIP");
            px2.setNguoiXuat("Manager");
            phieuXuatRepository.save(px2);
            
            // Tạo chi tiết phiếu xuất
            if (hangHoaRepository.count() > 0) {
                java.util.List<HangHoa> hangHoas = hangHoaRepository.findAll();
                
                // Chi tiết cho PX001
                if (hangHoas.size() > 0) {
                    ChiTietPhieuXuat ct1 = new ChiTietPhieuXuat();
                    ct1.setPhieuXuat(px1);
                    ct1.setHangHoa(hangHoas.get(0)); // Laptop Dell XPS 13
                    ct1.setSoLuong(2);
                    ct1.setDonGia(new BigDecimal("25000000"));
                    ct1.setThanhTien(new BigDecimal("50000000"));
                    chiTietPhieuXuatRepository.save(ct1);
                    
                    ChiTietPhieuXuat ct2 = new ChiTietPhieuXuat();
                    ct2.setPhieuXuat(px1);
                    ct2.setHangHoa(hangHoas.get(2)); // Bàn làm việc
                    ct2.setSoLuong(1);
                    ct2.setDonGia(new BigDecimal("5000000"));
                    ct2.setThanhTien(new BigDecimal("5000000"));
                    chiTietPhieuXuatRepository.save(ct2);
                }
                
                // Chi tiết cho PX002
                if (hangHoas.size() > 1) {
                    ChiTietPhieuXuat ct3 = new ChiTietPhieuXuat();
                    ct3.setPhieuXuat(px2);
                    ct3.setHangHoa(hangHoas.get(1)); // iPhone 15
                    ct3.setSoLuong(1);
                    ct3.setDonGia(new BigDecimal("30000000"));
                    ct3.setThanhTien(new BigDecimal("30000000"));
                    chiTietPhieuXuatRepository.save(ct3);
                }
            }
            
            log.info("✅ Đã tạo " + phieuXuatRepository.count() + " phiếu xuất mẫu với " + 
                             chiTietPhieuXuatRepository.count() + " chi tiết.");
        }
    }
    
    private void initializeTraHang() {
        if (traHangRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu Trả hàng...");
            
            if (hangHoaRepository.count() > 0) {
                java.util.List<HangHoa> hangHoas = hangHoaRepository.findAll();
                
                // Trả hàng đã duyệt
                TraHang th1 = new TraHang();
                th1.setMaTraHang("TH001");
                th1.setHangHoa(hangHoas.get(0));
                th1.setSoLuong(1);
                th1.setDonGia(new BigDecimal("25000000"));
                th1.setThanhTien(new BigDecimal("25000000"));
                th1.setTenKhachHang("Công ty TNHH ABC");
                th1.setSoDienThoai("0987654321");
                th1.setLyDo("Sản phẩm bị lỗi màn hình");
                th1.setTrangThai(TraHang.TrangThaiTraHang.DA_DUYET);
                th1.setNgayTraHang(LocalDateTime.now().minusDays(2));
                th1.setNgayXuLy(LocalDateTime.now().minusDays(1));
                th1.setNguoiXuLy("admin");
                traHangRepository.save(th1);
                
                // Trả hàng chờ duyệt
                TraHang th2 = new TraHang();
                th2.setMaTraHang("TH002");
                th2.setHangHoa(hangHoas.get(1));
                th2.setSoLuong(1);
                th2.setDonGia(new BigDecimal("30000000"));
                th2.setThanhTien(new BigDecimal("30000000"));
                th2.setTenKhachHang("Nguyễn Thị Lan");
                th2.setSoDienThoai("0912345678");
                th2.setLyDo("Không ưng ý sản phẩm");
                th2.setTrangThai(TraHang.TrangThaiTraHang.CHO_DUYET);
                th2.setNgayTraHang(LocalDateTime.now().minusHours(6));
                traHangRepository.save(th2);
                
                // Trả hàng bị từ chối
                TraHang th3 = new TraHang();
                th3.setMaTraHang("TH003");
                th3.setHangHoa(hangHoas.get(2));
                th3.setSoLuong(1);
                th3.setDonGia(new BigDecimal("5000000"));
                th3.setThanhTien(new BigDecimal("5000000"));
                th3.setTenKhachHang("Phạm Văn Tùng");
                th3.setSoDienThoai("0901111222");
                th3.setLyDo("Sản phẩm hỏng do vận chuyển");
                th3.setTrangThai(TraHang.TrangThaiTraHang.TU_CHOI);
                th3.setNgayTraHang(LocalDateTime.now().minusDays(4));
                th3.setNgayXuLy(LocalDateTime.now().minusDays(3));
                th3.setNguoiXuLy("manager");
                traHangRepository.save(th3);
            }
            
            log.info("✅ Đã tạo " + traHangRepository.count() + " trả hàng mẫu.");
        }
    }
    
    private void initializeDoiTraHangHoa() {
        if (doiTraHangHoaRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu Đổi trả hàng hóa...");
            
            if (hangHoaRepository.count() > 1) {
                java.util.List<HangHoa> hangHoas = hangHoaRepository.findAll();
                
                // Đổi hàng đã duyệt
                DoiTraHangHoa dt1 = new DoiTraHangHoa();
                dt1.setMaDoiTra("DT001");
                dt1.setHangHoa(hangHoas.get(0));
                dt1.setHangHoaDoiMoi(hangHoas.get(1));
                dt1.setLoaiDoiTra(DoiTraHangHoa.LoaiDoiTra.DOI_HANG);
                dt1.setSoLuong(1);
                dt1.setDonGia(new BigDecimal("25000000"));
                dt1.setThanhTien(new BigDecimal("25000000"));
                dt1.setTenKhachHang("Công ty TNHH XYZ");
                dt1.setSoDienThoai("0988777666");
                dt1.setLyDo("Đổi sang model mới hơn");
                dt1.setTrangThai(DoiTraHangHoa.TrangThaiDoiTra.DA_DUYET);
                dt1.setNgayDoiTra(LocalDateTime.now().minusDays(7));
                dt1.setNgayXuLy(LocalDateTime.now().minusDays(6));
                dt1.setNguoiXuLy("admin");
                doiTraHangHoaRepository.save(dt1);
                
                // Trả hàng chờ duyệt
                DoiTraHangHoa dt2 = new DoiTraHangHoa();
                dt2.setMaDoiTra("DT002");
                dt2.setHangHoa(hangHoas.get(2));
                dt2.setLoaiDoiTra(DoiTraHangHoa.LoaiDoiTra.TRA_HANG);
                dt2.setSoLuong(2);
                dt2.setDonGia(new BigDecimal("5000000"));
                dt2.setThanhTien(new BigDecimal("10000000"));
                dt2.setTenKhachHang("Lê Thị Mai");
                dt2.setSoDienThoai("0977666555");
                dt2.setLyDo("Không phù hợp với không gian");
                dt2.setTrangThai(DoiTraHangHoa.TrangThaiDoiTra.CHO_DUYET);
                dt2.setNgayDoiTra(LocalDateTime.now().minusHours(12));
                doiTraHangHoaRepository.save(dt2);
            }
            
            log.info("✅ Đã tạo " + doiTraHangHoaRepository.count() + " đổi trả hàng hóa mẫu.");
        }
        
        log.info("🎉 Hoàn tất khởi tạo dữ liệu mẫu cho TẤT CẢ các module!");
    }
}