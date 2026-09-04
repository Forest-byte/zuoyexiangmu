Read "D:\zuoyexiangmu\ERP系统开发文档.md":

     1	---
     2	AIGC:
     3	    Label: "1"
     4	    ContentProducer: 001191440300708461136T1XGW3
     5	    ProduceID: c00b9e2277119ca47dfc451df575ea27_f32a5a1fa74111f1ac80525400aeaaa3
     6	    ReservedCode1: 1rHZqlS3otiySNkCv5GCCd+LrzKp3PcfEUsHEwJoO0oLAHS7hnBeD7mfvocyeD6sMDAmNEJSg9avTI4eDjgBqgNeFkdq3iowT9If3c8n01XiipXPaWekJnVbUPJS5G4Vgc9qMeWN5aeVDgUjl8i9XsqLz3everhCOq5oWqtLjhs2NaQ4xEYA4kZ/LZ4=
     7	    ContentPropagator: 001191440300708461136T1XGW3
     8	    PropagateID: c00b9e2277119ca47dfc451df575ea27_f32a5a1fa74111f1ac80525400aeaaa3
     9	    ReservedCode2: 1rHZqlS3otiySNkCv5GCCd+LrzKp3PcfEUsHEwJoO0oLAHS7hnBeD7mfvocyeD6sMDAmNEJSg9avTI4eDjgBqgNeFkdq3iowT9If3c8n01XiipXPaWekJnVbUPJS5G4Vgc9qMeWN5aeVDgUjl8i9XsqLz3everhCOq5oWqtLjhs2NaQ4xEYA4kZ/LZ4=
    10	---
    11	
    12	# ERP 系统开发文档
    13	
    14	---
    15	
    16	# 一、业务背景
    17	
    18	## 1.1 项目背景
    19	面向中小企业的进销存一体化 ERP 系统，覆盖主数据、客户关系(CRM)、采购、销售、仓储、财务、报表与定时任务七大子系统，实现**物流、资金流、信息流三流合一**。系统以现有纯前端原型（`C:\Users\34900\Downloads\原型页面\system\`）为界面与交互的**对标基准**，落地为真实前后端分离系统。
    20	
    21	## 1.2 项目目标
    22	1. 完整走一遍企业开发流程：需求 → 设计 → 开发 → 测试 → 上线
    23	2. 交付可运行、可演示、可答辩的前后端分离 ERP 系统（7 大子系统、63 个功能点）
    24	3. 界面交互对标原型：登录页、深蓝渐变主框架、9 大业务页面
    25	4. 支持 5 类角色分权协作，全过程可审计、可追溯
    26	
    27	## 1.3 技术栈与架构
    28	| 类别 | 技术 | 版本 |
    29	|---|---|---|
    30	| 后端 | Java / Spring Boot / MyBatis | JDK 17 / 3.x / 3.5.x |
    31	| 数据库 | MySQL | 8.0.x（utf8mb4） |
    32	| 前端 | Vue / Element Plus / Vite / Axios / Pinia | 3.x / 2.x |
    33	| 工具 | Git / Maven / Node | 稳定版 / 3.8+ / 18+ |
    34	
    35	**架构链路**：浏览器 → Vue3 SPA（路由视图代替原型 iframe）→ RESTful API → Spring Boot 3.x（controller/service/mapper）→ MyBatis → MySQL。
    36	
    37	## 1.4 内置账号与角色
    38	| 账号 | 密码 | 角色 |
    39	|---|---|---|
    40	| admin | admin123 | 管理员（全权限） |
    41	| purchase | 123456 | 采购员 |
    42	| saler | 123456 | 销售员 |
    43	| warehouse | 123456 | 库管 |
    44	| finance | 123456 | 财务 |
    45	
    46	## 1.5 通用约定
    47	1. 统一返回 `{code, message, data}`；分页入参 `page/pageSize`，返回 `{total, list}`
    48	2. 单据号规则：`前缀+yyyyMMdd+3位当日流水`（如 PO20260903001），全局唯一
    49	3. 通用字段：`create_time / update_time / create_by / deleted(逻辑删除) / status`
    50	4. 金额 `DECIMAL(18,2)`、库存 `DECIMAL(18,3)`，禁止浮点
    51	5. 关键写操作写审计日志（sys_audit_log）
    52	6. JWT 登录 + RBAC 权限，越权 403、未登录 401
    53	
    54	---
    55	
    56	# 二、功能梳理
    57	
    58	## 2.1 基础维护系统（BM-01~10，负责人：组A）
    59	| 编码 | 功能 | 说明 |
    60	|---|---|---|
    61	| BM-01 | 地区管理 | 树形结构、父子级维护 |
    62	| BM-02 | 分公司管理 | 关联地区、编码唯一 |
    63	| BM-03 | 部门管理 | 部门树、关联公司/负责人 |
    64	| BM-04 | 员工管理 | 关联部门/岗位、账号绑定 |
    65	| BM-05 | 角色维护 | 角色 CRUD、授权入口 |
    66	| BM-06 | 资源维护 | 菜单/按钮四级资源定义 |
    67	| BM-07 | 仓库管理 | 仓库档案、状态启停 |
    68	| BM-08 | 员工-角色授权 | 用户与角色关联 |
    69	| BM-09 | 用户登录/改密 | JWT 签发、密码策略 |
    70	| BM-10 | 系统参数/车辆/会议室/编码规则/字典/审批规则/审计 | 公共配置与审计查询 |
    71	
    72	## 2.2 CRM 系统（CRM-01~07，负责人：组B）
    73	| 编码 | 功能 | 说明 |
    74	|---|---|---|
    75	| CRM-01 | 客户档案例建/维护 | 档案+分类+信用额度 |
    76	| CRM-02 | 供应商档案例建/维护 | 档案+分类+应付累计 |
    77	| CRM-03 | 伙伴分类维护 | 客户/供应商共用分类树 |
    78	| CRM-04 | 信用管理 | 额度、可用额度、超限预警 |
    79	| CRM-05 | 跟进记录 | 时间线、下次跟进提醒 |
    80	| CRM-06 | 应收/应付对账 | 往来明细与销核状态 |
    81	| CRM-07 | 客户合并 | 从客户并入主客户、历史单归属 |
    82	
    83	## 2.3 进销存系统（INV-01~15，负责人：组C）
    84	| 编码 | 功能 | 说明 |
    85	|---|---|---|
    86	| INV-01 | 商品档案维护 | SKU 粒度、编码唯一 |
    87	| INV-02 | 商品分类维护 | 树形 |
    88	| INV-03 | 计量单位维护 | 单位换算 |
    89	| INV-04 | 采购需求 | 需求登记、汇总转单 |
    90	| INV-05 | 采购单+审批 | 状态机+审批链 |
    91	| INV-06 | 采购报备/票据 | 票据登记 |
    92	| INV-07 | 车辆调度 | 单据分配车辆、状态流转 |
    93	| INV-08 | 运输任务 | 创建/指派/签收 |
    94	| INV-09 | 到货入库登记 | 触发 WMS 入库 |
    95	| INV-10 | 采购结算 | 生成应付联动财务 |
    96	| INV-11 | 采购跟单结单 | 节点跟踪、订单关闭 |
    97	| INV-12 | 销售订单 | 订单登记+信用校验 |
    98	| INV-13 | 销售单+审批 | 审批+出库联动 |
    99	| INV-14 | 出库发货 | 触发 WMS 出库扣库存 |
   100	| INV-15 | 销售回款/结单 | 应收核销、结单 |
   101	
   102	## 2.4 仓储系统（WMS-01~09，负责人：组D）
   103	| 编码 | 功能 | 说明 |
   104	|---|---|---|
   105	| WMS-01 | 入库单 | 采购到货/退货等来源 |
   106	| WMS-02 | 出库单 | 销售/领用 |
   107	| WMS-03 | 库存流水 | 每笔变动可追溯 |
   108	| WMS-04 | 库存统计 | 仓库×商品实时查询 |
   109	| WMS-05 | 库存上下限 | 上下限维护+预警 |
   110	| WMS-06 | 盘点 | 盘点单+差异计算 |
   111	| WMS-07 | 盘盈盘亏 | 差异审核调整 |
   112	| WMS-08 | 货物转接 | 仓间调拨 |
   113	| WMS-09 | 库位管理 | 库位维度扩展 |
   114	
   115	## 2.5 业务报表系统（RPT-01~08，负责人：组E）
   116	| 编码 | 功能 | 说明 |
   117	|---|---|---|
   118	| RPT-01 | 采购报表 | 按供应商/商品/时间 |
   119	| RPT-02 | 销售报表 | 按客户/商品/时间 |
   120	| RPT-03 | 库存报表 | 仓库×商品、低库存、周转 |
   121	| RPT-04 | 财务收支报表 | 期初+收-支=期末 |
   122	| RPT-05 | 应收应付账龄报表 | 按账龄区间 |
   123	| RPT-06 | 利润汇总报表 | 毛利口径 |
   124	| RPT-07 | 伙伴贡献度分析 | 客户/供应商贡献排行 |
   125	| RPT-08 | 报表导出 | Excel 导出 |
   126	
   127	## 2.6 定时任务系统（JOB-01~07，负责人：组E）
   128	| 编码 | 功能 | 说明 |
   129	|---|---|---|
   130	| JOB-01 | 定时货物预警 | 扫描低于安全下限 |
   131	| JOB-02 | 安全预警 | 临期/保质期预警 |
   132	| JOB-03 | 常用商品频度统计 | T+1 聚合 |
   133	| JOB-04 | 定时报表 | 预生成报表快照 |
   134	| JOB-05 | 任务执行日志 | 成功/失败记录 |
   135	| JOB-06 | 任务启停管理 | 开关动态生效 |
   136	| JOB-07 | 数据归档/清理 | 过期单据归档 |
   137	
   138	## 2.7 财务系统（FIN-01~07，负责人：组D）
   139	| 编码 | 功能 | 说明 |
   140	|---|---|---|
   141	| FIN-01 | 资金账户管理 | 账户/期初余额/启停 |
   142	| FIN-02 | 收款单 | 销收核销+账户入账 |
   143	| FIN-03 | 付款单 | 采付核销+账户出账 |
   144	| FIN-04 | 应收核销 | 收款关联应收冲抵 |
   145	| FIN-05 | 应付核销 | 付款关联应付冲抵 |
   146	| FIN-06 | 跨系统转账 | 账户间资金转移+审批 |
   147	| FIN-07 | 资金流水 | 账户级全部变动查询 |
   148	
   149	---
   150	
   151	# 三、数据库设计
   152	
   153	> 库名 erp，utf8mb4。所有表含通用字段：`id BIGINT AUTO_INCREMENT PRIMARY KEY, create_time DATETIME, update_time DATETIME, create_by VARCHAR(50), deleted TINYINT DEFAULT 0`。以下字段表省略通用字段，仅列业务字段。
   154	
   155	## 3.1 平台基座（组A）
   156	
   157	### 表名：sys_user（用户表）
   158	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   159	| ------ | -------- | ---- | ---- | ---- |
   160	| username | VARCHAR(50) | NOT NULL UNIQUE | 登录名 | admin/purchase/saler/warehouse/finance |
   161	| password | VARCHAR(100) | NOT NULL | 密码 | 演示可明文，正式 BCrypt |
   162	| name | VARCHAR(50) | NOT NULL | 姓名 | 界面显示 |
   163	| role_code | VARCHAR(50) | NOT NULL | 主角色 | 关联 sys_role |
   164	| dept_id | BIGINT | NULL | 部门 | 关联 sys_dept |
   165	| employee_id | BIGINT | NULL | 员工 | 关联 sys_employee |
   166	| status | TINYINT | DEFAULT 1 | 状态 | 1启用 0停用 |
   167	
   168	### 表名：sys_role（角色表）
   169	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   170	| ------ | -------- | ---- | ---- | ---- |
   171	| role_code | VARCHAR(50) | NOT NULL UNIQUE | 角色编码 | ROLE_ADMIN/ROLE_PURCHASE/ROLE_SALER/ROLE_WAREHOUSE/ROLE_FINANCE |
   172	| name | VARCHAR(50) | NOT NULL | 角色名称 | 管理员/采购员/销售员/库管/财务 |
   173	| description | VARCHAR(200) | NULL | 描述 | 可选 |
   174	
   175	### 表名：sys_resource（资源表，四级）
   176	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   177	| ------ | -------- | ---- | ---- | ---- |
   178	| parent_id | BIGINT | DEFAULT 0 | 父级资源 | L1系统→L2模块→L3菜单→L4按钮 |
   179	| name | VARCHAR(50) | NOT NULL | 资源名 | 如 基础维护 |
   180	| type | VARCHAR(20) | NOT NULL | 类型 | menu/button |
   181	| code | VARCHAR(100) | NULL | 权限码 | 如 base:region:add |
   182	| path | VARCHAR(200) | NULL | 路由 | L3 菜单用 |
   183	| icon | VARCHAR(50) | NULL | 图标 | 前端图标名 |
   184	| sort | INT | DEFAULT 0 | 排序 | 菜单顺序 |
   185	
   186	### 表名：sys_user_role（用户角色关联）
   187	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   188	| ------ | -------- | ---- | ---- | ---- |
   189	| user_id | BIGINT | NOT NULL | 用户 | 关联 sys_user |
   190	| role_id | BIGINT | NOT NULL | 角色 | 关联 sys_role，唯一(user_id,role_id) |
   191	
   192	### 表名：sys_role_resource（角色资源关联）
   193	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   194	| ------ | -------- | ---- | ---- | ---- |
   195	| role_id | BIGINT | NOT NULL | 角色 | 关联 sys_role |
   196	| resource_id | BIGINT | NOT NULL | 资源 | 关联 sys_resource，唯一(role_id,resource_id) |
   197	
   198	### 表名：sys_region（地区表）
   199	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   200	| ------ | -------- | ---- | ---- | ---- |
   201	| name | VARCHAR(50) | NOT NULL | 地区名 | 华东地区/上海市 等 |
   202	| parent_id | BIGINT | DEFAULT 0 | 父地区 | 树形 |
   203	
   204	### 表名：sys_company（分公司表）
   205	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   206	| ------ | -------- | ---- | ---- | ---- |
   207	| name | VARCHAR(50) | NOT NULL | 公司名 | 集团总部 等 |
   208	| code | VARCHAR(20) | UNIQUE | 公司编码 | HQ/HD-MFG 等 |
   209	| region_id | BIGINT | NULL | 所属地区 | 关联 sys_region |
   210	| address | VARCHAR(200) | NULL | 地址 | — |
   211	| phone | VARCHAR(20) | NULL | 电话 | — |
   212	| status | VARCHAR(20) | DEFAULT 启用 | 状态 | 启用/停用 |
   213	
   214	### 表名：sys_dept（部门表）
   215	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   216	| ------ | -------- | ---- | ---- | ---- |
   217	| name | VARCHAR(50) | NOT NULL | 部门名 | 财务部/采购部 等 |
   218	| code | VARCHAR(20) | UNIQUE | 部门编码 | D002 等 |
   219	| company_id | BIGINT | NOT NULL | 所属公司 | 关联 sys_company |
   220	| manager | VARCHAR(50) | NULL | 负责人 | — |
   221	| phone | VARCHAR(20) | NULL | 电话 | — |
   222	
   223	### 表名：sys_employee（员工表）
   224	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   225	| ------ | -------- | ---- | ---- | ---- |
   226	| name | VARCHAR(50) | NOT NULL | 姓名 | 张伟 等 |
   227	| code | VARCHAR(20) | UNIQUE | 工号 | E001 等 |
   228	| dept_id | BIGINT | NOT NULL | 部门 | 关联 sys_dept |
   229	| position | VARCHAR(50) | NULL | 岗位 | 总经理/采购主管 等 |
   230	| phone | VARCHAR(20) | NULL | 电话 | — |
   231	| email | VARCHAR(100) | NULL | 邮箱 | — |
   232	| status | VARCHAR(20) | DEFAULT 在职 | 状态 | 在职/离职 |
   233	
   234	### 表名：sys_warehouse（仓库表）
   235	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   236	| ------ | -------- | ---- | ---- | ---- |
   237	| name | VARCHAR(50) | NOT NULL | 仓库名 | 上海一号库 等 |
   238	| code | VARCHAR(20) | UNIQUE | 仓库编码 | WH001 等 |
   239	| address | VARCHAR(200) | NULL | 地址 | — |
   240	| manager | VARCHAR(50) | NULL | 负责人 | — |
   241	| phone | VARCHAR(20) | NULL | 电话 | — |
   242	| status | VARCHAR(20) | DEFAULT 启用 | 状态 | 启用/停用 |
   243	
   244	### 表名：sys_vehicle（车辆表）
   245	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   246	| ------ | -------- | ---- | ---- | ---- |
   247	| name | VARCHAR(50) | NOT NULL | 车牌号 | 沪A·8K216 |
   248	| code | VARCHAR(20) | UNIQUE | 车辆编码 | V001 |
   249	| type | VARCHAR(20) | NULL | 车型 | 厢式货车/冷藏车 等 |
   250	| capacity | VARCHAR(20) | NULL | 载重 | 2吨 等 |
   251	| driver | VARCHAR(50) | NULL | 司机 | — |
   252	| phone | VARCHAR(20) | NULL | 司机电话 | — |
   253	| status | VARCHAR(20) | DEFAULT 空闲 | 状态 | 空闲/在途/维修 |
   254	
   255	### 表名：sys_meeting（会议室表）
   256	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   257	| ------ | -------- | ---- | ---- | ---- |
   258	| name | VARCHAR(50) | NOT NULL | 会议室名 | — |
   259	| capacity | INT | NULL | 容纳人数 | — |
   260	| status | VARCHAR(20) | DEFAULT 可用 | 状态 | 可用/占用 |
   261	
   262	### 表名：sys_param（系统参数表）
   263	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   264	| ------ | -------- | ---- | ---- | ---- |
   265	| param_key | VARCHAR(50) | UNIQUE | 参数键 | 如 report.path |
   266	| param_value | VARCHAR(200) | NULL | 参数值 | — |
   267	| description | VARCHAR(200) | NULL | 说明 | — |
   268	
   269	### 表名：sys_dict（数据字典表）
   270	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   271	| ------ | -------- | ---- | ---- | ---- |
   272	| dict_type | VARCHAR(50) | NOT NULL | 字典类型 | 单据状态/客户状态 等 |
   273	| label | VARCHAR(50) | NOT NULL | 显示值 | 草稿/待审批 等 |
   274	| value | VARCHAR(50) | NOT NULL | 存储值 | DRAFT/PENDING 等 |
   275	| sort | INT | DEFAULT 0 | 排序 | — |
   276	
   277	### 表名：approval_rule（审批规则表）
   278	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   279	| ------ | -------- | ---- | ---- | ---- |
   280	| doc_type | VARCHAR(50) | NOT NULL | 单据类型 | PURCHASE/SALE/RETURN/TRANSFER |
   281	| role_code | VARCHAR(50) | NOT NULL | 审批角色 | 关联 sys_role |
   282	| level | INT | DEFAULT 1 | 审批层级 | 预留多级 |
   283	| enabled | TINYINT | DEFAULT 1 | 是否启用 | 1启用 0停用 |
   284	
   285	### 表名：sys_code_rule（编码规则表）
   286	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   287	| ------ | -------- | ---- | ---- | ---- |
   288	| doc_type | VARCHAR(50) | NOT NULL | 单据类型 | PO/SO/IN/OUT 等 |
   289	| prefix | VARCHAR(10) | NOT NULL | 前缀 | — |
   290	| format | VARCHAR(20) | NOT NULL | 格式 | {prefix}{yyyyMMdd}{seq} |
   291	| seq_len | INT | DEFAULT 3 | 流水位数 | — |
   292	
   293	### 表名：sys_audit_log（审计日志表）
   294	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   295	| ------ | -------- | ---- | ---- | ---- |
   296	| operator | VARCHAR(50) | NOT NULL | 操作人 | 取自登录用户 |
   297	| action | VARCHAR(50) | NOT NULL | 动作 | 登录系统/新增客户 等 |
   298	| target | VARCHAR(100) | NULL | 操作对象 | 单据号/实体名 |
   299	| before | TEXT | NULL | 变更前 | JSON |
   300	| after | TEXT | NULL | 变更后 | JSON |
   301	| time | DATETIME | NOT NULL | 时间 | — |
   302	
   303	## 3.2 CRM 系统（组B）
   304	
   305	### 表名：crm_customer（客户表）
   306	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   307	| ------ | -------- | ---- | ---- | ---- |
   308	| code | VARCHAR(20) | UNIQUE | 客户编码 | C001 等 |
   309	| name | VARCHAR(100) | NOT NULL | 客户名称 | 北京XX贸易有限公司 等 |
   310	| category_id | BIGINT | NULL | 伙伴分类 | 关联 crm_category |
   311	| linkman | VARCHAR(50) | NULL | 联系人 | — |
   312	| phone | VARCHAR(20) | NULL | 电话 | — |
   313	| address | VARCHAR(200) | NULL | 地址 | — |
   314	| credit_limit | DECIMAL(18,2) | DEFAULT 0 | 信用额度 | — |
   315	| used_credit | DECIMAL(18,2) | DEFAULT 0 | 已用额度 | 应收未核销累计 |
   316	| debt_amount | DECIMAL(18,2) | DEFAULT 0 | 欠款金额 | 冗余便于对账 |
   317	| status | VARCHAR(20) | DEFAULT 正常 | 状态 | 正常/冻结/停用 |
   318	| approval_status | VARCHAR(20) | DEFAULT 草稿 | 审批状态 | 草稿/待审批/已通过/已驳回 |
   319	| merge_from | BIGINT | NULL | 来源主客户 | 合并时记录 |
   320	
   321	### 表名：crm_supplier（供应商表）
   322	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   323	| ------ | -------- | ---- | ---- | ---- |
   324	| code | VARCHAR(20) | UNIQUE | 供应商编码 | S001 等 |
   325	| name | VARCHAR(100) | NOT NULL | 供应商名称 | 上海XX机电有限公司 等 |
   326	| category_id | BIGINT | NULL | 伙伴分类 | 关联 crm_category |
   327	| linkman | VARCHAR(50) | NULL | 联系人 | — |
   328	| phone | VARCHAR(20) | NULL | 电话 | — |
   329	| address | VARCHAR(200) | NULL | 地址 | — |
   330	| payable_amount | DECIMAL(18,2) | DEFAULT 0 | 应付累计 | 汇总未核销应付 |
   331	| status | VARCHAR(20) | DEFAULT 正常 | 状态 | 正常/冻结/停用 |
   332	
   333	### 表名：crm_category（伙伴分类表）
   334	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   335	| ------ | -------- | ---- | ---- | ---- |
   336	| parent_id | BIGINT | DEFAULT 0 | 父分类 | 树形 |
   337	| name | VARCHAR(50) | NOT NULL | 分类名 | 客户分类/供应商分类 下挂 |
   338	| kind | VARCHAR(20) | NOT NULL | 类别 | CUSTOMER/SUPPLIER |
   339	
   340	### 表名：crm_contact（联系人表）
   341	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   342	| ------ | -------- | ---- | ---- | ---- |
   343	| partner_type | VARCHAR(20) | NOT NULL | 伙伴类型 | CUSTOMER/SUPPLIER |
   344	| partner_id | BIGINT | NOT NULL | 伙伴ID | 关联客户或供应商 |
   345	| name | VARCHAR(50) | NOT NULL | 联系人 | — |
   346	| phone | VARCHAR(20) | NULL | 电话 | — |
   347	| email | VARCHAR(100) | NULL | 邮箱 | — |
   348	| is_default | TINYINT | DEFAULT 0 | 默认联系人 | 1是 0否 |
   349	
   350	### 表名：crm_follow_record（跟进记录表）
   351	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   352	| ------ | -------- | ---- | ---- | ---- |
   353	| customer_id | BIGINT | NOT NULL | 客户 | 关联 crm_customer |
   354	| content | VARCHAR(500) | NOT NULL | 跟进内容 | — |
   355	| next_time | DATE | NULL | 下次跟进日 | 超期提醒 |
   356	| recorder | VARCHAR(50) | NOT NULL | 记录人 | 当前登录人 |
   357	| record_time | DATETIME | NOT NULL | 记录时间 | — |
   358	
   359	### 表名：crm_arc_detail（应收明细表）
   360	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   361	| ------ | -------- | ---- | ---- | ---- |
   362	| customer_id | BIGINT | NOT NULL | 客户 | 关联 crm_customer |
   363	| ref_type | VARCHAR(20) | NOT NULL | 来源 | SALE/OTHER |
   364	| ref_no | VARCHAR(30) | NOT NULL | 来源单号 | 销售单号 |
   365	| amount | DECIMAL(18,2) | NOT NULL | 金额 | — |
   366	| received | DECIMAL(18,2) | DEFAULT 0 | 已核销 | — |
   367	| balance | DECIMAL(18,2) | DEFAULT 0 | 未核销 | amount-received |
   368	| status | VARCHAR(20) | DEFAULT 未结清 | 状态 | 未结清/部分/已结清 |
   369	| due_date | DATE | NULL | 到期日 | 账龄计算 |
   370	
   371	### 表名：crm_ap_detail（应付明细表）
   372	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   373	| ------ | -------- | ---- | ---- | ---- |
   374	| supplier_id | BIGINT | NOT NULL | 供应商 | 关联 crm_supplier |
   375	| ref_type | VARCHAR(20) | NOT NULL | 来源 | PURCHASE/OTHER |
   376	| ref_no | VARCHAR(30) | NOT NULL | 来源单号 | 采购单号 |
   377	| amount | DECIMAL(18,2) | NOT NULL | 金额 | — |
   378	| paid | DECIMAL(18,2) | DEFAULT 0 | 已核销 | — |
   379	| balance | DECIMAL(18,2) | DEFAULT 0 | 未核销 | — |
   380	| status | VARCHAR(20) | DEFAULT 未结清 | 状态 | 未结清/部分/已结清 |
   381	| due_date | DATE | NULL | 到期日 | 账龄计算 |
   382	
   383	### 表名：crm_credit_log（信用变更日志表）
   384	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   385	| ------ | -------- | ---- | ---- | ---- |
   386	| customer_id | BIGINT | NOT NULL | 客户 | — |
   387	| change_amount | DECIMAL(18,2) | NOT NULL | 变动额 | 正增负减 |
   388	| reason | VARCHAR(200) | NOT NULL | 原因 | — |
   389	| operator | VARCHAR(50) | NOT NULL | 操作人 | — |
   390	| operate_time | DATETIME | NOT NULL | 操作时间 | — |
   391	
   392	## 3.3 进销存系统（组C）
   393	
   394	### 表名：goods_category（商品分类表）
   395	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   396	| ------ | -------- | ---- | ---- | ---- |
   397	| parent_id | BIGINT | DEFAULT 0 | 父分类 | 树形 |
   398	| name | VARCHAR(50) | NOT NULL | 分类名 | 食品/饮料/酒类 等 |
   399	
   400	### 表名：goods_unit（计量单位表）
   401	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   402	| ------ | -------- | ---- | ---- | ---- |
   403	| name | VARCHAR(20) | NOT NULL | 单位名 | 件/箱/公斤 等 |
   404	| rate | DECIMAL(18,4) | DEFAULT 1 | 换算率 | 相对基准单位 |
   405	
   406	### 表名：goods（商品表）
   407	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   408	| ------ | -------- | ---- | ---- | ---- |
   409	| code | VARCHAR(30) | UNIQUE | 商品编码 | G001 等 |
   410	| name | VARCHAR(100) | NOT NULL | 商品名称 | 蒙牛纯牛奶 250ml 等 |
   411	| category_id | BIGINT | NOT NULL | 分类 | 关联 goods_category |
   412	| unit_id | BIGINT | NOT NULL | 单位 | 关联 goods_unit |
   413	| spec | VARCHAR(50) | NULL | 规格 | 24盒/箱 |
   414	| brand | VARCHAR(50) | NULL | 品牌 | — |
   415	| barcode | VARCHAR(50) | NULL | 条码 | — |
   416	| purchase_price | DECIMAL(18,2) | NOT NULL | 进价 | — |
   417	| sale_price | DECIMAL(18,2) | NOT NULL | 售价 | — |
   418	| last_in_price | DECIMAL(18,2) | NULL | 上次进价 | 采购参考 |
   419	| low_limit | DECIMAL(18,3) | DEFAULT 0 | 安全库存下限 | 低于则预警 |
   420	| high_limit | DECIMAL(18,3) | DEFAULT 0 | 安全库存上限 | — |
   421	| supplier_id | BIGINT | NULL | 常用供应商 | 关联 crm_supplier |
   422	| is_raw | TINYINT | DEFAULT 0 | 是否原材料 | 采购分类口径 |
   423	| status | VARCHAR(20) | DEFAULT 在售 | 状态 | 在售/停售 |
   424	
   425	### 表名：purchase_demand（采购需求表）
   426	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   427	| ------ | -------- | ---- | ---- | ---- |
   428	| demand_no | VARCHAR(30) | UNIQUE | 需求单号 | PD+日期+流水 |
   429	| goods_id | BIGINT | NOT NULL | 商品 | — |
   430	| quantity | DECIMAL(18,3) | NOT NULL | 需求数量 | — |
   431	| note | VARCHAR(200) | NULL | 备注 | 需求原因 |
   432	| need_date | DATE | NULL | 需求日期 | — |
   433	| applicant | VARCHAR(50) | NOT NULL | 申请人 | — |
   434	| status | VARCHAR(20) | DEFAULT 待处理 | 状态 | 待处理/已生成采购 |
   435	
   436	### 表名：purchase_order（采购主表）
   437	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   438	| ------ | -------- | ---- | ---- | ---- |
   439	| order_no | VARCHAR(30) | UNIQUE | 采购单号 | PO+日期+流水 |
   440	| supplier_id | BIGINT | NOT NULL | 供应商 | 关联 crm_supplier |
   441	| apply_date | DATE | NOT NULL | 申请日期 | — |
   442	| all_amount | DECIMAL(18,2) | NOT NULL | 含税总金额 | 明细汇总 |
   443	| tax_rate | DECIMAL(5,2) | DEFAULT 0 | 税率 | % |
   444	| tax_amount | DECIMAL(18,2) | DEFAULT 0 | 税额 | — |
   445	| status | VARCHAR(20) | DEFAULT 草稿 | 状态 | 见状态机 |
   446	| audit_status | VARCHAR(20) | DEFAULT 未审核 | 审核状态 | 未审核/已审核/已驳回 |
   447	| approve_person | VARCHAR(50) | NULL | 审批人 | — |
   448	| approve_time | DATETIME | NULL | 审批时间 | — |
   449	| order_states | VARCHAR(50) | NULL | 单据状态 | 采购中/部分到货/结单 等 |
   450	| vehicle_id | BIGINT | NULL | 调度车辆 | 关联 sys_vehicle |
   451	| warehouse_id | BIGINT | NULL | 入库仓库 | 关联 sys_warehouse |
   452	| arrival_date | DATE | NULL | 预计到货日 | — |
   453	| remark | VARCHAR(200) | NULL | 备注 | — |
   454	
   455	### 表名：purchase_order_item（采购明细表）
   456	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   457	| ------ | -------- | ---- | ---- | ---- |
   458	| order_id | BIGINT | NOT NULL | 采购主表 | 关联 purchase_order |
   459	| goods_id | BIGINT | NOT NULL | 商品 | — |
   460	| quantity | DECIMAL(18,3) | NOT NULL | 数量 | — |
   461	| price | DECIMAL(18,2) | NOT NULL | 单价 | — |
   462	| amount | DECIMAL(18,2) | NOT NULL | 金额 | quantity*price |
   463	| received_qty | DECIMAL(18,3) | DEFAULT 0 | 已入库 | 到货登记回写 |
   464	| remark | VARCHAR(200) | NULL | 备注 | — |
   465	
   466	### 表名：approval_record（审批记录表）
   467	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   468	| ------ | -------- | ---- | ---- | ---- |
   469	| doc_type | VARCHAR(20) | NOT NULL | 单据类型 | PURCHASE/SALE/TRANSFER |
   470	| doc_id | BIGINT | NOT NULL | 单据ID | 关联各主表 |
   471	| level | INT | DEFAULT 1 | 层级 | — |
   472	| approver | VARCHAR(50) | NOT NULL | 审批人 | — |
   473	| result | VARCHAR(20) | NOT NULL | 结果 | 通过/驳回 |
   474	| comment | VARCHAR(200) | NULL | 意见 | — |
   475	| approve_time | DATETIME | NOT NULL | 时间 | — |
   476	
   477	### 表名：sale_order（销售主表）
   478	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   479	| ------ | -------- | ---- | ---- | ---- |
   480	| order_no | VARCHAR(30) | UNIQUE | 销售单号 | SO+日期+流水 |
   481	| customer_id | BIGINT | NOT NULL | 客户 | 关联 crm_customer |
   482	| order_date | DATE | NOT NULL | 下单日期 | — |
   483	| all_amount | DECIMAL(18,2) | NOT NULL | 总额 | — |
   484	| discount | DECIMAL(18,2) | DEFAULT 0 | 折扣 | — |
   485	| received_amount | DECIMAL(18,2) | DEFAULT 0 | 已收款 | 回款回写 |
   486	| status | VARCHAR(20) | DEFAULT 草稿 | 状态 | 见状态机 |
   487	| audit_status | VARCHAR(20) | DEFAULT 未审核 | 审核状态 | — |
   488	| warehouse_id | BIGINT | NULL | 发货仓库 | — |
   489	| delivery_date | DATE | NULL | 预计发货日 | — |
   490	| settle_status | VARCHAR(20) | DEFAULT 未结清 | 结算状态 | 未结清/部分/已结清 |
   491	| settle_person | VARCHAR(50) | NULL | 结算人 | — |
   492	| remark | VARCHAR(200) | NULL | 备注 | — |
   493	
   494	### 表名：sale_order_item（销售明细表）
   495	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   496	| ------ | -------- | ---- | ---- | ---- |
   497	| order_id | BIGINT | NOT NULL | 销售主表 | — |
   498	| goods_id | BIGINT | NOT NULL | 商品 | — |
   499	| quantity | DECIMAL(18,3) | NOT NULL | 数量 | — |
   500	| price | DECIMAL(18,2) | NOT NULL | 单价 | — |
   501	| amount | DECIMAL(18,2) | NOT NULL | 金额 | — |
   502	| delivered_qty | DECIMAL(18,3) | DEFAULT 0 | 已出库 | — |
   503	| remark | VARCHAR(200) | NULL | 备注 | — |
   504	
   505	### 表名：return_order（退货主表）
   506	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   507	| ------ | -------- | ---- | ---- | ---- |
   508	| return_no | VARCHAR(30) | UNIQUE | 退货单号 | — |
   509	| src_type | VARCHAR(20) | NOT NULL | 来源 | PURCHASE/SALE |
   510	| src_id | BIGINT | NOT NULL | 来源单 | — |
   511	| partner_id | BIGINT | NOT NULL | 往来方 | 客户或供应商 |
   512	| reason | VARCHAR(200) | NOT NULL | 退货原因 | — |
   513	| amount | DECIMAL(18,2) | NOT NULL | 金额 | — |
   514	| status | VARCHAR(20) | DEFAULT 待审核 | 状态 | 待审核/已通过/已驳回/已完成 |
   515	| return_date | DATE | NOT NULL | 退货日期 | — |
   516	
   517	### 表名：purchase_bill（采购票据登记表）
   518	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   519	| ------ | -------- | ---- | ---- | ---- |
   520	| order_id | BIGINT | NOT NULL | 采购单 | 关联 purchase_order |
   521	| bill_type | VARCHAR(20) | DEFAULT 增值税专用发票 | 票据类型 | — |
   522	| bill_no | VARCHAR(50) | NULL | 票据号码 | — |
   523	| amount | DECIMAL(18,2) | NOT NULL | 票面金额 | — |
   524	| file_url | VARCHAR(200) | NULL | 附件 | 上传文件路径 |
   525	| register_time | DATETIME | NOT NULL | 登记时间 | — |
   526	
   527	### 表名：follow_ups（跟单节点表）
   528	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   529	| ------ | -------- | ---- | ---- | ---- |
   530	| doc_type | VARCHAR(20) | NOT NULL | 单据类型 | PURCHASE |
   531	| doc_id | BIGINT | NOT NULL | 单据 | — |
   532	| node_name | VARCHAR(50) | NOT NULL | 节点 | 下单/发货/到货/结算 |
   533	| node_status | VARCHAR(20) | NOT NULL | 状态 | 进行中/已完成 |
   534	| operator | VARCHAR(50) | NULL | 操作人 | — |
   535	| operate_time | DATETIME | NULL | 操作时间 | — |
   536	
   537	## 3.4 仓储系统（组D）
   538	
   539	### 表名：wms_inbound（入库主表）
   540	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   541	| ------ | -------- | ---- | ---- | ---- |
   542	| in_no | VARCHAR(30) | UNIQUE | 入库单号 | IN+日期+流水 |
   543	| in_type | VARCHAR(20) | NOT NULL | 入库类型 | PURCHASE采购到货/RETURN退货入库/OTHER |
   544	| src_no | VARCHAR(30) | NULL | 来源单号 | 采购单号/退货单号 |
   545	| warehouse_id | BIGINT | NOT NULL | 入库仓库 | 关联 sys_warehouse |
   546	| in_date | DATE | NOT NULL | 入库日期 | — |
   547	| total_amount | DECIMAL(18,2) | DEFAULT 0 | 总金额 | — |
   548	| operator | VARCHAR(50) | NOT NULL | 入库人 | — |
   549	| status | VARCHAR(20) | DEFAULT 已入库 | 状态 | 草稿/已入库/已审核 |
   550	
   551	### 表名：wms_inbound_item（入库明细表）
   552	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   553	| ------ | -------- | ---- | ---- | ---- |
   554	| inbound_id | BIGINT | NOT NULL | 入库主表 | — |
   555	| goods_id | BIGINT | NOT NULL | 商品 | — |
   556	| quantity | DECIMAL(18,3) | NOT NULL | 数量 | — |
   557	| price | DECIMAL(18,2) | NULL | 入库单价 | 采购价 |
   558	| amount | DECIMAL(18,2) | NULL | 金额 | — |
   559	
   560	### 表名：wms_outbound（出库主表）
   561	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   562	| ------ | -------- | ---- | ---- | ---- |
   563	| out_no | VARCHAR(30) | UNIQUE | 出库单号 | OUT+日期+流水 |
   564	| out_type | VARCHAR(20) | NOT NULL | 出库类型 | SALE销售出库/RETURN退货出库/OTHER |
   565	| src_no | VARCHAR(30) | NULL | 来源单号 | 销售单号/退货单号 |
   566	| warehouse_id | BIGINT | NOT NULL | 出库仓库 | — |
   567	| out_date | DATE | NOT NULL | 出库日期 | — |
   568	| operator | VARCHAR(50) | NOT NULL | 出库人 | — |
   569	| status | VARCHAR(20) | DEFAULT 已出库 | 状态 | — |
   570	
   571	### 表名：wms_outbound_item（出库明细表）
   572	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   573	| ------ | -------- | ---- | ---- | ---- |
   574	| outbound_id | BIGINT | NOT NULL | 出库主表 | — |
   575	| goods_id | BIGINT | NOT NULL | 商品 | — |
   576	| quantity | DECIMAL(18,3) | NOT NULL | 数量 | — |
   577	
   578	### 表名：wms_stock（商品库存表）
   579	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   580	| ------ | -------- | ---- | ---- | ---- |
   581	| warehouse_id | BIGINT | NOT NULL | 仓库 | 关联 sys_warehouse |
   582	| goods_id | BIGINT | NOT NULL | 商品 | 关联 goods |
   583	| quantity | DECIMAL(18,3) | DEFAULT 0 | 当前库存 | 唯一(warehouse_id,goods_id) |
   584	| unit | VARCHAR(20) | NULL | 单位冗余 | — |
   585	| update_time | DATETIME | NULL | 更新时间 | — |
   586	
   587	### 表名：wms_stock_log（库存流水表）
   588	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   589	| ------ | -------- | ---- | ---- | ---- |
   590	| goods_id | BIGINT | NOT NULL | 商品 | — |
   591	| warehouse_id | BIGINT | NOT NULL | 仓库 | — |
   592	| change_type | VARCHAR(20) | NOT NULL | 变动类型 | 采购入库/销售出库/盘点/调拨 等 |
   593	| change_qty | DECIMAL(18,3) | NOT NULL | 变动数量 | 正入负出 |
   594	| before_qty | DECIMAL(18,3) | NOT NULL | 变动前 | — |
   595	| after_qty | DECIMAL(18,3) | NOT NULL | 变动后 | — |
   596	| ref_no | VARCHAR(30) | NULL | 关联单号 | — |
   597	| operator | VARCHAR(50) | NOT NULL | 操作人 | — |
   598	| change_time | DATETIME | NOT NULL | 变动时间 | 索引优化查询 |
   599	
   600	### 表名：wms_check（盘点单主表）
   601	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   602	| ------ | -------- | ---- | ---- | ---- |
   603	| check_no | VARCHAR(30) | UNIQUE | 盘点单号 | — |
   604	| warehouse_id | BIGINT | NOT NULL | 盘点仓库 | — |
   605	| check_date | DATE | NOT NULL | 盘点日期 | — |
   606	| status | VARCHAR(20) | DEFAULT 草稿 | 状态 | 草稿/盘点中/已完成 |
   607	| checker | VARCHAR(50) | NULL | 盘点人 | — |
   608	
   609	### 表名：wms_check_item（盘点明细表）
   610	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   611	| ------ | -------- | ---- | ---- | ---- |
   612	| check_id | BIGINT | NOT NULL | 盘点单 | — |
   613	| goods_id | BIGINT | NOT NULL | 商品 | — |
   614	| book_qty | DECIMAL(18,3) | NOT NULL | 账面数 | 当前库存 |
   615	| real_qty | DECIMAL(18,3) | NOT NULL | 实盘数 | — |
   616	| diff_qty | DECIMAL(18,3) | DEFAULT 0 | 差异数 | 盈正亏负 |
   617	
   618	### 表名：wms_transfer（调拨单）
   619	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   620	| ------ | -------- | ---- | ---- | ---- |
   621	| transfer_no | VARCHAR(30) | UNIQUE | 调拨单号 | — |
   622	| from_warehouse | BIGINT | NOT NULL | 调出仓库 | — |
   623	| to_warehouse | BIGINT | NOT NULL | 调入仓库 | — |
   624	| goods_id | BIGINT | NOT NULL | 商品 | — |
   625	| quantity | DECIMAL(18,3) | NOT NULL | 数量 | — |
   626	| status | VARCHAR(20) | DEFAULT 待出库 | 状态 | 待出库/已出库/已完成 |
   627	| applicant | VARCHAR(50) | NOT NULL | 申请人 | — |
   628	| apply_time | DATETIME | NOT NULL | 申请时间 | — |
   629	
   630	## 3.5 财务系统（组D）
   631	
   632	### 表名：fin_account（资金账户表）
   633	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   634	| ------ | -------- | ---- | ---- | ---- |
   635	| name | VARCHAR(50) | NOT NULL | 账户名 | 公司银行账号/微信/支付宝 等 |
   636	| account_no | VARCHAR(50) | NULL | 账号 | — |
   637	| begin_balance | DECIMAL(18,2) | DEFAULT 0 | 期初余额 | — |
   638	| balance | DECIMAL(18,2) | DEFAULT 0 | 当前余额 | — |
   639	| bank | VARCHAR(50) | NULL | 开户行 | — |
   640	| status | VARCHAR(20) | DEFAULT 启用 | 状态 | 启用/停用 |
   641	
   642	### 表名：fin_con_list（收付款单主表）
   643	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   644	| ------ | -------- | ---- | ---- | ---- |
   645	| list_no | VARCHAR(30) | UNIQUE | 收/付款单号 | — |
   646	| list_type | VARCHAR(20) | NOT NULL | 类型 | RECEIPT收/PAYMENT付 |
   647	| orders_key | VARCHAR(30) | UNIQUE | 关联单号 | 销售单/采购单 |
   648	| partner_id | BIGINT | NOT NULL | 往来方 | 客户或供应商 |
   649	| account_id | BIGINT | NOT NULL | 资金账户 | 关联 fin_account |
   650	| all_money | DECIMAL(18,2) | NOT NULL | 金额 | — |
   651	| pay_type | VARCHAR(50) | NULL | 付款方式 | 银行转账/现金 等 |
   652	| receipt_date | DATE | NOT NULL | 收/付款日期 | — |
   653	| states | VARCHAR(20) | DEFAULT 草稿 | 状态 | 草稿/已审核/已入账 |
   654	| payer | VARCHAR(50) | NULL | 交款人 | 收/付款人 |
   655	| order_amount | DECIMAL(18,2) | DEFAULT 0 | 关联单金额 | 回填核对 |
   656	| is_dingdao | VARCHAR(20) | NULL | 到账标志 | — |
   657	| remark | VARCHAR(200) | NULL | 备注 | — |
   658	
   659	### 表名：fin_receipt_rel（应收核销关联表）
   660	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   661	| ------ | -------- | ---- | ---- | ---- |
   662	| list_id | BIGINT | NOT NULL | 收款单 | 关联 fin_con_list |
   663	| arc_detail_id | BIGINT | NOT NULL | 应收明细 | 关联 crm_arc_detail |
   664	| amount | DECIMAL(18,2) | NOT NULL | 核销金额 | — |
   665	
   666	### 表名：fin_payable_rel（应付核销关联表）
   667	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   668	| ------ | -------- | ---- | ---- | ---- |
   669	| list_id | BIGINT | NOT NULL | 付款单 | 关联 fin_con_list |
   670	| ap_detail_id | BIGINT | NOT NULL | 应付明细 | 关联 crm_ap_detail |
   671	| amount | DECIMAL(18,2) | NOT NULL | 核销金额 | — |
   672	
   673	### 表名：fin_transfer（转账单）
   674	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   675	| ------ | -------- | ---- | ---- | ---- |
   676	| transfer_no | VARCHAR(30) | UNIQUE | 转账单号 | — |
   677	| from_account | BIGINT | NOT NULL | 转出账户 | — |
   678	| to_account | BIGINT | NOT NULL | 转入账户 | — |
   679	| amount | DECIMAL(18,2) | NOT NULL | 金额 | — |
   680	| status | VARCHAR(20) | DEFAULT 待审批 | 状态 | 待审批/已审批/已完成 |
   681	| applicant | VARCHAR(50) | NOT NULL | 申请人 | — |
   682	| apply_time | DATETIME | NOT NULL | 申请时间 | — |
   683	
   684	### 表名：fin_account_log（资金流水表）
   685	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   686	| ------ | -------- | ---- | ---- | ---- |
   687	| account_id | BIGINT | NOT NULL | 账户 | — |
   688	| biz_type | VARCHAR(20) | NOT NULL | 业务类型 | 收款/付款/转出/转入 等 |
   689	| ref_no | VARCHAR(30) | NULL | 关联单号 | — |
   690	| in_amount | DECIMAL(18,2) | DEFAULT 0 | 收入 | — |
   691	| out_amount | DECIMAL(18,2) | DEFAULT 0 | 支出 | — |
   692	| balance_after | DECIMAL(18,2) | NOT NULL | 变动后余额 | — |
   693	| biz_date | DATE | NOT NULL | 业务日期 | — |
   694	| operator | VARCHAR(50) | NOT NULL | 操作人 | — |
   695	
   696	## 3.6 业务报表系统（组E）
   697	
   698	### 表名：rpt_snapshot（报表快照表）
   699	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   700	| ------ | -------- | ---- | ---- | ---- |
   701	| rpt_code | VARCHAR(30) | NOT NULL | 报表编码 | RPT_INVENTORY 等 |
   702	| biz_date | DATE | NOT NULL | 业务日期 | — |
   703	| content | JSON | NULL | 快照数据 | 预聚合结果 |
   704	| generate_time | DATETIME | NOT NULL | 生成时间 | — |
   705	
   706	## 3.7 定时任务系统（组E）
   707	
   708	### 表名：job_task（任务定义表）
   709	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   710	| ------ | -------- | ---- | ---- | ---- |
   711	| job_code | VARCHAR(30) | UNIQUE | 任务编码 | job_stock_warning |
   712	| job_name | VARCHAR(50) | NOT NULL | 任务名 | 定时货物预警 |
   713	| cron_expr | VARCHAR(50) | NOT NULL | 表达式 | 0 0 8 * * ? 等 |
   714	| job_group | VARCHAR(30) | NULL | 分组 | STOCK/REPORT 等 |
   715	| enabled | TINYINT | DEFAULT 1 | 是否启用 | 1启用 0停用 |
   716	| description | VARCHAR(200) | NULL | 描述 | — |
   717	
   718	### 表名：job_task_log（任务执行日志表）
   719	| 字段名 | 数据类型 | 约束 | 注释 | 备注 |
   720	| ------ | -------- | ---- | ---- | ---- |
   721	| job_id | BIGINT | NOT NULL | 任务 | 关联 job_task |
   722	| start_time | DATETIME | NOT NULL | 开始时间 | — |
   723	| end_time | DATETIME | NULL | 结束时间 | — |
   724	| result | VARCHAR(20) | NOT NULL | 结果 | SUCCESS/FAIL |
   725	| message | VARCHAR(500) | NULL | 执行信息 | 错误/结果摘要 |
   726	| operator | VARCHAR(50) | NULL | 触发人 | 手动触发时 |
   727	
   728	---
   729	
   730	# 四、业务逻辑
   731	
   732	> 业务逻辑贯穿通用链条：**生成单据号 → 校验 → 保存（事务） → 审批 → 联动上下游 → 回写状态 → 写审计日志**。以下按子系统逐功能说明实现要点。
   733	
   734	## 4.1 基础维护系统（组A）
   735	
   736	### 4.1.1 地区管理（BM-01）
   737	- 树形 `parent_id=0` 为根，递归查询子节点，`sort` 排序
   738	- 新增/编辑：校验同级名称唯一；删除：存在子地区或已被公司引用时禁止
   739	
   740	### 4.1.2 分公司管理（BM-02）
   741	- 编码 `code` 全局唯一；选择地区下拉（树）；状态启用/停用控制登录归属
   742	- 删除校验：被部门引用或有员工归属时禁止，改为停用
   743	
   744	### 4.1.3 部门管理（BM-03）
   745	- 按公司维度展示部门树；记录负责人；停用部门禁止新增员工归属
   746	
   747	### 4.1.4 员工管理（BM-04）
   748	- 工号唯一；保存时同步插入/更新 `sys_user` 账号（初始密码 123456）实现"建员工=开账号"
   749	- 可勾选分配一个或多个角色，写入 `sys_user_role`（默认给基础角色）
   750	
   751	### 4.1.5 角色维护（BM-05）
   752	- 角色 CRUD；点击"授权"进入资源树勾选，保存写 `sys_role_resource`
   753	- admin 内置全权限，禁止删除；删除角色需无用户引用
   754	
   755	### 4.1.6 资源维护（BM-06）
   756	- 四级资源树：L1系统 → L2模块 → L3菜单 → L4按钮（权限码如 `base:region:list`）
   757	- 前端登录后按权限码过滤按钮显隐；后端注解 `@PreAuthorize` 二次拦截
   758	
   759	### 4.1.7 仓库管理（BM-07）
   760	- 仓库档案；停用仓库禁止入库/出库；初始化种子 6 条（WH001~WH006）
   761	
   762	### 4.1.8 用户-角色授权（BM-08）
   763	- 用户列表关联角色；授权变更写审计日志；登录态缓存按需刷新
   764	
   765	### 4.1.9 登录/改密（BM-09）
   766	- 登录：用户名+密码校验 → 生成 JWT（含 userId/username/roles）→ 写审计"登录系统"
   767	- 改密：校验旧密码 → 新密码强度（≥6 位含字母数字）→ 更新并提示重新登录
   768	
   769	### 4.1.10 公共配置（BM-10）
   770	- 系统参数：键值对维护，供报表路径等使用（读写分离缓存）
   771	- 字典维护：`dict_type` 统一各单据状态下拉
   772	- 车辆/会议室：启停状态维护，供运输调度与资源查询引用
   773	- 审批规则：配置各单据类型审批角色与层级，供进销存联动
   774	- 编码规则：配置各单据前缀/格式/流水位，生成时段内串行加锁
   775	- 审计查询：按操作人/动作/时间范围分页查询 `sys_audit_log`
   776	
   777	## 4.2 CRM 系统（组B）
   778	
   779	### 4.2.1 客户档案例建/维护（CRM-01）
   780	- 新增：编码自动生成（`C+流水`）也可手输，名称/分类/信用额度必填
   781	- 提交后进入审批流（`approval_status`：草稿→待审批→已通过/已驳回），驳回可修改重提
   782	- 编辑仅限已通过或草稿；已通过后修改需重提审批（除备注等次要字段）
   783	- 冻结客户：禁止继续下单，已有单据不受影响
   784	
   785	### 4.2.2 供应商档案例建/维护（CRM-02）
   786	- 档案维护 + 应付累计 `payable_amount` 由付款核销回写
   787	- 停用供应商：禁止新增采购单
   788	
   789	### 4.2.3 伙伴分类维护（CRM-03）
   790	- 客户与供应商共用分类树，`kind` 区分；删除前校验分类下无伙伴
   791	
   792	### 4.2.4 信用管理（CRM-04）
   793	- 客户表冗余 `used_credit`：新增待结清销售单时累加，回款/结清时扣减
   794	- 可用额度 = 信用额度 - 已用额度；下单时校验剩余可用额度 > 0，否则拦截并提示
   795	- 变更信用额度/额度超限写 `crm_credit_log`
   796	
   797	### 4.2.5 跟进记录（CRM-05）
   798	- 按客户时间线展示；`next_time` 早于今天的记录在首页待办高亮
   799	- 记录人自动取当前登录用户
   800	
   801	### 4.2.6 应收/应付对账（CRM-06）
   802	- 按伙伴汇总收/应付明细表，展示 期初→发生→核销→余额
   803	- 未结清/部分/已结清三种状态；结清后明细关闭
   804	
   805	### 4.2.7 客户合并（CRM-07）
   806	- 选择从客户与主客户：将从客户单据、应收明细、跟进记录转移至主客户
   807	- 从客户标记停用并记录 `merge_from`，过程写审计
   808	
   809	## 4.3 进销存系统（组C）
   810	
   811	### 4.3.1 商品档案维护（INV-01）
   812	- 编码唯一；进价/售价必填；保存后 `goods` 与库存表初始化为 0
   813	- 状态在售/停售；停售商品禁止新增销售单明细
   814	
   815	### 4.3.2 商品分类/计量单位（INV-02、03）
   816	- 分类树维护；单位维护换算率，多单位展示换算
   817	
   818	### 4.3.3 采购需求（INV-04）
   819	- 需求登记（商品/数量/需求日期/原因）；"生成采购单"按需求聚合同供应商生成 PO
   820	- 转单后需求状态置为"已生成采购"，防止重复转单
   821	
   822	### 4.3.4 采购单+审批（INV-05）
   823	- 采购单状态机：`草稿 → 待审批 → 已通过(采购审批) → 采购中 → 部分到货 → 已到货 → 结算 → 结单`；驳回回草稿
   824	- 提交审批：按 `approval_rule` 写入 `approval_record`，审批通过更新 `audit_status=已审核、approve_person/approve_time`（试算当前用户即走"自审"便于演示）
   825	- 通过后自动在 `wms_inbound` 侧登记待入库线索，并给供应商追加应付明细 `crm_ap_detail`
   826	
   827	### 4.3.5 采购报备/票据（INV-06）
   828	- 对采购单登记票据类型/号码/票面金额/附件，回写票据登记表
   829	
   830	### 4.3.6 车辆调度（INV-07）
   831	- 选中采购单分配 `vehicle_id` 并置车辆状态"在途"；卸货/结单后置回"空闲"
   832	- 同车可挂多单，显示车辆当前状态
   833	
   834	### 4.3.7 运输任务（INV-08）
   835	- 创建运输任务（选单/车辆/司机/预计到达）→ 指派 → 签收，节点写 `follow_ups`
   836	
   837	### 4.3.8 到货入库登记（INV-09）
   838	- 采购单明细按商品录入实收数量 → 生成 `wms_inbound`（来源=采购）→ 触发"入库事务"：加库存、写库存流水、回写明细 `received_qty`
   839	- 全部到齐 → 单据状态"已到货"，否则"部分到货"
   840	
   841	### 4.3.9 采购结算（INV-10）
   842	- 结算=生成应付确认：把应付明细状态置待核销，供财务付款核销；完成后单据状态"结算"
   843	- 金额口径 = 明细金额×(1+税率)
   844	
   845	### 4.3.10 采购跟单结单（INV-11）
   846	- `follow_ups` 节点跟踪（下单→发货→到货→结算）；所有节点完成可"结单"关闭采购单，防止再操作
   847	
   848	### 4.3.11 销售订单（INV-12）
   849	- 订单登记：明细含商品/数量/单价/折扣；金额=Σ(数量×单价)-折扣
   850	- 提交订单前调用信用校验（`used_credit`）；超限拦截提示
   851	- 状态机：`草稿 → 待审批 → 已通过 → 出库中 → 部分出库 → 已出库 → 已结算 → 结单`
   852	
   853	### 4.3.12 销售单+审批（INV-13）
   854	- 同采购审批逻辑；通过后向 `wms_outbound` 侧登记出库线索，并给客户追加应收明细 `crm_arc_detail`、累加 `used_credit`
   855	
   856	### 4.3.13 出库发货（INV-14）
   857	- 销售单明细按商品出库 → 生成 `wms_outbound`（来源=销售）→ "出库事务"：减库存（实时校验库存充足）、写流水、回写 `delivered_qty`
   858	
   859	### 4.3.14 销售回款/结单（INV-15）
   860	- 回款走财务收款单核销；全部核销后应收明细"已结清"→ 扣减 `used_credit`，销售单 `settle_status=已结清` → 可结单
   861	- 结单后锁定单据，退款走退货单
   862	
   863	### 4.3.15 退货处理
   864	- 采购退→`return_order(来源=PURCHASE)`：通过后生成出库单，冲减应付
   865	- 销售退→`return_order(来源=SALE)`：通过后生成入库单回补库存，冲减应收与 `used_credit`
   866	
   867	## 4.4 仓储系统（组D）
   868	
   869	### 4.4.1 入库单（WMS-01）
   870	- 来源：采购到货/销售退货/其它；保存主从表，审核后执行入库事务
   871	- 入库事务（必须同事务）：查 `wms_stock`，无则创建 → `quantity+=x` → 写 `wms_stock_log(before/after/ref_no)` → 回写来源单据实收
   872	
   873	### 4.4.2 出库单（WMS-02）
   874	- 出库事务（必须同事务）：校验库存足够（`quantity>=x` 否则抛"库存不足"）→ `quantity-=x` → 写流水 → 回写来源单据实发
   875	
   876	### 4.4.3 库存流水（WMS-03）
   877	- 每笔出入库/盘点/调拨均写流水，支持按商品/仓库/时间/类型过滤，实现全链路追溯
   878	
   879	### 4.4.4 库存统计（WMS-04）
   880	- 按仓库×商品联表查询实时库存；支持商品编码/名称模糊、分类筛选；分页返回
   881	
   882	### 4.4.5 库存上下限（WMS-05）
   883	- 商品档案维护 low/high；低于下限时在首页待办预警（数据来源：定时任务扫描）
   884	- 可在此功能手工设置阈值并立即触发一次预警扫描
   885	
   886	### 4.4.6 盘点（WMS-06）
   887	- 生成盘点单（选仓库/日期）→ 系统自动带出账面数 → 录入实盘数 → 计算差异 `diff_qty`
   888	- 差异未处理时盘点单状态"盘点中"
   889	
   890	### 4.4.7 盘盈盘亏调整（WMS-07）
   891	- 对盘点差异生成调整单，审核后执行库存调整事务：`quantity+=diff`、写流水（类型=盘盈/盘亏）
   892	- 调整完成后盘点单状态"已完成"
   893	
   894	### 4.4.8 货物转接（调拨）（WMS-08）
   895	- 调拨单：调出仓→调入仓，审核通过先减调出仓库存、后加调入仓库存（同事务），写两条流水（配平）
   896	- 状态：待出库→已出库→已完成
   897	
   898	### 4.4.9 库位管理（WMS-09）
   899	- 可选扩展：仓库下挂库位，库存表增加 `location` 维度；本期提供库位查询与绑定界面
   900	
   901	## 4.5 业务报表系统（组E）
   902	
   903	### 4.5.1 采购报表（RPT-01）
   904	- 维度：按供应商/商品/时间区间聚合 `purchase_order`，输出数量、金额（含税）、平均单价
   905	- 大表聚合用 MySQL GROUP BY，数据量小无需预聚合
   906	
   907	### 4.5.2 销售报表（RPT-02）
   908	- 按客户/商品/时间聚合 `sale_order`，输出数量、金额、折扣、毛利估计
   909	
   910	### 4.5.3 库存报表（RPT-03）
   911	- 实时库存表 + 低库存（quantity<low_limit）TOP 清单 + 周转率（期间出库量/平均库存）
   912	
   913	### 4.5.4 财务收支报表（RPT-04）
   914	- 按账户/时间：期初余额 + 期间收入 - 期间支出 = 期末余额，取自 `fin_account_log` 按天聚合
   915	
   916	### 4.5.5 应收应付账龄报表（RPT-05）
   917	- 按账龄区间（0-30/31-60/61-90/>90 天）统计未核销金额，`due_date` 计算账龄
   918	
   919	### 4.5.6 利润汇总报表（RPT-06）
   920	- 销售毛利 = 销售收入 - 销售成本（成本取商品 `purchase_price` 或加权平均），按期间/商品汇总
   921	
   922	### 4.5.7 伙伴贡献度（RPT-07）
   923	- 客户：按销售额/毛利排行 TOP N；供应商：按采购额排行；饼图+表格展示
   924	
   925	### 4.5.8 报表导出（RPT-08）
   926	- 前端基于当前查询结果导出 Excel（xlsx），服务端可选提供 CSV 导出接口；导出走 `report.path` 参数指定目录
   927	
   928	## 4.6 定时任务系统（组E）
   929	
   930	### 4.6.1 定时货物预警（JOB-01）
   931	- 每日 08:00 扫描 `wms_stock` join `goods`：`quantity < low_limit` 且状态在售 → 写入预警列表（存入 job_task_log.message 与首页待办）
   932	
   933	### 4.6.2 安全预警（JOB-02）
   934	- 扫描商品期限/批次信息（扩展字段）临期 → 预警；演示可用"上次进价超过 N 天未变"替代演示逻辑
   935	
   936	### 4.6.3 常用商品频度统计（JOB-03）
   937	- T+1（每日 01:00）按商品聚合近 N 日销售频次写入 `rpt_snapshot`
   938	
   939	### 4.6.4 定时报表（JOB-04）
   940	- 按 cron（如每日 06:00）预生成报表快照写入 `rpt_snapshot`，报表页优先读快照、可选实时刷新
   941	
   942	### 4.6.5 任务执行日志（JOB-05）
   943	- 每次执行记录 start/end/result/message 到 `job_task_log`，页面可查执行历史、失败原因
   944	
   945	### 4.6.6 任务启停管理（JOB-06）
   946	- 页面开关动态改 `enabled`，调度器按周期重载；手动"立即执行一次"
   947	
   948	### 4.6.7 数据归档/清理（JOB-07）
   949	- 按规则将超期结单数据软归档（`deleted=1` 或归档表），控制主表膨胀；每日执行，保留最近 N 天
   950	
   951	## 4.7 财务系统（组D）
   952	
   953	### 4.7.1 资金账户管理（FIN-01）
   954	- 账户维护（名称/账号/期初/余额）；期初计入 `fin_account_log` 首笔余额；停用账户禁止收付款与转账
   955	
   956	### 4.7.2 收款单（FIN-02）
   957	- 关联销售单（`orders_key=销售单号`）→ 自动带出应收明细 → 逐条录入核销金额 → 收款事务：
   958	  - 账户 `balance += 收款额`，写 `fin_account_log`（业务类型=收款）
   959	  - 核销应收明细：`received+=x、balance-=x`，余额=0 置"已结清"
   960	  - 回写销售单 `received_amount`、客户 `used_credit -= x`、`debt_amount -= x`
   961	- 单金额不得超过未核销余额；整单核销完成则 `states=已入账`
   962	
   963	### 4.7.3 付款单（FIN-03）
   964	- 关联采购单 → 带出应付明细 → 核销收款事务（方向相反）：
   965	  - 账户 `balance -= 付款额`（校验余额充足），写流水（业务类型=付款）
   966	  - 核销应付：`paid+=x、balance-=x`
   967	  - 回写供应商 `payable_amount -= x`
   968	
   969	### 4.7.4 应收核销（FIN-04）
   970	- 即收款单的核销环节的独立视图：按客户展示未结清应收明细，勾选核销金额生成收款单
   971	
   972	### 4.7.5 应付核销（FIN-05）
   973	- 按供应商展示未结清应付明细，勾选生成付款单
   974	
   975	### 4.7.6 跨系统转账（FIN-06）
   976	- 转账单：转出户-、转入户+（同事务），写两条流水（业务类型=转出/转入）
   977	- 需审批（`approval_record`），审批通过后入账
   978	
   979	### 4.7.7 资金流水（FIN-07）
   980	- 按账户分页查询 `fin_account_log`，支持类型/日期/关联单号过滤；展示变动前后余额
   981	
   982	---
   983	
   984	# 五、相关接口
   985	
   986	> 接口格式：`对应的功能 | path | 请求方式 | 参数 | 返回`。统一前缀 `/api`，响应统一 `{code, message, data}`，分页 `data={total, list}`。以下为核心接口清单（按子系统归纳，同一资源的增删改查合并列出）。
   987	
   988	## 5.1 认证与平台基座（组A）
   989	| 项 | 内容 |
   990	| --- | --- |
   991	| 对应的功能 | 用户登录 / 安全退出 / 当前用户 / 修改密码 |
   992	| path | POST /api/auth/login，POST /api/auth/logout，GET /api/auth/info，POST /api/auth/password |
   993	| 请求方式 | POST/POST/GET/POST |
   994	| 参数 | 登录：`{username, password}`；改密：`{oldPassword, newPassword}` |
   995	| 返回 | 登录：`{token, user:{id,username,name,roles}}`；info：`{user, menus, permissions}` |
   996	
   997	| 项 | 内容 |
   998	| --- | --- |
   999	| 对应的功能 | 地区 / 分公司 / 部门 / 员工 / 角色 / 资源管理 |
  1000	| path | GET /api/regions，POST/GET /api/regions；GET /api/companys，POST /api/companys，PUT /api/companys/{id}，DELETE /api/companys/{id}；同理 /api/depts、/api/employees、/api/roles、/api/resources |
  1001	| 请求方式 | RESTful |
  1002	| 参数 | 树查询 parentId；分页 `{page,pageSize,keyword}`；保存传实体 JSON |
  1003	| 返回 | 树：`[{id,name,children}]`；分页：`{total,list}` |
  1004	
  1005	| 项 | 内容 |
  1006	| --- | --- |
  1007	| 对应的功能 | 角色授权 / 员工-角色授权 / 仓库管理 |
  1008	| path | PUT /api/roles/{id}/resources（授权），GET /api/roles/{id}/resources；PUT /api/employees/{id}/roles；GET/POST/PUT/DELETE /api/warehouses |
  1009	| 请求方式 | PUT/GET/PUT/RESTful |
  1010	| 参数 | 授权：`{resourceIds:[...]}`；保存传实体 JSON |
  1011	| 返回 | 成功 `{code:200,message:"成功"}`；授权树回显 `{resourceIds}` |
  1012	
  1013	| 项 | 内容 |
  1014	| --- | --- |
  1015	| 对应的功能 | 系统参数 / 字典 / 车辆 / 会议室 / 编码规则 / 审批规则 / 审计查询 |
  1016	| path | GET/POST/PUT /api/params；GET/POST/PUT /api/dicts；GET/POST/PUT/DELETE /api/vehicles；/api/meetings；/api/code-rules；/api/approval-rules；GET /api/audit-logs |
  1017	| 请求方式 | RESTful + 查询分页 |
  1018	| 参数 | 字典：`{dictType}` 过滤；审计：`{operator,action,startTime,endTime,page,pageSize}` |
  1019	| 返回 | 分页 `{total,list}` |
  1020	
  1021	## 5.2 客户与供应商（组B）
  1022	| 项 | 内容 |
  1023	| --- | --- |
  1024	| 对应的功能 | 客户 / 供应商档案例建、维护、审批、信用、跟进、对账、合并 |
  1025	| path | GET/POST /api/customers，PUT /api/customers/{id}，DELETE /api/customers/{id}，POST /api/customers/{id}/approve（审批），POST /api/customers/{id}/follow（跟进），PUT /api/customers/{id}/credit（信用变更），POST /api/customers/merge（合并）；同理 /api/suppliers |
  1026	| 请求方式 | RESTful |
  1027	| 参数 | 建档：`{code,name,categoryId,linkman,phone,address,creditLimit}`；审批：`{approve:true,comment}`；跟进：`{content,nextTime}`；合并：`{fromId,toId}` |
  1028	| 返回 | 列表分页：`{total,list}`；明细 `{...customer}`；信用：`{available}` |
  1029	| 其他 | 供应商特有的应付视图：GET /api/suppliers/{id}/payables |
  1030	
  1031	| 项 | 内容 |
  1032	| --- | --- |
  1033	| 对应的功能 | 应收 / 应付对账明细 |
  1034	| path | GET /api/customers/{id}/receivables，GET /api/suppliers/{id}/payables |
  1035	| 请求方式 | GET |
  1036	| 参数 | `{page,pageSize,status}` |
  1037	| 返回 | `{total,list:[{refNo,amount,received,balance,dueDate,status}]}` |
  1038	
  1039	## 5.3 进销存（组C）
  1040	| 项 | 内容 |
  1041	| --- | --- |
  1042	| 对应的功能 | 商品 / 分类 / 单位管理 |
  1043	| path | GET/POST /api/goods，PUT/DELETE /api/goods/{id}，GET /api/goods/categories，POST /api/goods/categories，GET/POST/DELETE /api/goods/units |
  1044	| 请求方式 | RESTful |
  1045	| 参数 | 商品：`{code,name,categoryId,unitId,spec,brand,spec, purchasePrice,salePrice,lowLimit,highLimit,status}` |
  1046	| 返回 | 分页 `{total,list}`；分类树 `[{id,name,children}]` |
  1047	
  1048	| 项 | 内容 |
  1049	| --- | --- |
  1050	| 对应的功能 | 采购需求 / 采购单 / 采购审批 / 车辆调度 / 到货入库 / 采购结算 / 跟单结单 |
  1051	| path | GET/POST /api/purchase/demands，POST /api/purchase/demands/{ids}/convert；GET/POST /api/purchase/orders，GET /api/purchase/orders/{id}，PUT /api/purchase/orders/{id}，POST /api/purchase/orders/{id}/submit（提交审批），POST /api/purchase/orders/{id}/approve，POST /api/purchase/orders/{id}/dispatch（调车），POST /api/purchase/orders/{id}/arrival（到货登记），POST /api/purchase/orders/{id}/settle（结算），POST /api/purchase/orders/{id}/close（结单） |
  1052	| 请求方式 | RESTful |
  1053	| 参数 | 保存：`{supplierId,applyDate,taxRate,remark,items:[{goodsId,quantity,price}]}`；到货：`{warehouseId,arrivalDate,items:[{goodsId,receivedQty}]}`；调车：`{vehicleId}` |
  1054	| 返回 | 主表详情含明细；操作类返回 `{code,message}` 与最新状态 |
  1055	
  1056	| 项 | 内容 |
  1057	| --- | --- |
  1058	| 对应的功能 | 销售订单 / 销售审批 / 出库发货 / 回款结单 |
  1059	| path | GET/POST /api/sale/orders，GET /api/sale/orders/{id}，PUT /api/sale/orders/{id}，POST /api/sale/orders/{id}/submit，POST /api/sale/orders/{id}/approve，POST /api/sale/orders/{id}/deliver（发货），POST /api/sale/orders/{id}/close |
  1060	| 请求方式 | RESTful |
  1061	| 参数 | 保存：`{customerId,orderDate,discount,warehouseId,items:[{goodsId,quantity,price}]}`；发货：`{warehouseId,items:[{goodsId,qty}]}` |
  1062	| 返回 | 发货时库存不足返回 `{code:500,message:"库存不足：商品名 仅剩 x"}` |
  1063	
  1064	| 项 | 内容 |
  1065	| --- | --- |
  1066	| 对应的功能 | 退货单 / 采购票据 / 运输任务 / 跟单节点 |
  1067	| path | GET/POST /api/returns，POST /api/returns/{id}/approve；GET/POST /api/purchase/bills；GET/POST /api/transports，POST /api/transports/{id}/receive；GET /api/purchase/orders/{id}/follows |
  1068	| 请求方式 | RESTful |
  1069	| 参数 | 退货：`{srcType,srcId,reason,amount}`；票据：`{orderId,billType,billNo,amount}` |
  1070	| 返回 | 分页/详情 + 操作结果 |
  1071	
  1072	## 5.4 仓储（组D）
  1073	| 项 | 内容 |
  1074	| --- | --- |
  1075	| 对应的功能 | 入库 / 出库 / 库存 / 流水 / 盘点 / 盘盈亏 / 调拨 / 库位 |
  1076	| path | GET/POST /api/wms/inbounds，GET /api/wms/inbounds/{id}，POST /api/wms/inbounds/{id}/submit；同理 /api/wms/outbounds；GET /api/wms/stocks，GET /api/wms/stock-logs，GET/POST /api/wms/checks，POST /api/wms/checks/{id}/adjust，GET/POST /api/wms/transfers，POST /api/wms/transfers/{id}/approve，GET/POST /api/wms/locations |
  1077	| 请求方式 | RESTful |
  1078	| 参数 | 入库：`{warehouseId,inDate,inType,srcNo,items:[{goodsId,quantity,price}]}`；调拨：`{fromWarehouse,toWarehouse,goodsId,quantity}` |
  1079	| 返回 | 库存页：`{total,list:[{goodsId,name,warehouseId,quantity,lowLimit,status}]}`；流水：分页含 beforeQty/afterQty |
  1080	
  1081	## 5.5 财务报表（组E）
  1082	| 项 | 内容 |
  1083	| --- | --- |
  1084	| 对应的功能 | 资金账户 / 收款 / 付款 / 核销 / 转账 / 资金流水 |
  1085	| path | GET/POST /api/finance/accounts，PUT /api/finance/accounts/{id}；GET/POST /api/finance/lists（收付通用），GET /api/finance/lists/{id}，POST /api/finance/lists/{id}/submit；POST /api/finance/transfers，POST /api/finance/transfers/{id}/approve；GET /api/finance/logs |
  1086	| 请求方式 | RESTful |
  1087	| 参数 | 收款：`{listType:"RECEIPT",ordersKey:销售单号,accountId,partnerId,receiptDate,details:[{detailId,amount}]}`；转账：`{fromAccount,toAccount,amount}` |
  1088	| 返回 | 保存返回单号与核销结果；转账余额不足返回 500+提示 |
  1089	
  1090	## 5.6 报表与定时任务（组E）
  1091	| 项 | 内容 |
  1092	| --- | --- |
  1093	| 对应的功能 | 采购 / 销售 / 库存 / 收支 / 账龄 / 利润 / 贡献度报表 |
  1094	| path | GET /api/reports/purchase，GET /api/reports/sale，GET /api/reports/inventory，GET /api/reports/finance，GET /api/reports/aging，GET /api/reports/profit，GET /api/reports/contribution |
  1095	| 请求方式 | GET |
  1096	| 参数 | 公共：`{startDate,endDate}`；按维度：`{supplierId/customerId/goodsId/warehouseId}` |
  1097	| 返回 | `data:{columns:[...], rows:[...], total}`，供前端表格+图表渲染 |
  1098	
  1099	| 项 | 内容 |
  1100	| --- | --- |
  1101	| 对应的功能 | 定时任务管理 / 执行日志 / 立即执行 |
  1102	| path | GET/POST /api/jobs，PUT /api/jobs/{id}（启停/改cron），POST /api/jobs/{id}/run（立即执行），GET /api/jobs/logs |
  1103	| 请求方式 | RESTful |
  1104	| 参数 | 任务：`{jobCode,jobName,cronExpr,enabled,description}` |
  1105	| 返回 | 分页 `{total,list}`；run 返回执行结果 message |
  1106	
  1107	---
  1108	
  1109	# 附录：交付清单与分工
  1110	| 模块 | 负责人 | 覆盖章节 |
  1111	| --- | --- | --- |
  1112	| 平台基座（登录/基础维护/权限/规则） | 组A | 3.1、4.1、5.1 |
  1113	| 客户关系 CRM | 组B | 3.2、4.2、5.2 |
  1114	| 进销存核心（商品/采购/销售/退货） | 组C | 3.3、4.3、5.3 |
  1115	| 仓储 + 财务 | 组D | 3.4、3.5、4.4、4.7、5.4、5.5 |
  1116	| 报表 + 定时任务 + 运维 | 组E | 3.6、3.7、4.5、4.6、5.6 |
  1117	
  1118	> 说明：本开发文档严格按模板「业务背景 / 功能梳理 / 数据库设计 / 业务逻辑 / 相关接口」五段式生成，内容对标原型数据（地区8/公司4/部门13/员工20/仓库6/车辆8/供应商12/客户15/商品40），可作为五人分组各自交付 AI 的编码依据。
  1119	
  1120	*（内容由AI生成，仅供参考）*
