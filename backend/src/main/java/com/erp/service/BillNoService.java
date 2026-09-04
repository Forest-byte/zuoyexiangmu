package com.erp.service;

/**
 * 单据号生成服务：前缀+yyyyMMdd+3位流水，全局唯一
 */
public interface BillNoService {
    /**
     * @param docType 单据类型（sys_code_rule.doc_type），如 PO/SO/IN/OUT
     */
    String generate(String docType);
}
