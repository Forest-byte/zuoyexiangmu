package com.erp.controller;

import com.erp.annotation.RequirePermission;
import com.erp.common.Result;
import com.erp.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 报表接口：采购/销售/财务/库存/应收应付/利润/贡献度/周转
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/purchase")
    @RequirePermission("report:purchase:list")
    public Result<List<Map<String, Object>>> purchase(@RequestParam(required = false) String startDate,
                                                      @RequestParam(required = false) String endDate,
                                                      @RequestParam(required = false) Long supplierId,
                                                      @RequestParam(required = false) Long goodsId) {
        return Result.ok(reportService.purchaseReport(startDate, endDate, supplierId, goodsId));
    }

    @GetMapping("/sale")
    @RequirePermission("report:sale:list")
    public Result<List<Map<String, Object>>> sale(@RequestParam(required = false) String startDate,
                                                  @RequestParam(required = false) String endDate,
                                                  @RequestParam(required = false) Long customerId,
                                                  @RequestParam(required = false) Long goodsId) {
        return Result.ok(reportService.saleReport(startDate, endDate, customerId, goodsId));
    }

    @GetMapping("/low-stock")
    @RequirePermission("report:stock:list")
    public Result<List<Map<String, Object>>> lowStock(@RequestParam(required = false) Long warehouseId) {
        return Result.ok(reportService.lowStock(warehouseId));
    }

    @GetMapping("/finance")
    @RequirePermission("report:finance:list")
    public Result<List<Map<String, Object>>> finance(@RequestParam(required = false) String startDate,
                                                     @RequestParam(required = false) String endDate,
                                                     @RequestParam(required = false) Long accountId) {
        return Result.ok(reportService.financeReport(startDate, endDate, accountId));
    }

    @GetMapping("/aging")
    @RequirePermission("report:finance:list")
    public Result<Map<String, Object>> aging() { return Result.ok(reportService.agingReport()); }

    @GetMapping("/profit")
    @RequirePermission("report:sale:list")
    public Result<List<Map<String, Object>>> profit(@RequestParam(required = false) String startDate,
                                                    @RequestParam(required = false) String endDate) {
        return Result.ok(reportService.profitReport(startDate, endDate));
    }

    @GetMapping("/customer-contribution")
    @RequirePermission("report:sale:list")
    public Result<List<Map<String, Object>>> customerContribution(@RequestParam(required = false) String startDate,
                                                                  @RequestParam(required = false) String endDate,
                                                                  @RequestParam(defaultValue = "10") int topN) {
        return Result.ok(reportService.customerContribution(startDate, endDate, topN));
    }

    @GetMapping("/supplier-contribution")
    @RequirePermission("report:purchase:list")
    public Result<List<Map<String, Object>>> supplierContribution(@RequestParam(required = false) String startDate,
                                                                  @RequestParam(required = false) String endDate,
                                                                  @RequestParam(defaultValue = "10") int topN) {
        return Result.ok(reportService.supplierContribution(startDate, endDate, topN));
    }

    @GetMapping("/turnover")
    @RequirePermission("report:stock:list")
    public Result<List<Map<String, Object>>> turnover(@RequestParam(required = false) String startDate,
                                                      @RequestParam(required = false) String endDate) {
        return Result.ok(reportService.turnoverReport(startDate, endDate));
    }

    @GetMapping("/snapshots")
    @RequirePermission("report:finance:list")
    public Result<List<Map<String, Object>>> snapshots() { return Result.ok(reportService.recentSnapshots()); }
}
