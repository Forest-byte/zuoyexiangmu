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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CRM 服务：客户/供应商/伙伴分类/跟进/信用/对账/客户合并
 */
@Service
public class CrmService {

    private final CustomerMapper customerMapper;
    private final SupplierMapper supplierMapper;
    private final CategoryMapper categoryMapper;
    private final FollowRecordMapper followRecordMapper;
    private final ArcDetailMapper arcDetailMapper;
    private final ApDetailMapper apDetailMapper;
    private final CreditLogMapper creditLogMapper;
    private final AuditService auditService;

    public CrmService(CustomerMapper customerMapper, SupplierMapper supplierMapper, CategoryMapper categoryMapper,
                      FollowRecordMapper followRecordMapper, ArcDetailMapper arcDetailMapper, ApDetailMapper apDetailMapper,
                      CreditLogMapper creditLogMapper, AuditService auditService) {
        this.customerMapper = customerMapper;
        this.supplierMapper = supplierMapper;
        this.categoryMapper = categoryMapper;
        this.followRecordMapper = followRecordMapper;
        this.arcDetailMapper = arcDetailMapper;
        this.apDetailMapper = apDetailMapper;
        this.creditLogMapper = creditLogMapper;
        this.auditService = auditService;
    }

    // ==================== 客户 ====================
    public PageResult<CrmCustomer> customerPage(String keyword, String status, int page, int pageSize) {
        return PageResult.of(customerMapper.count(keyword, status), customerMapper.page(keyword, status, (page - 1) * pageSize, pageSize));
    }

    public List<CrmCustomer> customers() { return customerMapper.selectAll(); }

    public CrmCustomer customerDetail(Long id) { return customerMapper.findById(id); }

    @Transactional
    public CrmCustomer saveCustomer(CrmCustomer c) {
        if (c.getId() == null) {
            // 编码自动生成 C+流水
            if (c.getCode() == null || c.getCode().isEmpty()) {
                Long maxId = customerMapper.maxId();
                c.setCode(String.format("CUS%03d", (maxId == null ? 0 : maxId) + 1));
            }
            c.setApprovalStatus("草稿");
            c.setStatus("正常");
            c.setUsedCredit(BigDecimal.ZERO);
            c.setDebtAmount(BigDecimal.ZERO);
            c.setCreateBy(UserContext.currentName());
            customerMapper.insert(c);
            auditService.log("新增客户", c.getName(), "", "");
        } else {
            customerMapper.update(c);
            auditService.log("编辑客户", c.getName(), "", "");
        }
        return c;
    }

    /** 提交审批 / 审批 */
    @Transactional
    public void approveCustomer(Long id, boolean approve, String comment) {
        CrmCustomer c = customerMapper.findById(id);
        if (c == null) throw new BusinessException("客户不存在");
        String from = c.getApprovalStatus();
        String to = approve ? "已通过" : "已驳回";
        customerMapper.updateApprovalStatus(id, to);
        auditService.log(approve ? "客户审批通过" : "客户审批驳回", c.getName(), from, to);
    }

    @Transactional
    public void submitCustomer(Long id) {
        CrmCustomer c = customerMapper.findById(id);
        if (c == null) throw new BusinessException("客户不存在");
        customerMapper.updateApprovalStatus(id, "待审批");
        auditService.log("提交客户审批", c.getName(), c.getApprovalStatus(), "待审批");
    }

    @Transactional
    public void deleteCustomer(Long id) {
        CrmCustomer c = customerMapper.findById(id);
        customerMapper.delete(id);
        auditService.log("删除客户", c == null ? String.valueOf(id) : c.getName(), "", "");
    }

    // ==================== 供应商 ====================
    public PageResult<CrmSupplier> supplierPage(String keyword, int page, int pageSize) {
        return PageResult.of(supplierMapper.count(keyword), supplierMapper.page(keyword, (page - 1) * pageSize, pageSize));
    }

    public List<CrmSupplier> suppliers() { return supplierMapper.selectAll(); }

    @Transactional
    public CrmSupplier saveSupplier(CrmSupplier s) {
        if (s.getId() == null) {
            if (s.getCode() == null || s.getCode().isEmpty()) {
                Long maxId = supplierMapper.maxId();
                s.setCode(String.format("SUP%03d", (maxId == null ? 0 : maxId) + 1));
            }
            s.setPayableAmount(BigDecimal.ZERO);
            s.setStatus("正常");
            s.setCreateBy(UserContext.currentName());
            supplierMapper.insert(s);
            auditService.log("新增供应商", s.getName(), "", "");
        } else {
            supplierMapper.update(s);
            auditService.log("编辑供应商", s.getName(), "", "");
        }
        return s;
    }

    @Transactional
    public void deleteSupplier(Long id) {
        CrmSupplier s = supplierMapper.findById(id);
        supplierMapper.delete(id);
        auditService.log("删除供应商", s == null ? String.valueOf(id) : s.getName(), "", "");
    }

