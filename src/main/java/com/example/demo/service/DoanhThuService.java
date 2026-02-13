package com.example.demo.service;

import com.example.demo.entity.TraHang;
import com.example.demo.repository.TraHangRepository;
import com.example.demo.repository.PhieuXuatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DoanhThuService {
    
    @Autowired
    private PhieuXuatRepository phieuXuatRepository;
    
    @Autowired
    private TraHangRepository traHangRepository;
    
    /**
     * Tính doanh thu theo tháng (có tính trừ trả hàng)
     */
    public Map<Integer, BigDecimal> getDoanhThuThangCoTraHang() {
        Map<Integer, BigDecimal> result = new HashMap<>();
        
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(23, 59, 59);
        
        // 1. Tính doanh thu từ bán hàng (PhieuXuat)
        phieuXuatRepository.findAll().stream()
            .filter(px -> px.getNgayXuat() != null && 
                         !px.getNgayXuat().isBefore(startDateTime) && 
                         !px.getNgayXuat().isAfter(endDateTime))
            .forEach(px -> {
                int ngay = px.getNgayXuat().getDayOfMonth();
                BigDecimal tongTien = px.getTongTien() != null ? px.getTongTien() : BigDecimal.ZERO;
                result.merge(ngay, tongTien, BigDecimal::add);
            });
        
        // 2. Xử lý trả hàng - tính doanh thu dựa trên lịch sử thực tế
        traHangRepository.findAll().stream()
            .forEach(th -> {
                // Nếu đang ở trạng thái DA_DUYET -> trừ tiền
                if (th.getTrangThai() == TraHang.TrangThaiTraHang.DA_DUYET && 
                    th.getNgayXuLy() != null &&
                    !th.getNgayXuLy().isBefore(startDateTime) && 
                    !th.getNgayXuLy().isAfter(endDateTime)) {
                    
                    int ngay = th.getNgayXuLy().getDayOfMonth();
                    BigDecimal soTienTra = th.getThanhTien() != null ? th.getThanhTien() : BigDecimal.ZERO;
                    result.merge(ngay, soTienTra.negate(), BigDecimal::add);
                }
                
                // Nếu đã từng duyệt nhưng hiện tại bị từ chối -> cộng lại tiền
                else if (th.getTrangThai() == TraHang.TrangThaiTraHang.TU_CHOI && 
                         th.getDaDuyetTruocDo() != null && th.getDaDuyetTruocDo() &&
                         th.getNgayXuLy() != null &&
                         !th.getNgayXuLy().isBefore(startDateTime) && 
                         !th.getNgayXuLy().isAfter(endDateTime)) {
                    
                    // Ngày từ chối (cộng lại tiền)
                    int ngayTuChoi = th.getNgayXuLy().getDayOfMonth();
                    BigDecimal soTienHoanLai = th.getThanhTien() != null ? th.getThanhTien() : BigDecimal.ZERO;
                    result.merge(ngayTuChoi, soTienHoanLai, BigDecimal::add);
                    
                    // Ngày duyệt (trừ tiền) - nếu trong cùng tháng
                    if (th.getNgayDuyet() != null &&
                        !th.getNgayDuyet().isBefore(startDateTime) && 
                        !th.getNgayDuyet().isAfter(endDateTime)) {
                        
                        int ngayDuyet = th.getNgayDuyet().getDayOfMonth();
                        result.merge(ngayDuyet, soTienHoanLai.negate(), BigDecimal::add);
                    }
                }
            });
        
        return result;
    }
    
    /**
     * Tính tổng doanh thu trong tháng (có tính trừ trả hàng)
     */
    public BigDecimal getTongDoanhThuThangCoTraHang() {
        Map<Integer, BigDecimal> doanhThuTheoNgay = getDoanhThuThangCoTraHang();
        return doanhThuTheoNgay.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Tính tổng số tiền đã trả khách hàng trong tháng
     */
    public BigDecimal getTongTienTraHangTrongThang() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(23, 59, 59);
        
        return traHangRepository.findAll().stream()
                .filter(th -> th.getTrangThai() == TraHang.TrangThaiTraHang.DA_DUYET)
                .filter(th -> th.getNgayXuLy() != null && 
                             !th.getNgayXuLy().isBefore(startDateTime) && 
                             !th.getNgayXuLy().isAfter(endDateTime))
                .map(th -> th.getThanhTien() != null ? th.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Tính doanh thu gộp (chưa trừ trả hàng) trong tháng
     */
    public BigDecimal getDoanhThuGopTrongThang() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(23, 59, 59);
        
        return phieuXuatRepository.findAll().stream()
                .filter(px -> px.getNgayXuat() != null && 
                             !px.getNgayXuat().isBefore(startDateTime) && 
                             !px.getNgayXuat().isAfter(endDateTime))
                .map(px -> px.getTongTien() != null ? px.getTongTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Tính tổng doanh thu trong ngày hôm nay (có tính trừ trả hàng)
     */
    public BigDecimal getTongDoanhThuHomNayCoTraHang() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay(); // 0:00:00
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1); // 23:59:59.999999999
        
        // 1. Tính doanh thu từ bán hàng (PhieuXuat)
        BigDecimal doanhThuBanHang = phieuXuatRepository.findAll().stream()
                .filter(px -> px.getNgayXuat() != null && 
                             !px.getNgayXuat().isBefore(startOfDay) && 
                             !px.getNgayXuat().isAfter(endOfDay))
                .map(px -> px.getTongTien() != null ? px.getTongTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 2. Tính tiền trả hàng trong ngày
        BigDecimal tienTraHang = traHangRepository.findAll().stream()
                .filter(th -> th.getTrangThai() == TraHang.TrangThaiTraHang.DA_DUYET)
                .filter(th -> th.getNgayXuLy() != null && 
                             !th.getNgayXuLy().isBefore(startOfDay) && 
                             !th.getNgayXuLy().isAfter(endOfDay))
                .map(th -> th.getThanhTien() != null ? th.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 3. Tính tiền hoàn lại từ việc từ chối trả hàng sau khi đã duyệt
        BigDecimal tienHoanLai = traHangRepository.findAll().stream()
                .filter(th -> th.getTrangThai() == TraHang.TrangThaiTraHang.TU_CHOI && 
                             th.getDaDuyetTruocDo() != null && th.getDaDuyetTruocDo())
                .filter(th -> th.getNgayXuLy() != null && 
                             !th.getNgayXuLy().isBefore(startOfDay) && 
                             !th.getNgayXuLy().isAfter(endOfDay))
                .map(th -> th.getThanhTien() != null ? th.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return doanhThuBanHang.subtract(tienTraHang).add(tienHoanLai);
    }
    
    /**
     * Tính tổng số tiền đã trả khách hàng trong ngày hôm nay
     */
    public BigDecimal getTongTienTraHangHomNay() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);
        
        return traHangRepository.findAll().stream()
                .filter(th -> th.getTrangThai() == TraHang.TrangThaiTraHang.DA_DUYET)
                .filter(th -> th.getNgayXuLy() != null && 
                             !th.getNgayXuLy().isBefore(startOfDay) && 
                             !th.getNgayXuLy().isAfter(endOfDay))
                .map(th -> th.getThanhTien() != null ? th.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Tính doanh thu gộp (chưa trừ trả hàng) trong ngày hôm nay
     */
    public BigDecimal getDoanhThuGopHomNay() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);
        
        return phieuXuatRepository.findAll().stream()
                .filter(px -> px.getNgayXuat() != null && 
                             !px.getNgayXuat().isBefore(startOfDay) && 
                             !px.getNgayXuat().isAfter(endOfDay))
                .map(px -> px.getTongTien() != null ? px.getTongTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}