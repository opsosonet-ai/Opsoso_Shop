package com.example.demo.controller;

import com.example.demo.entity.debt.CustomerDebt;
import com.example.demo.entity.debt.SupplierDebt;
import com.example.demo.repository.SupplierDebtRepository;
import com.example.demo.repository.SupplierDebtPaymentRepository;
import com.example.demo.repository.CustomerDebtRepository;
import com.example.demo.repository.CustomerDebtPaymentRepository;
import com.example.demo.repository.NhaPhanPhoiRepository;
import com.example.demo.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UI Controller for Debt Management System
 * Serves Thymeleaf templates with data for display
 * 
 * URL Routes:
 * - GET /cong-no/dashboard - Dashboard overview
 * - GET /cong-no/nha-phan-phoi/list - Supplier debt list
 * - GET /cong-no/khach-hang/list - Customer debt list
 * - GET /cong-no/lich-su-thanh-toan - Payment history
 */
@Controller
@RequestMapping("/cong-no")
public class DebtUIController {

    @Autowired
    private SupplierDebtRepository supplierDebtRepository;

    @Autowired
    private SupplierDebtPaymentRepository supplierDebtPaymentRepository;

    @Autowired
    private CustomerDebtRepository customerDebtRepository;

    @Autowired
    private CustomerDebtPaymentRepository customerDebtPaymentRepository;

