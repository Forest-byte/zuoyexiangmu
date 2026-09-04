---
AIGC:
    Label: "1"
    ContentProducer: 001191440300708461136T1XGW3
    ProduceID: c00b9e2277119ca47dfc451df575ea27_f32a5a1fa74111f1ac80525400aeaaa3
    ReservedCode1: 1rHZqlS3otiySNkCv5GCCd+LrzKp3PcfEUsHEwJoO0oLAHS7hnBeD7mfvocyeD6sMDAmNEJSg9avTI4eDjgBqgNeFkdq3iowT9If3c8n01XiipXPaWekJnVbUPJS5G4Vgc9qMeWN5aeVDgUjl8i9XsqLz3everhCOq5oWqtLjhs2NaQ4xEYA4kZ/LZ4=
    ContentPropagator: 001191440300708461136T1XGW3
    PropagateID: c00b9e2277119ca47dfc451df575ea27_f32a5a1fa74111f1ac80525400aeaaa3
    ReservedCode2: 1rHZqlS3otiySNkCv5GCCd+LrzKp3PcfEUsHEwJoO0oLAHS7hnBeD7mfvocyeD6sMDAmNEJSg9avTI4eDjgBqgNeFkdq3iowT9If3c8n01XiipXPaWekJnVbUPJS5G4Vgc9qMeWN5aeVDgUjl8i9XsqLz3everhCOq5oWqtLjhs2NaQ4xEYA4kZ/LZ4=
---

# ERP 系统开发文档

---

# 一、业务背景

