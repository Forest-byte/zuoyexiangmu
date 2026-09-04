-- ============================================================
-- ERP 进销存一体化系统 数据库脚本
-- 数据库: erp (utf8mb4)
-- 说明: 所有表含通用字段 id/create_time/update_time/create_by/deleted
-- ============================================================
CREATE DATABASE IF NOT EXISTS erp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE erp;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 一、平台基座（组A）
-- ============================================================

CREATE TABLE sys_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录名',
  password VARCHAR(100) NOT NULL COMMENT '密码',
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  role_code VARCHAR(50) NOT NULL COMMENT '主角色',
  dept_id BIGINT NULL COMMENT '部门',
  employee_id BIGINT NULL COMMENT '员工',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='用户表';

CREATE TABLE sys_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
  name VARCHAR(50) NOT NULL COMMENT '角色名称',
  description VARCHAR(200) NULL COMMENT '描述',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='角色表';

CREATE TABLE sys_resource (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  parent_id BIGINT DEFAULT 0 COMMENT '父级资源',
  name VARCHAR(50) NOT NULL COMMENT '资源名',
  type VARCHAR(20) NOT NULL COMMENT 'menu/button',
  code VARCHAR(100) NULL COMMENT '权限码',
  path VARCHAR(200) NULL COMMENT '路由',
  icon VARCHAR(50) NULL COMMENT '图标',
  sort INT DEFAULT 0 COMMENT '排序',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='资源表(四级)';

CREATE TABLE sys_user_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '用户',
  role_id BIGINT NOT NULL COMMENT '角色',
  UNIQUE KEY uk_user_role (user_id, role_id),
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='用户角色关联';

CREATE TABLE sys_role_resource (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_id BIGINT NOT NULL COMMENT '角色',
  resource_id BIGINT NOT NULL COMMENT '资源',
  UNIQUE KEY uk_role_res (role_id, resource_id),
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='角色资源关联';

CREATE TABLE sys_region (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL COMMENT '地区名',
  parent_id BIGINT DEFAULT 0 COMMENT '父地区',
  sort INT DEFAULT 0 COMMENT '排序',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='地区表';

CREATE TABLE sys_company (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL COMMENT '公司名',
  code VARCHAR(20) UNIQUE COMMENT '公司编码',
  region_id BIGINT NULL COMMENT '所属地区',
  address VARCHAR(200) NULL COMMENT '地址',
  phone VARCHAR(20) NULL COMMENT '电话',
  status VARCHAR(20) DEFAULT '启用' COMMENT '启用/停用',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='分公司表';

CREATE TABLE sys_dept (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL COMMENT '部门名',
  code VARCHAR(20) UNIQUE COMMENT '部门编码',
  company_id BIGINT NOT NULL COMMENT '所属公司',
  manager VARCHAR(50) NULL COMMENT '负责人',
  phone VARCHAR(20) NULL COMMENT '电话',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='部门表';

CREATE TABLE sys_employee (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  code VARCHAR(20) UNIQUE COMMENT '工号',
  dept_id BIGINT NOT NULL COMMENT '部门',
  position VARCHAR(50) NULL COMMENT '岗位',
  phone VARCHAR(20) NULL COMMENT '电话',
  email VARCHAR(100) NULL COMMENT '邮箱',
  status VARCHAR(20) DEFAULT '在职' COMMENT '在职/离职',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='员工表';

CREATE TABLE sys_warehouse (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL COMMENT '仓库名',
  code VARCHAR(20) UNIQUE COMMENT '仓库编码',
  address VARCHAR(200) NULL COMMENT '地址',
  manager VARCHAR(50) NULL COMMENT '负责人',
  phone VARCHAR(20) NULL COMMENT '电话',
  status VARCHAR(20) DEFAULT '启用' COMMENT '启用/停用',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='仓库表';

CREATE TABLE sys_vehicle (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL COMMENT '车牌号',
  code VARCHAR(20) UNIQUE COMMENT '车辆编码',
  type VARCHAR(20) NULL COMMENT '车型',
  capacity VARCHAR(20) NULL COMMENT '载重',
  driver VARCHAR(50) NULL COMMENT '司机',
  phone VARCHAR(20) NULL COMMENT '司机电话',
  status VARCHAR(20) DEFAULT '空闲' COMMENT '空闲/在途/维修',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='车辆表';

CREATE TABLE sys_meeting (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL COMMENT '会议室名',
  capacity INT NULL COMMENT '容纳人数',
  status VARCHAR(20) DEFAULT '可用' COMMENT '可用/占用',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='会议室表';

CREATE TABLE sys_param (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  param_key VARCHAR(50) UNIQUE COMMENT '参数键',
  param_value VARCHAR(200) NULL COMMENT '参数值',
  description VARCHAR(200) NULL COMMENT '说明',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='系统参数表';

CREATE TABLE sys_dict (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  dict_type VARCHAR(50) NOT NULL COMMENT '字典类型',
  label VARCHAR(50) NOT NULL COMMENT '显示值',
  value VARCHAR(50) NOT NULL COMMENT '存储值',
  sort INT DEFAULT 0 COMMENT '排序',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='数据字典表';

CREATE TABLE approval_rule (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doc_type VARCHAR(50) NOT NULL COMMENT '单据类型',
  role_code VARCHAR(50) NOT NULL COMMENT '审批角色',
  level INT DEFAULT 1 COMMENT '审批层级',
  enabled TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='审批规则表';

CREATE TABLE sys_code_rule (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doc_type VARCHAR(50) NOT NULL COMMENT '单据类型',
  prefix VARCHAR(10) NOT NULL COMMENT '前缀',
  format VARCHAR(50) NOT NULL COMMENT '格式',
  seq_len INT DEFAULT 3 COMMENT '流水位数',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='编码规则表';

CREATE TABLE sys_audit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  operator VARCHAR(50) NOT NULL COMMENT '操作人',
  action VARCHAR(50) NOT NULL COMMENT '动作',
  target VARCHAR(100) NULL COMMENT '操作对象',
  before_data TEXT NULL COMMENT '变更前',
  after_data TEXT NULL COMMENT '变更后',
  time DATETIME NOT NULL COMMENT '时间',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='审计日志表';

-- ============================================================
-- 二、CRM 系统（组B）
-- ============================================================

CREATE TABLE crm_category (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  parent_id BIGINT DEFAULT 0 COMMENT '父分类',
  name VARCHAR(50) NOT NULL COMMENT '分类名',
  kind VARCHAR(20) NOT NULL COMMENT 'CUSTOMER/SUPPLIER',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='伙伴分类表';

CREATE TABLE crm_customer (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(20) UNIQUE COMMENT '客户编码',
  name VARCHAR(100) NOT NULL COMMENT '客户名称',
  category_id BIGINT NULL COMMENT '伙伴分类',
  linkman VARCHAR(50) NULL COMMENT '联系人',
  phone VARCHAR(20) NULL COMMENT '电话',
  address VARCHAR(200) NULL COMMENT '地址',
  credit_limit DECIMAL(18,2) DEFAULT 0 COMMENT '信用额度',
  used_credit DECIMAL(18,2) DEFAULT 0 COMMENT '已用额度',
  debt_amount DECIMAL(18,2) DEFAULT 0 COMMENT '欠款金额',
  status VARCHAR(20) DEFAULT '正常' COMMENT '正常/冻结/停用',
  approval_status VARCHAR(20) DEFAULT '草稿' COMMENT '草稿/待审批/已通过/已驳回',
  merge_from BIGINT NULL COMMENT '来源主客户',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='客户表';

CREATE TABLE crm_supplier (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(20) UNIQUE COMMENT '供应商编码',
  name VARCHAR(100) NOT NULL COMMENT '供应商名称',
  category_id BIGINT NULL COMMENT '伙伴分类',
  linkman VARCHAR(50) NULL COMMENT '联系人',
  phone VARCHAR(20) NULL COMMENT '电话',
  address VARCHAR(200) NULL COMMENT '地址',
  payable_amount DECIMAL(18,2) DEFAULT 0 COMMENT '应付累计',
  status VARCHAR(20) DEFAULT '正常' COMMENT '正常/冻结/停用',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='供应商表';

CREATE TABLE crm_contact (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  partner_type VARCHAR(20) NOT NULL COMMENT 'CUSTOMER/SUPPLIER',
  partner_id BIGINT NOT NULL COMMENT '伙伴ID',
  name VARCHAR(50) NOT NULL COMMENT '联系人',
  phone VARCHAR(20) NULL COMMENT '电话',
  email VARCHAR(100) NULL COMMENT '邮箱',
  is_default TINYINT DEFAULT 0 COMMENT '默认联系人',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='联系人表';

CREATE TABLE crm_follow_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT NOT NULL COMMENT '客户',
  content VARCHAR(500) NOT NULL COMMENT '跟进内容',
  next_time DATE NULL COMMENT '下次跟进日',
  recorder VARCHAR(50) NOT NULL COMMENT '记录人',
  record_time DATETIME NOT NULL COMMENT '记录时间',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='跟进记录表';

CREATE TABLE crm_arc_detail (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT NOT NULL COMMENT '客户',
  ref_type VARCHAR(20) NOT NULL COMMENT 'SALE/OTHER',
  ref_no VARCHAR(30) NOT NULL COMMENT '来源单号',
  amount DECIMAL(18,2) NOT NULL COMMENT '金额',
  received DECIMAL(18,2) DEFAULT 0 COMMENT '已核销',
  balance DECIMAL(18,2) DEFAULT 0 COMMENT '未核销',
  status VARCHAR(20) DEFAULT '未结清' COMMENT '未结清/部分/已结清',
  due_date DATE NULL COMMENT '到期日',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='应收明细表';

CREATE TABLE crm_ap_detail (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  supplier_id BIGINT NOT NULL COMMENT '供应商',
  ref_type VARCHAR(20) NOT NULL COMMENT 'PURCHASE/OTHER',
  ref_no VARCHAR(30) NOT NULL COMMENT '来源单号',
  amount DECIMAL(18,2) NOT NULL COMMENT '金额',
  paid DECIMAL(18,2) DEFAULT 0 COMMENT '已核销',
  balance DECIMAL(18,2) DEFAULT 0 COMMENT '未核销',
  status VARCHAR(20) DEFAULT '未结清' COMMENT '未结清/部分/已结清',
  due_date DATE NULL COMMENT '到期日',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='应付明细表';

CREATE TABLE crm_credit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT NOT NULL COMMENT '客户',
  change_amount DECIMAL(18,2) NOT NULL COMMENT '变动额',
  reason VARCHAR(200) NOT NULL COMMENT '原因',
  operator VARCHAR(50) NOT NULL COMMENT '操作人',
  operate_time DATETIME NOT NULL COMMENT '操作时间',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='信用变更日志表';

-- ============================================================
-- 三、进销存系统（组C）
-- ============================================================

CREATE TABLE goods_category (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  parent_id BIGINT DEFAULT 0 COMMENT '父分类',
  name VARCHAR(50) NOT NULL COMMENT '分类名',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='商品分类表';

CREATE TABLE goods_unit (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(20) NOT NULL COMMENT '单位名',
  rate DECIMAL(18,4) DEFAULT 1 COMMENT '换算率',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='计量单位表';

CREATE TABLE goods (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(30) UNIQUE COMMENT '商品编码',
  name VARCHAR(100) NOT NULL COMMENT '商品名称',
  category_id BIGINT NOT NULL COMMENT '分类',
  unit_id BIGINT NOT NULL COMMENT '单位',
  spec VARCHAR(50) NULL COMMENT '规格',
  brand VARCHAR(50) NULL COMMENT '品牌',
  barcode VARCHAR(50) NULL COMMENT '条码',
  purchase_price DECIMAL(18,2) NOT NULL COMMENT '进价',
  sale_price DECIMAL(18,2) NOT NULL COMMENT '售价',
  last_in_price DECIMAL(18,2) NULL COMMENT '上次进价',
  low_limit DECIMAL(18,3) DEFAULT 0 COMMENT '安全库存下限',
  high_limit DECIMAL(18,3) DEFAULT 0 COMMENT '安全库存上限',
  supplier_id BIGINT NULL COMMENT '常用供应商',
  is_raw TINYINT DEFAULT 0 COMMENT '是否原材料',
  status VARCHAR(20) DEFAULT '在售' COMMENT '在售/停售',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='商品表';

CREATE TABLE purchase_demand (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  demand_no VARCHAR(30) UNIQUE COMMENT '需求单号',
  goods_id BIGINT NOT NULL COMMENT '商品',
  quantity DECIMAL(18,3) NOT NULL COMMENT '需求数量',
  note VARCHAR(200) NULL COMMENT '备注',
  need_date DATE NULL COMMENT '需求日期',
  applicant VARCHAR(50) NOT NULL COMMENT '申请人',
  status VARCHAR(20) DEFAULT '待处理' COMMENT '待处理/已生成采购',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='采购需求表';

CREATE TABLE purchase_order (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(30) UNIQUE COMMENT '采购单号',
  supplier_id BIGINT NOT NULL COMMENT '供应商',
  apply_date DATE NOT NULL COMMENT '申请日期',
  all_amount DECIMAL(18,2) NOT NULL COMMENT '含税总金额',
  tax_rate DECIMAL(5,2) DEFAULT 0 COMMENT '税率%',
  tax_amount DECIMAL(18,2) DEFAULT 0 COMMENT '税额',
  status VARCHAR(20) DEFAULT '草稿' COMMENT '状态',
  audit_status VARCHAR(20) DEFAULT '未审核' COMMENT '未审核/已审核/已驳回',
  approve_person VARCHAR(50) NULL COMMENT '审批人',
  approve_time DATETIME NULL COMMENT '审批时间',
  order_states VARCHAR(50) NULL COMMENT '采购中/部分到货/结单',
  vehicle_id BIGINT NULL COMMENT '调度车辆',
  warehouse_id BIGINT NULL COMMENT '入库仓库',
  arrival_date DATE NULL COMMENT '预计到货日',
  remark VARCHAR(200) NULL COMMENT '备注',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='采购主表';

CREATE TABLE purchase_order_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL COMMENT '采购主表',
  goods_id BIGINT NOT NULL COMMENT '商品',
  quantity DECIMAL(18,3) NOT NULL COMMENT '数量',
  price DECIMAL(18,2) NOT NULL COMMENT '单价',
  amount DECIMAL(18,2) NOT NULL COMMENT '金额',
  received_qty DECIMAL(18,3) DEFAULT 0 COMMENT '已入库',
  remark VARCHAR(200) NULL COMMENT '备注',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='采购明细表';

CREATE TABLE approval_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doc_type VARCHAR(20) NOT NULL COMMENT 'PURCHASE/SALE/TRANSFER',
  doc_id BIGINT NOT NULL COMMENT '单据ID',
  level INT DEFAULT 1 COMMENT '层级',
  approver VARCHAR(50) NOT NULL COMMENT '审批人',
  result VARCHAR(20) NOT NULL COMMENT '通过/驳回',
  comment VARCHAR(200) NULL COMMENT '意见',
  approve_time DATETIME NOT NULL COMMENT '时间',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='审批记录表';

CREATE TABLE sale_order (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(30) UNIQUE COMMENT '销售单号',
  customer_id BIGINT NOT NULL COMMENT '客户',
  order_date DATE NOT NULL COMMENT '下单日期',
  all_amount DECIMAL(18,2) NOT NULL COMMENT '总额',
  discount DECIMAL(18,2) DEFAULT 0 COMMENT '折扣',
  received_amount DECIMAL(18,2) DEFAULT 0 COMMENT '已收款',
  status VARCHAR(20) DEFAULT '草稿' COMMENT '状态',
  audit_status VARCHAR(20) DEFAULT '未审核' COMMENT '审核状态',
  order_states VARCHAR(50) NULL COMMENT '流程阶段',
  warehouse_id BIGINT NULL COMMENT '发货仓库',
  delivery_date DATE NULL COMMENT '预计发货日',
  settle_status VARCHAR(20) DEFAULT '未结清' COMMENT '未结清/部分/已结清',
  settle_person VARCHAR(50) NULL COMMENT '结算人',
  remark VARCHAR(200) NULL COMMENT '备注',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='销售主表';

CREATE TABLE sale_order_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL COMMENT '销售主表',
  goods_id BIGINT NOT NULL COMMENT '商品',
  quantity DECIMAL(18,3) NOT NULL COMMENT '数量',
  price DECIMAL(18,2) NOT NULL COMMENT '单价',
  amount DECIMAL(18,2) NOT NULL COMMENT '金额',
  delivered_qty DECIMAL(18,3) DEFAULT 0 COMMENT '已出库',
  remark VARCHAR(200) NULL COMMENT '备注',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='销售明细表';

CREATE TABLE return_order (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  return_no VARCHAR(30) UNIQUE COMMENT '退货单号',
  src_type VARCHAR(20) NOT NULL COMMENT 'PURCHASE/SALE',
  src_id BIGINT NOT NULL COMMENT '来源单',
  partner_id BIGINT NOT NULL COMMENT '客户或供应商',
  reason VARCHAR(200) NOT NULL COMMENT '退货原因',
  amount DECIMAL(18,2) NOT NULL COMMENT '金额',
  status VARCHAR(20) DEFAULT '待审核' COMMENT '待审核/已通过/已驳回/已完成',
  return_date DATE NOT NULL COMMENT '退货日期',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='退货主表';

CREATE TABLE purchase_bill (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL COMMENT '采购单',
  bill_type VARCHAR(20) DEFAULT '增值税专用发票' COMMENT '票据类型',
  bill_no VARCHAR(50) NULL COMMENT '票据号码',
  amount DECIMAL(18,2) NOT NULL COMMENT '票面金额',
  file_url VARCHAR(200) NULL COMMENT '附件',
  register_time DATETIME NOT NULL COMMENT '登记时间',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='采购票据登记表';

CREATE TABLE follow_ups (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doc_type VARCHAR(20) NOT NULL COMMENT 'PURCHASE',
  doc_id BIGINT NOT NULL COMMENT '单据',
  node_name VARCHAR(50) NOT NULL COMMENT '节点',
  node_status VARCHAR(20) NOT NULL COMMENT '进行中/已完成',
  operator VARCHAR(50) NULL COMMENT '操作人',
  operate_time DATETIME NULL COMMENT '操作时间',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='跟单节点表';

-- ============================================================
-- 四、仓储系统（组D）
-- ============================================================

CREATE TABLE wms_inbound (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  in_no VARCHAR(30) UNIQUE COMMENT '入库单号',
  in_type VARCHAR(20) NOT NULL COMMENT 'PURCHASE/RETURN/OTHER',
  src_no VARCHAR(30) NULL COMMENT '来源单号',
  warehouse_id BIGINT NOT NULL COMMENT '入库仓库',
  in_date DATE NOT NULL COMMENT '入库日期',
  total_amount DECIMAL(18,2) DEFAULT 0 COMMENT '总金额',
  operator VARCHAR(50) NOT NULL COMMENT '入库人',
  status VARCHAR(20) DEFAULT '已入库' COMMENT '草稿/已入库/已审核',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='入库主表';

CREATE TABLE wms_inbound_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  inbound_id BIGINT NOT NULL COMMENT '入库主表',
  goods_id BIGINT NOT NULL COMMENT '商品',
  quantity DECIMAL(18,3) NOT NULL COMMENT '数量',
  price DECIMAL(18,2) NULL COMMENT '入库单价',
  amount DECIMAL(18,2) NULL COMMENT '金额',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='入库明细表';

CREATE TABLE wms_outbound (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  out_no VARCHAR(30) UNIQUE COMMENT '出库单号',
  out_type VARCHAR(20) NOT NULL COMMENT 'SALE/RETURN/OTHER',
  src_no VARCHAR(30) NULL COMMENT '来源单号',
  warehouse_id BIGINT NOT NULL COMMENT '出库仓库',
  out_date DATE NOT NULL COMMENT '出库日期',
  operator VARCHAR(50) NOT NULL COMMENT '出库人',
  status VARCHAR(20) DEFAULT '已出库' COMMENT '状态',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='出库主表';

CREATE TABLE wms_outbound_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  outbound_id BIGINT NOT NULL COMMENT '出库主表',
  goods_id BIGINT NOT NULL COMMENT '商品',
  quantity DECIMAL(18,3) NOT NULL COMMENT '数量',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='出库明细表';

CREATE TABLE wms_stock (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  warehouse_id BIGINT NOT NULL COMMENT '仓库',
  goods_id BIGINT NOT NULL COMMENT '商品',
  quantity DECIMAL(18,3) DEFAULT 0 COMMENT '当前库存',
  unit VARCHAR(20) NULL COMMENT '单位冗余',
  UNIQUE KEY uk_wh_goods (warehouse_id, goods_id),
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='商品库存表';

CREATE TABLE wms_stock_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  goods_id BIGINT NOT NULL COMMENT '商品',
  warehouse_id BIGINT NOT NULL COMMENT '仓库',
  change_type VARCHAR(20) NOT NULL COMMENT '采购入库/销售出库/盘点/调拨',
  change_qty DECIMAL(18,3) NOT NULL COMMENT '变动数量 正入负出',
  before_qty DECIMAL(18,3) NOT NULL COMMENT '变动前',
  after_qty DECIMAL(18,3) NOT NULL COMMENT '变动后',
  ref_no VARCHAR(30) NULL COMMENT '关联单号',
  operator VARCHAR(50) NOT NULL COMMENT '操作人',
  change_time DATETIME NOT NULL COMMENT '变动时间',
  KEY idx_goods_time (goods_id, change_time),
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='库存流水表';

CREATE TABLE wms_check (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  check_no VARCHAR(30) UNIQUE COMMENT '盘点单号',
  warehouse_id BIGINT NOT NULL COMMENT '盘点仓库',
  check_date DATE NOT NULL COMMENT '盘点日期',
  status VARCHAR(20) DEFAULT '草稿' COMMENT '草稿/盘点中/已完成',
  checker VARCHAR(50) NULL COMMENT '盘点人',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='盘点单主表';

CREATE TABLE wms_check_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  check_id BIGINT NOT NULL COMMENT '盘点单',
  goods_id BIGINT NOT NULL COMMENT '商品',
  book_qty DECIMAL(18,3) NOT NULL COMMENT '账面数',
  real_qty DECIMAL(18,3) NOT NULL COMMENT '实盘数',
  diff_qty DECIMAL(18,3) DEFAULT 0 COMMENT '差异数',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='盘点明细表';

CREATE TABLE wms_transfer (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  transfer_no VARCHAR(30) UNIQUE COMMENT '调拨单号',
  from_warehouse BIGINT NOT NULL COMMENT '调出仓库',
  to_warehouse BIGINT NOT NULL COMMENT '调入仓库',
  goods_id BIGINT NOT NULL COMMENT '商品',
  quantity DECIMAL(18,3) NOT NULL COMMENT '数量',
  status VARCHAR(20) DEFAULT '待出库' COMMENT '待出库/已出库/已完成',
  applicant VARCHAR(50) NOT NULL COMMENT '申请人',
  apply_time DATETIME NOT NULL COMMENT '申请时间',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='调拨单';

-- ============================================================
-- 五、财务系统（组D）
-- ============================================================

CREATE TABLE fin_account (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL COMMENT '账户名',
  account_no VARCHAR(50) NULL COMMENT '账号',
  begin_balance DECIMAL(18,2) DEFAULT 0 COMMENT '期初余额',
  balance DECIMAL(18,2) DEFAULT 0 COMMENT '当前余额',
  bank VARCHAR(50) NULL COMMENT '开户行',
  status VARCHAR(20) DEFAULT '启用' COMMENT '启用/停用',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='资金账户表';

CREATE TABLE fin_con_list (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  list_no VARCHAR(30) UNIQUE COMMENT '收/付款单号',
  list_type VARCHAR(20) NOT NULL COMMENT 'RECEIPT收/PAYMENT付',
  orders_key VARCHAR(30) UNIQUE COMMENT '关联单号',
  partner_id BIGINT NOT NULL COMMENT '客户或供应商',
  account_id BIGINT NOT NULL COMMENT '资金账户',
  all_money DECIMAL(18,2) NOT NULL COMMENT '金额',
  pay_type VARCHAR(50) NULL COMMENT '付款方式',
  receipt_date DATE NOT NULL COMMENT '收/付款日期',
  states VARCHAR(20) DEFAULT '草稿' COMMENT '草稿/已审核/已入账',
  payer VARCHAR(50) NULL COMMENT '收/付款人',
  order_amount DECIMAL(18,2) DEFAULT 0 COMMENT '关联单金额',
  is_dingdao VARCHAR(20) NULL COMMENT '到账标志',
  remark VARCHAR(200) NULL COMMENT '备注',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='收付款单主表';

CREATE TABLE fin_receipt_rel (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  list_id BIGINT NOT NULL COMMENT '收款单',
  arc_detail_id BIGINT NOT NULL COMMENT '应收明细',
  amount DECIMAL(18,2) NOT NULL COMMENT '核销金额',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='应收核销关联表';

CREATE TABLE fin_payable_rel (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  list_id BIGINT NOT NULL COMMENT '付款单',
  ap_detail_id BIGINT NOT NULL COMMENT '应付明细',
  amount DECIMAL(18,2) NOT NULL COMMENT '核销金额',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='应付核销关联表';

CREATE TABLE fin_transfer (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  transfer_no VARCHAR(30) UNIQUE COMMENT '转账单号',
  from_account BIGINT NOT NULL COMMENT '转出账户',
  to_account BIGINT NOT NULL COMMENT '转入账户',
  amount DECIMAL(18,2) NOT NULL COMMENT '金额',
  status VARCHAR(20) DEFAULT '待审批' COMMENT '待审批/已审批/已完成',
  applicant VARCHAR(50) NOT NULL COMMENT '申请人',
  apply_time DATETIME NOT NULL COMMENT '申请时间',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='转账单';

CREATE TABLE fin_account_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL COMMENT '账户',
  biz_type VARCHAR(20) NOT NULL COMMENT '收款/付款/转出/转入',
  ref_no VARCHAR(30) NULL COMMENT '关联单号',
  in_amount DECIMAL(18,2) DEFAULT 0 COMMENT '收入',
  out_amount DECIMAL(18,2) DEFAULT 0 COMMENT '支出',
  balance_after DECIMAL(18,2) NOT NULL COMMENT '变动后余额',
  biz_date DATE NOT NULL COMMENT '业务日期',
  operator VARCHAR(50) NOT NULL COMMENT '操作人',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='资金流水表';

-- ============================================================
-- 六、业务报表系统（组E）
-- ============================================================

CREATE TABLE rpt_snapshot (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rpt_code VARCHAR(30) NOT NULL COMMENT '报表编码',
  biz_date DATE NOT NULL COMMENT '业务日期',
  content JSON NULL COMMENT '快照数据',
  generate_time DATETIME NOT NULL COMMENT '生成时间',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='报表快照表';

-- ============================================================
-- 七、定时任务系统（组E）
-- ============================================================

CREATE TABLE job_task (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  job_code VARCHAR(30) UNIQUE COMMENT '任务编码',
  job_name VARCHAR(50) NOT NULL COMMENT '任务名',
  cron_expr VARCHAR(50) NOT NULL COMMENT '表达式',
  job_group VARCHAR(30) NULL COMMENT '分组',
  enabled TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  description VARCHAR(200) NULL COMMENT '描述',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='任务定义表';

CREATE TABLE job_task_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  job_id BIGINT NOT NULL COMMENT '任务',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NULL COMMENT '结束时间',
  result VARCHAR(20) NOT NULL COMMENT 'SUCCESS/FAIL',
  message VARCHAR(500) NULL COMMENT '执行信息',
  operator VARCHAR(50) NULL COMMENT '触发人',
  create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0
) COMMENT='任务执行日志表';

SET FOREIGN_KEY_CHECKS = 1;
