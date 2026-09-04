-- ============================================================
-- ERP 种子数据 - 业务演示数据
-- ============================================================
USE erp;

-- ---------- 采购需求 ----------
INSERT INTO purchase_demand (demand_no, goods_id, quantity, note, need_date, applicant, status) VALUES
('PD20260801001',1,100,'月度生产备料','2026-08-10','周强','已生成采购'),
('PD20260801002',2,300,'补库','2026-08-12','李采购','已生成采购'),
('PD20260802001',5,200,'常用件补充','2026-08-15','冯雪','待处理'),
('PD20260802002',9,150,'包装耗材','2026-08-18','李采购','待处理');

-- ---------- 采购单 ----------
INSERT INTO purchase_order (id, order_no, supplier_id, apply_date, all_amount, tax_rate, tax_amount, status, audit_status, approve_person, approve_time, order_states, vehicle_id, warehouse_id, arrival_date, remark) VALUES
(1,'PO20260801001',1,'2026-08-01',64500,13,7420.35,'已到货','已审核','李采购',NOW(),'已到货',1,1,'2026-08-05','钢材月度采购'),
(2,'PO20260805001',2,'2026-08-05',114000,13,13115.04,'部分到货','已审核','李采购',NOW(),'部分到货',2,2,'2026-08-10','原材料采购'),
(3,'PO20260810001',5,'2026-08-10',12160,13,1398.94,'采购中','已审核','李采购',NOW(),'采购中',NULL,4,'2026-08-18','电子件采购'),
(4,'PO20260815001',7,'2026-08-15',6500,13,747.79,'草稿','未审核',NULL,NULL,'采购中',NULL,3,'2026-08-22','五金件采购'),
(5,'PO20260818001',10,'2026-08-18',10900,13,1254.0,'待审批','未审核',NULL,NULL,'采购中',NULL,6,'2026-08-25','机械件采购');

INSERT INTO purchase_order_item (order_id, goods_id, quantity, price, amount, received_qty, remark) VALUES
(1,1,50,1290,64500,50,''),
(2,2,300,340,102000,300,''),
(2,3,500,24,12000,300,''),
(3,8,80,72,5760,0,''),
(3,9,40,160,6400,0,''),
(4,5,200,16,3200,0,''),
(4,6,100,33,3300,0,''),
(5,12,50,92,4600,0,''),
(5,13,60,105,6300,0,'');

-- ---------- 销售单 ----------
INSERT INTO sale_order (id, order_no, customer_id, order_date, all_amount, discount, received_amount, status, audit_status, warehouse_id, delivery_date, settle_status, settle_person, remark) VALUES
(1,'SO20260802001',1,'2026-08-02',52500,0,30000,'已出库','已审核',1,'2026-08-05','部分','', ''),
(2,'SO20260806001',2,'2026-08-06',100800,0,80000,'已出库','已审核',2,'2026-08-10','部分','', ''),
(3,'SO20260810001',5,'2026-08-10',4160,0,0,'出库中','已审核',4,'2026-08-15','未结清','', ''),
(4,'SO20260815001',7,'2026-08-15',9540,0,5000,'已出库','已审核',6,'2026-08-18','部分','', ''),
(5,'SO20260818001',9,'2026-08-18',5280,0,0,'草稿','未审核',5,'2026-08-25','未结清','', ''),
(6,'SO20260820001',12,'2026-08-20',6720,0,0,'待审批','未审核',6,'2026-08-28','未结清','', '');

INSERT INTO sale_order_item (order_id, goods_id, quantity, price, amount, delivered_qty, remark) VALUES
(1,3,1500,35,52500,1500,''),
(2,1,60,1680,100800,60,''),
(3,6,80,52,4160,50,''),
(4,10,30,318,9540,30,''),
(5,13,30,168,5040,0,''),
(6,19,80,66,5280,0,''),
(6,20,10,168,1680,0,'');

-- ---------- 应收明细 ----------
INSERT INTO crm_arc_detail (customer_id, ref_type, ref_no, amount, received, balance, status, due_date) VALUES
(1,'SALE','SO20260802001',52500,30000,22500,'部分','2026-09-02'),
(2,'SALE','SO20260806001',100800,80000,20800,'部分','2026-09-06'),
(5,'SALE','SO20260810001',4160,0,4160,'未结清','2026-09-10'),
(7,'SALE','SO20260815001',9540,5000,4540,'部分','2026-09-15'),
(9,'SALE','SO20260818001',5280,0,5280,'未结清','2026-09-18'),
(12,'SALE','SO20260820001',6720,0,6720,'未结清','2026-09-20');

-- ---------- 应付明细 ----------
INSERT INTO crm_ap_detail (supplier_id, ref_type, ref_no, amount, paid, balance, status, due_date) VALUES
(1,'PURCHASE','PO20260801001',64500,30000,34500,'部分','2026-09-01'),
(2,'PURCHASE','PO20260805001',114000,60000,54000,'部分','2026-09-05'),
(5,'PURCHASE','PO20260810001',12160,0,12160,'未结清','2026-09-10'),
(7,'PURCHASE','PO20260815001',6500,0,6500,'未结清','2026-09-15'),
(10,'PURCHASE','PO20260818001',10900,0,10900,'未结清','2026-09-18');