    // ==================== 伙伴分类 ====================
    public List<CrmCategory> categoryTree(String kind) {
        List<CrmCategory> all = (kind == null || kind.isEmpty()) ? categoryMapper.selectAll() : categoryMapper.selectByKind(kind);
        Map<Long, List<CrmCategory>> byParent = new LinkedHashMap<>();
        for (CrmCategory c : all) byParent.computeIfAbsent(c.getParentId() == null ? 0L : c.getParentId(), k -> new ArrayList<>()).add(c);
        List<CrmCategory> roots = new ArrayList<>();
        for (CrmCategory c : byParent.getOrDefault(0L, new ArrayList<>())) {
            fillCat(c, byParent);
            roots.add(c);
        }
        return roots;
    }

    private void fillCat(CrmCategory p, Map<Long, List<CrmCategory>> byParent) {
        List<CrmCategory> children = byParent.getOrDefault(p.getId(), new ArrayList<>());
        for (CrmCategory c : children) fillCat(c, byParent);
        p.setChildren(children);
    }

    @Transactional
    public void saveCategory(CrmCategory c) {
        if (c.getId() == null) {
            c.setCreateBy(UserContext.currentName());
            categoryMapper.insert(c);
        } else {
            categoryMapper.update(c);
        }
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (categoryMapper.countChildren(id) > 0) throw new BusinessException("存在子分类，禁止删除");
        if (customerMapper.countByCategory(id) > 0 || supplierMapper.countByCategory(id) > 0) {
            throw new BusinessException("分类下存在客户/供应商，禁止删除");
        }
        categoryMapper.delete(id);
    }

    // ==================== 跟进记录 ====================
    public List<CrmFollowRecord> follows(Long customerId) { return followRecordMapper.selectByCustomer(customerId); }

    @Transactional
    public void addFollow(Long customerId, String content, LocalDate nextTime) {
        CrmFollowRecord r = new CrmFollowRecord();
        r.setCustomerId(customerId);
        r.setContent(content);
        r.setNextTime(nextTime);
        r.setRecorder(UserContext.currentName());
        r.setRecordTime(LocalDateTime.now());
        r.setCreateBy(UserContext.currentName());
        followRecordMapper.insert(r);
        auditService.log("新增跟进", customerId.toString(), "", "");
    }

    // ==================== 信用管理 ====================
    public Map<String, Object> creditInfo(Long customerId) {
        CrmCustomer c = customerMapper.findById(customerId);
        if (c == null) throw new BusinessException("客户不存在");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("customerId", c.getId());
        m.put("name", c.getName());
        m.put("creditLimit", c.getCreditLimit());
        m.put("usedCredit", c.getUsedCredit());
        m.put("available", c.getCreditLimit().subtract(c.getUsedCredit()));
        m.put("debtAmount", c.getDebtAmount());
        m.put("logs", creditLogMapper.selectByCustomer(customerId));
        return m;
    }

    @Transactional
    public void changeCredit(Long customerId, BigDecimal newLimit, String reason) {
        CrmCustomer c = customerMapper.findById(customerId);
        if (c == null) throw new BusinessException("客户不存在");
        BigDecimal delta = newLimit.subtract(c.getCreditLimit() == null ? BigDecimal.ZERO : c.getCreditLimit());
        customerMapper.updateCreditLimit(customerId, newLimit);
        CrmCreditLog log = new CrmCreditLog();
        log.setCustomerId(customerId);
        log.setChangeAmount(delta);
        log.setReason(reason);
        log.setOperator(UserContext.currentName());
        log.setOperateTime(LocalDateTime.now());
        creditLogMapper.insert(log);
        auditService.log("信用变更", c.getName(), c.getCreditLimit() == null ? "0" : c.getCreditLimit().toString(), newLimit.toString());
    }

    // ==================== 应收应付对账 ====================
    public PageResult<CrmArcDetail> arcPage(Long customerId, String status, int page, int pageSize) {
        return PageResult.of(arcDetailMapper.count(customerId, status), arcDetailMapper.page(customerId, status, (page - 1) * pageSize, pageSize));
    }

    public PageResult<CrmApDetail> apPage(Long supplierId, String status, int page, int pageSize) {
        return PageResult.of(apDetailMapper.count(supplierId, status), apDetailMapper.page(supplierId, status, (page - 1) * pageSize, pageSize));
    }

    // ==================== 客户合并 ====================
    @Transactional
    public void mergeCustomer(Long fromId, Long toId) {
        if (fromId.equals(toId)) throw new BusinessException("主客户与从客户不能相同");
        CrmCustomer from = customerMapper.findById(fromId);
        CrmCustomer to = customerMapper.findById(toId);
        if (from == null || to == null) throw new BusinessException("客户不存在");
        // 转移单据/应收/跟进
        arcDetailMapper.merge(fromId, toId);
        followRecordMapper.merge(fromId, toId);
        // 从客户停用并记录
        customerMapper.disableForMerge(fromId, toId);
        // 更新主客户欠款合计（以转移后的应收余额为准）
        BigDecimal sum = arcDetailMapper.sumBalance(toId);
        customerMapper.resetUsedCredit(toId);
        customerMapper.changeUsedCredit(toId, sum);
        auditService.log("客户合并", from.getName() + "->" + to.getName(), "", "");
    }
}
