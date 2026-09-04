package com.erp.service;

import com.erp.common.BusinessException;
import com.erp.common.PageResult;
import com.erp.entity.*;
import com.erp.mapper.*;
import com.erp.util.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 财务服务：账户/收款核销/付款核销/内部转账/资金流水
 */
@Service
public class FinanceService {

    private final FinAccountMapper accountMapper;
    private final FinAccountLogMapper accountLogMapper;
    private final FinConListMapper conListMapper;
    private final WriteOffMapper writeOffMapper;
    private final ArcDetailMapper arcDetailMapper;
    private final ApDetailMapper apDetailMapper;
    private final CustomerMapper customerMapper;
    private final SupplierMapper supplierMapper;
    private final FinTransferMapper transferMapper;
    private final BillNoService billNoService;
    private final AuditService auditService;

    public FinanceService(FinAccountMapper accountMapper, FinAccountLogMapper accountLogMapper, FinConListMapper conListMapper,
                          WriteOffMapper writeOffMapper, ArcDetailMapper arcDetailMapper, ApDetailMapper apDetailMapper,
                          CustomerMapper customerMapper, SupplierMapper supplierMapper, FinTransferMapper transferMapper,
                          BillNoService billNoService, AuditService auditService) {
        this.accountMapper = accountMapper;
        this.accountLogMapper = accountLogMapper;
        this.conListMapper = conListMapper;
        this.writeOffMapper = writeOffMapper;
        this.arcDetailMapper = arcDetailMapper;
        this.apDetailMapper = apDetailMapper;
        this.customerMapper = customerMapper;
        this.supplierMapper = supplierMapper;
        this.transferMapper = transferMapper;
        this.billNoService = billNoService;
        this.auditService = auditService;
    }

    // ==================== 账户 ====================
    public List<FinAccount> accounts() { return accountMapper.selectAll(); }

    @Transactional
    public FinAccount saveAccount(FinAccount a) {
        if (a.getId() == null) {
            a.setBalance(a.getBeginBalance() == null ? BigDecimal.ZERO : a.getBeginBalance());
            a.setStatus(a.getStatus() == null ? "正常" : a.getStatus());
            a.setCreateBy(UserContext.currentName());
            accountMapper.insert(a);
        } else {
            accountMapper.update(a);
        }
        return a;
    }

    @Transactional
    public void deleteAccount(Long id) {
        accountMapper.delete(id);
    }

    // ==================== 收款核销（应收） ====================
    public PageResult<FinConList> receiptPage(String keyword, int page, int pageSize) {
        return PageResult.of(conListMapper.count("RECEIPT", keyword), conListMapper.page("RECEIPT", keyword, (page - 1) * pageSize, pageSize));
    }

    /**
     * 收款登记：生成收款单 + 核销应收 + 账户入账 + 流水
     * @param customerId 客户
     * @param accountId 收款账户
     * @param payType 收款方式
     * @param receiptDate 收款日期
     * @param details 核销明细 [{arcDetailId, amount}]
     */
    @Transactional
    public FinConList receipt(Long customerId, Long accountId, String payType, LocalDate receiptDate,
                              String remark, List<Map<String, Object>> details) {
        FinConList list = new FinConList();
        list.setListNo(billNoService.generate("RC"));
        list.setListType("RECEIPT");
        list.setPartnerId(customerId);
        list.setAccountId(accountId);
        list.setPayType(payType);
        list.setReceiptDate(receiptDate == null ? LocalDate.now() : receiptDate);
        list.setStates("已收");
        list.setPayer(UserContext.currentName());
        list.setRemark(remark);
        list.setCreateBy(UserContext.currentName());
        conListMapper.insert(list);

        BigDecimal total = BigDecimal.ZERO;
        if (details != null) {
            for (Map<String, Object> m : details) {
                Long detailId = Long.valueOf(String.valueOf(m.get("arcDetailId")));
                BigDecimal amount = new BigDecimal(String.valueOf(m.get("amount")));
                CrmArcDetail arc = arcDetailMapper.findById(detailId);
                if (arc == null || arc.getBalance().signum() <= 0) continue;
                BigDecimal writeOff = amount.min(arc.getBalance());
                arcDetailMapper.writeOff(detailId, writeOff);
                writeOffMapper.insertReceiptRel(list.getId(), detailId, writeOff);
                total = total.add(writeOff);
            }
        }
        // 账户入账 + 流水
        accountMapper.changeBalance(accountId, total);
        insertAccountLog(accountId, "RECEIPT", list.getListNo(), total, BigDecimal.ZERO, "收款入账", receiptDate == null ? LocalDate.now() : receiptDate);
        // 客户欠款减少
        customerMapper.changeUsedCredit(customerId, total.negate());
        auditService.log("收款核销", list.getListNo(), "", total.toString());
        return list;
    }

