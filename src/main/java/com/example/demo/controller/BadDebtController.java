package com.example.demo.controller;

import com.example.demo.entity.debt.CustomerDebt;
import com.example.demo.entity.KhachHang;
import com.example.demo.repository.CustomerDebtRepository;
import com.example.demo.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller quản lý nợ không thể thu hồi
 * Hiển thị danh sách các khoản nợ xấu từ khách hàng
 */
@Controller
@RequestMapping("/cong-no/no-khong-the-thu-hoi")
public class BadDebtController {
    
    @Autowired
    private CustomerDebtRepository customerDebtRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
    /**
     * Trang danh sách nợ không thể thu hồi
     */
    @GetMapping("/list")
    public String badDebtList(Model model) {
        // Get all customer debts
        List<CustomerDebt> allDebts = customerDebtRepository.findAllWithKhachHang();
        
        // Filter only uncollectible debts (uncollectibleAmount > 0)
        List<CustomerDebt> uncollectibleDebts = allDebts.stream()
            .filter(debt -> debt.getUncollectibleAmount() != null && 
                           debt.getUncollectibleAmount().compareTo(BigDecimal.ZERO) > 0)
            .sorted((d1, d2) -> d2.getUncollectibleAmount().compareTo(d1.getUncollectibleAmount()))
            .collect(Collectors.toList());
        
        // Calculate statistics
        BigDecimal totalUncollectible = BigDecimal.ZERO;
        int debtCount = 0;
        
        for (CustomerDebt debt : uncollectibleDebts) {
            if (debt.getUncollectibleAmount() != null) {
                totalUncollectible = totalUncollectible.add(debt.getUncollectibleAmount());
                debtCount++;
            }
        }
        
        BigDecimal averageUncollectible = debtCount > 0 ? 
            totalUncollectible.divide(BigDecimal.valueOf(debtCount), 2, java.math.RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        // Add to model
        model.addAttribute("uncollectibleDebts", uncollectibleDebts);
        model.addAttribute("totalUncollectible", totalUncollectible);
        model.addAttribute("debtCount", debtCount);
        model.addAttribute("averageUncollectible", averageUncollectible);
        
        return "cong-no/bad-debt-list";
    }
    
    /**
     * Chi tiết nợ không thể thu hồi
     */
    @GetMapping("/{id}/detail")
    public String badDebtDetail(@PathVariable Long id, Model model) {
        // Use findAllWithKhachHang to ensure khachHang is loaded
        List<CustomerDebt> allDebts = customerDebtRepository.findAllWithKhachHang();
        CustomerDebt debt = allDebts.stream()
            .filter(d -> d.getId().equals(id))
            .findFirst()
            .orElse(null);
        
        if (debt == null) {
            return "redirect:/cong-no/no-khong-the-thu-hoi/list";
        }
        
        model.addAttribute("debt", debt);
        return "cong-no/bad-debt-detail";
    }
    
    /**
     * Biểu mẫu thêm/sửa nợ không thể thu hồi
     */
    @GetMapping("/{id}/form")
    public String badDebtForm(@PathVariable Long id, Model model) {
        // Use findAllWithKhachHang to ensure khachHang is loaded
        List<CustomerDebt> allDebts = customerDebtRepository.findAllWithKhachHang();
        CustomerDebt debt = allDebts.stream()
            .filter(d -> d.getId().equals(id))
            .findFirst()
            .orElse(null);
        
        if (debt == null) {
            return "redirect:/cong-no/no-khong-the-thu-hoi/list";
        }
        
        model.addAttribute("debt", debt);
        return "cong-no/bad-debt-form";
    }
    
    /**
     * Lưu nợ không thể thu hồi
     */
    @PostMapping("/{id}/save")
    public String saveUncollectible(
            @PathVariable Long id,
            @RequestParam BigDecimal uncollectibleAmount,
            @RequestParam String uncollectibleReason,
            Model model) {
        
        // Use findAllWithKhachHang to ensure khachHang is loaded
        List<CustomerDebt> allDebts = customerDebtRepository.findAllWithKhachHang();
        CustomerDebt debt = allDebts.stream()
            .filter(d -> d.getId().equals(id))
            .findFirst()
            .orElse(null);
        
        if (debt == null) {
            return "redirect:/cong-no/no-khong-the-thu-hoi/list";
        }
        
        // Validate uncollectible amount
        if (uncollectibleAmount == null || uncollectibleAmount.compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("error", "Số tiền không thể thu hồi phải lớn hơn 0");
            model.addAttribute("debt", debt);
            return "cong-no/bad-debt-form";
        }
        
        if (uncollectibleAmount.compareTo(debt.getTongTienConNo()) > 0) {
            model.addAttribute("error", "Số tiền không thể thu hồi không được vượt quá tiền còn nợ");
            model.addAttribute("debt", debt);
            return "cong-no/bad-debt-form";
        }
        
        // Update debt
        debt.setUncollectibleAmount(uncollectibleAmount);
        debt.setUncollectibleReason(uncollectibleReason);
        
        customerDebtRepository.save(debt);
        
        // Update khachHang to mark as bad debt
        KhachHang customer = debt.getKhachHang();
        if (customer != null) {
            customer.setIsBadDebt(true);
            khachHangRepository.save(customer);
        }
        
        return "redirect:/cong-no/no-khong-the-thu-hoi/list";
    }
    
    /**
     * Xóa nợ không thể thu hồi
     */
    @PostMapping("/{id}/delete")
    public String deleteUncollectible(@PathVariable Long id) {
        // Use findAllWithKhachHang to ensure khachHang is loaded
        List<CustomerDebt> allDebts = customerDebtRepository.findAllWithKhachHang();
        CustomerDebt debt = allDebts.stream()
            .filter(d -> d.getId().equals(id))
            .findFirst()
            .orElse(null);
        
        if (debt != null) {
            debt.setUncollectibleAmount(BigDecimal.ZERO);
            debt.setUncollectibleReason(null);
            customerDebtRepository.save(debt);
            
            // Check if customer still has other bad debts
            KhachHang customer = debt.getKhachHang();
            if (customer != null) {
                // Check if this customer has any other bad debts
                boolean hasBadDebt = allDebts.stream()
                    .filter(d -> d.getKhachHang() != null && d.getKhachHang().getId().equals(customer.getId()))
                    .filter(d -> !d.getId().equals(id)) // Exclude current debt
                    .anyMatch(d -> d.getUncollectibleAmount() != null && 
                              d.getUncollectibleAmount().compareTo(BigDecimal.ZERO) > 0);
                
                // Only mark as not bad debt if no other bad debts exist
                if (!hasBadDebt) {
                    customer.setIsBadDebt(false);
                    khachHangRepository.save(customer);
                }
            }
        }
        
        return "redirect:/cong-no/no-khong-the-thu-hoi/list";
    }
}
