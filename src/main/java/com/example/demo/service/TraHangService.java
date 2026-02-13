package com.example.demo.service;

import com.example.demo.entity.TraHang;
import com.example.demo.entity.HangHoa;
import com.example.demo.repository.TraHangRepository;
import com.example.demo.repository.HangHoaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TraHangService {
    
    private static final Logger log = LoggerFactory.getLogger(TraHangService.class);
    
    @Autowired
    private TraHangRepository traHangRepository;
    
    @Autowired
    private HangHoaRepository hangHoaRepository;
    
    /**
     * Xử lý duyệt trả hàng - cập nhật tồn kho và doanh thu
     */
    @Transactional
    @CacheEvict(value = {"allHangHoaOrdered", "hangHoaBySoLuongTon"}, allEntries = true)
    public void duyetTraHang(Long traHangId, String nguoiDuyet) {
        // Validate traHangId parameter
        if (traHangId == null || traHangId <= 0) {
            throw new IllegalArgumentException("ID đơn trả hàng không hợp lệ");
        }
        
        Optional<TraHang> traHangOpt = traHangRepository.findById(traHangId);
        if (traHangOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy đơn trả hàng với ID: " + traHangId);
        }
        
        TraHang traHang = traHangOpt.get();
        
        // Kiểm tra trạng thái hiện tại
        if (traHang.getTrangThai() != TraHang.TrangThaiTraHang.CHO_DUYET) {
            throw new IllegalStateException("Đơn trả hàng này đã được xử lý trước đó!");
        }
        
        // 1. Cập nhật trạng thái trả hàng
        traHang.setTrangThai(TraHang.TrangThaiTraHang.DA_DUYET);
        traHang.setNgayXuLy(LocalDateTime.now());
        traHang.setNgayDuyet(LocalDateTime.now());
        traHang.setDaDuyetTruocDo(true);
        traHang.setNguoiXuLy(nguoiDuyet);
        
        // 2. Cập nhật tồn kho - TĂNG số lượng tồn kho
        HangHoa hangHoa = traHang.getHangHoa();
        if (hangHoa != null) {
            Integer soLuongTonHienTai = hangHoa.getSoLuongTon() != null ? hangHoa.getSoLuongTon() : 0;
            Integer soLuongTraVe = traHang.getSoLuong();
            
            // Tăng số lượng tồn kho
            hangHoa.setSoLuongTon(soLuongTonHienTai + soLuongTraVe);
            hangHoaRepository.save(hangHoa);
            
            log.info("📦 Đã cập nhật tồn kho cho " + hangHoa.getTenHangHoa() + 
                             ": " + soLuongTonHienTai + " + " + soLuongTraVe + 
                             " = " + hangHoa.getSoLuongTon());
        }
        
        // 3. Lưu thông tin trả hàng
        traHangRepository.save(traHang);
        
        log.info("✅ Đã duyệt đơn trả hàng " + traHang.getMaTraHang() + 
                          " - Số tiền: " + traHang.getThanhTien() + 
                          " - Người duyệt: " + nguoiDuyet);
    }
    
    /**
     * Từ chối trả hàng (có thể từ chối cả đơn đã duyệt)
     */
    @Transactional
    @CacheEvict(value = {"allHangHoaOrdered", "hangHoaBySoLuongTon"}, allEntries = true)
    public void tuChoiTraHang(Long traHangId, String nguoiTuChoi, String lyDoTuChoi) {
        // Validate traHangId parameter
        if (traHangId == null || traHangId <= 0) {
            throw new IllegalArgumentException("ID đơn trả hàng không hợp lệ");
        }
        
        Optional<TraHang> traHangOpt = traHangRepository.findById(traHangId);
        if (traHangOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy đơn trả hàng với ID: " + traHangId);
        }
        
        TraHang traHang = traHangOpt.get();
        TraHang.TrangThaiTraHang trangThaiCu = traHang.getTrangThai();
        
        // Kiểm tra trạng thái hợp lệ để từ chối
        if (traHang.getTrangThai() == TraHang.TrangThaiTraHang.TU_CHOI) {
            throw new IllegalStateException("Đơn trả hàng này đã được từ chối trước đó!");
        }
        
        // Nếu đang ở trạng thái DA_DUYET, cần hoàn tác lại kho hàng và đánh dấu đã từng duyệt
        if (trangThaiCu == TraHang.TrangThaiTraHang.DA_DUYET) {
            // Đánh dấu đã từng được duyệt trước đó
            traHang.setDaDuyetTruocDo(true);
            
            // Hoàn tác tồn kho - trừ lại số lượng đã thêm
            HangHoa hangHoa = traHang.getHangHoa();
            if (hangHoa != null) {
                Integer soLuongTonHienTai = hangHoa.getSoLuongTon() != null ? hangHoa.getSoLuongTon() : 0;
                Integer soLuongTraVe = traHang.getSoLuong();
                
                // Kiểm tra xem có đủ hàng để trừ không
                if (soLuongTonHienTai < soLuongTraVe) {
                    throw new IllegalStateException("Không đủ hàng tồn kho để từ chối! Hiện tại: " + 
                                                   soLuongTonHienTai + ", cần trừ: " + soLuongTraVe);
                }
                
                // Trừ số lượng tồn kho (hoàn tác việc nhập kho)
                hangHoa.setSoLuongTon(soLuongTonHienTai - soLuongTraVe);
                hangHoaRepository.save(hangHoa);
                
                log.info("📦 Đã hoàn tác tồn kho do từ chối: " + hangHoa.getTenHangHoa() + 
                                 ": " + soLuongTonHienTai + " - " + soLuongTraVe + 
                                 " = " + hangHoa.getSoLuongTon());
            }
            
            log.info("🔄 Hoàn tác doanh thu do từ chối đơn đã duyệt: " + traHang.getThanhTien() + " VNĐ");
        }
        
        // Cập nhật trạng thái
        traHang.setTrangThai(TraHang.TrangThaiTraHang.TU_CHOI);
        traHang.setNgayXuLy(LocalDateTime.now());
        traHang.setNguoiXuLy(nguoiTuChoi);
        
        // Cập nhật lý do (nếu có)
        String lyDoMoi = "[TỪ CHỐI";
        if (trangThaiCu == TraHang.TrangThaiTraHang.DA_DUYET) {
            lyDoMoi += " SAU KHI ĐÃ DUYỆT";
        }
        lyDoMoi += "] ";
        
        if (lyDoTuChoi != null && !lyDoTuChoi.trim().isEmpty()) {
            lyDoMoi += lyDoTuChoi;
        } else {
            lyDoMoi += "Không có lý do cụ thể";
        }
        
        String lyDoCu = traHang.getLyDo() != null ? traHang.getLyDo() : "";
        traHang.setLyDo(lyDoCu + "\n" + lyDoMoi);
        
        traHangRepository.save(traHang);
        
        log.info("❌ Đã từ chối đơn trả hàng " + traHang.getMaTraHang() + 
                          " (trạng thái cũ: " + trangThaiCu + ") - Người từ chối: " + nguoiTuChoi);
    }
    
    /**
     * Hoàn tác việc duyệt trả hàng (trường hợp đặc biệt)
     */
    @Transactional
    @CacheEvict(value = {"allHangHoaOrdered", "hangHoaBySoLuongTon"}, allEntries = true)
    public void hoanTacDuyetTraHang(Long traHangId, String nguoiHoanTac) {
        // Validate traHangId parameter
        if (traHangId == null || traHangId <= 0) {
            throw new IllegalArgumentException("ID đơn trả hàng không hợp lệ");
        }
        
        Optional<TraHang> traHangOpt = traHangRepository.findById(traHangId);
        if (traHangOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy đơn trả hàng với ID: " + traHangId);
        }
        
        TraHang traHang = traHangOpt.get();
        
        // Kiểm tra trạng thái hiện tại
        if (traHang.getTrangThai() != TraHang.TrangThaiTraHang.DA_DUYET) {
            throw new IllegalStateException("Chỉ có thể hoàn tác đơn trả hàng đã được duyệt!");
        }
        
        // 1. Trừ lại số lượng tồn kho
        HangHoa hangHoa = traHang.getHangHoa();
        if (hangHoa != null) {
            Integer soLuongTonHienTai = hangHoa.getSoLuongTon() != null ? hangHoa.getSoLuongTon() : 0;
            Integer soLuongTraVe = traHang.getSoLuong();
            
            // Kiểm tra xem có đủ hàng để trừ không
            if (soLuongTonHienTai < soLuongTraVe) {
                throw new IllegalStateException("Không đủ hàng tồn kho để hoàn tác! Hiện tại: " + 
                                               soLuongTonHienTai + ", cần trừ: " + soLuongTraVe);
            }
            
            // Trừ số lượng tồn kho
            hangHoa.setSoLuongTon(soLuongTonHienTai - soLuongTraVe);
            hangHoaRepository.save(hangHoa);
            
            log.info("📦 Đã hoàn tác tồn kho cho " + hangHoa.getTenHangHoa() + 
                             ": " + soLuongTonHienTai + " - " + soLuongTraVe + 
                             " = " + hangHoa.getSoLuongTon());
        }
        
        // 2. Đưa trạng thái về chờ duyệt
        traHang.setTrangThai(TraHang.TrangThaiTraHang.CHO_DUYET);
        traHang.setNgayXuLy(null);
        traHang.setNguoiXuLy(null);
        
        traHangRepository.save(traHang);
        
        log.info("🔄 Đã hoàn tác duyệt đơn trả hàng " + traHang.getMaTraHang() + 
                          " - Người hoàn tác: " + nguoiHoanTac);
    }
}