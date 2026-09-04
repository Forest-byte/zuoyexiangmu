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
 * 采购业务服务：采购需求/采购单/审批/车辆调度/到货入库/票据/跟单/结算
 */
@Service
public class PurchaseService {

    private final PurchaseDemandMapper demandMapper;
    private final PurchaseOrderMapper orderMapper;
    private final SupplierMapper supplierMapper;
    private final GoodsMapper goodsMapper;
    private final StockMapper stockMapper;
    private final StockLogMapper stockLogMapper;
    private final InboundMapper inboundMapper;
    private final PurchaseBillMapper billMapper;
    private final FollowUpsMapper followUpsMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final ApDetailMapper apDetailMapper;
    private final BillNoService billNoService;
    private final AuditService auditService;

    public PurchaseService(PurchaseDemandMapper demandMapper, PurchaseOrderMapper orderMapper, SupplierMapper supplierMapper,
                           GoodsMapper goodsMapper, StockMapper stockMapper, StockLogMapper stockLogMapper, InboundMapper inboundMapper,
                           PurchaseBillMapper billMapper, FollowUpsMapper followUpsMapper, ApprovalRecordMapper approvalRecordMapper,
                           ApDetailMapper apDetailMapper, BillNoService billNoService, AuditService auditService) {
        this.demandMapper = demandMapper;
        this.orderMapper = orderMapper;
        this.supplierMapper = supplierMapper;
        this.goodsMapper = goodsMapper;
        this.stockMapper = stockMapper;
        this.stockLogMapper = stockLogMapper;
        this.inboundMapper = inboundMapper;
        this.billMapper = billMapper;
        this.followUpsMapper = followUpsMapper;
        this.approvalRecordMapper = approvalRecordMapper;
        this.apDetailMapper = apDetailMapper;
        this.billNoService = billNoService;
        this.auditService = auditService;
    }

    // ==================== 采购需求 ====================
    public PageResult<PurchaseDemand> demandPage(String status, int page, int pageSize) {
        return PageResult.of(demandMapper.count(status), demandMapper.page(status, (page - 1) * pageSize, pageSize));
    }

    @Transactional
    public PurchaseDemand saveDemand(PurchaseDemand d) {
        if (d.getId() == null) {
            d.setDemandNo(billNoService.generate("PD"));
            d.setApplicant(UserContext.currentName());
            d.setStatus("待处理");
            d.setCreateBy(UserContext.currentName());
            demandMapper.insert(d);
            auditService.log("新增采购需求", d.getDemandNo(), "", "");
        }
        return d;
    }

    @Transactional
    public void deleteDemand(Long id) {
        PurchaseDemand d = demandMapper.selectByIds(String.valueOf(id)).stream().findFirst().orElse(null);
        if (d != null && !"待处理".equals(d.getStatus())) throw new BusinessException("已生成采购单的需求不能删除");
        demandMapper.delete(id);
    }

    /** 删除采购单（仅草稿可删） */
    @Transactional
    public void deleteOrder(Long id) {
        PurchaseOrder po = orderMapper.findById(id);
        if (po == null) throw new BusinessException("采购单不存在");
        if (!"草稿".equals(po.getStatus())) throw new BusinessException("仅草稿状态采购单可删除");
        orderMapper.deleteItems(id);
        orderMapper.delete(id);
        auditService.log("删除采购单", po.getOrderNo(), "", "");
    }