    // ==================== 付款核销（应付） ====================
    public PageResult<FinConList> paymentPage(String keyword, int page, int pageSize) {
        return PageResult.of(conListMapper.count("PAYMENT", keyword), conListMapper.page("PAYMENT", keyword, (page - 1) * pageSize, pageSize));
    }

    @Transactional
    public FinConList payment(Long supplierId, Long accountId, String payType, LocalDate payDate,
                              String remark, List<Map<String, Object>> details) {
        FinConList list = new FinConList();
        list.setListNo(billNoService.generate("PY"));
        list.setListType("PAYMENT");
        list.setPartnerId(supplierId);
        list.setAccountId(accountId);
        list.setPayType(payType);
        list.setReceiptDate(payDate == null ? LocalDate.now() : payDate);
        list.setStates("已付");
        list.setPayer(UserContext.currentName());
        list.setRemark(remark);
        list.setCreateBy(UserContext.currentName());
        conListMapper.insert(list);

        BigDecimal total = BigDecimal.ZERO;
        if (details != null) {
            for (Map<String, Object> m : details) {
                Long detailId = Long.valueOf(String.valueOf(m.get("apDetailId")));
                BigDecimal amount = new BigDecimal(String.valueOf(m.get("amount")));
                CrmApDetail ap = apDetailMapper.findById(detailId);
                if (ap == null || ap.getBalance().signum() <= 0) continue;
                BigDecimal writeOff = amount.min(ap.getBalance());
                apDetailMapper.writeOff(detailId, writeOff);
                writeOffMapper.insertPayableRel(list.getId(), detailId, writeOff);
                total = total.add(writeOff);
            }
        }
        // 账户出账 + 流水
        accountMapper.changeBalance(accountId, total.negate());
        insertAccountLog(accountId, "PAYMENT", list.getListNo(), BigDecimal.ZERO, total, "付款出账", payDate == null ? LocalDate.now() : payDate);
        supplierMapper.changePayable(supplierId, total.negate());
        auditService.log("付款核销", list.getListNo(), "", total.toString());
        return list;
    }

    // ==================== 内部转账 ====================
    public List<FinTransfer> transfers() { return transferMapper.selectAll(); }

    @Transactional
    public FinTransfer createTransfer(FinTransfer t) {
        t.setTransferNo(billNoService.generate("FT"));
        t.setStatus("待审核");
        t.setApplicant(UserContext.currentName());
        t.setCreateBy(UserContext.currentName());
        transferMapper.insert(t);
        auditService.log("申请内部转账", t.getTransferNo(), "", "");
        return t;
    }

    @Transactional
    public void approveTransfer(Long id, boolean pass, String comment) {
        FinTransfer t = transferMapper.findById(id);
        if (t == null) throw new BusinessException("转账单不存在");
        if (pass) {
            accountMapper.changeBalance(t.getFromAccount(), t.getAmount().negate());
            accountMapper.changeBalance(t.getToAccount(), t.getAmount());
            insertAccountLog(t.getFromAccount(), "TRANSFER", t.getTransferNo(), BigDecimal.ZERO, t.getAmount(), "转出", LocalDate.now());
            insertAccountLog(t.getToAccount(), "TRANSFER", t.getTransferNo(), t.getAmount(), BigDecimal.ZERO, "转入", LocalDate.now());
            transferMapper.updateStatus(id, "已通过");
        } else {
            transferMapper.updateStatus(id, "已驳回");
        }
        auditService.log(pass ? "转账审批通过" : "转账审批驳回", t.getTransferNo(), "", "");
    }

    // ==================== 资金流水 ====================
    public PageResult<FinAccountLog> accountLogPage(Long accountId, String bizType, String refNo, String startDate, String endDate, int page, int pageSize) {
        return PageResult.of(accountLogMapper.count(accountId, bizType, refNo, startDate, endDate),
                accountLogMapper.page(accountId, bizType, refNo, startDate, endDate, (page - 1) * pageSize, pageSize));
    }

    private void insertAccountLog(Long accountId, String bizType, String refNo, BigDecimal inAmount, BigDecimal outAmount,
                                  String operator, LocalDate bizDate) {
        FinAccount a = accountMapper.findById(accountId);
        BigDecimal balanceAfter = a == null ? BigDecimal.ZERO : a.getBalance();
        FinAccountLog log = new FinAccountLog();
        log.setAccountId(accountId);
        log.setBizType(bizType);
        log.setRefNo(refNo);
        log.setInAmount(inAmount);
        log.setOutAmount(outAmount);
        log.setBalanceAfter(balanceAfter);
        log.setBizDate(bizDate);
        log.setOperator(UserContext.currentName());
        log.setCreateBy(UserContext.currentName());
        accountLogMapper.insert(log);
    }
}
