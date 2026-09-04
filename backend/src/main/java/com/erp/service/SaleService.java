package com.erp.service;

import com.erp.common.BusinessException;
import com.erp.common.PageResult;
import com.erp.entity.*;
import com.erp.mapper.*;
import com.erp.util.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 销售业务服务：销售单/审批/信用校验/出库发货/退货
 */
@Service
public class SaleService {

    private final SaleOrderMapper orderMapper;
    private final CustomerMapper customerMapper;
    private final GoodsMapper goodsMapper;
    private final StockMapper stockMapper;
    private final StockLogMapper stockLogMapper;
    private final OutboundMapper outboundMapper;
    private final ArcDetailMapper arcDetailMapper;
    private final ReturnOrderMapper returnOrderMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final BillNoService billNoService;
    private final AuditService auditService;

    public SaleService(SaleOrderMapper orderMapper, CustomerMapper customerMapper, GoodsMapper goodsMapper,
                       StockMapper stockMapper, StockLogMapper stockLogMapper, OutboundMapper outboundMapper,
                       ArcDetailMapper arcDetailMapper, ReturnOrderMapper returnOrderMapper,
                       ApprovalRecordMapper approvalRecordMapper, BillNoService billNoService, AuditService auditService) {
        this.orderMapper = orderMapper;
        this.customerMapper = customerMapper;
        this.goodsMapper = goodsMapper;
        this.stockMapper = stockMapper;
        this.stockLogMapper = stockLogMapper;
        this.outboundMapper = outboundMapper;
        this.arcDetailMapper = arcDetailMapper;
        this.returnOrderMapper = returnOrderMapper;
        this.approvalRecordMapper = approvalRecordMapper;
        this.billNoService = billNoService;
        this.auditService = auditService;
    }

    // ==================== 销售单 ====================
    public PageResult<SaleOrder> orderPage(String keyword, String status, int page, int pageSize) {
        return PageResult.of(orderMapper.count(keyword, status), orderMapper.page(keyword, status, (page - 1) * pageSize, pageSize));
    }

    public SaleOrder orderDetail(Long id) {
        SaleOrder so = orderMapper.findById(id);
        if (so != null) so.setItems(orderMapper.selectItems(id));
        return so;
    }

    @Transactional
    public SaleOrder saveOrder(SaleOrder so) {
        if (so.getId() == null) {
            so.setOrderNo(billNoService.generate("SO"));
            so.setStatus("草稿");
            so.setAuditStatus("未审核");
            so.setOrderStates("出库中");
            if (so.getTaxRate() == null) so.setTaxRate(new BigDecimal("13"));
            if (so.getAllAmount() == null) so.setAllAmount(BigDecimal.ZERO);
            if (so.getTaxAmount() == null) so.setTaxAmount(BigDecimal.ZERO);
            if (so.getDiscount() == null) so.setDiscount(BigDecimal.ZERO);
            if (so.getReceivedAmount() == null) so.setReceivedAmount(BigDecimal.ZERO);
            so.setCreateBy(UserContext.currentName());
            orderMapper.insert(so);
            saveItems(so);
            recalc(so.getId());
            auditService.log("新增销售单", so.getOrderNo(), "", "");
        } else {
            orderMapper.update(so);
            orderMapper.deleteItems(so.getId());
            saveItems(so);
            recalc(so.getId());
            auditService.log("编辑销售单", so.getOrderNo(), "", "");
        }
        return orderDetail(so.getId());
    }

    private void saveItems(SaleOrder so) {
        if (so.getItems() != null) {
            for (SaleOrderItem item : so.getItems()) {
                item.setOrderId(so.getId());
                item.setDeliveredQty(item.getDeliveredQty() == null ? BigDecimal.ZERO : item.getDeliveredQty());
                item.setAmount(item.getQuantity().multiply(item.getPrice()));
                orderMapper.insertItem(item);
            }
        }
    }

    /** 删除销售单（仅草稿可删） */
    @Transactional
    public void deleteOrder(Long id) {
        SaleOrder so = orderMapper.findById(id);
        if (so == null) throw new BusinessException("销售单不存在");
        if (!"草稿".equals(so.getStatus())) throw new BusinessException("仅草稿状态销售单可删除");
        orderMapper.deleteItems(id);
        orderMapper.delete(id);
        auditService.log("删除销售单", so.getOrderNo(), "", "");
    }