## 1.1 项目背景
面向中小企业的进销存一体化 ERP 系统，覆盖主数据、客户关系(CRM)、采购、销售、仓储、财务、报表与定时任务七大子系统，实现**物流、资金流、信息流三流合一**。系统以现有纯前端原型（`C:\Users\34900\Downloads\原型页面\system\`）为界面与交互的**对标基准**，落地为真实前后端分离系统。

## 1.2 项目目标
1. 完整走一遍企业开发流程：需求 → 设计 → 开发 → 测试 → 上线
2. 交付可运行、可演示、可答辩的前后端分离 ERP 系统（7 大子系统、63 个功能点）
3. 界面交互对标原型：登录页、深蓝渐变主框架、9 大业务页面
4. 支持 5 类角色分权协作，全过程可审计、可追溯

## 1.3 技术栈与架构
| 类别 | 技术 | 版本 |
|---|---|---|
| 后端 | Java / Spring Boot / MyBatis | JDK 17 / 3.x / 3.5.x |
| 数据库 | MySQL | 8.0.x（utf8mb4） |
| 前端 | Vue / Element Plus / Vite / Axios / Pinia | 3.x / 2.x |
| 工具 | Git / Maven / Node | 稳定版 / 3.8+ / 18+ |

**架构链路**：浏览器 → Vue3 SPA（路由视图代替原型 iframe）→ RESTful API → Spring Boot 3.x（controller/service/mapper）→ MyBatis → MySQL。

## 1.4 内置账号与角色
| 账号 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 管理员（全权限） |
| purchase | 123456 | 采购员 |
| saler | 123456 | 销售员 |
| warehouse | 123456 | 库管 |
| finance | 123456 | 财务 |

## 1.5 通用约定
1. 统一返回 `{code, message, data}`；分页入参 `page/pageSize`，返回 `{total, list}`
2. 单据号规则：`前缀+yyyyMMdd+3位当日流水`（如 PO20260903001），全局唯一
3. 通用字段：`create_time / update_time / create_by / deleted(逻辑删除) / status`
4. 金额 `DECIMAL(18,2)`、库存 `DECIMAL(18,3)`，禁止浮点
5. 关键写操作写审计日志（sys_audit_log）
6. JWT 登录 + RBAC 权限，越权 403、未登录 401

---

# 二、功能梳理

## 2.1 基础维护系统（BM-01~10，负责人：组A）
| 编码 | 功能 | 说明 |
|---|---|---|
| BM-01 | 地区管理 | 树形结构、父子级维护 |
| BM-02 | 分公司管理 | 关联地区、编码唯一 |
| BM-03 | 部门管理 | 部门树、关联公司/负责人 |
| BM-04 | 员工管理 | 关联部门/岗位、账号绑定 |
| BM-05 | 角色维护 | 角色 CRUD、授权入口 |
| BM-06 | 资源维护 | 菜单/按钮四级资源定义 |
| BM-07 | 仓库管理 | 仓库档案、状态启停 |
| BM-08 | 员工-角色授权 | 用户与角色关联 |
| BM-09 | 用户登录/改密 | JWT 签发、密码策略 |
| BM-10 | 系统参数/车辆/会议室/编码规则/字典/审批规则/审计 | 公共配置与审计查询 |

## 2.2 CRM 系统（CRM-01~07，负责人：组B）
| 编码 | 功能 | 说明 |
|---|---|---|
| CRM-01 | 客户档案例建/维护 | 档案+分类+信用额度 |
| CRM-02 | 供应商档案例建/维护 | 档案+分类+应付累计 |
| CRM-03 | 伙伴分类维护 | 客户/供应商共用分类树 |
| CRM-04 | 信用管理 | 额度、可用额度、超限预警 |
| CRM-05 | 跟进记录 | 时间线、下次跟进提醒 |
| CRM-06 | 应收/应付对账 | 往来明细与销核状态 |
| CRM-07 | 客户合并 | 从客户并入主客户、历史单归属 |

## 2.3 进销存系统（INV-01~15，负责人：组C）
| 编码 | 功能 | 说明 |
|---|---|---|
| INV-01 | 商品档案维护 | SKU 粒度、编码唯一 |
| INV-02 | 商品分类维护 | 树形 |
| INV-03 | 计量单位维护 | 单位换算 |
| INV-04 | 采购需求 | 需求登记、汇总转单 |
| INV-05 | 采购单+审批 | 状态机+审批链 |
| INV-06 | 采购报备/票据 | 票据登记 |
| INV-07 | 车辆调度 | 单据分配车辆、状态流转 |
| INV-08 | 运输任务 | 创建/指派/签收 |
| INV-09 | 到货入库登记 | 触发 WMS 入库 |
| INV-10 | 采购结算 | 生成应付联动财务 |
| INV-11 | 采购跟单结单 | 节点跟踪、订单关闭 |
| INV-12 | 销售订单 | 订单登记+信用校验 |
| INV-13 | 销售单+审批 | 审批+出库联动 |
| INV-14 | 出库发货 | 触发 WMS 出库扣库存 |
| INV-15 | 销售回款/结单 | 应收核销、结单 |

## 2.4 仓储系统（WMS-01~09，负责人：组D）
| 编码 | 功能 | 说明 |
|---|---|---|
| WMS-01 | 入库单 | 采购到货/退货等来源 |
| WMS-02 | 出库单 | 销售/领用 |
| WMS-03 | 库存流水 | 每笔变动可追溯 |
| WMS-04 | 库存统计 | 仓库×商品实时查询 |
| WMS-05 | 库存上下限 | 上下限维护+预警 |
| WMS-06 | 盘点 | 盘点单+差异计算 |
| WMS-07 | 盘盈盘亏 | 差异审核调整 |
| WMS-08 | 货物转接 | 仓间调拨 |
| WMS-09 | 库位管理 | 库位维度扩展 |

## 2.5 业务报表系统（RPT-01~08，负责人：组E）
| 编码 | 功能 | 说明 |
|---|---|---|
| RPT-01 | 采购报表 | 按供应商/商品/时间 |
| RPT-02 | 销售报表 | 按客户/商品/时间 |
| RPT-03 | 库存报表 | 仓库×商品、低库存、周转 |
| RPT-04 | 财务收支报表 | 期初+收-支=期末 |
| RPT-05 | 应收应付账龄报表 | 按账龄区间 |
| RPT-06 | 利润汇总报表 | 毛利口径 |
| RPT-07 | 伙伴贡献度分析 | 客户/供应商贡献排行 |
| RPT-08 | 报表导出 | Excel 导出 |

## 2.6 定时任务系统（JOB-01~07，负责人：组E）
| 编码 | 功能 | 说明 |
|---|---|---|
| JOB-01 | 定时货物预警 | 扫描低于安全下限 |
| JOB-02 | 安全预警 | 临期/保质期预警 |
| JOB-03 | 常用商品频度统计 | T+1 聚合 |
| JOB-04 | 定时报表 | 预生成报表快照 |
| JOB-05 | 任务执行日志 | 成功/失败记录 |
| JOB-06 | 任务启停管理 | 开关动态生效 |
| JOB-07 | 数据归档/清理 | 过期单据归档 |

## 2.7 财务系统（FIN-01~07，负责人：组D）
| 编码 | 功能 | 说明 |
|---|---|---|
| FIN-01 | 资金账户管理 | 账户/期初余额/启停 |
| FIN-02 | 收款单 | 销收核销+账户入账 |
| FIN-03 | 付款单 | 采付核销+账户出账 |
| FIN-04 | 应收核销 | 收款关联应收冲抵 |
| FIN-05 | 应付核销 | 付款关联应付冲抵 |
| FIN-06 | 跨系统转账 | 账户间资金转移+审批 |
| FIN-07 | 资金流水 | 账户级全部变动查询 |

---

# 三、数据库设计

> 库名 erp，utf8mb4。所有表含通用字段：`id BIGINT AUTO_INCREMENT PRIMARY KEY, create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0`。以下字段表省略通用字段，仅列业务字段。

## 3.1 平台基座（组A）

### 表名：sys_user（用户表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| username | VARCHAR(50) | NOT NULL UNIQUE | 登录名 | admin/purchase/saler/warehouse/finance |
| password | VARCHAR(100) | NOT NULL | 密码 | 演示可明文，正式 BCrypt |
| name | VARCHAR(50) | NOT NULL | 姓名 | 界面显示 |
| role_code | VARCHAR(50) | NOT NULL | 主角色 | 关联 sys_role |
| dept_id | BIGINT | NULL | 部门 | 关联 sys_dept |
| employee_id | BIGINT | NULL | 员工 | 关联 sys_employee |
| status | TINYINT | DEFAULT 1 | 状态 | 1启用 0停用 |

### 表名：sys_role（角色表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| role_code | VARCHAR(50) | NOT NULL UNIQUE | 角色编码 | ROLE_ADMIN/ROLE_PURCHASE/ROLE_SALER/ROLE_WAREHOUSE/ROLE_FINANCE |
| name | VARCHAR(50) | NOT NULL | 角色名称 | 管理员/采购员/销售员/库管/财务 |
| description | VARCHAR(200) | NULL | 描述 | 可选 |

### 表名：sys_resource（资源表，四级）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| parent_id | BIGINT | DEFAULT 0 | 父级资源 | L1系统→L2模块→L3菜单→L4按钮 |
| name | VARCHAR(50) | NOT NULL | 资源名 | 如 基础维护 |
| type | VARCHAR(20) | NOT NULL | 类型 | menu/button |
| code | VARCHAR(100) | NULL | 权限码 | 如 base:region:add |
| path | VARCHAR(200) | NULL | 路由 | L3 菜单用 |
| icon | VARCHAR(50) | NULL | 图标 | 前端图标名 |
| sort | INT | DEFAULT 0 | 排序 | 菜单顺序 |

### 表名：sys_user_role（用户角色关联）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| user_id | BIGINT | NOT NULL | 用户 | 关联 sys_user |
| role_id | BIGINT | NOT NULL | 角色 | 关联 sys_role，唯一(user_id,role_id) |

### 表名：sys_role_resource（角色资源关联）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| role_id | BIGINT | NOT NULL | 角色 | 关联 sys_role |
| resource_id | BIGINT | NOT NULL | 资源 | 关联 sys_resource，唯一(role_id,resource_id) |

### 表名：sys_region（地区表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| name | VARCHAR(50) | NOT NULL | 地区名 | 华东地区/上海市 等 |
| parent_id | BIGINT | DEFAULT 0 | 父地区 | 树形 |

### 表名：sys_company（分公司表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| name | VARCHAR(50) | NOT NULL | 公司名 | 集团总部 等 |
| code | VARCHAR(20) | UNIQUE | 公司编码 | HQ/HD-MFG 等 |
| region_id | BIGINT | NULL | 所属地区 | 关联 sys_region |
| address | VARCHAR(200) | NULL | 地址 | — |
| phone | VARCHAR(20) | NULL | 电话 | — |
| status | VARCHAR(20) | DEFAULT 启用 | 状态 | 启用/停用 |

### 表名：sys_dept（部门表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| name | VARCHAR(50) | NOT NULL | 部门名 | 财务部/采购部 等 |
| code | VARCHAR(20) | UNIQUE | 部门编码 | D002 等 |
| company_id | BIGINT | NOT NULL | 所属公司 | 关联 sys_company |
| manager | VARCHAR(50) | NULL | 负责人 | — |
| phone | VARCHAR(20) | NULL | 电话 | — |

### 表名：sys_employee（员工表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| name | VARCHAR(50) | NOT NULL | 姓名 | 张伟 等 |
| code | VARCHAR(20) | UNIQUE | 工号 | E001 等 |
| dept_id | BIGINT | NOT NULL | 部门 | 关联 sys_dept |
| position | VARCHAR(50) | NULL | 岗位 | 总经理/采购主管 等 |
| phone | VARCHAR(20) | NULL | 电话 | — |
| email | VARCHAR(100) | NULL | 邮箱 | — |
| status | VARCHAR(20) | DEFAULT 在职 | 状态 | 在职/离职 |

### 表名：sys_warehouse（仓库表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| name | VARCHAR(50) | NOT NULL | 仓库名 | 上海一号库 等 |
| code | VARCHAR(20) | UNIQUE | 仓库编码 | WH001 等 |
| address | VARCHAR(200) | NULL | 地址 | — |
| manager | VARCHAR(50) | NULL | 负责人 | — |
| phone | VARCHAR(20) | NULL | 电话 | — |
| status | VARCHAR(20) | DEFAULT 启用 | 状态 | 启用/停用 |

### 表名：sys_vehicle（车辆表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| name | VARCHAR(50) | NOT NULL | 车牌号 | 沪A·8K216 |
| code | VARCHAR(20) | UNIQUE | 车辆编码 | V001 |
| type | VARCHAR(20) | NULL | 车型 | 厢式货车/冷藏车 等 |
| capacity | VARCHAR(20) | NULL | 载重 | 2吨 等 |
| driver | VARCHAR(50) | NULL | 司机 | — |
| phone | VARCHAR(20) | NULL | 司机电话 | — |
| status | VARCHAR(20) | DEFAULT 空闲 | 状态 | 空闲/在途/维修 |

### 表名：sys_meeting（会议室表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| name | VARCHAR(50) | NOT NULL | 会议室名 | — |
| capacity | INT | NULL | 容纳人数 | — |
| status | VARCHAR(20) | DEFAULT 可用 | 状态 | 可用/占用 |

### 表名：sys_param（系统参数表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| param_key | VARCHAR(50) | UNIQUE | 参数键 | 如 report.path |
| param_value | VARCHAR(200) | NULL | 参数值 | — |
| description | VARCHAR(200) | NULL | 说明 | — |

### 表名：sys_dict（数据字典表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| dict_type | VARCHAR(50) | NOT NULL | 字典类型 | 单据状态/客户状态 等 |
| label | VARCHAR(50) | NOT NULL | 显示值 | 草稿/待审批 等 |
| value | VARCHAR(50) | NOT NULL | 存储值 | DRAFT/PENDING 等 |
| sort | INT | DEFAULT 0 | 排序 | — |

### 表名：approval_rule（审批规则表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| doc_type | VARCHAR(50) | NOT NULL | 单据类型 | PURCHASE/SALE/RETURN/TRANSFER |
| role_code | VARCHAR(50) | NOT NULL | 审批角色 | 关联 sys_role |
| level | INT | DEFAULT 1 | 审批层级 | 预留多级 |
| enabled | TINYINT | DEFAULT 1 | 是否启用 | 1启用 0停用 |

### 表名：sys_code_rule（编码规则表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| doc_type | VARCHAR(50) | NOT NULL | 单据类型 | PO/SO/IN/OUT 等 |
| prefix | VARCHAR(10) | NOT NULL | 前缀 | — |
| format | VARCHAR(20) | NOT NULL | 格式 | {prefix}{yyyyMMdd}{seq} |
| seq_len | INT | DEFAULT 3 | 流水位数 | — |

### 表名：sys_audit_log（审计日志表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| operator | VARCHAR(50) | NOT NULL | 操作人 | 取自登录用户 |
| action | VARCHAR(50) | NOT NULL | 动作 | 登录系统/新增客户 等 |
| target | VARCHAR(100) | NULL | 操作对象 | 单据号/实体名 |
| before | TEXT | NULL | 变更前 | JSON |
| after | TEXT | NULL | 变更后 | JSON |
| time | DATETIME | NOT NULL | 时间 | — |

## 3.2 CRM 系统（组B）

### 表名：crm_customer（客户表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| code | VARCHAR(20) | UNIQUE | 客户编码 | C001 等 |
| name | VARCHAR(100) | NOT NULL | 客户名称 | 北京XX贸易有限公司 等 |
| category_id | BIGINT | NULL | 伙伴分类 | 关联 crm_category |
| linkman | VARCHAR(50) | NULL | 联系人 | — |
| phone | VARCHAR(20) | NULL | 电话 | — |
| address | VARCHAR(200) | NULL | 地址 | — |
| credit_limit | DECIMAL(18,2) | DEFAULT 0 | 信用额度 | — |
| used_credit | DECIMAL(18,2) | DEFAULT 0 | 已用额度 | 应收未核销累计 |
| debt_amount | DECIMAL(18,2) | DEFAULT 0 | 欠款金额 | 冗余便于对账 |
| status | VARCHAR(20) | DEFAULT 正常 | 状态 | 正常/冻结/停用 |
| approval_status | VARCHAR(20) | DEFAULT 草稿 | 审批状态 | 草稿/待审批/已通过/已驳回 |
| merge_from | BIGINT | NULL | 来源主客户 | 合并时记录 |

### 表名：crm_supplier（供应商表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| code | VARCHAR(20) | UNIQUE | 供应商编码 | S001 等 |
| name | VARCHAR(100) | NOT NULL | 供应商名称 | 上海XX机电有限公司 等 |
| category_id | BIGINT | NULL | 伙伴分类 | 关联 crm_category |
| linkman | VARCHAR(50) | NULL | 联系人 | — |
| phone | VARCHAR(20) | NULL | 电话 | — |
| address | VARCHAR(200) | NULL | 地址 | — |
| payable_amount | DECIMAL(18,2) | DEFAULT 0 | 应付累计 | 汇总未核销应付 |
| status | VARCHAR(20) | DEFAULT 正常 | 状态 | 正常/冻结/停用 |

### 表名：crm_category（伙伴分类表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| parent_id | BIGINT | DEFAULT 0 | 父分类 | 树形 |
| name | VARCHAR(50) | NOT NULL | 分类名 | 客户分类/供应商分类 下挂 |
| kind | VARCHAR(20) | NOT NULL | 类别 | CUSTOMER/SUPPLIER |

### 表名：crm_contact（联系人表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| partner_type | VARCHAR(20) | NOT NULL | 伙伴类型 | CUSTOMER/SUPPLIER |
| partner_id | BIGINT | NOT NULL | 伙伴ID | 关联客户或供应商 |
| name | VARCHAR(50) | NOT NULL | 联系人 | — |
| phone | VARCHAR(20) | NULL | 电话 | — |
| email | VARCHAR(100) | NULL | 邮箱 | — |
| is_default | TINYINT | DEFAULT 0 | 默认联系人 | 1是 0否 |

### 表名：crm_follow_record（跟进记录表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| customer_id | BIGINT | NOT NULL | 客户 | 关联 crm_customer |
| content | VARCHAR(500) | NOT NULL | 跟进内容 | — |
| next_time | DATE | NULL | 下次跟进日 | 超期提醒 |
| recorder | VARCHAR(50) | NOT NULL | 记录人 | 当前登录人 |
| record_time | DATETIME | NOT NULL | 记录时间 | — |

### 表名：crm_arc_detail（应收明细表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| customer_id | BIGINT | NOT NULL | 客户 | 关联 crm_customer |
| ref_type | VARCHAR(20) | NOT NULL | 来源 | SALE/OTHER |
| ref_no | VARCHAR(30) | NOT NULL | 来源单号 | 销售单号 |
| amount | DECIMAL(18,2) | NOT NULL | 金额 | — |
| received | DECIMAL(18,2) | DEFAULT 0 | 已核销 | — |
| balance | DECIMAL(18,2) | DEFAULT 0 | 未核销 | amount-received |
| status | VARCHAR(20) | DEFAULT 未结清 | 状态 | 未结清/部分/已结清 |
| due_date | DATE | NULL | 到期日 | 账龄计算 |

### 表名：crm_ap_detail（应付明细表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| supplier_id | BIGINT | NOT NULL | 供应商 | 关联 crm_supplier |
| ref_type | VARCHAR(20) | NOT NULL | 来源 | PURCHASE/OTHER |
| ref_no | VARCHAR(30) | NOT NULL | 来源单号 | 采购单号 |
| amount | DECIMAL(18,2) | NOT NULL | 金额 | — |
| paid | DECIMAL(18,2) | DEFAULT 0 | 已核销 | — |
| balance | DECIMAL(18,2) | DEFAULT 0 | 未核销 | — |
| status | VARCHAR(20) | DEFAULT 未结清 | 状态 | 未结清/部分/已结清 |
| due_date | DATE | NULL | 到期日 | 账龄计算 |

### 表名：crm_credit_log（信用变更日志表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| customer_id | BIGINT | NOT NULL | 客户 | — |
| change_amount | DECIMAL(18,2) | NOT NULL | 变动额 | 正增负减 |
| reason | VARCHAR(200) | NOT NULL | 原因 | — |
| operator | VARCHAR(50) | NOT NULL | 操作人 | — |
| operate_time | DATETIME | NOT NULL | 操作时间 | — |

## 3.3 进销存系统（组C）

### 表名：goods_category（商品分类表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| parent_id | BIGINT | DEFAULT 0 | 父分类 | 树形 |
| name | VARCHAR(50) | NOT NULL | 分类名 | 食品/饮料/酒类 等 |

### 表名：goods_unit（计量单位表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| name | VARCHAR(20) | NOT NULL | 单位名 | 件/箱/公斤 等 |
| rate | DECIMAL(18,4) | DEFAULT 1 | 换算率 | 相对基准单位 |

### 表名：goods（商品表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| code | VARCHAR(30) | UNIQUE | 商品编码 | G001 等 |
| name | VARCHAR(100) | NOT NULL | 商品名称 | 蒙牛纯牛奶 250ml 等 |
| category_id | BIGINT | NOT NULL | 分类 | 关联 goods_category |
| unit_id | BIGINT | NOT NULL | 单位 | 关联 goods_unit |
| spec | VARCHAR(50) | NULL | 规格 | 24盒/箱 |
| brand | VARCHAR(50) | NULL | 品牌 | — |
| barcode | VARCHAR(50) | NULL | 条码 | — |
| purchase_price | DECIMAL(18,2) | NOT NULL | 进价 | — |
| sale_price | DECIMAL(18,2) | NOT NULL | 售价 | — |
| last_in_price | DECIMAL(18,2) | NULL | 上次进价 | 采购参考 |
| low_limit | DECIMAL(18,3) | DEFAULT 0 | 安全库存下限 | 低于则预警 |
| high_limit | DECIMAL(18,3) | DEFAULT 0 | 安全库存上限 | — |
| supplier_id | BIGINT | NULL | 常用供应商 | 关联 crm_supplier |
| is_raw | TINYINT | DEFAULT 0 | 是否原材料 | 采购分类口径 |
| status | VARCHAR(20) | DEFAULT 在售 | 状态 | 在售/停售 |

### 表名：purchase_demand（采购需求表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| demand_no | VARCHAR(30) | UNIQUE | 需求单号 | PD+日期+流水 |
| goods_id | BIGINT | NOT NULL | 商品 | — |
| quantity | DECIMAL(18,3) | NOT NULL | 需求数量 | — |
| note | VARCHAR(200) | NULL | 备注 | 需求原因 |
| need_date | DATE | NULL | 需求日期 | — |
| applicant | VARCHAR(50) | NOT NULL | 申请人 | — |
| status | VARCHAR(20) | DEFAULT 待处理 | 状态 | 待处理/已生成采购 |

### 表名：purchase_order（采购主表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| order_no | VARCHAR(30) | UNIQUE | 采购单号 | PO+日期+流水 |
| supplier_id | BIGINT | NOT NULL | 供应商 | 关联 crm_supplier |
| apply_date | DATE | NOT NULL | 申请日期 | — |
| all_amount | DECIMAL(18,2) | NOT NULL | 含税总金额 | 明细汇总 |
| tax_rate | DECIMAL(5,2) | DEFAULT 0 | 税率 | % |
| tax_amount | DECIMAL(18,2) | DEFAULT 0 | 税额 | — |
| status | VARCHAR(20) | DEFAULT 草稿 | 状态 | 见状态机 |
| audit_status | VARCHAR(20) | DEFAULT 未审核 | 审核状态 | 未审核/已审核/已驳回 |
| approve_person | VARCHAR(50) | NULL | 审批人 | — |
| approve_time | DATETIME | NULL | 审批时间 | — |
| order_states | VARCHAR(50) | NULL | 单据状态 | 采购中/部分到货/结单 等 |
| vehicle_id | BIGINT | NULL | 调度车辆 | 关联 sys_vehicle |
| warehouse_id | BIGINT | NULL | 入库仓库 | 关联 sys_warehouse |
| arrival_date | DATE | NULL | 预计到货日 | — |
| remark | VARCHAR(200) | NULL | 备注 | — |

### 表名：purchase_order_item（采购明细表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| order_id | BIGINT | NOT NULL | 采购主表 | 关联 purchase_order |
| goods_id | BIGINT | NOT NULL | 商品 | — |
| quantity | DECIMAL(18,3) | NOT NULL | 数量 | — |
| price | DECIMAL(18,2) | NOT NULL | 单价 | — |
| amount | DECIMAL(18,2) | NOT NULL | 金额 | quantity*price |
| received_qty | DECIMAL(18,3) | DEFAULT 0 | 已入库 | 到货登记回写 |
| remark | VARCHAR(200) | NULL | 备注 | — |

### 表名：approval_record（审批记录表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| doc_type | VARCHAR(20) | NOT NULL | 单据类型 | PURCHASE/SALE/TRANSFER |
| doc_id | BIGINT | NOT NULL | 单据ID | 关联各主表 |
| level | INT | DEFAULT 1 | 层级 | — |
| approver | VARCHAR(50) | NOT NULL | 审批人 | — |
| result | VARCHAR(20) | NOT NULL | 结果 | 通过/驳回 |
| comment | VARCHAR(200) | NULL | 意见 | — |
| approve_time | DATETIME | NOT NULL | 时间 | — |

### 表名：sale_order（销售主表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| order_no | VARCHAR(30) | UNIQUE | 销售单号 | SO+日期+流水 |
| customer_id | BIGINT | NOT NULL | 客户 | 关联 crm_customer |
| order_date | DATE | NOT NULL | 下单日期 | — |
| all_amount | DECIMAL(18,2) | NOT NULL | 总额 | — |
| discount | DECIMAL(18,2) | DEFAULT 0 | 折扣 | — |
| received_amount | DECIMAL(18,2) | DEFAULT 0 | 已收款 | 回款回写 |
| status | VARCHAR(20) | DEFAULT 草稿 | 状态 | 见状态机 |
| audit_status | VARCHAR(20) | DEFAULT 未审核 | 审核状态 | — |
| warehouse_id | BIGINT | NULL | 发货仓库 | — |
| delivery_date | DATE | NULL | 预计发货日 | — |
| settle_status | VARCHAR(20) | DEFAULT 未结清 | 结算状态 | 未结清/部分/已结清 |
| settle_person | VARCHAR(50) | NULL | 结算人 | — |
| remark | VARCHAR(200) | NULL | 备注 | — |

### 表名：sale_order_item（销售明细表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| order_id | BIGINT | NOT NULL | 销售主表 | — |
| goods_id | BIGINT | NOT NULL | 商品 | — |
| quantity | DECIMAL(18,3) | NOT NULL | 数量 | — |
| price | DECIMAL(18,2) | NOT NULL | 单价 | — |
| amount | DECIMAL(18,2) | NOT NULL | 金额 | — |
| delivered_qty | DECIMAL(18,3) | DEFAULT 0 | 已出库 | — |
| remark | VARCHAR(200) | NULL | 备注 | — |

### 表名：return_order（退货主表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| return_no | VARCHAR(30) | UNIQUE | 退货单号 | — |
| src_type | VARCHAR(20) | NOT NULL | 来源 | PURCHASE/SALE |
| src_id | BIGINT | NOT NULL | 来源单 | — |
| partner_id | BIGINT | NOT NULL | 往来方 | 客户或供应商 |
| reason | VARCHAR(200) | NOT NULL | 退货原因 | — |
| amount | DECIMAL(18,2) | NOT NULL | 金额 | — |
| status | VARCHAR(20) | DEFAULT 待审核 | 状态 | 待审核/已通过/已驳回/已完成 |
| return_date | DATE | NOT NULL | 退货日期 | — |

### 表名：purchase_bill（采购票据登记表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| order_id | BIGINT | NOT NULL | 采购单 | 关联 purchase_order |
| bill_type | VARCHAR(20) | DEFAULT 增值税专用发票 | 票据类型 | — |
| bill_no | VARCHAR(50) | NULL | 票据号码 | — |
| amount | DECIMAL(18,2) | NOT NULL | 票面金额 | — |
| file_url | VARCHAR(200) | NULL | 附件 | 上传文件路径 |
| register_time | DATETIME | NOT NULL | 登记时间 | — |

### 表名：follow_ups（跟单节点表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| doc_type | VARCHAR(20) | NOT NULL | 单据类型 | PURCHASE |
| doc_id | BIGINT | NOT NULL | 单据 | — |
| node_name | VARCHAR(50) | NOT NULL | 节点 | 下单/发货/到货/结算 |
| node_status | VARCHAR(20) | NOT NULL | 状态 | 进行中/已完成 |
| operator | VARCHAR(50) | NULL | 操作人 | — |
| operate_time | DATETIME | NULL | 操作时间 | — |

## 3.4 仓储系统（组D）

### 表名：wms_inbound（入库主表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| in_no | VARCHAR(30) | UNIQUE | 入库单号 | IN+日期+流水 |
| in_type | VARCHAR(20) | NOT NULL | 入库类型 | PURCHASE采购到货/RETURN退货入库/OTHER |
| src_no | VARCHAR(30) | NULL | 来源单号 | 采购单号/退货单号 |
| warehouse_id | BIGINT | NOT NULL | 入库仓库 | 关联 sys_warehouse |
| in_date | DATE | NOT NULL | 入库日期 | — |
| total_amount | DECIMAL(18,2) | DEFAULT 0 | 总金额 | — |
| operator | VARCHAR(50) | NOT NULL | 入库人 | — |
| status | VARCHAR(20) | DEFAULT 已入库 | 状态 | 草稿/已入库/已审核 |

### 表名：wms_inbound_item（入库明细表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| inbound_id | BIGINT | NOT NULL | 入库主表 | — |
| goods_id | BIGINT | NOT NULL | 商品 | — |
| quantity | DECIMAL(18,3) | NOT NULL | 数量 | — |
| price | DECIMAL(18,2) | NULL | 入库单价 | 采购价 |
| amount | DECIMAL(18,2) | NULL | 金额 | — |

### 表名：wms_outbound（出库主表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| out_no | VARCHAR(30) | UNIQUE | 出库单号 | OUT+日期+流水 |
| out_type | VARCHAR(20) | NOT NULL | 出库类型 | SALE销售出库/RETURN退货出库/OTHER |
| src_no | VARCHAR(30) | NULL | 来源单号 | 销售单号/退货单号 |
| warehouse_id | BIGINT | NOT NULL | 出库仓库 | — |
| out_date | DATE | NOT NULL | 出库日期 | — |
| operator | VARCHAR(50) | NOT NULL | 出库人 | — |
| status | VARCHAR(20) | DEFAULT 已出库 | 状态 | — |

### 表名：wms_outbound_item（出库明细表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| outbound_id | BIGINT | NOT NULL | 出库主表 | — |
| goods_id | BIGINT | NOT NULL | 商品 | — |
| quantity | DECIMAL(18,3) | NOT NULL | 数量 | — |

### 表名：wms_stock（商品库存表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| warehouse_id | BIGINT | NOT NULL | 仓库 | 关联 sys_warehouse |
| goods_id | BIGINT | NOT NULL | 商品 | 关联 goods |
| quantity | DECIMAL(18,3) | DEFAULT 0 | 当前库存 | 唯一(warehouse_id,goods_id) |
| unit | VARCHAR(20) | NULL | 单位冗余 | — |
| update_time | DATETIME | NULL | 更新时间 | — |

### 表名：wms_stock_log（库存流水表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| goods_id | BIGINT | NOT NULL | 商品 | — |
| warehouse_id | BIGINT | NOT NULL | 仓库 | — |
| change_type | VARCHAR(20) | NOT NULL | 变动类型 | 采购入库/销售出库/盘点/调拨 等 |
| change_qty | DECIMAL(18,3) | NOT NULL | 变动数量 | 正入负出 |
| before_qty | DECIMAL(18,3) | NOT NULL | 变动前 | — |
| after_qty | DECIMAL(18,3) | NOT NULL | 变动后 | — |
| ref_no | VARCHAR(30) | NULL | 关联单号 | — |
| operator | VARCHAR(50) | NOT NULL | 操作人 | — |
| change_time | DATETIME | NOT NULL | 变动时间 | 索引优化查询 |

### 表名：wms_check（盘点单主表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| check_no | VARCHAR(30) | UNIQUE | 盘点单号 | — |
| warehouse_id | BIGINT | NOT NULL | 盘点仓库 | — |
| check_date | DATE | NOT NULL | 盘点日期 | — |
| status | VARCHAR(20) | DEFAULT 草稿 | 状态 | 草稿/盘点中/已完成 |
| checker | VARCHAR(50) | NULL | 盘点人 | — |

### 表名：wms_check_item（盘点明细表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| check_id | BIGINT | NOT NULL | 盘点单 | — |
| goods_id | BIGINT | NOT NULL | 商品 | — |
| book_qty | DECIMAL(18,3) | NOT NULL | 账面数 | 当前库存 |
| real_qty | DECIMAL(18,3) | NOT NULL | 实盘数 | — |
| diff_qty | DECIMAL(18,3) | DEFAULT 0 | 差异数 | 盈正亏负 |

### 表名：wms_transfer（调拨单）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| transfer_no | VARCHAR(30) | UNIQUE | 调拨单号 | — |
| from_warehouse | BIGINT | NOT NULL | 调出仓库 | — |
| to_warehouse | BIGINT | NOT NULL | 调入仓库 | — |
| goods_id | BIGINT | NOT NULL | 商品 | — |
| quantity | DECIMAL(18,3) | NOT NULL | 数量 | — |
| status | VARCHAR(20) | DEFAULT 待出库 | 状态 | 待出库/已出库/已完成 |
| applicant | VARCHAR(50) | NOT NULL | 申请人 | — |
| apply_time | DATETIME | NOT NULL | 申请时间 | — |

## 3.5 财务系统（组D）

### 表名：fin_account（资金账户表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| name | VARCHAR(50) | NOT NULL | 账户名 | 公司银行账号/微信/支付宝 等 |
| account_no | VARCHAR(50) | NULL | 账号 | — |
| begin_balance | DECIMAL(18,2) | DEFAULT 0 | 期初余额 | — |
| balance | DECIMAL(18,2) | DEFAULT 0 | 当前余额 | — |
| bank | VARCHAR(50) | NULL | 开户行 | — |
| status | VARCHAR(20) | DEFAULT 启用 | 状态 | 启用/停用 |

### 表名：fin_con_list（收付款单主表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| list_no | VARCHAR(30) | UNIQUE | 收/付款单号 | — |
| list_type | VARCHAR(20) | NOT NULL | 类型 | RECEIPT收/PAYMENT付 |
| orders_key | VARCHAR(30) | UNIQUE | 关联单号 | 销售单/采购单 |
| partner_id | BIGINT | NOT NULL | 往来方 | 客户或供应商 |
| account_id | BIGINT | NOT NULL | 资金账户 | 关联 fin_account |
| all_money | DECIMAL(18,2) | NOT NULL | 金额 | — |
| pay_type | VARCHAR(50) | NULL | 付款方式 | 银行转账/现金 等 |
| receipt_date | DATE | NOT NULL | 收/付款日期 | — |
| states | VARCHAR(20) | DEFAULT 草稿 | 状态 | 草稿/已审核/已入账 |
| payer | VARCHAR(50) | NULL | 交款人 | 收/付款人 |
| order_amount | DECIMAL(18,2) | DEFAULT 0 | 关联单金额 | 回填核对 |
| is_dingdao | VARCHAR(20) | NULL | 到账标志 | — |
| remark | VARCHAR(200) | NULL | 备注 | — |

### 表名：fin_receipt_rel（应收核销关联表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| list_id | BIGINT | NOT NULL | 收款单 | 关联 fin_con_list |
| arc_detail_id | BIGINT | NOT NULL | 应收明细 | 关联 crm_arc_detail |
| amount | DECIMAL(18,2) | NOT NULL | 核销金额 | — |

### 表名：fin_payable_rel（应付核销关联表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| list_id | BIGINT | NOT NULL | 付款单 | 关联 fin_con_list |
| ap_detail_id | BIGINT | NOT NULL | 应付明细 | 关联 crm_ap_detail |
| amount | DECIMAL(18,2) | NOT NULL | 核销金额 | — |

### 表名：fin_transfer（转账单）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| transfer_no | VARCHAR(30) | UNIQUE | 转账单号 | — |
| from_account | BIGINT | NOT NULL | 转出账户 | — |
| to_account | BIGINT | NOT NULL | 转入账户 | — |
| amount | DECIMAL(18,2) | NOT NULL | 金额 | — |
| status | VARCHAR(20) | DEFAULT 待审批 | 状态 | 待审批/已审批/已完成 |
| applicant | VARCHAR(50) | NOT NULL | 申请人 | — |
| apply_time | DATETIME | NOT NULL | 申请时间 | — |

### 表名：fin_account_log（资金流水表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| account_id | BIGINT | NOT NULL | 账户 | — |
| biz_type | VARCHAR(20) | NOT NULL | 业务类型 | 收款/付款/转出/转入 等 |
| ref_no | VARCHAR(30) | NULL | 关联单号 | — |
| in_amount | DECIMAL(18,2) | DEFAULT 0 | 收入 | — |
| out_amount | DECIMAL(18,2) | DEFAULT 0 | 支出 | — |
| balance_after | DECIMAL(18,2) | NOT NULL | 变动后余额 | — |
| biz_date | DATE | NOT NULL | 业务日期 | — |
| operator | VARCHAR(50) | NOT NULL | 操作人 | — |

## 3.6 业务报表系统（组E）

### 表名：rpt_snapshot（报表快照表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| rpt_code | VARCHAR(30) | NOT NULL | 报表编码 | RPT_INVENTORY 等 |
| biz_date | DATE | NOT NULL | 业务日期 | — |
| content | JSON | NULL | 快照数据 | 预聚合结果 |
| generate_time | DATETIME | NOT NULL | 生成时间 | — |

## 3.7 定时任务系统（组E）

### 表名：job_task（任务定义表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| job_code | VARCHAR(30) | UNIQUE | 任务编码 | job_stock_warning |
| job_name | VARCHAR(50) | NOT NULL | 任务名 | 定时货物预警 |
| cron_expr | VARCHAR(50) | NOT NULL | 表达式 | 0 0 8 * * ? 等 |
| job_group | VARCHAR(30) | NULL | 分组 | STOCK/REPORT 等 |
| enabled | TINYINT | DEFAULT 1 | 是否启用 | 1启用 0停用 |
| description | VARCHAR(200) | NULL | 描述 | — |

### 表名：job_task_log（任务执行日志表）
| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
| ------ | -------- | ---- | ---- | ---- |
| job_id | BIGINT | NOT NULL | 任务 | 关联 job_task |
| start_time | DATETIME | NOT NULL | 开始时间 | — |
| end_time | DATETIME | NULL | 结束时间 | — |
| result | VARCHAR(20) | NOT NULL | 结果 | SUCCESS/FAIL |
| message | VARCHAR(500) | NULL | 执行信息 | 错误/结果摘要 |
| operator | VARCHAR(50) | NULL | 触发人 | 手动触发时 |

---

# 四、业务逻辑

> 业务逻辑贯穿通用链条：**生成单据号 → 校验 → 保存（事务） → 审批 → 联动上下游 → 回写状态 → 写审计日志**。以下按子系统逐功能说明实现要点。

## 4.1 基础维护系统（组A）

### 4.1.1 地区管理（BM-01）
- 树形 `parent_id=0` 为根，递归查询子节点，`sort` 排序
- 新增/编辑：校验同级名称唯一；删除：存在子地区或已被公司引用时禁止

### 4.1.2 分公司管理（BM-02）
- 编码 `code` 全局唯一；选择地区下拉（树）；状态启用/停用控制登录归属
- 删除校验：被部门引用或有员工归属时禁止，改为停用

### 4.1.3 部门管理（BM-03）
- 按公司维度展示部门树；记录负责人；停用部门禁止新增员工归属

### 4.1.4 员工管理（BM-04）
- 工号唯一；保存时同步插入/更新 `sys_user` 账号（初始密码 123456）实现"建员工=开账号"
- 可勾选分配一个或多个角色，写入 `sys_user_role`（默认给基础角色）

### 4.1.5 角色维护（BM-05）
- 角色 CRUD；点击"授权"进入资源树勾选，保存写 `sys_role_resource`
- admin 内置全权限，禁止删除；删除角色需无用户引用

### 4.1.6 资源维护（BM-06）
- 四级资源树：L1系统 → L2模块 → L3菜单 → L4按钮（权限码如 `base:region:list`）
- 前端登录后按权限码过滤按钮显隐；后端注解 `@PreAuthorize` 二次拦截

### 4.1.7 仓库管理（BM-07）
- 仓库档案；停用仓库禁止入库/出库；初始化种子 6 条（WH001~WH006）

### 4.1.8 用户-角色授权（BM-08）
- 用户列表关联角色；授权变更写审计日志；登录态缓存按需刷新

### 4.1.9 登录/改密（BM-09）
- 登录：用户名+密码校验 → 生成 JWT（含 userId/username/roles）→ 写审计"登录系统"
- 改密：校验旧密码 → 新密码强度（≥6 位含字母数字）→ 更新并提示重新登录

### 4.1.10 公共配置（BM-10）
- 系统参数：键值对维护，供报表路径等使用（读写分离缓存）
- 字典维护：`dict_type` 统一各单据状态下拉
- 车辆/会议室：启停状态维护，供运输调度与资源查询引用
- 审批规则：配置各单据类型审批角色与层级，供进销存联动
- 编码规则：配置各单据前缀/格式/流水位，生成时段内串行加锁
- 审计查询：按操作人/动作/时间范围分页查询 `sys_audit_log`

## 4.2 CRM 系统（组B）

### 4.2.1 客户档案例建/维护（CRM-01）
- 新增：编码自动生成（`C+流水`）也可手输，名称/分类/信用额度必填
- 提交后进入审批流（`approval_status`：草稿→待审批→已通过/已驳回），驳回可修改重提
- 编辑仅限已通过或草稿；已通过后修改需重提审批（除备注等次要字段）
- 冻结客户：禁止继续下单，已有单据不受影响

### 4.2.2 供应商档案例建/维护（CRM-02）
- 档案维护 + 应付累计 `payable_amount` 由付款核销回写
- 停用供应商：禁止新增采购单

### 4.2.3 伙伴分类维护（CRM-03）
- 客户与供应商共用分类树，`kind` 区分；删除前校验分类下无伙伴

### 4.2.4 信用管理（CRM-04）
- 客户表冗余 `used_credit`：新增待结清销售单时累加，回款/结清时扣减
- 可用额度 = 信用额度 - 已用额度；下单时校验剩余可用额度 > 0，否则拦截并提示
- 变更信用额度/额度超限写 `crm_credit_log`

### 4.2.5 跟进记录（CRM-05）
- 按客户时间线展示；`next_time` 早于今天的记录在首页待办高亮
- 记录人自动取当前登录用户

### 4.2.6 应收/应付对账（CRM-06）
- 按伙伴汇总收/应付明细表，展示 期初→发生→核销→余额
- 未结清/部分/已结清三种状态；结清后明细关闭

### 4.2.7 客户合并（CRM-07）
- 选择从客户与主客户：将从客户单据、应收明细、跟进记录转移至主客户
- 从客户标记停用并记录 `merge_from`，过程写审计

## 4.3 进销存系统（组C）

### 4.3.1 商品档案维护（INV-01）
- 编码唯一；进价/售价必填；保存后 `goods` 与库存表初始化为 0
- 状态在售/停售；停售商品禁止新增销售单明细

### 4.3.2 商品分类/计量单位（INV-02、03）
- 分类树维护；单位维护换算率，多单位展示换算

### 4.3.3 采购需求（INV-04）
- 需求登记（商品/数量/需求日期/原因）；"生成采购单"按需求聚合同供应商生成 PO
- 转单后需求状态置为"已生成采购"，防止重复转单

### 4.3.4 采购单+审批（INV-05）
- 采购单状态机：`草稿 → 待审批 → 已通过(采购审批) → 采购中 → 部分到货 → 已到货 → 结算 → 结单`；驳回回草稿
- 提交审批：按 `approval_rule` 写入 `approval_record`，审批通过更新 `audit_status=已审核、approve_person/approve_time`（试算当前用户即走"自审"便于演示）
- 通过后自动在 `wms_inbound` 侧登记待入库线索，并给供应商追加应付明细 `crm_ap_detail`

### 4.3.5 采购报备/票据（INV-06）
- 对采购单登记票据类型/号码/票面金额/附件，回写票据登记表

### 4.3.6 车辆调度（INV-07）
- 选中采购单分配 `vehicle_id` 并置车辆状态"在途"；卸货/结单后置回"空闲"
- 同车可挂多单，显示车辆当前状态

### 4.3.7 运输任务（INV-08）
- 创建运输任务（选单/车辆/司机/预计到达）→ 指派 → 签收，节点写 `follow_ups`

### 4.3.8 到货入库登记（INV-09）
- 采购单明细按商品录入实收数量 → 生成 `wms_inbound`（来源=采购）→ 触发"入库事务"：加库存、写库存流水、回写明细 `received_qty`
- 全部到齐 → 单据状态"已到货"，否则"部分到货"

### 4.3.9 采购结算（INV-10）
- 结算=生成应付确认：把应付明细状态置待核销，供财务付款核销；完成后单据状态"结算"
- 金额口径 = 明细金额×(1+税率)

### 4.3.10 采购跟单结单（INV-11）
- `follow_ups` 节点跟踪（下单→发货→到货→结算）；所有节点完成可"结单"关闭采购单，防止再操作

### 4.3.11 销售订单（INV-12）
- 订单登记：明细含商品/数量/单价/折扣；金额=Σ(数量×单价)-折扣
- 提交订单前调用信用校验（`used_credit`）；超限拦截提示
- 状态机：`草稿 → 待审批 → 已通过 → 出库中 → 部分出库 → 已出库 → 已结算 → 结单`

### 4.3.12 销售单+审批（INV-13）
- 同采购审批逻辑；通过后向 `wms_outbound` 侧登记出库线索，并给客户追加应收明细 `crm_arc_detail`、累加 `used_credit`

### 4.3.13 出库发货（INV-14）
- 销售单明细按商品出库 → 生成 `wms_outbound`（来源=销售）→ "出库事务"：减库存（实时校验库存充足）、写流水、回写 `delivered_qty`

### 4.3.14 销售回款/结单（INV-15）
- 回款走财务收款单核销；全部核销后应收明细"已结清"→ 扣减 `used_credit`，销售单 `settle_status=已结清` → 可结单
- 结单后锁定单据，退款走退货单

### 4.3.15 退货处理
- 采购退→`return_order(来源=PURCHASE)`：通过后生成出库单，冲减应付
- 销售退→`return_order(来源=SALE)`：通过后生成入库单回补库存，冲减应收与 `used_credit`

## 4.4 仓储系统（组D）

### 4.4.1 入库单（WMS-01）
- 来源：采购到货/销售退货/其它；保存主从表，审核后执行入库事务
- 入库事务（必须同事务）：查 `wms_stock`，无则创建 → `quantity+=x` → 写 `wms_stock_log(before/after/ref_no)` → 回写来源单据实收

### 4.4.2 出库单（WMS-02）
- 出库事务（必须同事务）：校验库存足够（`quantity>=x` 否则抛"库存不足"）→ `quantity-=x` → 写流水 → 回写来源单据实发

### 4.4.3 库存流水（WMS-03）
- 每笔出入库/盘点/调拨均写流水，支持按商品/仓库/时间/类型过滤，实现全链路追溯

### 4.4.4 库存统计（WMS-04）
- 按仓库×商品联表查询实时库存；支持商品编码/名称模糊、分类筛选；分页返回

### 4.4.5 库存上下限（WMS-05）
- 商品档案维护 low/high；低于下限时在首页待办预警（数据来源：定时任务扫描）
- 可在此功能手工设置阈值并立即触发一次预警扫描

### 4.4.6 盘点（WMS-06）
- 生成盘点单（选仓库/日期）→ 系统自动带出账面数 → 录入实盘数 → 计算差异 `diff_qty`
- 差异未处理时盘点单状态"盘点中"

### 4.4.7 盘盈盘亏调整（WMS-07）
- 对盘点差异生成调整单，审核后执行库存调整事务：`quantity+=diff`、写流水（类型=盘盈/盘亏）
- 调整完成后盘点单状态"已完成"

### 4.4.8 货物转接（调拨）（WMS-08）
- 调拨单：调出仓→调入仓，审核通过先减调出仓库存、后加调入仓库存（同事务），写两条流水（配平）
- 状态：待出库→已出库→已完成

### 4.4.9 库位管理（WMS-09）
- 可选扩展：仓库下挂库位，库存表增加 `location` 维度；本期提供库位查询与绑定界面

## 4.5 业务报表系统（组E）

### 4.5.1 采购报表（RPT-01）
- 维度：按供应商/商品/时间区间聚合 `purchase_order`，输出数量、金额（含税）、平均单价
- 大表聚合用 MySQL GROUP BY，数据量小无需预聚合

### 4.5.2 销售报表（RPT-02）
- 按客户/商品/时间聚合 `sale_order`，输出数量、金额、折扣、毛利估计

### 4.5.3 库存报表（RPT-03）
- 实时库存表 + 低库存（quantity<low_limit）TOP 清单 + 周转率（期间出库量/平均库存）

### 4.5.4 财务收支报表（RPT-04）
- 按账户/时间：期初余额 + 期间收入 - 期间支出 = 期末余额，取自 `fin_account_log` 按天聚合

### 4.5.5 应收应付账龄报表（RPT-05）
- 按账龄区间（0-30/31-60/61-90/>90 天）统计未核销金额，`due_date` 计算账龄

### 4.5.6 利润汇总报表（RPT-06）
- 销售毛利 = 销售收入 - 销售成本（成本取商品 `purchase_price` 或加权平均），按期间/商品汇总

### 4.5.7 伙伴贡献度（RPT-07）
- 客户：按销售额/毛利排行 TOP N；供应商：按采购额排行；饼图+表格展示

### 4.5.8 报表导出（RPT-08）
- 前端基于当前查询结果导出 Excel（xlsx），服务端可选提供 CSV 导出接口；导出走 `report.path` 参数指定目录

## 4.6 定时任务系统（组E）

### 4.6.1 定时货物预警（JOB-01）
- 每日 08:00 扫描 `wms_stock` join `goods`：`quantity < low_limit` 且状态在售 → 写入预警列表（存入 job_task_log.message 与首页待办）

### 4.6.2 安全预警（JOB-02）
- 扫描商品期限/批次信息（扩展字段）临期 → 预警；演示可用"上次进价超过 N 天未变"替代演示逻辑

### 4.6.3 常用商品频度统计（JOB-03）
- T+1（每日 01:00）按商品聚合近 N 日销售频次写入 `rpt_snapshot`

### 4.6.4 定时报表（JOB-04）
- 按 cron（如每日 06:00）预生成报表快照写入 `rpt_snapshot`，报表页优先读快照、可选实时刷新

### 4.6.5 任务执行日志（JOB-05）
- 每次执行记录 start/end/result/message 到 `job_task_log`，页面可查执行历史、失败原因

### 4.6.6 任务启停管理（JOB-06）
- 页面开关动态改 `enabled`，调度器按周期重载；手动"立即执行一次"

### 4.6.7 数据归档/清理（JOB-07）
- 按规则将超期结单数据软归档（`deleted=1` 或归档表），控制主表膨胀；每日执行，保留最近 N 天

## 4.7 财务系统（组D）

### 4.7.1 资金账户管理（FIN-01）
- 账户维护（名称/账号/期初/余额）；期初计入 `fin_account_log` 首笔余额；停用账户禁止收付款与转账

### 4.7.2 收款单（FIN-02）
- 关联销售单（`orders_key=销售单号`）→ 自动带出应收明细 → 逐条录入核销金额 → 收款事务：
  - 账户 `balance += 收款额`，写 `fin_account_log`（业务类型=收款）
  - 核销应收明细：`received+=x、balance-=x`，余额=0 置"已结清"
  - 回写销售单 `received_amount`、客户 `used_credit -= x`、`debt_amount -= x`
- 单金额不得超过未核销余额；整单核销完成则 `states=已入账`

### 4.7.3 付款单（FIN-03）
- 关联采购单 → 带出应付明细 → 核销收款事务（方向相反）：
  - 账户 `balance -= 付款额`（校验余额充足），写流水（业务类型=付款）
  - 核销应付：`paid+=x、balance-=x`
  - 回写供应商 `payable_amount -= x`

### 4.7.4 应收核销（FIN-04）
- 即收款单的核销环节的独立视图：按客户展示未结清应收明细，勾选核销金额生成收款单

### 4.7.5 应付核销（FIN-05）
- 按供应商展示未结清应付明细，勾选生成付款单

### 4.7.6 跨系统转账（FIN-06）
- 转账单：转出户-、转入户+（同事务），写两条流水（业务类型=转出/转入）
- 需审批（`approval_record`），审批通过后入账

### 4.7.7 资金流水（FIN-07）
- 按账户分页查询 `fin_account_log`，支持类型/日期/关联单号过滤；展示变动前后余额

---

# 五、相关接口

> 接口格式：`对应的功能 | path | 请求方式 | 参数 | 返回`。统一前缀 `/api`，响应统一 `{code, message, data}`，分页 `data={total, list}`。以下为核心接口清单（按子系统归纳，同一资源的增删改查合并列出）。

## 5.1 认证与平台基座（组A）
| 项 | 内容 |
| --- | --- |
| 对应的功能 | 用户登录 / 安全退出 / 当前用户 / 修改密码 |
| path | POST /api/auth/login，POST /api/auth/logout，GET /api/auth/info，POST /api/auth/password |
| 请求方式 | POST/POST/GET/POST |
| 参数 | 登录：`{username, password}`；改密：`{oldPassword, newPassword}` |
| 返回 | 登录：`{token, user:{id,username,name,roles}}`；info：`{user, menus, permissions}` |

| 项 | 内容 |
| --- | --- |
| 对应的功能 | 地区 / 分公司 / 部门 / 员工 / 角色 / 资源管理 |
| path | GET /api/regions，POST/GET /api/regions；GET /api/companys，POST /api/companys，PUT /api/companys/{id}，DELETE /api/companys/{id}；同理 /api/depts、/api/employees、/api/roles、/api/resources |
| 请求方式 | RESTful |
| 参数 | 树查询 parentId；分页 `{page,pageSize,keyword}`；保存传实体 JSON |
| 返回 | 树：`[{id,name,children}]`；分页：`{total,list}` |

| 项 | 内容 |
| --- | --- |
| 对应的功能 | 角色授权 / 员工-角色授权 / 仓库管理 |
| path | PUT /api/roles/{id}/resources（授权），GET /api/roles/{id}/resources；PUT /api/employees/{id}/roles；GET/POST/PUT/DELETE /api/warehouses |
| 请求方式 | PUT/GET/PUT/RESTful |
| 参数 | 授权：`{resourceIds:[...]}`；保存传实体 JSON |
| 返回 | 成功 `{code:200,message:"成功"}`；授权树回显 `{resourceIds}` |

| 项 | 内容 |
| --- | --- |
| 对应的功能 | 系统参数 / 字典 / 车辆 / 会议室 / 编码规则 / 审批规则 / 审计查询 |
| path | GET/POST/PUT /api/params；GET/POST/PUT /api/dicts；GET/POST/PUT/DELETE /api/vehicles；/api/meetings；/api/code-rules；/api/approval-rules；GET /api/audit-logs |
| 请求方式 | RESTful + 查询分页 |
| 参数 | 字典：`{dictType}` 过滤；审计：`{operator,action,startTime,endTime,page,pageSize}` |
| 返回 | 分页 `{total,list}` |

## 5.2 客户与供应商（组B）
| 项 | 内容 |
| --- | --- |
| 对应的功能 | 客户 / 供应商档案例建、维护、审批、信用、跟进、对账、合并 |
| path | GET/POST /api/customers，PUT /api/customers/{id}，DELETE /api/customers/{id}，POST /api/customers/{id}/approve（审批），POST /api/customers/{id}/follow（跟进），PUT /api/customers/{id}/credit（信用变更），POST /api/customers/merge（合并）；同理 /api/suppliers |
| 请求方式 | RESTful |
| 参数 | 建档：`{code,name,categoryId,linkman,phone,address,creditLimit}`；审批：`{approve:true,comment}`；跟进：`{content,nextTime}`；合并：`{fromId,toId}` |
| 返回 | 列表分页：`{total,list}`；明细 `{...customer}`；信用：`{available}` |
| 其他 | 供应商特有的应付视图：GET /api/suppliers/{id}/payables |

| 项 | 内容 |
| --- | --- |
| 对应的功能 | 应收 / 应付对账明细 |
| path | GET /api/customers/{id}/receivables，GET /api/suppliers/{id}/payables |
| 请求方式 | GET |
| 参数 | `{page,pageSize,status}` |
| 返回 | `{total,list:[{refNo,amount,received,balance,dueDate,status}]}` |

## 5.3 进销存（组C）
| 项 | 内容 |
| --- | --- |
| 对应的功能 | 商品 / 分类 / 单位管理 |
| path | GET/POST /api/goods，PUT/DELETE /api/goods/{id}，GET /api/goods/categories，POST /api/goods/categories，GET/POST/DELETE /api/goods/units |
| 请求方式 | RESTful |
| 参数 | 商品：`{code,name,categoryId,unitId,spec,brand,spec, purchasePrice,salePrice,lowLimit,highLimit,status}` |
| 返回 | 分页 `{total,list}`；分类树 `[{id,name,children}]` |

| 项 | 内容 |
| --- | --- |
| 对应的功能 | 采购需求 / 采购单 / 采购审批 / 车辆调度 / 到货入库 / 采购结算 / 跟单结单 |
| path | GET/POST /api/purchase/demands，POST /api/purchase/demands/{ids}/convert；GET/POST /api/purchase/orders，GET /api/purchase/orders/{id}，PUT /api/purchase/orders/{id}，POST /api/purchase/orders/{id}/submit（提交审批），POST /api/purchase/orders/{id}/approve，POST /api/purchase/orders/{id}/dispatch（调车），POST /api/purchase/orders/{id}/arrival（到货登记），POST /api/purchase/orders/{id}/settle（结算），POST /api/purchase/orders/{id}/close（结单） |
| 请求方式 | RESTful |
| 参数 | 保存：`{supplierId,applyDate,taxRate,remark,items:[{goodsId,quantity,price}]}`；到货：`{warehouseId,arrivalDate,items:[{goodsId,receivedQty}]}`；调车：`{vehicleId}` |
| 返回 | 主表详情含明细；操作类返回 `{code,message}` 与最新状态 |

| 项 | 内容 |
| --- | --- |
| 对应的功能 | 销售订单 / 销售审批 / 出库发货 / 回款结单 |
| path | GET/POST /api/sale/orders，GET /api/sale/orders/{id}，PUT /api/sale/orders/{id}，POST /api/sale/orders/{id}/submit，POST /api/sale/orders/{id}/approve，POST /api/sale/orders/{id}/deliver（发货），POST /api/sale/orders/{id}/close |
| 请求方式 | RESTful |
| 参数 | 保存：`{customerId,orderDate,discount,warehouseId,items:[{goodsId,quantity,price}]}`；发货：`{warehouseId,items:[{goodsId,qty}]}` |
| 返回 | 发货时库存不足返回 `{code:500,message:"库存不足：商品名 仅剩 x"}` |

| 项 | 内容 |
| --- | --- |
| 对应的功能 | 退货单 / 采购票据 / 运输任务 / 跟单节点 |
| path | GET/POST /api/returns，POST /api/returns/{id}/approve；GET/POST /api/purchase/bills；GET/POST /api/transports，POST /api/transports/{id}/receive；GET /api/purchase/orders/{id}/follows |
| 请求方式 | RESTful |
| 参数 | 退货：`{srcType,srcId,reason,amount}`；票据：`{orderId,billType,billNo,amount}` |
| 返回 | 分页/详情 + 操作结果 |

## 5.4 仓储（组D）
| 项 | 内容 |
| --- | --- |
| 对应的功能 | 入库 / 出库 / 库存 / 流水 / 盘点 / 盘盈亏 / 调拨 / 库位 |
| path | GET/POST /api/wms/inbounds，GET /api/wms/inbounds/{id}，POST /api/wms/inbounds/{id}/submit；同理 /api/wms/outbounds；GET /api/wms/stocks，GET /api/wms/stock-logs，GET/POST /api/wms/checks，POST /api/wms/checks/{id}/adjust，GET/POST /api/wms/transfers，POST /api/wms/transfers/{id}/approve，GET/POST /api/wms/locations |
| 请求方式 | RESTful |
| 参数 | 入库：`{warehouseId,inDate,inType,srcNo,items:[{goodsId,quantity,price}]}`；调拨：`{fromWarehouse,toWarehouse,goodsId,quantity}` |
| 返回 | 库存页：`{total,list:[{goodsId,name,warehouseId,quantity,lowLimit,status}]}`；流水：分页含 beforeQty/afterQty |

## 5.5 财务报表（组E）
| 项 | 内容 |
| --- | --- |
| 对应的功能 | 资金账户 / 收款 / 付款 / 核销 / 转账 / 资金流水 |
| path | GET/POST /api/finance/accounts，PUT /api/finance/accounts/{id}；GET/POST /api/finance/lists（收付通用），GET /api/finance/lists/{id}，POST /api/finance/lists/{id}/submit；POST /api/finance/transfers，POST /api/finance/transfers/{id}/approve；GET /api/finance/logs |
| 请求方式 | RESTful |
| 参数 | 收款：`{listType:"RECEIPT",ordersKey:销售单号,accountId,partnerId,receiptDate,details:[{detailId,amount}]}`；转账：`{fromAccount,toAccount,amount}` |
| 返回 | 保存返回单号与核销结果；转账余额不足返回 500+提示 |

## 5.6 报表与定时任务（组E）
| 项 | 内容 |
| --- | --- |
| 对应的功能 | 采购 / 销售 / 库存 / 收支 / 账龄 / 利润 / 贡献度报表 |
| path | GET /api/reports/purchase，GET /api/reports/sale，GET /api/reports/inventory，GET /api/reports/finance，GET /api/reports/aging，GET /api/reports/profit，GET /api/reports/contribution |
| 请求方式 | GET |
| 参数 | 公共：`{startDate,endDate}`；按维度：`{supplierId/customerId/goodsId/warehouseId}` |
| 返回 | `data:{columns:[...], rows:[...], total}`，供前端表格+图表渲染 |

| 项 | 内容 |
| --- | --- |
| 对应的功能 | 定时任务管理 / 执行日志 / 立即执行 |
| path | GET/POST /api/jobs，PUT /api/jobs/{id}（启停/改cron），POST /api/jobs/{id}/run（立即执行），GET /api/jobs/logs |
| 请求方式 | RESTful |
| 参数 | 任务：`{jobCode,jobName,cronExpr,enabled,description}` |
| 返回 | 分页 `{total,list}`；run 返回执行结果 message |

---

# 附录：交付清单与分工
| 模块 | 负责人 | 覆盖章节 |
| --- | --- | --- |
| 平台基座（登录/基础维护/权限/规则） | 组A | 3.1、4.1、5.1 |
| 客户关系 CRM | 组B | 3.2、4.2、5.2 |
| 进销存核心（商品/采购/销售/退货） | 组C | 3.3、4.3、5.3 |
| 仓储 + 财务 | 组D | 3.4、3.5、4.4、4.7、5.4、5.5 |
| 报表 + 定时任务 + 运维 | 组E | 3.6、3.7、4.5、4.6、5.6 |

> 说明：本开发文档严格按模板「业务背景 / 功能梳理 / 数据库设计 / 业务逻辑 / 相关接口」五段式生成，内容对标原型数据（地区8/公司4/部门13/员工20/仓库6/车辆8/供应商12/客户15/商品40），可作为五人分组各自交付 AI 的编码依据。

*（内容由AI生成，仅供参考）*
