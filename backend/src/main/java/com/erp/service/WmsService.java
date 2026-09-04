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
import java.util.List;

/**
 * 仓储服务：入库/出库/库存/流水/盘点/调拨
 */
@Service
public class WmsService {

    private final InboundMapper inboundMapper;
    private final OutboundMapper outboundMapper;
    private final StockMapper stockMapper;
    private final StockLogMapper stockLogMapper;
    private final CheckMapper checkMapper;
    private final WmsTransferMapper transferMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final BillNoService billNoService;
    private final AuditService auditService;

    public WmsService(InboundMapper inboundMapper, OutboundMapper outboundMapper, StockMapper stockMapper,
                      StockLogMapper stockLogMapper, CheckMapper checkMapper, WmsTransferMapper transferMapper,
                      ApprovalRecordMapper approvalRecordMapper, BillNoService billNoService, AuditService auditService) {
        this.inboundMapper = inboundMapper;
        this.outboundMapper = outboundMapper;
        this.stockMapper = stockMapper;
        this.stockLogMapper = stockLogMapper;
        this.checkMapper = checkMapper;
        this.transferMapper = transferMapper;
        this.approvalRecordMapper = approvalRecordMapper;
        this.billNoService = billNoService;
        this.auditService = auditService;
    }

    // ==================== 入库单 ====================
    public PageResult<WmsInbound> inboundPage(String inType, String keyword, int page, int pageSize) {
        return PageResult.of(inboundMapper.count(inType, keyword), inboundMapper.page(inType, keyword, (page - 1) * pageSize, pageSize));
    }

    public WmsInbound inboundDetail(Long id) {
        WmsInbound i = inboundMapper.findById(id);
        if (i != null) i.setItems(inboundMapper.selectItems(id));
        return i;
    }

    /** 手工入库（非采购到货，如期初/退补/盘盈入库） */
    @Transactional
    public WmsInbound manualInbound(WmsInbound in, List<WmsInboundItem> items) {
        in.setInNo(billNoService.generate("IN"));
        in.setInType(in.getInType() == null ? "MANUAL" : in.getInType());
        in.setSrcNo(in.getSrcNo() == null ? "手工入库" : in.getSrcNo());
        in.setInDate(LocalDate.now());
        in.setOperator(UserContext.currentName());
        in.setStatus("已入库");
        in.setCreateBy(UserContext.currentName());
        inboundMapper.insert(in);
        BigDecimal total = BigDecimal.ZERO;
        if (items != null) {
            for (WmsInboundItem item : items) {
                item.setInboundId(in.getId());
                item.setAmount(item.getQuantity().multiply(item.getPrice()));
                inboundMapper.insertItem(item);
                total = total.add(item.getAmount());
                doStockChange(item.getGoodsId(), in.getWarehouseId(), item.getQuantity(), "手工入库", in.getInNo());
            }
        }
        in.setTotalAmount(total);
        inboundMapper.updateTotal(in.getId(), total);
        auditService.log("手工入库", in.getInNo(), "", "");
        return in;
    }

    // ==================== 出库单 ====================
    public PageResult<WmsOutbound> outboundPage(String outType, String keyword, int page, int pageSize) {
        return PageResult.of(outboundMapper.count(outType, keyword), outboundMapper.page(outType, keyword, (page - 1) * pageSize, pageSize));
    }

    public WmsOutbound outboundDetail(Long id) {
        WmsOutbound o = outboundMapper.findById(id);
        if (o != null) o.setItems(outboundMapper.selectItems(id));
        return o;
    }

    /** 手工出库（如领用/报损/盘亏出库） */
    @Transactional
    public WmsOutbound manualOutbound(WmsOutbound out, List<WmsOutboundItem> items) {
        out.setOutNo(billNoService.generate("OUT"));
        out.setOutType(out.getOutType() == null ? "MANUAL" : out.getOutType());
        out.setSrcNo(out.getSrcNo() == null ? "手工出库" : out.getSrcNo());
        out.setOutDate(LocalDate.now());
        out.setOperator(UserContext.currentName());
        out.setStatus("已出库");
        out.setCreateBy(UserContext.currentName());
        outboundMapper.insert(out);
        if (items != null) {
            for (WmsOutboundItem item : items) {
                item.setOutboundId(out.getId());
                outboundMapper.insertItem(item);
                doStockChange(item.getGoodsId(), out.getWarehouseId(), item.getQuantity().negate(), "手工出库", out.getOutNo());
            }
        }
        auditService.log("手工出库", out.getOutNo(), "", "");
        return out;
    }

    // ==================== 库存 ====================
    public PageResult<WmsStock> stockPage(Long warehouseId, String keyword, int page, int pageSize) {
        return PageResult.of(stockMapper.count(keyword, warehouseId, null),
                stockMapper.page(keyword, warehouseId, null, (page - 1) * pageSize, pageSize));
    }

    public List<WmsStock> lowStock() { return stockMapper.selectLowStock(); }

