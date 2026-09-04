package com.erp.service.impl;

import com.erp.common.BusinessException;
import com.erp.entity.SysCodeRule;
import com.erp.mapper.CodeRuleMapper;
import com.erp.service.BillNoService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 单据号生成：前缀+yyyyMMdd+3位流水，全局唯一
 */
@Service
public class BillNoServiceImpl implements BillNoService {

    /** docType -> (表名, 单号列名) */
    private static final Map<String, String[]> TABLE_MAP = Map.of(
            "PO", new String[]{"purchase_order", "order_no"},
            "SO", new String[]{"sale_order", "order_no"},
            "IN", new String[]{"wms_inbound", "in_no"},
            "OUT", new String[]{"wms_outbound", "out_no"},
            "PD", new String[]{"purchase_demand", "demand_no"},
            "PR", new String[]{"return_order", "return_no"},
            "TR", new String[]{"wms_transfer", "transfer_no"},
            "RC", new String[]{"fin_con_list", "list_no"},
            "PY", new String[]{"fin_con_list", "list_no"},
            "FT", new String[]{"fin_transfer", "transfer_no"}
    );

    private final CodeRuleMapper codeRuleMapper;
    private final com.erp.mapper.MaxNoMapper maxNoMapper;

    public BillNoServiceImpl(CodeRuleMapper codeRuleMapper, com.erp.mapper.MaxNoMapper maxNoMapper) {
        this.codeRuleMapper = codeRuleMapper;
        this.maxNoMapper = maxNoMapper;
    }

    @Override
    public synchronized String generate(String docType) {
        SysCodeRule rule = codeRuleMapper.findByDocType(docType);
        if (rule == null) {
            throw new BusinessException("未配置编码规则: " + docType);
        }
        String prefix = rule.getPrefix();
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String[] tm = TABLE_MAP.get(docType);
        if (tm == null) {
            throw new BusinessException("单据类型未映射表: " + docType);
        }
        String maxNo = maxNoMapper.maxBillNo(tm[0], tm[1], prefix, dateStr);
        int seq = 1;
        if (maxNo != null && !maxNo.isEmpty()) {
            String tail = maxNo.substring(prefix.length() + dateStr.length());
            try { seq = Integer.parseInt(tail) + 1; } catch (NumberFormatException ignored) {}
        }
        int len = rule.getSeqLen() == null || rule.getSeqLen() <= 0 ? 3 : rule.getSeqLen();
        String seqStr = String.format("%0" + len + "d", seq);
        return prefix + dateStr + seqStr;
    }
}
