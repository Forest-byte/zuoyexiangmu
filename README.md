# ERP 进销存一体化管理系统

依据《ERP 系统开发文档》开发的前后端分离进销存一体化系统，覆盖 **7 大子系统、63 个功能点**，支持 5 类角色分权协作、全流程可审计可追溯。界面交互对标原型（登录页、深蓝渐变主框架、9 大业务页面）。

---

## 一、技术栈

| 端 | 技术 |
| --- | --- |
| 后端 | Java 17 · Spring Boot 3.x · MyBatis（注解式 Mapper）· MySQL 8.0 · JWT 认证 · RBAC 权限 |
| 前端 | Vue 3.4 · Element Plus 2.7 · ECharts 5.5 · Axios · Pinia · Vue Router · Vite 5 |
| 部署 | 后端 Jar 内置前端构建产物，单服务一体化运行（亦支持前后端分离开发模式） |

## 二、系统架构

```
浏览器
  │  http://localhost:8080 （生产：后端托管前端；开发：5173 → /api 代理 8080）
  ▼
Spring Boot (8080)
  ├─ SpaForwardController  前端 SPA 路由回退
  ├─ AuthService/JwtInterceptor  JWT 认证 + 动态权限鉴权（admin 全放行）
  ├─ Controller ×12        采购/销售/仓储/财务/CRM/报表/任务/基础/权限/规则/认证
  ├─ Service ×15           业务事务（库存变动统一入口 doStockChange）
  └─ Mapper ×44            注解 SQL 数据访问
  ▼
MySQL 8.0（库 erp，55 张表 + 种子数据）
```

## 三、功能清单（7 大子系统 / 63 功能点）

| 子系统 | 编号 | 功能点 |
| --- | --- | --- |
| 基础维护（BM） | BM-01~10 | 地区、分公司、部门、员工、角色、资源（菜单/按钮四级）、仓库、员工-角色授权、用户登录/改密、系统参数/车辆/会议室/编码规则/字典/审批规则/审计 |
| 客户关系（CRM） | CRM-01~07 | 客户档案、供应商档案、伙伴分类、信用管理（额度/超限预警）、跟进记录、应收/应付对账、客户合并 |
| 进销存（INV） | INV-01~15 | 商品档案、商品分类、计量单位、采购需求（汇总转单）、采购单+审批、采购票据、车辆调度、运输任务、到货入库、采购结算（联动应付）、采购跟单结单（节点跟踪）、销售订单（信用校验）、销售单+审批、出库发货（联动库存）、销售回款/结单 |
| 仓储（WMS） | WMS-01~09 | 入库单、出库单、库存流水（逐笔可追溯）、库存统计、库存上下限预警、盘点、盘盈盘亏审核、货物转接（仓间调拨）、库位管理 |
| 业务报表（RPT） | RPT-01~08 | 采购报表、销售报表、库存报表、财务收支报表、应收应付账龄、利润汇总、伙伴贡献度、报表导出 |
| 定时任务（JOB） | JOB-01~07 | 货物预警、安全预警、频度统计、定时报表快照、任务执行日志、任务启停、数据归档 |
| 财务（FIN） | FIN-01~07 | 资金账户、收款单（销收核销+入账）、付款单（采付核销+出账）、应收核销、应付核销、跨账户转账（含审批）、资金流水 |

## 四、演示账号（5 类角色）

| 账号 | 密码 | 角色 | 权限域 |
| --- | --- | --- | --- |
| admin | admin123 | 系统管理员 | 全部权限（admin 全放行） |
| purchase | 123456 | 采购员 | 采购域 + 基础 + 供应商 |
| saler | 123456 | 销售员 | 销售 + CRM 客户/信用 |
| warehouse | 123456 | 仓管员 | 仓储 + 采购销售查看 |
| finance | 123456 | 财务 | 财务 + 报表 + CRM 核销 |

## 五、快速启动

