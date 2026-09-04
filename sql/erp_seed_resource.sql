-- ============================================================
-- ERP 种子数据 - 资源权限
-- ============================================================
USE erp;

-- ---------- L1 系统 ----------
INSERT INTO sys_resource (id, parent_id, name, type, code, path, icon, sort) VALUES
(1,0,'主数据','menu',NULL,NULL,'grid',1),
(2,0,'业务中心','menu',NULL,NULL,'box',2),
(3,0,'运营管理','menu',NULL,NULL,'chart',3);

-- ---------- L2 模块 ----------
INSERT INTO sys_resource (id, parent_id, name, type, code, path, icon, sort) VALUES
(11,1,'基础维护','menu',NULL,NULL,'base',1),
(12,1,'角色权限','menu',NULL,NULL,'permission',2),
(13,1,'公共规则','menu',NULL,NULL,'rules',3),
(14,2,'CRM管理','menu',NULL,NULL,'crm',1),
(15,2,'进销存','menu',NULL,NULL,'inventory',2),
(16,2,'仓储管理','menu',NULL,NULL,'warehouse',3),
(17,3,'业务报表','menu',NULL,NULL,'reports',1),
(18,3,'定时任务','menu',NULL,NULL,'jobs',2),
(19,3,'财务管理','menu',NULL,NULL,'finance',3);

-- ---------- L3 菜单 ----------
INSERT INTO sys_resource (id, parent_id, name, type, code, path, icon, sort) VALUES
(21,11,'地区管理','menu','base:region','/base/region',NULL,1),
(22,11,'分公司管理','menu','base:company','/base/company',NULL,2),
(23,11,'部门管理','menu','base:dept','/base/dept',NULL,3),
(24,11,'员工管理','menu','base:employee','/base/employee',NULL,4),
(25,11,'仓库管理','menu','base:warehouse','/base/warehouse',NULL,5),
(26,12,'角色维护','menu','system:role','/permission/role',NULL,1),
(27,12,'资源维护','menu','system:resource','/permission/resource',NULL,2),
(28,12,'用户授权','menu','system:user','/permission/grant',NULL,3),
(29,13,'系统参数','menu','base:param','/rules/param',NULL,1),
(30,13,'数据字典','menu','base:dict','/rules/dict',NULL,2),
(31,13,'车辆管理','menu','base:vehicle','/rules/vehicle',NULL,3),
(32,13,'会议室','menu','base:meeting','/rules/meeting',NULL,4),
(33,13,'编码规则','menu','base:coderule','/rules/coderule',NULL,5),
(34,13,'审批规则','menu','base:approvalrule','/rules/approval',NULL,6),
(35,13,'审计查询','menu','system:audit','/rules/audit',NULL,7),
(36,14,'客户管理','menu','crm:customer','/crm/customer',NULL,1),
(37,14,'供应商管理','menu','crm:supplier','/crm/supplier',NULL,2),
(38,14,'伙伴分类','menu','crm:category','/crm/category',NULL,3),
(39,14,'信用管理','menu','crm:credit','/crm/credit',NULL,4),
(40,14,'跟进记录','menu','crm:follow','/crm/follow',NULL,5),
(41,14,'往来对账','menu','crm:reconcile','/crm/reconcile',NULL,6),
(42,14,'客户合并','menu','crm:merge','/crm/merge',NULL,7),
(43,15,'商品档案','menu','inventory:goods','/inv/goods',NULL,1),
(44,15,'商品分类','menu','inventory:category','/inv/category',NULL,2),
(45,15,'计量单位','menu','inventory:unit','/inv/unit',NULL,3),
(46,15,'采购需求','menu','inventory:demand','/inv/demand',NULL,4),
(47,15,'采购单','menu','inventory:purchase','/inv/purchase',NULL,5),
(48,15,'采购票据','menu','inventory:bill','/inv/bill',NULL,6),
(49,15,'车辆调度','menu','inventory:dispatch','/inv/dispatch',NULL,7),
(50,15,'运输任务','menu','inventory:transport','/inv/transport',NULL,8),
(51,15,'到货入库','menu','inventory:arrival','/inv/arrival',NULL,9),
(52,15,'销售订单','menu','inventory:salord','/inv/salord',NULL,10),
(53,15,'销售单','menu','inventory:sale','/inv/sale',NULL,11),
(54,15,'出库发货','menu','inventory:deliver','/inv/deliver',NULL,12),
(55,15,'销售回款','menu','inventory:receipt','/inv/receipt',NULL,13),
(56,15,'退货管理','menu','inventory:return','/inv/return',NULL,14),
(57,16,'入库单','menu','warehouse:inbound','/wms/inbound',NULL,1),
(58,16,'出库单','menu','warehouse:outbound','/wms/outbound',NULL,2),
(59,16,'库存流水','menu','warehouse:stocklog','/wms/stocklog',NULL,3),
(60,16,'库存统计','menu','warehouse:stock','/wms/stock',NULL,4),
(61,16,'库存上下限','menu','warehouse:limit','/wms/limit',NULL,5),
(62,16,'盘点管理','menu','warehouse:check','/wms/check',NULL,6),
(63,16,'盘盈盘亏','menu','warehouse:adjust','/wms/adjust',NULL,7),
(64,16,'货物转接','menu','warehouse:transfer','/wms/transfer',NULL,8),
(65,16,'库位管理','menu','warehouse:location','/wms/location',NULL,9),
(66,17,'采购报表','menu','report:purchase','/rpt/purchase',NULL,1),
(67,17,'销售报表','menu','report:sale','/rpt/sale',NULL,2),
(68,17,'库存报表','menu','report:inventory','/rpt/inventory',NULL,3),
(69,17,'财务收支','menu','report:finance','/rpt/finance',NULL,4),
(70,17,'账龄报表','menu','report:aging','/rpt/aging',NULL,5),
(71,17,'利润汇总','menu','report:profit','/rpt/profit',NULL,6),
(72,17,'贡献度分析','menu','report:contribution','/rpt/contribution',NULL,7),
(73,18,'任务管理','menu','system:job','/job/task',NULL,1),
(74,18,'执行日志','menu','system:joblog','/job/log',NULL,2),
(75,19,'资金账户','menu','finance:account','/fin/account',NULL,1),
(76,19,'收款单','menu','finance:receipt','/fin/receipt',NULL,2),
(77,19,'付款单','menu','finance:payment','/fin/payment',NULL,3),
(78,19,'应收核销','menu','finance:arc','/fin/arc',NULL,4),
(79,19,'应付核销','menu','finance:ap','/fin/ap',NULL,5),
(80,19,'跨系统转账','menu','finance:transfer','/fin/transfer',NULL,6),
(81,19,'资金流水','menu','finance:log','/fin/log',NULL,7);