-- ---------- 采购票据 ----------
INSERT INTO purchase_bill (order_id, bill_type, bill_no, amount, register_time) VALUES
(1,'增值税专用发票','ZZP202608050001',64500,NOW()),
(2,'增值税专用发票','ZZP202608100002',114000,NOW());

-- ---------- 入库单 ----------
INSERT INTO wms_inbound (in_no, in_type, src_no, warehouse_id, in_date, total_amount, operator, status) VALUES
('IN20260805001','PURCHASE','PO20260801001',1,'2026-08-05',64500,'王库管','已入库'),
('IN20260810001','PURCHASE','PO20260805001',2,'2026-08-10',102000,'高翔','已入库'),
('IN20260811001','PURCHASE','PO20260805001',2,'2026-08-11',12000,'高翔','已入库');

INSERT INTO wms_inbound_item (inbound_id, goods_id, quantity, price, amount) VALUES
(1,1,50,1290,64500),
(2,2,300,340,102000),
(3,3,500,24,12000);

-- ---------- 出库单 ----------
INSERT INTO wms_outbound (out_no, out_type, src_no, warehouse_id, out_date, operator, status) VALUES
('OUT20260805001','SALE','SO20260802001',1,'2026-08-05','王库管','已出库'),
('OUT20260810001','SALE','SO20260806001',2,'2026-08-10','高翔','已出库'),
('OUT20260815001','SALE','SO20260810001',4,'2026-08-15','刘洋','已出库'),
('OUT20260818001','SALE','SO20260815001',6,'2026-08-18','何军','已出库');

INSERT INTO wms_outbound_item (outbound_id, goods_id, quantity) VALUES
(1,3,1500),(2,1,60),(3,6,50),(4,10,30);

-- ---------- 库存流水 ----------
INSERT INTO wms_stock_log (goods_id, warehouse_id, change_type, change_qty, before_qty, after_qty, ref_no, operator, change_time) VALUES
(1,1,'采购入库',50,70,120,'PO20260801001','王库管','2026-08-05 14:20'),
(3,1,'销售出库',-1500,3300,1800,'SO20260802001','王库管','2026-08-05 15:00'),
(2,2,'采购入库',300,150,450,'PO20260805001','高翔','2026-08-10 16:40'),
(1,2,'销售出库',-60,85,25,'SO20260806001','高翔','2026-08-10 17:00');

-- ---------- 跟单节点 ----------
INSERT INTO follow_ups (doc_type, doc_id, node_name, node_status, operator, operate_time) VALUES
('PURCHASE',1,'下单','已完成','李采购',NOW()),
('PURCHASE',1,'发货','已完成','李采购',NOW()),
('PURCHASE',1,'到货','已完成','王库管',NOW()),
('PURCHASE',1,'结算','待处理',NULL,NULL),
('PURCHASE',2,'下单','已完成','李采购',NOW()),
('PURCHASE',2,'发货','已完成','李采购',NOW()),
('PURCHASE',2,'到货','已完成','王库管',NOW()),
('PURCHASE',2,'结算','待处理',NULL,NULL),
('PURCHASE',3,'下单','已完成','李采购',NOW()),
('PURCHASE',3,'发货','待处理',NULL,NULL),
('PURCHASE',3,'到货','待处理',NULL,NULL),
('PURCHASE',3,'结算','待处理',NULL,NULL),
('PURCHASE',4,'下单','已完成','李采购',NOW()),
('PURCHASE',4,'发货','待处理',NULL,NULL),
('PURCHASE',4,'到货','待处理',NULL,NULL),
('PURCHASE',4,'结算','待处理',NULL,NULL),
('PURCHASE',5,'下单','已完成','李采购',NOW()),
('PURCHASE',5,'发货','待处理',NULL,NULL),
('PURCHASE',5,'到货','待处理',NULL,NULL),
('PURCHASE',5,'结算','待处理',NULL,NULL);

-- ---------- 资金流水 ----------
INSERT INTO fin_account_log (account_id, biz_type, ref_no, in_amount, out_amount, balance_after, biz_date, operator) VALUES
(1,'收款','SO20260802001',30000,0,3230000,'2026-08-08','徐静'),
(3,'收款','SO20260806001',80000,0,1580000,'2026-08-12','徐静'),
(1,'付款','PO20260801001',0,30000,3200000,'2026-08-08','徐静'),
(3,'付款','PO20260805001',0,60000,1520000,'2026-08-12','徐静');

-- ---------- 审计日志 ----------
INSERT INTO sys_audit_log (operator, action, target, before_data, after_data, time) VALUES
('张伟','登录系统','admin','','','2026-08-31 08:30'),
('李采购','新增采购单','PO20260818001','','','2026-08-28 10:05'),
('李销售','新增销售单','SO20260820001','','','2026-08-20 09:00'),
('王库管','入库操作','PO20260801001','','','2026-08-05 14:20'),
('赵敏','审批付款单','PY20260810001','待审核','已审核','2026-08-12 10:10');

-- ---------- 报表快照 ----------
INSERT INTO rpt_snapshot (rpt_code, biz_date, content, generate_time) VALUES
('RPT_INVENTORY','2026-08-31','{"total":12,"lowStock":3,"avgQty":293}','2026-08-31 06:00'),
('RPT_SALE','2026-08-31','{"totalAmount":179300,"orders":6}','2026-08-31 06:00');