    /** 由需求批量生成采购单 */
    @Transactional
    public PurchaseOrder createFromDemands(List<Long> demandIds, Long supplierId, Long warehouseId, LocalDate applyDate) {
        String ids = String.join(",", demandIds.stream().map(String::valueOf).toList());
        List<PurchaseDemand> demands = demandMapper.selectByIds(ids);
        if (demands.isEmpty()) throw new BusinessException("所选需求不存在或已处理");

        PurchaseOrder po = new PurchaseOrder();
        po.setOrderNo(billNoService.generate("PO"));
        po.setSupplierId(supplierId);
        po.setApplyDate(applyDate == null ? LocalDate.now() : applyDate);
        po.setWarehouseId(warehouseId);
        po.setStatus("草稿");
        po.setAuditStatus("未审核");
        po.setOrderStates("采购中");
        po.setTaxRate(new BigDecimal("13"));
        po.setAllAmount(BigDecimal.ZERO);
        po.setTaxAmount(BigDecimal.ZERO);
        po.setCreateBy(UserContext.currentName());
        orderMapper.insert(po);

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        for (PurchaseDemand d : demands) {
            Goods g = goodsById(d.getGoodsId());
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setOrderId(po.getId());
            item.setGoodsId(d.getGoodsId());
            item.setQuantity(d.getQuantity());
            item.setPrice(g == null ? BigDecimal.ZERO : g.getPurchasePrice());
            item.setAmount(item.getQuantity().multiply(item.getPrice()));
            item.setReceivedQty(BigDecimal.ZERO);
            orderMapper.insertItem(item);
            total = total.add(item.getAmount());
            // 需求标记已处理
            demandMapper.updateStatus(d.getId(), "已生成采购");
        }
        po.setAllAmount(total);
        po.setTaxAmount(calTax(total, po.getTaxRate()));
        orderMapper.update(po);
        initFollows(po.getId());
        auditService.log("需求生成采购单", po.getOrderNo(), "", "");
        return po;
    }

    // ==================== 采购单 ====================
    public PageResult<PurchaseOrder> orderPage(String keyword, String status, int page, int pageSize) {
        return PageResult.of(orderMapper.count(keyword, status), orderMapper.page(keyword, status, (page - 1) * pageSize, pageSize));
    }

    public PurchaseOrder orderDetail(Long id) {
        PurchaseOrder po = orderMapper.findById(id);
        if (po != null) {
            po.setItems(orderMapper.selectItems(id));
        }
        return po;
    }

    @Transactional
    public PurchaseOrder saveOrder(PurchaseOrder po) {
        if (po.getId() == null) {
            po.setOrderNo(billNoService.generate("PO"));
            po.setStatus("草稿");
            po.setAuditStatus("未审核");
            po.setOrderStates("采购中");
            if (po.getTaxRate() == null) po.setTaxRate(new BigDecimal("13"));
            if (po.getAllAmount() == null) po.setAllAmount(BigDecimal.ZERO);
            if (po.getTaxAmount() == null) po.setTaxAmount(BigDecimal.ZERO);
            po.setCreateBy(UserContext.currentName());
            orderMapper.insert(po);
            saveItems(po);
            recalc(po.getId());
            initFollows(po.getId());
            auditService.log("新增采购单", po.getOrderNo(), "", "");
        } else {
            orderMapper.update(po);
            orderMapper.deleteItems(po.getId());
            saveItems(po);
            recalc(po.getId());
            auditService.log("编辑采购单", po.getOrderNo(), "", "");
        }
        return orderDetail(po.getId());
    }

    private void saveItems(PurchaseOrder po) {
        if (po.getItems() != null) {
            for (PurchaseOrderItem item : po.getItems()) {
                item.setOrderId(po.getId());
                item.setReceivedQty(item.getReceivedQty() == null ? BigDecimal.ZERO : item.getReceivedQty());
                item.setAmount(item.getQuantity().multiply(item.getPrice()));
                orderMapper.insertItem(item);
            }
        }
    }