    // ==================== 库存流水 ====================
    public PageResult<WmsStockLog> stockLogPage(Long goodsId, Long warehouseId, String type, int page, int pageSize) {
        return PageResult.of(stockLogMapper.count(goodsId, warehouseId, type, null, null),
                stockLogMapper.page(goodsId, warehouseId, type, null, null, (page - 1) * pageSize, pageSize));
    }

    // ==================== 盘点 ====================
    public PageResult<WmsCheck> checkPage(String status, int page, int pageSize) {
        return PageResult.of(checkMapper.count(status), checkMapper.page(status, (page - 1) * pageSize, pageSize));
    }

    public WmsCheck checkDetail(Long id) {
        WmsCheck c = checkMapper.findById(id);
        if (c != null) c.setItems(checkMapper.selectItems(id));
        return c;
    }

    /** 创建盘点单（自动带出账面数量） */
    @Transactional
    public WmsCheck createCheck(Long warehouseId) {
        WmsCheck c = new WmsCheck();
        c.setCheckNo(billNoService.generate("CH"));
        c.setWarehouseId(warehouseId);
        c.setCheckDate(LocalDate.now());
        c.setStatus("待盘点");
        c.setChecker(UserContext.currentName());
        c.setCreateBy(UserContext.currentName());
        checkMapper.insert(c);
        // 自动生成明细：该仓库所有库存商品
        for (WmsStock s : stockMapper.selectByWarehouse(warehouseId)) {
            WmsCheckItem item = new WmsCheckItem();
            item.setCheckId(c.getId());
            item.setGoodsId(s.getGoodsId());
            item.setBookQty(s.getQuantity());
            item.setRealQty(null);
            item.setDiffQty(BigDecimal.ZERO);
            checkMapper.insertItem(item);
        }
        auditService.log("创建盘点单", c.getCheckNo(), "", "");
        return c;
    }

    /** 提交盘点结果：盘盈/盘亏调整库存 */
    @Transactional
    public void submitCheck(Long id, List<WmsCheckItem> items) {
        WmsCheck c = checkMapper.findById(id);
        if (c == null) throw new BusinessException("盘点单不存在");
        if (items != null) {
            for (WmsCheckItem it : items) {
                BigDecimal real = it.getRealQty() == null ? it.getBookQty() : it.getRealQty();
                BigDecimal diff = real.subtract(it.getBookQty());
                it.setDiffQty(diff);
                checkMapper.updateItemReal(it);
                if (diff.signum() != 0) {
                    doStockChange(it.getGoodsId(), c.getWarehouseId(), diff, diff.signum() > 0 ? "盘盈入库" : "盘亏出库", c.getCheckNo());
                }
            }
        }
        checkMapper.updateStatus(id, "已盘点", UserContext.currentName());
        auditService.log("提交盘点结果", c.getCheckNo(), "", "");
    }

    // ==================== 调拨 ====================
    public PageResult<WmsTransfer> transferPage(String status, int page, int pageSize) {
        return PageResult.of(transferMapper.count(status), transferMapper.page(status, (page - 1) * pageSize, pageSize));
    }

    @Transactional
    public WmsTransfer createTransfer(WmsTransfer t) {
        t.setTransferNo(billNoService.generate("TR"));
        t.setStatus("待审核");
        t.setApplicant(UserContext.currentName());
        t.setApplyTime(LocalDateTime.now());
        t.setCreateBy(UserContext.currentName());
        transferMapper.insert(t);
        auditService.log("申请调拨", t.getTransferNo(), "", "");
        return t;
    }

    /** 调拨审批通过：同事务减源仓 + 增目标仓 + 流水 */
    @Transactional
    public void approveTransfer(Long id, boolean pass, String comment) {
        WmsTransfer t = transferMapper.findById(id);
        if (t == null) throw new BusinessException("调拨单不存在");
        if (pass) {
            doStockChange(t.getGoodsId(), t.getFromWarehouse(), t.getQuantity().negate(), "调拨出库", t.getTransferNo());
            doStockChange(t.getGoodsId(), t.getToWarehouse(), t.getQuantity(), "调拨入库", t.getTransferNo());
            transferMapper.updateStatus(id, "已通过");
        } else {
            transferMapper.updateStatus(id, "已驳回");
        }
        ApprovalRecord ar = new ApprovalRecord();
        ar.setDocType("TRANSFER");
        ar.setDocId(id);
        ar.setLevel(1);
        ar.setApprover(UserContext.currentName());
        ar.setResult(pass ? "通过" : "驳回");
        ar.setComment(comment);
        ar.setApproveTime(LocalDateTime.now());
        approvalRecordMapper.insert(ar);
        auditService.log(pass ? "调拨审批通过" : "调拨审批驳回", t.getTransferNo(), "", "");
    }

    // ==================== 库存变动统一入口 ====================
    @Transactional
    public void doStockChange(Long goodsId, Long warehouseId, BigDecimal delta, String type, String refNo) {
        WmsStock stock = stockMapper.find(warehouseId, goodsId);
        BigDecimal before = stock == null ? BigDecimal.ZERO : stock.getQuantity();
        if (delta.signum() < 0 && before.add(delta).signum() < 0) {
            throw new BusinessException("库存不足，无法出库（商品ID=" + goodsId + "）");
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
}