    private void recalc(Long orderId) {
        List<SaleOrderItem> items = orderMapper.selectItems(orderId);
        BigDecimal total = items.stream().map(SaleOrderItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        SaleOrder so = orderMapper.findById(orderId);
        if (so == null) return;
        so.setAllAmount(total);
        so.setTaxAmount(calTax(total, so.getTaxRate()));
        so.setDiscount(so.getDiscount() == null ? BigDecimal.ZERO : so.getDiscount());
        orderMapper.update(so);
    }

    private BigDecimal calTax(BigDecimal amount, BigDecimal rate) {
        if (rate == null) return BigDecimal.ZERO;
        return amount.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /** 提交审批（含信用预占校验） */
    @Transactional
    public void submitApprove(Long id) {
        SaleOrder so = orderMapper.findById(id);
        if (so == null) throw new BusinessException("销售单不存在");
        checkCredit(so.getCustomerId(), so.getAllAmount(), null);
        orderMapper.updateApprove(id, so.getStatus(), "待审核", UserContext.currentName());
        auditService.log("提交销售审批", so.getOrderNo(), so.getAuditStatus(), "待审核");
    }

    /** 审批通过/驳回 */
    @Transactional
    public void approve(Long id, boolean pass, String comment) {
        SaleOrder so = orderMapper.findById(id);
        if (so == null) throw new BusinessException("销售单不存在");
        if (pass) {
            checkCredit(so.getCustomerId(), so.getAllAmount(), so.getId());
        }
        String toAudit = pass ? "已审核" : "已驳回";
        String toStatus = pass ? "已通过" : "草稿";
        orderMapper.updateApprove(id, toStatus, toAudit, UserContext.currentName());
        ApprovalRecord ar = new ApprovalRecord();
        ar.setDocType("SALE");
        ar.setDocId(id);
        ar.setLevel(1);
        ar.setApprover(UserContext.currentName());
        ar.setResult(pass ? "通过" : "驳回");
        ar.setComment(comment);
        ar.setApproveTime(LocalDateTime.now());
        approvalRecordMapper.insert(ar);
        auditService.log(pass ? "销售审批通过" : "销售审批驳回", so.getOrderNo(), so.getAuditStatus(), toAudit);
    }

    /** 信用校验：used + 订单额 <= limit */
    private void checkCredit(Long customerId, BigDecimal amount, Long excludeOrderId) {
        CrmCustomer c = customerMapper.findById(customerId);
        if (c == null) throw new BusinessException("客户不存在");
        BigDecimal limit = c.getCreditLimit() == null ? BigDecimal.ZERO : c.getCreditLimit();
        BigDecimal used = c.getUsedCredit() == null ? BigDecimal.ZERO : c.getUsedCredit();
        // 排除本单已占用的（若已累计过）
        BigDecimal occupied = used.add(amount == null ? BigDecimal.ZERO : amount);
        if (limit.signum() > 0 && occupied.compareTo(limit) > 0) {
            throw new BusinessException("客户【" + c.getName() + "】信用额度不足：可用额度 "
                    + limit.subtract(used) + "，本单需占用 " + amount);
        }
    }

    /** 出库发货（可部分出库） */
    @Transactional
    public WmsOutbound deliver(Long orderId, Long warehouseId, List<Map<String, Object>> outItems) {
        SaleOrder so = orderMapper.findById(orderId);
        if (so == null) throw new BusinessException("销售单不存在");
        WmsOutbound out = new WmsOutbound();
        out.setOutNo(billNoService.generate("OUT"));
        out.setOutType("SALE");
        out.setSrcNo(so.getOrderNo());
        out.setWarehouseId(warehouseId);
        out.setOutDate(LocalDate.now());
        out.setOperator(UserContext.currentName());
        out.setStatus("已出库");
        out.setCreateBy(UserContext.currentName());
        outboundMapper.insert(out);

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal planQty = BigDecimal.ZERO;
        // 订单明细单价映射（应收按订单价而非商品最新销售价）
        java.util.Map<Long, BigDecimal> priceMap = new java.util.HashMap<>();
        for (SaleOrderItem it : orderMapper.selectItems(orderId)) priceMap.put(it.getGoodsId(), it.getPrice());
        if (outItems != null) {
            for (Map<String, Object> m : outItems) {
                Long goodsId = Long.valueOf(String.valueOf(m.get("goodsId")));
                BigDecimal qty = new BigDecimal(String.valueOf(m.get("quantity")));
                // 库存扣减（负向变动）
                doStockChange(goodsId, warehouseId, qty.negate(), "销售出库", so.getOrderNo());
                WmsOutboundItem item = new WmsOutboundItem();
                item.setOutboundId(out.getId());
                item.setGoodsId(goodsId);
                item.setQuantity(qty);
                outboundMapper.insertItem(item);
                total = total.add(qty.multiply(priceMap.getOrDefault(goodsId, BigDecimal.ZERO)));
                totalQty = totalQty.add(qty);
                updateItemDelivered(orderId, goodsId, qty);
            }
        }

        List<SaleOrderItem> items = orderMapper.selectItems(orderId);
        for (SaleOrderItem it : items) planQty = planQty.add(it.getQuantity());
        boolean done = totalQty.compareTo(planQty) >= 0;
        String states = done ? "已出库" : "部分出库";
        orderMapper.updateStatus(orderId, "已出库".equals(states) ? "已出库" : "部分出库", states);

        // 生成应收明细
        generateArc(so, total);
        return out;
    }

    private void updateItemDelivered(Long orderId, Long goodsId, BigDecimal qty) {
        List<SaleOrderItem> items = orderMapper.selectItems(orderId);
        for (SaleOrderItem it : items) {
            if (it.getGoodsId().equals(goodsId)) {
                orderMapper.addDeliveredQty(it.getId(), qty);
            }
        }
    }

    /** 出库后生成应收 + 客户欠款 */
    private void generateArc(SaleOrder so, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) return;
        CrmArcDetail arc = new CrmArcDetail();
        arc.setCustomerId(so.getCustomerId());
        arc.setRefType("SALE");
        arc.setRefNo(so.getOrderNo());
        arc.setAmount(amount);
        arc.setReceived(BigDecimal.ZERO);
        arc.setBalance(amount);
        arc.setStatus("未结清");
        arc.setDueDate(LocalDate.now().plusDays(30));
        arcDetailMapper.insert(arc);
        customerMapper.changeUsedCredit(so.getCustomerId(), amount);
    }

    /** 库存变动统一入口 */
    @Transactional
    public void doStockChange(Long goodsId, Long warehouseId, BigDecimal delta, String type, String refNo) {
        WmsStock stock = stockMapper.find(warehouseId, goodsId);
        BigDecimal before = stock == null ? BigDecimal.ZERO : stock.getQuantity();
        if (delta.signum() < 0 && before.add(delta).signum() < 0) {
            throw new BusinessException("库存不足，无法出库");
        }
        if (stock == null) {
            WmsStock ns = new WmsStock();
            ns.setWarehouseId(warehouseId);
            ns.setGoodsId(goodsId);
            ns.setQuantity(delta);
            ns.setCreateBy(UserContext.currentName());
            stockMapper.insert(ns);
        } else {
            stockMapper.change(warehouseId, goodsId, delta);
        }
        WmsStockLog log = new WmsStockLog();
        log.setGoodsId(goodsId);
        log.setWarehouseId(warehouseId);
        log.setChangeType(type);
        log.setChangeQty(delta);
        log.setBeforeQty(before);
        log.setAfterQty(before.add(delta));
        log.setRefNo(refNo);
        log.setOperator(UserContext.currentName());
        stockLogMapper.insert(log);
    }

    // ==================== 销售退货 ====================
    public PageResult<ReturnOrder> returnPage(String status, int page, int pageSize) {
        return PageResult.of(returnOrderMapper.count(null, status), returnOrderMapper.page(null, status, (page - 1) * pageSize, pageSize));
    }

    /**
     * 退货登记：冲减应收并回补库存
     * @param ro 退货单（srcType=SALE, srcId=销售单id, partnerId=客户id, amount=退货金额, reason=原因）
     * @param goodsId 退货商品
     * @param warehouseId 退货仓库
     * @param quantity 退货数量
     */
    @Transactional
    public ReturnOrder saveReturn(ReturnOrder ro, Long goodsId, Long warehouseId, BigDecimal quantity) {
        if (ro.getId() != null) throw new BusinessException("退货单不允许编辑");
        SaleOrder so = orderMapper.findById(ro.getSrcId());
        if (so == null) throw new BusinessException("来源销售单不存在");
        ro.setSrcType("SALE");
        ro.setReturnNo(billNoService.generate("PR"));
        ro.setReturnDate(LocalDate.now());
        ro.setStatus("已退");
        ro.setCreateBy(UserContext.currentName());
        returnOrderMapper.insert(ro);
        // 回补库存
        doStockChange(goodsId, warehouseId, quantity, "销售退货", ro.getReturnNo());
        // 冲减应收（按销售单号 + 金额）
        arcDetailMapper.returnRefund(so.getOrderNo(), ro.getAmount());
        customerMapper.changeUsedCredit(so.getCustomerId(), ro.getAmount().negate());
        auditService.log("登记销售退货", ro.getReturnNo(), "", "");
        return ro;
    }
}
