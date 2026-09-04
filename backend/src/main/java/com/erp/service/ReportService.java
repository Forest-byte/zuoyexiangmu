package com.erp.service;

import com.erp.mapper.ReportMapper;
import com.erp.mapper.RptSnapshotMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表服务：采购/销售/财务/库存/应收应付/利润/贡献度聚合
 */
@Service
public class ReportService {

    private final ReportMapper reportMapper;
    private final RptSnapshotMapper snapshotMapper;

    public ReportService(ReportMapper reportMapper, RptSnapshotMapper snapshotMapper) {
        this.reportMapper = reportMapper;
        this.snapshotMapper = snapshotMapper;
    }

    public List<Map<String, Object>> purchaseReport(String startDate, String endDate, Long supplierId, Long goodsId) {
        return reportMapper.purchaseReport(startDate, endDate, supplierId, goodsId);
    }

    public List<Map<String, Object>> saleReport(String startDate, String endDate, Long customerId, Long goodsId) {
        return reportMapper.saleReport(startDate, endDate, customerId, goodsId);
    }

    public List<Map<String, Object>> lowStock(Long warehouseId) {
        return reportMapper.lowStock(warehouseId);
    }

    public List<Map<String, Object>> financeReport(String startDate, String endDate, Long accountId) {
        return reportMapper.financeReport(startDate, endDate, accountId);
    }

    public Map<String, Object> agingReport() {
        Map<String, Object> data = new HashMap<>();
        data.put("arc", reportMapper.arcAging());
        data.put("ap", reportMapper.apAging());
        return data;
    }

    public List<Map<String, Object>> profitReport(String startDate, String endDate) {
        return reportMapper.profitReport(startDate, endDate);
    }

    public List<Map<String, Object>> customerContribution(String startDate, String endDate, int topN) {
        return reportMapper.customerContribution(startDate, endDate, topN);
    }

    public List<Map<String, Object>> supplierContribution(String startDate, String endDate, int topN) {
        return reportMapper.supplierContribution(startDate, endDate, topN);
    }

    public List<Map<String, Object>> turnoverReport(String startDate, String endDate) {
        return reportMapper.turnoverReport(startDate, endDate);
    }

    /** 生成快照并返回快照 JSON */
    public String buildSnapshot(String rptCode, String startDate, String endDate) {
        Map<String, Object> data = new HashMap<>();
        switch (rptCode) {
            case "RPT_SALE" -> data.put("sale", saleReport(startDate, endDate, null, null));
            case "RPT_PURCHASE" -> data.put("purchase", purchaseReport(startDate, endDate, null, null));
            case "RPT_FINANCE" -> data.put("finance", financeReport(startDate, endDate, null));
            case "RPT_STOCK" -> data.put("lowStock", lowStock(null));
            default -> data.put("default", true);
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }

    /** 保存日报快照 */
    public void saveSnapshot(String rptCode) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String content = buildSnapshot(rptCode, today, today);
        if (snapshotMapper.findByCodeAndDate(rptCode, today) == null) {
            snapshotMapper.insert(rptCode, today, content);
        } else {
            // 已存在则更新：先删后插（简化）
            snapshotMapper.deleteByCodeAndDate(rptCode, today);
            snapshotMapper.insert(rptCode, today, content);
        }
    }

    public List<Map<String, Object>> recentSnapshots() { return snapshotMapper.selectRecent(); }
}