-- ---------- L4 按钮（批量生成 list/add/edit/del 四个按钮挂到每个菜单） ----------
INSERT INTO sys_resource (parent_id, name, type, code, sort)
SELECT id, '查询', 'button', CONCAT(code, ':list'), 1 FROM sys_resource WHERE type='menu';
INSERT INTO sys_resource (parent_id, name, type, code, sort)
SELECT id, '新增', 'button', CONCAT(code, ':add'), 2 FROM sys_resource WHERE type='menu';
INSERT INTO sys_resource (parent_id, name, type, code, sort)
SELECT id, '编辑', 'button', CONCAT(code, ':edit'), 3 FROM sys_resource WHERE type='menu';
INSERT INTO sys_resource (parent_id, name, type, code, sort)
SELECT id, '删除', 'button', CONCAT(code, ':del'), 4 FROM sys_resource WHERE type='menu';

-- ---------- 用户-角色关联 ----------
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1,1),(2,2),(3,3),(4,4),(5,5);

-- ---------- 角色-资源授权 ----------
-- 管理员：全部资源
INSERT INTO sys_role_resource (role_id, resource_id)
SELECT 1, id FROM sys_resource WHERE deleted=0;

-- 采购员 ROLE_PURCHASE：基础只读 + 公共规则 + 供应商 + 采购类 + 库存查看 + 采购报表
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 2, id FROM sys_resource WHERE id IN (11,13,21,22,23,24,29,30,31,32,33,34,35);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 2, id FROM sys_resource WHERE id IN (15,37,38,43,44,45,46,47,48,49,50,51,52,53,54,55,56);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 2, id FROM sys_resource WHERE id IN (16,60,61);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 2, id FROM sys_resource WHERE id IN (17,66,68);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 2, id FROM sys_resource WHERE parent_id IN (21,22,23,24,29,30,31,32,33,34,35,37,38,43,44,45,46,47,48,49,50,51,52,53,54,55,56,60,61,66,68) AND type='button';

-- 销售员 ROLE_SALER：CRM客户 + 销售类 + 商品查看 + 销售/利润报表
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 3, id FROM sys_resource WHERE id IN (14,36,39,40,41);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 3, id FROM sys_resource WHERE id IN (15,43,52,53,54,55,56);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 3, id FROM sys_resource WHERE id IN (16,60);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 3, id FROM sys_resource WHERE id IN (17,67,71,72);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 3, id FROM sys_resource WHERE parent_id IN (36,39,40,41,43,52,53,54,55,56,60,67,71,72) AND type='button';

-- 库管 ROLE_WAREHOUSE：仓库 + 仓储全部 + 到货/出库 + 单据查看 + 库存报表
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 4, id FROM sys_resource WHERE id IN (11,25);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 4, id FROM sys_resource WHERE id IN (15,47,51,53,54);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 4, id FROM sys_resource WHERE id IN (16,57,58,59,60,61,62,63,64,65);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 4, id FROM sys_resource WHERE id IN (17,68);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 4, id FROM sys_resource WHERE parent_id IN (25,47,51,53,54,57,58,59,60,61,62,63,64,65,68) AND type='button';

-- 财务 ROLE_FINANCE：财务全部 + CRM对账/应收应付 + 财务/账龄报表 + 审计
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 5, id FROM sys_resource WHERE id IN (11,35);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 5, id FROM sys_resource WHERE id IN (14,39,41);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 5, id FROM sys_resource WHERE id IN (17,69,70,71);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 5, id FROM sys_resource WHERE id IN (19,75,76,77,78,79,80,81);
INSERT INTO sys_role_resource (role_id, resource_id) SELECT 5, id FROM sys_resource WHERE parent_id IN (35,39,41,69,70,71,75,76,77,78,79,80,81) AND type='button';