    @Autowired
    private NhaPhanPhoiRepository nhaPhanPhoiRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    /**
     * Dashboard - Overall debt overview
     * Displays key metrics and trends
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Populate model with data for dashboard
        
        // Get all customer debts (with eager loading of relationships)
        List<CustomerDebt> allCustomerDebts = customerDebtRepository.findAllWithKhachHang();
        
        // Calculate total customer debt (remaining amount - chưa thu hồi)
        BigDecimal totalCustomerDebt = allCustomerDebts.stream()
            .map(CustomerDebt::getTongTienConNo)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate total customer debt remaining (duplicate for clarity)
        BigDecimal totalCustomerDebtRemaining = allCustomerDebts.stream()
            .map(CustomerDebt::getTongTienConNo)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Count overdue customer debts
        java.time.LocalDate today = java.time.LocalDate.now();
        long overdueCustomerCount = allCustomerDebts.stream()
            .filter(d -> d.getNgayHanChot() != null && d.getNgayHanChot().isBefore(today))
            .filter(d -> d.getTongTienConNo() != null && d.getTongTienConNo().compareTo(BigDecimal.ZERO) > 0)
            .count();
        
        BigDecimal overdueCustomerDebt = allCustomerDebts.stream()
            .filter(d -> d.getNgayHanChot() != null && d.getNgayHanChot().isBefore(today))
            .filter(d -> d.getTongTienConNo() != null && d.getTongTienConNo().compareTo(BigDecimal.ZERO) > 0)
            .map(CustomerDebt::getTongTienConNo)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Get top 10 customers by debt
        List<CustomerDebt> topCustomers = allCustomerDebts.stream()
            .sorted(Comparator.comparing(cd -> cd.getTongTienConNo() != null ? cd.getTongTienConNo() : BigDecimal.ZERO, Comparator.reverseOrder()))
            .limit(10)
            .collect(Collectors.toList());
        
        // Get all supplier debts (with eager loading of relationships)
        List<com.example.demo.entity.debt.SupplierDebt> allSupplierDebts = supplierDebtRepository.findAllWithNhaPhanPhoi();
        
        // Calculate total supplier debt (unpaid amount - chưa thanh toán)
        BigDecimal totalSupplierDebt = allSupplierDebts.stream()
            .map(com.example.demo.entity.debt.SupplierDebt::getTongTienConNo)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Count overdue supplier debts
        long overdueSupplierCount = allSupplierDebts.stream()
            .filter(d -> d.getNgayHanChot() != null && d.getNgayHanChot().isBefore(today))
            .filter(d -> d.getTongTienConNo() != null && d.getTongTienConNo().compareTo(BigDecimal.ZERO) > 0)
            .count();
        
        BigDecimal overdueSupplierDebt = allSupplierDebts.stream()
            .filter(d -> d.getNgayHanChot() != null && d.getNgayHanChot().isBefore(today))
            .filter(d -> d.getTongTienConNo() != null && d.getTongTienConNo().compareTo(BigDecimal.ZERO) > 0)
            .map(com.example.demo.entity.debt.SupplierDebt::getTongTienConNo)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Get top 10 suppliers by debt
        List<com.example.demo.entity.debt.SupplierDebt> topSuppliers = allSupplierDebts.stream()
            .sorted(Comparator.comparing(sd -> sd.getTongTienConNo() != null ? sd.getTongTienConNo() : BigDecimal.ZERO, Comparator.reverseOrder()))
            .limit(10)
            .collect(Collectors.toList());
        
        // Get recent supplier payments (last 10) - with eager loading
        java.util.List<com.example.demo.dto.RecentTransactionDTO> recentSupplierPaymentDTOs = 
            supplierDebtPaymentRepository.findAllWithEagerLoading()
                .stream()
                .filter(p -> p.getNgayThanhToan() != null && p.getSupplierDebt() != null)
                .limit(10)
                .map(p -> {
                    String supplierName = "N/A";
                    String invoiceNumber = "N/A";
                    if (p.getSupplierDebt().getNhaPhanPhoi() != null) {
                        supplierName = p.getSupplierDebt().getNhaPhanPhoi().getTenNhaPhanPhoi();
                    }
                    invoiceNumber = p.getSupplierDebt().getSoPhieuXuatChi();
                    return new com.example.demo.dto.RecentTransactionDTO(
                        p.getId(),
                        p.getNgayThanhToan(),
                        "SUPPLIER",
                        invoiceNumber,
                        supplierName,
                        p.getSoTienThanhToan(),
                        p.getPhuongThucThanhToan() != null ? p.getPhuongThucThanhToan().toString() : "N/A"
                    );
                })
                .collect(Collectors.toList());
        
        // Get recent customer collections (last 10) - with eager loading
        java.util.List<com.example.demo.dto.RecentTransactionDTO> recentCustomerPaymentDTOs = 
            customerDebtPaymentRepository.findAllWithEagerLoading()
                .stream()
                .filter(p -> p.getNgayThuHoi() != null && p.getCustomerDebt() != null)
                .limit(10)
                .map(p -> {
                    String customerName = "N/A";
                    String invoiceNumber = "N/A";
                    if (p.getCustomerDebt().getKhachHang() != null) {
                        customerName = p.getCustomerDebt().getKhachHang().getHoTen();
                    }
                    invoiceNumber = p.getCustomerDebt().getSoPhieuXuatBan();
                    return new com.example.demo.dto.RecentTransactionDTO(
                        p.getId(),
                        p.getNgayThuHoi(),
                        "CUSTOMER",
                        invoiceNumber,
                        customerName,
                        p.getSoTienThuHoi(),
                        p.getPhuongThucThuHoi() != null ? p.getPhuongThucThuHoi().toString() : "N/A"
                    );
                })
                .collect(Collectors.toList());
        
        // Combine and sort by date
        java.util.List<com.example.demo.dto.RecentTransactionDTO> recentTransactions = new java.util.ArrayList<>();
        recentTransactions.addAll(recentSupplierPaymentDTOs);
        recentTransactions.addAll(recentCustomerPaymentDTOs);
        recentTransactions.sort(Comparator.comparing(com.example.demo.dto.RecentTransactionDTO::getNgayThanhToan).reversed());
        
        // Limit to 10 most recent
        if (recentTransactions.size() > 10) {
            recentTransactions = recentTransactions.subList(0, 10);
        }
        
        // Add attributes to model
        model.addAttribute("totalCustomerDebt", totalCustomerDebt);
        model.addAttribute("totalCustomerDebtRemaining", totalCustomerDebtRemaining);
        model.addAttribute("overdueCustomerCount", overdueCustomerCount);
        model.addAttribute("overdueCustomerDebt", overdueCustomerDebt);
        model.addAttribute("topCustomers", topCustomers);
        
        // Add supplier debt attributes
        model.addAttribute("totalSupplierDebt", totalSupplierDebt);
        model.addAttribute("overdueSupplierDebt", overdueSupplierDebt);
        model.addAttribute("overdueSupplierCount", overdueSupplierCount);
        model.addAttribute("topSuppliers", topSuppliers);
        
        // Add recent transactions
        model.addAttribute("recentTransactions", recentTransactions);
        
        // Calculate debt status distribution for chart (combining both supplier and customer debts)
        List<com.example.demo.entity.debt.SupplierDebt> allDebtsSupplier = supplierDebtRepository.findAllWithNhaPhanPhoi();
        List<CustomerDebt> allDebtsCustomer = customerDebtRepository.findAllWithKhachHang();
        
        // Combine all debts for status distribution
        java.util.List<Object> allDebts = new java.util.ArrayList<>();
        allDebts.addAll(allDebtsSupplier);
        allDebts.addAll(allDebtsCustomer);
        
        // Count by status
        long dangNoCount = allDebts.stream()
            .filter(d -> {
                if (d instanceof com.example.demo.entity.debt.SupplierDebt debt) {
                    return debt.getTrangThai() == 
                           com.example.demo.entity.enums.DebtStatus.DANG_NO;
                } else {
                    return ((CustomerDebt) d).getTrangThai() == 
                           com.example.demo.entity.enums.DebtStatus.DANG_NO;
                }
            }).count();
        
        long thanhToanTuanTuanCount = allDebts.stream()
            .filter(d -> {
                if (d instanceof com.example.demo.entity.debt.SupplierDebt debt) {
                    return debt.getTrangThai() == 
                           com.example.demo.entity.enums.DebtStatus.THANH_TOAN_TUAN_TUAN;
                } else {
                    return ((CustomerDebt) d).getTrangThai() == 
                           com.example.demo.entity.enums.DebtStatus.THANH_TOAN_TUAN_TUAN;
                }
            }).count();
        
        long daThanhToanCount = allDebts.stream()
            .filter(d -> {
                if (d instanceof com.example.demo.entity.debt.SupplierDebt debt) {
                    return debt.getTrangThai() == 
                           com.example.demo.entity.enums.DebtStatus.DA_THANH_TOAN_HET;
                } else {
                    return ((CustomerDebt) d).getTrangThai() == 
                           com.example.demo.entity.enums.DebtStatus.DA_THANH_TOAN_HET;
                }
            }).count();
        
        long quaHanCount = allDebts.stream()
            .filter(d -> {
                if (d instanceof com.example.demo.entity.debt.SupplierDebt debt) {
                    return debt.getTrangThai() == 
                           com.example.demo.entity.enums.DebtStatus.QUA_HAN;
                } else {
                    return ((CustomerDebt) d).getTrangThai() == 
                           com.example.demo.entity.enums.DebtStatus.QUA_HAN;
                }
            }).count();
        
        // Add change indicators (for trend visualization)
        model.addAttribute("supplierDebtChange", BigDecimal.ZERO);
        model.addAttribute("customerDebtChange", BigDecimal.ZERO);
        
        // Add debt status counts for chart
        model.addAttribute("dangNoCount", dangNoCount);
        model.addAttribute("thanhToanTuanTuanCount", thanhToanTuanTuanCount);
        model.addAttribute("daThanhToanCount", daThanhToanCount);
        model.addAttribute("quaHanCount", quaHanCount);
        
        return "cong-no/dashboard";
    }

    /**
     * Supplier Debt List
     * Displays all supplier debts with filtering and metrics
     * Sorted: Unpaid debts first (top), Completed payments last (bottom)
     */
    @GetMapping("/nha-phan-phoi/list")
    public String supplierDebtList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String supplierId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            Model model) {

        // Populate model with supplier debts (with eager loading of relationships)
        List<SupplierDebt> allDebts = supplierDebtRepository.findAllWithNhaPhanPhoi();
        
        // Sort debts: Unpaid (DA_THANH_TOAN_HET = false) first, then by remaining debt amount descending
        allDebts.sort((d1, d2) -> {
            // First priority: Unpaid debts (false) come before paid debts (true)
            Boolean isPaid1 = d1.getTrangThai() != null && 
                d1.getTrangThai().equals(com.example.demo.entity.enums.DebtStatus.DA_THANH_TOAN_HET);
            Boolean isPaid2 = d2.getTrangThai() != null && 
                d2.getTrangThai().equals(com.example.demo.entity.enums.DebtStatus.DA_THANH_TOAN_HET);
            
            if (!isPaid1.equals(isPaid2)) {
                return isPaid1.compareTo(isPaid2); // false (unpaid) comes before true (paid)
            }
            
            // Second priority: Within same status, sort by remaining debt amount (descending)
            BigDecimal remaining1 = d1.getTongTienConNo() != null ? d1.getTongTienConNo() : BigDecimal.ZERO;
            BigDecimal remaining2 = d2.getTongTienConNo() != null ? d2.getTongTienConNo() : BigDecimal.ZERO;
            return remaining2.compareTo(remaining1); // Descending order
        });
        
        // Calculate metrics for supplier debts
        BigDecimal totalDebt = allDebts.stream()
            .map(SupplierDebt::getTongTienNo)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCollected = allDebts.stream()
            .map(SupplierDebt::getTongTienDaThanhToan)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Count paid debts (fully paid)
        long paidDebtsCount = allDebts.stream()
            .filter(d -> d.getTrangThai() != null && 
                    d.getTrangThai().equals(com.example.demo.entity.enums.DebtStatus.DA_THANH_TOAN_HET))
            .count();
        
        // Count ongoing debts
        long ongoingDebtsCount = allDebts.size() - paidDebtsCount;
        
        // Count overdue debts
        long overdueDebtsCount = allDebts.stream()
            .filter(d -> d.getTrangThai() != null && 
                    d.getTrangThai().equals(com.example.demo.entity.enums.DebtStatus.QUA_HAN))
            .count();
        
        // Calculate average debt
        BigDecimal averageDebt = allDebts.isEmpty() ? BigDecimal.ZERO :
            totalDebt.divide(BigDecimal.valueOf(allDebts.size()), 2, RoundingMode.HALF_UP);
        
        // Calculate total remaining debt
        BigDecimal totalRemainingDebt = allDebts.stream()
            .map(SupplierDebt::getTongTienConNo)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Add attributes to model
        model.addAttribute("debts", allDebts);
        model.addAttribute("suppliers", nhaPhanPhoiRepository.findAll());
        model.addAttribute("totalDebt", totalDebt);
        model.addAttribute("totalCollected", totalCollected);
        model.addAttribute("totalRemainingDebt", totalRemainingDebt);
        model.addAttribute("paidDebtsCount", paidDebtsCount);
        model.addAttribute("ongoingDebtsCount", ongoingDebtsCount);
        model.addAttribute("overdueDebtsCount", overdueDebtsCount);
        model.addAttribute("averageDebt", averageDebt);
        
        return "cong-no/nha-phan-phoi/list";
    }

    /**
     * Customer Debt List
     * Displays all customer debts with filtering and metrics
     * Includes details about goods sold on credit
     * Sorted: Uncollected debts first (top), Completed collections last (bottom)
     */
    @GetMapping("/khach-hang/list")
    public String customerDebtList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            Model model) {

        // Get all customer debts with eager loading of relationships
        List<CustomerDebt> allDebts = customerDebtRepository.findAllWithKhachHang();
        
        // Filter by type (BAD_DEBT or NORMAL)
        if (type != null && !type.isEmpty()) {
            if ("BAD_DEBT".equals(type)) {
                allDebts = allDebts.stream()
                    .filter(d -> d.getUncollectibleAmount() != null && 
                            d.getUncollectibleAmount().compareTo(BigDecimal.ZERO) > 0)
                    .collect(Collectors.toList());
            } else if ("NORMAL".equals(type)) {
                allDebts = allDebts.stream()
                    .filter(d -> d.getUncollectibleAmount() == null || 
                            d.getUncollectibleAmount().compareTo(BigDecimal.ZERO) <= 0)
                    .collect(Collectors.toList());
            }
        }
        
        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            try {
                com.example.demo.entity.enums.DebtStatus debtStatus = 
                    com.example.demo.entity.enums.DebtStatus.valueOf(status);
                allDebts = allDebts.stream()
                    .filter(d -> d.getTrangThai() == debtStatus)
                    .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Invalid status, show all
            }
        }
        
        // Filter by customer if provided
        if (customerId != null) {
            allDebts = allDebts.stream()
                .filter(d -> d.getKhachHang() != null && d.getKhachHang().getId().equals(customerId))
                .collect(Collectors.toList());
        }
        
        // Sort debts by remaining debt amount (descending) - highest to lowest
        allDebts.sort((d1, d2) -> {
            BigDecimal remaining1 = d1.getTongTienConNo() != null ? d1.getTongTienConNo() : BigDecimal.ZERO;
            BigDecimal remaining2 = d2.getTongTienConNo() != null ? d2.getTongTienConNo() : BigDecimal.ZERO;
            return remaining2.compareTo(remaining1); // Descending order (highest first)
        });
        
        // Calculate metrics for filtered results
        BigDecimal totalDebt = allDebts.stream()
            .map(CustomerDebt::getTongTienNo)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCollected = allDebts.stream()
            .map(CustomerDebt::getTongTienDaThanhToan)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Count collected debts (fully paid)
        long collectedCount = allDebts.stream()
            .filter(d -> d.getTrangThai() != null && 
                    d.getTrangThai().equals(com.example.demo.entity.enums.DebtStatus.DA_THANH_TOAN_HET))
            .count();
        
        // Count overdue debts
        long overdueDebtsCount = allDebts.stream()
            .filter(d -> d.getTrangThai() != null && 
                    d.getTrangThai().equals(com.example.demo.entity.enums.DebtStatus.QUA_HAN))
            .count();
        
        // Calculate average debt
        BigDecimal averageDebt = allDebts.isEmpty() ? BigDecimal.ZERO :
            totalDebt.divide(BigDecimal.valueOf(allDebts.size()), 2, RoundingMode.HALF_UP);
        
        // Calculate remaining debt (not yet collected)
        BigDecimal remainingDebt = totalDebt.subtract(totalCollected);
        
        // Calculate current month debt
        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        BigDecimal currentMonthDebt = allDebts.stream()
            .filter(d -> {
                if (d.getNgayTaoNo() == null) return false;
                java.time.YearMonth debtMonth = java.time.YearMonth.from(d.getNgayTaoNo());
                return debtMonth.equals(currentMonth);
            })
            .map(CustomerDebt::getTongTienNo)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate bad debt (uncollectible) statistics
        BigDecimal totalBadDebt = allDebts.stream()
            .filter(d -> d.getUncollectibleAmount() != null && 
                    d.getUncollectibleAmount().compareTo(BigDecimal.ZERO) > 0)
            .map(CustomerDebt::getUncollectibleAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long badDebtCount = allDebts.stream()
            .filter(d -> d.getUncollectibleAmount() != null && 
                    d.getUncollectibleAmount().compareTo(BigDecimal.ZERO) > 0)
            .count();
        
        // Populate model with customer debts and related data
        model.addAttribute("debts", allDebts);
        model.addAttribute("customers", khachHangRepository.findAll());
        model.addAttribute("totalDebt", totalDebt);
        model.addAttribute("totalCollected", totalCollected);
        model.addAttribute("remainingDebt", remainingDebt);
        model.addAttribute("collectedCount", collectedCount);
        model.addAttribute("ongoingDebtsCount", allDebts.size() - collectedCount);
        model.addAttribute("overdueDebtsCount", overdueDebtsCount);
        model.addAttribute("averageDebt", averageDebt);
        model.addAttribute("currentMonthDebt", currentMonthDebt);
        model.addAttribute("totalBadDebt", totalBadDebt);
        model.addAttribute("badDebtCount", badDebtCount);
        
        return "cong-no/khach-hang/list";
    }

    /**
     * Payment History
     * Displays payment and collection transaction history
     */
    @GetMapping("/lich-su-thanh-toan")
    public String paymentHistory(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        // Get all supplier payments
        java.util.List<com.example.demo.entity.debt.SupplierDebtPayment> supplierPayments = 
            supplierDebtPaymentRepository.findAllWithEagerLoading();
        
        // Get all customer payments
        java.util.List<com.example.demo.entity.debt.CustomerDebtPayment> customerPayments = 
            customerDebtPaymentRepository.findAllWithEagerLoading();
        
        // Combine all payments
        java.util.List<Object> allPayments = new java.util.ArrayList<>();
        allPayments.addAll(supplierPayments);
        allPayments.addAll(customerPayments);
        
        // Calculate supplier payments in 30 days
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate thirtyDaysAgo = today.minusDays(30);
        BigDecimal totalPayments = BigDecimal.ZERO;
        
        for (com.example.demo.entity.debt.SupplierDebtPayment sp : supplierPayments) {
            if (sp.getNgayThanhToan() != null && 
                !sp.getNgayThanhToan().toLocalDate().isBefore(thirtyDaysAgo) &&
                sp.getSoTienThanhToan() != null) {
                totalPayments = totalPayments.add(sp.getSoTienThanhToan());
            }
        }
        
        // Calculate customer debt collection in 30 days
        BigDecimal collectedAmount30Days = BigDecimal.ZERO;
        
        for (com.example.demo.entity.debt.CustomerDebtPayment cp : customerPayments) {
            if (cp.getNgayThuHoi() != null && 
                !cp.getNgayThuHoi().toLocalDate().isBefore(thirtyDaysAgo) &&
                cp.getSoTienThuHoi() != null) {
                collectedAmount30Days = collectedAmount30Days.add(cp.getSoTienThuHoi());
            }
        }
        
        // Calculate today's payments
        BigDecimal todayPaymentAmount = BigDecimal.ZERO;
        long todayPaymentCount = 0;
        
        for (com.example.demo.entity.debt.SupplierDebtPayment sp : supplierPayments) {
            if (sp.getNgayThanhToan() != null && 
                sp.getNgayThanhToan().toLocalDate().equals(today) &&
                sp.getSoTienThanhToan() != null) {
                todayPaymentAmount = todayPaymentAmount.add(sp.getSoTienThanhToan());
                todayPaymentCount++;
            }
        }
        
        for (com.example.demo.entity.debt.CustomerDebtPayment cp : customerPayments) {
            if (cp.getNgayThuHoi() != null && 
                cp.getNgayThuHoi().toLocalDate().equals(today) &&
                cp.getSoTienThuHoi() != null) {
                todayPaymentAmount = todayPaymentAmount.add(cp.getSoTienThuHoi());
                todayPaymentCount++;
            }
        }
        
        // Create payment history DTOs
        java.util.List<com.example.demo.dto.RecentTransactionDTO> paymentHistoryDTOs = new java.util.ArrayList<>();
        
        for (com.example.demo.entity.debt.SupplierDebtPayment sp : supplierPayments) {
            if (sp.getNgayThanhToan() != null && sp.getSupplierDebt() != null) {
                String supplierName = "N/A";
                String invoiceNumber = "N/A";
                String originalInvoice = "N/A";
                if (sp.getSupplierDebt().getNhaPhanPhoi() != null) {
                    supplierName = sp.getSupplierDebt().getNhaPhanPhoi().getTenNhaPhanPhoi();
                }
                invoiceNumber = sp.getSupplierDebt().getSoPhieuXuatChi();
                originalInvoice = sp.getSupplierDebt().getSoPhieuXuatChi();
                
                paymentHistoryDTOs.add(new com.example.demo.dto.RecentTransactionDTO(
                    sp.getId(),
                    sp.getNgayThanhToan(),
                    "SUPPLIER",
                    invoiceNumber,
                    originalInvoice,
                    supplierName,
                    sp.getSoTienThanhToan(),
                    sp.getPhuongThucThanhToan() != null ? sp.getPhuongThucThanhToan().toString() : "N/A",
                    sp.getNguoiGhiNhan() != null ? sp.getNguoiGhiNhan() : "N/A",
                    sp.getGhiChu() != null ? sp.getGhiChu() : ""
                ));
            }
        }
        
        for (com.example.demo.entity.debt.CustomerDebtPayment cp : customerPayments) {
            if (cp.getNgayThuHoi() != null && cp.getCustomerDebt() != null) {
                String customerName = "N/A";
                String invoiceNumber = "N/A";
                String originalInvoice = "N/A";
                if (cp.getCustomerDebt().getKhachHang() != null) {
                    customerName = cp.getCustomerDebt().getKhachHang().getHoTen();
                }
                invoiceNumber = cp.getCustomerDebt().getSoPhieuXuatBan();
                originalInvoice = cp.getCustomerDebt().getSoPhieuXuatBan();
                
                paymentHistoryDTOs.add(new com.example.demo.dto.RecentTransactionDTO(
                    cp.getId(),
                    cp.getNgayThuHoi(),
                    "CUSTOMER",
                    invoiceNumber,
                    originalInvoice,
                    customerName,
                    cp.getSoTienThuHoi(),
                    cp.getPhuongThucThuHoi() != null ? cp.getPhuongThucThuHoi().toString() : "N/A",
                    cp.getNguoiGhiNhan() != null ? cp.getNguoiGhiNhan() : "N/A",
                    cp.getGhiChu() != null ? cp.getGhiChu() : ""
                ));
            }
        }
        
        // Sort by date descending
        paymentHistoryDTOs.sort(Comparator.comparing(com.example.demo.dto.RecentTransactionDTO::getNgayThanhToan).reversed());
        
        // Pagination logic
        int pageSize = 15;
        int totalPages = (int) Math.ceil((double) paymentHistoryDTOs.size() / pageSize);
        page = Math.max(1, Math.min(page, Math.max(1, totalPages)));
        
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, paymentHistoryDTOs.size());
        java.util.List<com.example.demo.dto.RecentTransactionDTO> pagedPayments = 
            paymentHistoryDTOs.subList(startIndex, endIndex);
        
        // Add to model
        model.addAttribute("payments", pagedPayments);
        model.addAttribute("allPayments", paymentHistoryDTOs);
        model.addAttribute("totalPayments", totalPayments);
        model.addAttribute("paymentCount", paymentHistoryDTOs.size());
        model.addAttribute("collectedAmount30Days", collectedAmount30Days);
        model.addAttribute("todayPaymentCount", todayPaymentCount);
        model.addAttribute("todayPaymentAmount", todayPaymentAmount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", pageSize);
        
        return "cong-no/payment-history";
    }
}