    private void recalc(Long orderId) {
        List<PurchaseOrderItem> items = orderMapper.selectItems(orderId);
        BigDecimal total = items.stream().map(PurchaseOrderItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        PurchaseOrder po = orderMapper.findById(orderId);
        if (po == null) return;
        po.setAllAmount(total);
        po.setTaxAmount(calTax(total, po.getTaxRate()));
        orderMapper.update(po);
    }

    private BigDecimal calTax(BigDecimal amount, BigDecimal rate) {
        if (rate == null) return BigDecimal.ZERO;
        return amount.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /** 提交审批 */
    @Transactional
    public void submitApprove(Long id) {
        PurchaseOrder po = orderMapper.findById(id);
        if (po == null) throw new BusinessException("采购单不存在");
        if ("未审核".equals(po.getAuditStatus()) || "已驳回".equals(po.getAuditStatus())) {
            orderMapper.updateApprove(id, po.getStatus(), "待审核", UserContext.currentName(), po.getOrderStates());
            auditService.log("提交采购审批", po.getOrderNo(), po.getAuditStatus(), "待审核");
        }
    }

    /** 审批（通过/驳回） */
    @Transactional
    public void approve(Long id, boolean pass, String comment) {
        PurchaseOrder po = orderMapper.findById(id);
        if (po == null) throw new BusinessException("采购单不存在");
        String toAudit = pass ? "已审核" : "已驳回";
        String toStatus = pass ? "已通过" : "草稿";
        orderMapper.updateApprove(id, toStatus, toAudit, UserContext.currentName(), po.getOrderStates());
        ApprovalRecord ar = new ApprovalRecord();
        ar.setDocType("PURCHASE");
        ar.setDocId(id);
        ar.setLevel(1);
        ar.setApprover(UserContext.currentName());
        ar.setResult(pass ? "通过" : "驳回");
        ar.setComment(comment);
        ar.setApproveTime(LocalDateTime.now());
        approvalRecordMapper.insert(ar);
        auditService.log(pass ? "采购审批通过" : "采购审批驳回", po.getOrderNo(), po.getAuditStatus(), toAudit);
    }

    /** 车辆调度 */
    @Transactional
    public void dispatchVehicle(Long id, Long vehicleId) {
        orderMapper.updateVehicle(id, vehicleId);
        completeNode(id, "发货");
        auditService.log("车辆调度", orderMapper.findById(id).getOrderNo(), "", String.valueOf(vehicleId));
    }

    /** 到货入库：可部分到货 */
    @Transactional
    public WmsInbound arrival(Long orderId, Long warehouseId, List<Map<String, Object>> inItems) {
        PurchaseOrder po = orderMapper.findById(orderId);
        if (po == null) throw new BusinessException("采购单不存在");
        WmsInbound inbound = new WmsInbound();
        inbound.setInNo(billNoService.generate("IN"));
        inbound.setInType("PURCHASE");
        inbound.setSrcNo(po.getOrderNo());
        inbound.setWarehouseId(warehouseId == null ? po.getWarehouseId() : warehouseId);
        inbound.setInDate(LocalDate.now());
        inbound.setOperator(UserContext.currentName());
        inbound.setStatus("已入库");
        inbound.setCreateBy(UserContext.currentName());
        inboundMapper.insert(inbound);

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal planQty = BigDecimal.ZERO;
        if (inItems != null) {
            for (Map<String, Object> m : inItems) {
                Long goodsId = Long.valueOf(String.valueOf(m.get("goodsId")));
                BigDecimal qty = new BigDecimal(String.valueOf(m.get("quantity")));
                WmsInboundItem item = new WmsInboundItem();
                item.setInboundId(inbound.getId());
                item.setGoodsId(goodsId);
                item.setQuantity(qty);
                Goods g = goodsById(goodsId);
                item.setPrice(g == null ? BigDecimal.ZERO : g.getPurchasePrice());
                item.setAmount(item.getQuantity().multiply(item.getPrice()));
                inboundMapper.insertItem(item);
                total = total.add(item.getAmount());
                totalQty = totalQty.add(qty);
                // 库存变动 + 流水
                doStockChange(goodsId, inbound.getWarehouseId(), qty, "采购入库", po.getOrderNo());
                // 更新采购明细已入库
                updateItemReceived(orderId, goodsId, qty);
            }
        }
        inbound.setTotalAmount(total);
        inboundMapper.updateStatus(inbound.getId(), "已入库");

        // 更新采购单状态
        List<PurchaseOrderItem> items = orderMapper.selectItems(orderId);
        for (PurchaseOrderItem it : items) planQty = planQty.add(it.getQuantity());
        String states = totalQty.compareTo(planQty) >= 0 ? "已到货" : "部分到货";
        orderMapper.updateArrival(orderId, inbound.getWarehouseId(), LocalDate.now().toString(), "已到货".equals(states) ? "已到货" : "部分到货", states);

        // 生成应付明细（按已入库金额）
        CrmApDetail ap = new CrmApDetail();
        ap.setSupplierId(po.getSupplierId());
        ap.setRefType("PURCHASE");
        ap.setRefNo(po.getOrderNo());
        ap.setAmount(total);
        ap.setPaid(BigDecimal.ZERO);
        ap.setBalance(total);
        ap.setStatus("未结清");
        ap.setDueDate(LocalDate.now().plusDays(30));
        apDetailMapper.insert(ap);
        supplierMapper.changePayable(po.getSupplierId(), total);

        auditService.log("采购到货入库", po.getOrderNo(), "", inbound.getInNo());
        completeNode(orderId, "到货");
        return inbound;
    }

    private void updateItemReceived(Long orderId, Long goodsId, BigDecimal qty) {
        List<PurchaseOrderItem> items = orderMapper.selectItems(orderId);
        for (PurchaseOrderItem it : items) {
            if (it.getGoodsId().equals(goodsId)) {
                orderMapper.addReceivedQty(it.getId(), qty);
            }
        }
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

    // ==================== 采购票据 ====================
    public List<PurchaseBill> bills() { return billMapper.selectAll(); }

    @Transactional
    public void registerBill(PurchaseBill b) {
        b.setCreateBy(UserContext.currentName());
        b.setRegisterTime(LocalDateTime.now());
        billMapper.insert(b);
        auditService.log("登记采购票据", b.getBillNo(), "", "");
    }

    // ==================== 采购跟单 ====================
    public List<FollowUps> followUps(Long orderId) { return followUpsMapper.selectByDoc("PURCHASE", orderId); }

    @Transactional
    public void completeNode(Long orderId, String nodeName) {
        FollowUps f = new FollowUps();
        f.setDocType("PURCHASE");
        f.setDocId(orderId);
        f.setNodeName(nodeName);
        f.setNodeStatus("已完成");
        f.setOperator(UserContext.currentName());
        f.setOperateTime(LocalDateTime.now());
        followUpsMapper.updateNode(f);
        if (followUpsMapper.selectByDoc("PURCHASE", orderId).stream()
                .noneMatch(n -> "进行中".equals(n.getNodeStatus()) || "待处理".equals(n.getNodeStatus()))) {
            orderMapper.updateStatus(orderId, "结单");
        }
    }

    // ==================== 采购结算（结单） ====================
    @Transactional
    public void settle(Long orderId) {
        PurchaseOrder po = orderMapper.findById(orderId);
        if (po == null) throw new BusinessException("采购单不存在");
        orderMapper.updateStatus(orderId, "结单");
        FollowUps f = new FollowUps();
        f.setDocType("PURCHASE");
        f.setDocId(orderId);
        f.setNodeName("结算");
        f.setNodeStatus("已完成");
        f.setOperator(UserContext.currentName());
        f.setOperateTime(LocalDateTime.now());
        followUpsMapper.updateNode(f);
        auditService.log("采购结单", po.getOrderNo(), "", "结单");
    }

    private Goods goodsById(Long id) {
        return goodsMapper.findById(id);
    }

    /** 初始化采购跟单节点（下单/发货/到货/结算） */
    private void initFollows(Long orderId) {
        String[] nodes = {"下单", "发货", "到货", "结算"};
        for (int i = 0; i < nodes.length; i++) {
            FollowUps f = new FollowUps();
            f.setDocType("PURCHASE");
            f.setDocId(orderId);
            f.setNodeName(nodes[i]);
            f.setNodeStatus(i == 0 ? "已完成" : "待处理");
            f.setOperator(i == 0 ? UserContext.currentName() : null);
            f.setOperateTime(i == 0 ? LocalDateTime.now() : null);
            f.setCreateBy(UserContext.currentName());
            followUpsMapper.insert(f);
        }
    }
}
