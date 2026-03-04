package com.pharmacy.service;

import com.pharmacy.dto.ReportData;
import com.pharmacy.entity.*;
import com.pharmacy.repository.*;
import com.pharmacy.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

        private final TenantAccessService tenantAccessService;
        private final SaleTransactionRepository saleTransactionRepository;
        private final SaleReturnRepository saleReturnRepository;
        private final PurchaseOrderRepository purchaseOrderRepository;
        private final PurchaseReturnRepository purchaseReturnRepository;
        private final StockBatchRepository stockBatchRepository;
        private final PrescriptionRepository prescriptionRepository;

        public ReportData getSalesReport(AppUserPrincipal principal, LocalDate start, LocalDate end) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                List<SaleTransaction> sales = saleTransactionRepository
                                .findByPharmacyAndSaleDateBetweenOrderBySaleDateDesc(pharmacy, start, end);

                List<List<Object>> rows = sales.stream()
                                .map(s -> List.<Object>of(
                                                s.getSaleDate().toString(),
                                                s.getSaleNumber(),
                                                s.getPrescription() != null ? s.getPrescription().getPatientName()
                                                                : "Walk-in",
                                                s.getTotal(),
                                                s.getAmountPaid(),
                                                s.getTotal().subtract(s.getAmountPaid())))
                                .toList();

                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("Total Sales",
                                sales.stream().map(SaleTransaction::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
                summary.put("Total Paid",
                                sales.stream().map(SaleTransaction::getAmountPaid).reduce(BigDecimal.ZERO,
                                                BigDecimal::add));
                summary.put("Total Due",
                                sales.stream()
                                                .map(s -> s.getTotal().subtract(s.getAmountPaid()))
                                                .reduce(BigDecimal.ZERO, BigDecimal::add));

                return new ReportData("Sales Report", List.of("Date", "Bill No", "Customer", "Total", "Paid", "Due"),
                                rows,
                                summary);
        }

        public ReportData getSaleReturnReport(AppUserPrincipal principal, LocalDate start, LocalDate end) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                List<SaleReturn> returns = saleReturnRepository
                                .findBySale_PharmacyAndReturnDateBetweenOrderByReturnDateDesc(pharmacy, start, end);

                List<List<Object>> rows = returns.stream()
                                .map(r -> List.<Object>of(
                                                r.getReturnDate().toString(),
                                                r.getReturnNumber(),
                                                r.getSale().getSaleNumber(),
                                                r.getTotalAmount(),
                                                r.getReason() != null ? r.getReason() : "-"))
                                .toList();

                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("Total Refunded",
                                returns.stream().map(SaleReturn::getTotalAmount).reduce(BigDecimal.ZERO,
                                                BigDecimal::add));

                return new ReportData("Sale Return Report",
                                List.of("Date", "Return No", "Original Sale", "Refund Amount", "Reason"), rows,
                                summary);
        }

        public ReportData getPurchaseReport(AppUserPrincipal principal, LocalDate start, LocalDate end) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                List<PurchaseOrder> purchases = purchaseOrderRepository
                                .findByPharmacyAndOrderDateBetweenOrderByOrderDateDesc(pharmacy, start, end);

                List<List<Object>> rows = purchases.stream()
                                .map(p -> List.<Object>of(
                                                p.getOrderDate().toString(),
                                                p.getOrderNumber(),
                                                p.getSupplier().getName(),
                                                p.getTotalAmount(),
                                                p.getAmountPaid(),
                                                p.getTotalAmount().subtract(p.getAmountPaid())))
                                .toList();

                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("Total Purchases",
                                purchases.stream().map(PurchaseOrder::getTotalAmount).reduce(BigDecimal.ZERO,
                                                BigDecimal::add));
                summary.put("Total Paid",
                                purchases.stream().map(PurchaseOrder::getAmountPaid).reduce(BigDecimal.ZERO,
                                                BigDecimal::add));
                summary.put("Total Due",
                                purchases.stream()
                                                .map(p -> p.getTotalAmount().subtract(p.getAmountPaid()))
                                                .reduce(BigDecimal.ZERO, BigDecimal::add));

                return new ReportData("Purchase Report",
                                List.of("Date", "Order No", "Supplier", "Total", "Paid", "Due"), rows,
                                summary);
        }

        public ReportData getPurchaseReturnReport(AppUserPrincipal principal, LocalDate start, LocalDate end) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                List<PurchaseReturn> returns = purchaseReturnRepository
                                .findByPurchaseOrder_PharmacyAndReturnDateBetweenOrderByReturnDateDesc(pharmacy, start,
                                                end);

                List<List<Object>> rows = returns.stream()
                                .map(r -> List.<Object>of(
                                                r.getReturnDate().toString(),
                                                r.getReturnNumber(),
                                                r.getPurchaseOrder().getOrderNumber(),
                                                r.getPurchaseOrder().getSupplier().getName(),
                                                r.getTotalAmount()))
                                .toList();

                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("Total Credit",
                                returns.stream().map(PurchaseReturn::getTotalAmount).reduce(BigDecimal.ZERO,
                                                BigDecimal::add));

                return new ReportData("Purchase Return Report",
                                List.of("Date", "Return No", "Order No", "Supplier", "Credit Amount"), rows, summary);
        }

        public ReportData getIncomeReport(AppUserPrincipal principal, LocalDate start, LocalDate end) {
                ReportData sales = getSalesReport(principal, start, end);
                return new ReportData("Income Report (Sales Collected)", sales.headers(), sales.rows(),
                                sales.summary());
        }

        public ReportData getExpenseReport(AppUserPrincipal principal, LocalDate start, LocalDate end) {
                ReportData purchases = getPurchaseReport(principal, start, end);
                return new ReportData("Expense Report (Purchases)", purchases.headers(), purchases.rows(),
                                purchases.summary());
        }

        public ReportData getCurrentStockReport(AppUserPrincipal principal) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                List<StockBatch> batches = stockBatchRepository.findByMedicine_PharmacyOrderByExpiryDateAsc(pharmacy);

                List<List<Object>> rows = batches.stream()
                                .map(b -> List.<Object>of(
                                                b.getMedicine().getName(),
                                                b.getBatchNumber(),
                                                b.getQuantity(),
                                                b.getExpiryDate() != null ? b.getExpiryDate().toString() : "N/A",
                                                b.getMedicine().getCategory() != null ? b.getMedicine().getCategory()
                                                                : "General"))
                                .toList();

                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("Total Items", batches.size());
                summary.put("Total Units", batches.stream().mapToInt(StockBatch::getQuantity).sum());

                return new ReportData("Current Stock Report",
                                List.of("Medicine", "Batch", "Quantity", "Expiry", "Category"),
                                rows, summary);
        }

        public ReportData getCustomerDueReport(AppUserPrincipal principal) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                List<SaleTransaction> dues = saleTransactionRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy)
                                .stream()
                                .filter(s -> s.getTotal().subtract(s.getAmountPaid()).compareTo(BigDecimal.ZERO) > 0)
                                .toList();

                List<List<Object>> rows = dues.stream()
                                .map(s -> List.<Object>of(
                                                s.getPrescription() != null ? s.getPrescription().getPatientName()
                                                                : "Walk-in",
                                                s.getSaleNumber(),
                                                s.getSaleDate().toString(),
                                                s.getTotal(),
                                                s.getAmountPaid(),
                                                s.getTotal().subtract(s.getAmountPaid())))
                                .toList();

                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("Total Receivables", dues.stream().map(s -> s.getTotal().subtract(s.getAmountPaid()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add));

                return new ReportData("Customer Due Report",
                                List.of("Customer", "Sale No", "Date", "Total", "Paid", "Due"),
                                rows, summary);
        }

        public ReportData getSupplierDueReport(AppUserPrincipal principal) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                List<PurchaseOrder> dues = purchaseOrderRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy).stream()
                                .filter(p -> p.getTotalAmount().subtract(p.getAmountPaid())
                                                .compareTo(BigDecimal.ZERO) > 0)
                                .toList();

                List<List<Object>> rows = dues.stream()
                                .map(p -> List.<Object>of(
                                                p.getSupplier().getName(),
                                                p.getOrderNumber(),
                                                p.getOrderDate().toString(),
                                                p.getTotalAmount(),
                                                p.getAmountPaid(),
                                                p.getTotalAmount().subtract(p.getAmountPaid())))
                                .toList();

                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("Total Payables", dues.stream().map(p -> p.getTotalAmount().subtract(p.getAmountPaid()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add));

                return new ReportData("Supplier Due Report",
                                List.of("Supplier", "Order No", "Date", "Total", "Paid", "Due"),
                                rows, summary);
        }

        public ReportData getProfitLossReport(AppUserPrincipal principal, LocalDate start, LocalDate end) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                BigDecimal totalSales = saleTransactionRepository.totalForRange(pharmacy, start, end);
                if (totalSales == null)
                        totalSales = BigDecimal.ZERO;

                BigDecimal totalPurchases = purchaseOrderRepository.totalForRange(pharmacy, start, end);
                if (totalPurchases == null)
                        totalPurchases = BigDecimal.ZERO;

                BigDecimal totalSaleReturns = saleReturnRepository.totalForRange(pharmacy, start, end);
                if (totalSaleReturns == null)
                        totalSaleReturns = BigDecimal.ZERO;

                BigDecimal netProfit = totalSales.subtract(totalPurchases).subtract(totalSaleReturns);

                List<List<Object>> rows = List.of(
                                List.of("Total Sales", totalSales),
                                List.of("Total Purchases", totalPurchases),
                                List.of("Sales Returns (Refunds)", totalSaleReturns),
                                List.of("Net Profit/Loss", netProfit));

                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("Summary Status", netProfit.compareTo(BigDecimal.ZERO) >= 0 ? "PROFIT" : "LOSS");

                return new ReportData("Profit & Loss Report", List.of("Description", "Amount"), rows, summary);
        }

        public ReportData getDueTransactionsReport(AppUserPrincipal principal) {
                return getCustomerDueReport(principal); // Placeholder for combined due
        }

        public ReportData getSubscriptionReport(AppUserPrincipal principal) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                // Assuming subscription means recurring sales or just prescription list for now
                List<Prescription> rxList = prescriptionRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy);
                List<List<Object>> rows = rxList.stream()
                                .map(r -> List.<Object>of(
                                                r.getPatientName(),
                                                r.getPrescriptionNumber(),
                                                r.getDoctorName() != null ? r.getDoctorName() : "N/A",
                                                r.getCreatedAt()))
                                .toList();

                return new ReportData("Subscription Report", List.of("Patient", "Ref No", "Doctor", "Date"), rows,
                                new HashMap<>());
        }

        public ReportData getExpiredProductReport(AppUserPrincipal principal) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                List<StockBatch> expired = stockBatchRepository.findByMedicine_Pharmacy_IdAndExpiryDateBefore(
                                pharmacy.getId(),
                                LocalDate.now());

                List<List<Object>> rows = expired.stream()
                                .map(b -> List.<Object>of(
                                                b.getMedicine().getName(),
                                                b.getBatchNumber(),
                                                b.getQuantity(),
                                                b.getExpiryDate().toString()))
                                .toList();

                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("Total Expired Batches", expired.size());

                return new ReportData("Expired Product Report", List.of("Medicine", "Batch", "Quantity", "Expiry Date"),
                                rows,
                                summary);
        }
}