### 方式 A：生产模式（推荐答辩演示，一条命令）

```bat
双击 start.bat
```

脚本自动检查/启动 MySQL80 服务 → 启动后端 Jar（`backend\target\erp-backend-1.0.0.jar`，内置前端页面）→ 打开浏览器 `http://localhost:8080`。

### 方式 B：前后端分离开发模式

```bat
# 1. 后端（8080，需 MySQL 已运行）
cd backend && mvn spring-boot:run
# 2. 前端（5173，热更新，/api 代理到 8080）
cd frontend && npm install && npm run dev
```

### 数据库初始化（首次部署）

```bat
# 依次执行（已内置全部表结构与演示数据）
mysql -uroot -p123456 < sql\erp_schema.sql
mysql -uroot -p123456 erp < sql\erp_seed_base.sql
mysql -uroot -p123456 erp < sql\erp_seed_resource.sql
mysql -uroot -p123456 erp < sql\erp_seed_biz.sql
```

## 六、核心业务流程验证

后端提供端到端流程（采购：需求→采购单→审批→车辆调度→到货入库→库存增加→结单；销售：订单→审批（信用校验）→出库发货→库存扣减→生成应收），已通过脚本验证：

```
python scripts\e2e_purchase.py   # 采购全流程（含跟单节点 下单→发货→到货→结算）
python scripts\e2e_sale.py       # 销售全流程（含应收生成、删除保护）
```

## 七、接口概览（前缀 /api）

| 模块 | 路径 | 说明 |
| --- | --- | --- |
| 认证 | POST /auth/login · GET /auth/info | JWT 签发、用户信息+权限+菜单 |
| 基础 | /base/regions·companies·departments·employees·warehouses·vehicles·meetings·params·dicts·coderules | 基础档案树/列表 |
| 权限 | /permission/roles·resources·users·grant | 角色/资源树/授权 |
| 规则 | /rules/audit·vehicle·meeting·param·dict·coderule·approval | 公共规则配置 |
| CRM | /crm/customers·suppliers·categories·follows·credit·arc·ap·merge | 客户/供应商/信用/对账/合并 |
| 进销存 | /goods·/purchase/demands·orders·bills·follow-ups·/sale/orders·returns | 商品/采购/销售全流程 |
| 仓储 | /wms/stocks·inbounds·outbounds·stock-logs·checks·transfers | 库存/出入库/盘点/调拨 |
| 报表 | /report/purchase·sale·profit·aging·contribution·lowStock·turnover·finance | 9 类聚合报表 |
| 财务 | /finance/accounts·receipts·payments·transfers·account-logs | 账户/收付款/转账/流水 |
| 任务 | /job/tasks·logs·run·toggle | 定时任务管理与执行日志 |

## 八、目录结构

```
D:\zuoyexiangmu
├─ start.bat                一键启动脚本
├─ README.md                本说明
├─ ERP系统开发文档_full.md  开发文档（口径来源）
├─ sql\                     erp_schema.sql / erp_seed_base.sql / erp_seed_resource.sql / erp_seed_biz.sql
├─ backend\                 Spring Boot 后端（src/main/java/com/erp + resources/static 前端产物）
│   └─ target\erp-backend-1.0.0.jar  可执行 Jar（含前端）
├─ frontend\                Vue3 前端（src/views：Login/Home/Base/Permission/Rules/Crm/Inventory/Warehouse/Reports/Jobs/Finance）
└─ scripts\                 e2e_purchase.py / e2e_sale.py 端到端验证脚本
```

## 九、认证与安全

- JWT 无状态认证（24h 有效），Token 仅含身份字段（不携带权限，权限由拦截器按用户实时加载）
- 12 个 Controller 全部挂 `@RequirePermission`，非 admin 按权限码动态鉴权（越权返回 403）
- 库存变动统一走 `doStockChange`，业务事务内保证一致性并逐笔写流水
- 全流程写审计日志（sys_audit_log），可追溯
