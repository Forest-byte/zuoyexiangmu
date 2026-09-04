---
AIGC:
    Label: "1"
    ContentProducer: 001191440300708461136T1XGW3
    ProduceID: 908adbfe72330206a374d5a1c791af91_21de135aa82f11f18ba4525400f8a581
    ReservedCode1: IocsVqvIveRwTKBJCJ24WOfSQUe1oJ/uHbTKsRAOaVaUF+IekYV8a5bF91Llf9D3oNuaw192MNoiENXK/GMIptYBJAe/GP/meQ9wxBZQVIvmRfg50+5rBwiZhs3GXaHbdguNXFy0utPnlg8SIcRHgANYoLFd4288j/2OnVg8mBGRzFz7y9CYcvSo0q4=
    ContentPropagator: 001191440300708461136T1XGW3
    PropagateID: 908adbfe72330206a374d5a1c791af91_21de135aa82f11f18ba4525400f8a581
    ReservedCode2: IocsVqvIveRwTKBJCJ24WOfSQUe1oJ/uHbTKsRAOaVaUF+IekYV8a5bF91Llf9D3oNuaw192MNoiENXK/GMIptYBJAe/GP/meQ9wxBZQVIvmRfg50+5rBwiZhs3GXaHbdguNXFy0utPnlg8SIcRHgANYoLFd4288j/2OnVg8mBGRzFz7y9CYcvSo0q4=
---

# 企业ERP管理系统 说明文档

本文档基于桌面 `erp-system` 项目的**真实代码与配置**整理，涵盖系统架构、数据库设计、后端接口、前端页面、权限模型、初始化数据与运行方式，供开发、部署与后续维护参考。

---

## 1. 项目概述

企业ERP管理系统是围绕**基础数据维护**与**角色权限管理（RBAC）**两个核心域构建的信息管理系统，覆盖公司、部门、员工、角色、资源、仓库等基础资料的管理，以及用户账号、角色授权、数据权限与操作审计。

- 前端：Vue 3 + Element Plus 构建的**纯静态站点**（本地依赖库加载，免构建）
- 后端：Spring Boot 2.7.18 + MyBatis-Plus 3.5.3.2，提供 RESTful API
- 前端静态资源**由后端统一伺服**，浏览器直接访问后端端口即可使用，无需单独部署 Node 服务
- 数据库：MySQL（库名 `j180`），建表与种子数据脚本**幂等可重复执行**

---

## 2. 技术栈与运行环境

| 类别 | 技术/版本 |
| --- | --- |
| 开发语言 | Java 17 |
| 后端框架 | Spring Boot 2.7.18、Spring MVC、Spring AOP |
| ORM | MyBatis-Plus 3.5.3.2（含分页、逻辑拦截器） |
| 安全 | JWT（jjwt 0.11.5）、Spring Security Crypto（BCrypt）、自定义鉴权拦截器与数据权限处理器 |
| 工具库 | Hutool 5.8.25（Excel 导入）、Apache POI 5.2.5 |
| 数据库 | MySQL 8.x（库名 `j180`，utf8mb4） |
| 前端 | Vue 3（全局构建版）、Element Plus、Element Plus Icons、Axios（均以本地 `lib/` 静态文件引入） |
| 构建工具 | Maven（后端） |
| 服务端口 | 8080 |

**数据库连接（`application.yml`）**：`jdbc:mysql://localhost:3306/j180?createDatabaseIfNotExist=true&...`，用户名 `root`，密码 `syz20051026`，时区 `Asia/Shanghai`。

---

## 3. 项目结构

```
erp-system/
├── README.md                              # 项目运行说明
├── erp-backend/                           # 后端工程
│   └── src/main/
│       ├── java/com/j180/erp/
│       │   ├── ErpApplication.java        # 启动类
│       │   ├── audit/                     # 审计注解 Audit + 切面 AuditAspect
│       │   ├── common/                    # Result / PageQuery / PageResult / BizException
│       │   │   ├── enums/                 # DataScope / EmployeeStatus / ResourceType / Status 枚举
│       │   │   └── util/                  # AssertUtil / DesensitizeUtil / IpUtil / JsonUtil
│       │   ├── config/                    # MybatisPlusConfig / WebMvcConfig / PasswordConfig
│       │   ├── controller/                # 11 个 REST 控制器
│       │   ├── dto/                       # 请求/响应对象（Form / Query / VO）
│       │   ├── entity/                    # 11 张表对应实体
│       │   ├── mapper/                    # MyBatis-Plus Mapper 接口
│       │   ├── security/                  # JwtUtil / AuthInterceptor / PermissionService
│       │   │   └── dataperm/              # ErpDataPermissionHandler / TableScope
│       │   ├── service/                   # 业务服务 + InitDataService 初始化
│       │   └── validator/                 # 各业务表单校验器（7 个）
│       └── resources/
│           ├── application.yml            # 数据源 / JWT / 日志配置
│           ├── schema.sql                 # 建表 DDL（幂等）
│           └── data.sql                   # 种子数据（INSERT IGNORE 幂等）
└── erp-frontend/                          # 前端静态站点（由后端伺服）
    ├── index.html                         # 入口页（引入 lib 与各页面脚本）
    ├── css/app.css                        # 全局样式
    ├── lib/                               # Vue / Element Plus / Axios / Icons 本地依赖
    └── js/
        ├── api.js                         # Axios 实例、拦截器、下载封装
        ├── store.js                       # AppStore 全局状态、hasPerm 权限判断
        ├── app.js                         # 登录页、主布局、Hash 路由、RBAC 路由守卫
        └── views/                         # 各业务页面组件
```

**模块划分**：
- 基础维护：公司信息、部门信息、员工信息、角色维护、资源维护、仓库信息
- 角色权限：用户账号、角色授权（功能权限 / 数据权限 / 用户分配）、审计日志

---

## 4. 数据库设计

数据库 `j180`，字符集 `utf8mb4`。共 **11 张表**（`schema.sql` 定义，均 `CREATE TABLE IF NOT EXISTS` 幂等）：

| 表名 | 说明 | 关键字段/约束 |
| --- | --- | --- |
| `sys_company` | 公司信息 | 唯一：公司编码/名称/统一信用代码 |
| `sys_department` | 部门（树形自关联） | `parent_id`、`ancestors` 祖先路径；`dept_code` 全局唯一、`dept_name` 同级唯一 |
| `sys_employee` | 员工档案 | `emp_no`/`id_card`/`user_id` 唯一；`status`: 0试用 1在职 2离职 |
| `sys_user` | 登录账号 | `username` 唯一；`password_hash`(BCrypt)；`is_builtin` 内置账号保护 |
| `sys_role` | 角色 | `role_code`/`role_name` 唯一；`data_scope` 数据范围；`is_builtin` 内置角色保护 |
| `sys_resource` | 资源（菜单/页面/按钮/接口） | `res_code` 唯一；`res_type`:1菜单 2页面 3按钮 4接口 |
| `wms_warehouse` | 仓库 | `wh_code`/`wh_name` 唯一；`wh_type`:1原料 2成品 3半成品 4退货 5其他 |
| `sys_user_role` | 用户-角色关联 | 唯一 `(user_id, role_id)` |
| `sys_role_resource` | 角色-资源关联 | 唯一 `(role_id, resource_id)` |
| `sys_employee_warehouse` | 员工-仓库绑定 | 唯一 `(employee_id, warehouse_id)`，用于"本仓库"数据权限 |
| `sys_audit_log` | 操作/权限审计日志 | 操作人（冗余用户名）、模块、动作、前后快照、IP |

**数据范围枚举（`sys_role.data_scope`）**：1=全部、2=本部门及子部门、3=本部门、4=本人、5=本仓库。

**种子数据（`data.sql`，幂等）**：
- 1 家公司（示例智能科技有限公司）
- 5 个部门（总公司 → 技术/人事/仓储/财务）
- 6 名员工（含试用/在职状态）
- 3 个角色：`SUPER_ADMIN`（系统管理员，内置）、`HR`（人事专员，本部门数据范围）、`WH_MANAGER`（仓库主管，本仓库数据范围）
- 47 条资源记录（2 个顶级菜单 + 页面/按钮）
- 2 个仓库，员工 3（李四）绑定仓库 1（华东成品仓）
- HR / WH_MANAGER 的初始角色-资源授权

> 内置账号 `admin/zhangsan/lisi` 及其 BCrypt 密码、角色绑定由后端启动时的 `InitDataService` 自动创建/修复，默认密码均为 `Admin123456`（详见第 6.5 节）。

---

## 5. 后端架构

### 5.1 分层结构

Controller（接口）→ Service（业务）→ Mapper（数据访问），校验独立为 `validator` 包：
- `common.Result<T>` 统一响应：`{code, message, data}`，`code=200` 成功
- `common.PageResult<T>` / `common.PageQuery` 统一分页
- `validator`（7 个）：Company / Department / Employee / Resource / Role / SysUser / Warehouse，遵循"controller 先校验再处理"
- `common.util.AssertUtil` 统一断言，不满足即抛 `BizException`，由 `GlobalExceptionHandler` 转为统一错误响应

### 5.2 认证与鉴权（RBAC）

- 登录成功签发 **JWT**，Token 中**仅含 userId**；角色/权限不在 Token 中
- `AuthInterceptor` 拦截 `/api/**`（仅放行 `/api/auth/login`）：
  1. 解析 `Authorization: Bearer <token>`；
  2. 调用 `PermissionService.buildContext(userId)` **每次请求实时读库**构建 `UserContext`（角色集合、功能权限码、数据权限范围，角色-资源映射经 `PermissionCache` 缓存）；
  3. 校验处理器方法上 `@RequiresPermission("权限码")` 声明的功能权限，无权限返回 **403** 并写入审计日志；
  4. 通过后把上下文放入 `ThreadLocal`（`UserContextHolder`），请求结束清理。
- 多角色功能权限**取并集**（R-MODEL-3）；数据权限**取最宽**（R-MODEL-4）。
- `SUPER_ADMIN` 走代码内置全量权限，无需配置 `sys_role_resource`。

### 5.3 数据权限

由 MyBatis-Plus 的 `ErpDataPermissionHandler`（`security/dataperm`）实现：`DataPermissionInterceptor` 依据 `mappedStatementId` 命中 `TableScope` 中受控的 Mapper（`EmployeeMapper`、`WarehouseMapper`、`AuditLogMapper`）时自动注入过滤 WHERE 条件；不在受控范围内的表（如 `sys_company`/`sys_department`/`sys_role`/`sys_resource`/`sys_user` 等基础配置类表）不做自动过滤。

IRR数据维度示例：
- 本部门及子部门：`department_id IN (本部门及全部子部门 id)`
- 本部门：`department_id = 当前部门`
- 本仓库：按员工-仓库绑定取 `id IN (绑定仓库)`
- 本人：Employee 按 `id`、Warehouse 按 `manager_id`、AuditLog 按 `user_id`
- 范围集合为空时注入 `1=0` 拒绝返回（安全优先）；超级管理员/范围=全部时不过滤

### 5.4 审计日志

- `@Audit(module, action, targetType)` 标注写操作接口，由 AOP 切面 `AuditAspect` 在方法成功/失败后统一记录
- 记录变更后快照（JSON，敏感字段脱敏）、操作人、IP、目标 ID
- `AuthInterceptor` 对**越权访问（403）**同样写入审计
- 审计写失败不影响主流程，仅记录 warn 日志

### 5.5 初始化（InitDataService）

应用启动时幂等执行：
- 用 BCrypt 创建/修复内置账号 `admin`（SUPER_ADMIN）、`zhangsan`（HR）、`lisi`（WH_MANAGER），密码 `Admin123456`
- 建立账号-角色绑定（SUPER_ADMIN / HR / WH_MANAGER），并把账号关联到种子数据中的演示员工档案

---

## 6. 后端接口清单（含权限码）

统一前缀 `/api`，除登录外均需 `Authorization: Bearer <token>`（OPTIONS 放行）。各模块接口与权限码如下（权限码均来自 `data.sql` 资源表）：

### 6.1 认证 `/api/auth`
| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | 放行 | 登录，返回 token/用户信息/菜单/角色 |
| GET | `/api/auth/me` | 需登录 | 当前用户信息 |
| POST | `/api/auth/password` | 需登录 | 修改本人密码 |
| POST | `/api/auth/logout` | 需登录 | 登出 |

> 图形验证码功能已在本次任务中按需求**整体移除**：`CaptchaService` 已删除、`/api/auth/captcha` 接口移除、登录不再校验验证码、`application.yml` 中 `erp.captcha` 配置移除、`WebMvcConfig` 相应排除项移除，前端登录页同步去掉验证码输入。

### 6.2 基础维护
公司信息 `/api/companies`（权限码 `P_COMPANY` / `B_COMPANY_ADD` / `B_COMPANY_EDIT` / `B_COMPANY_STATUS` / `B_COMPANY_DELETE` / `B_COMPANY_EXPORT`）：
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/companies/page` | 分页查询 |
| GET | `/api/companies/list` | 启用列表（下拉） |
| GET | `/api/companies/{id}` | 详情 |
| POST | `/api/companies` | 新增 |
| PUT | `/api/companies` | 编辑 |
| PUT | `/api/companies/{id}/status` | 停用/启用 |
| DELETE | `/api/companies/{id}` | 删除（另有 `/batch` 批量） |
| GET | `/api/companies/export` | 导出 CSV（需 `B_COMPANY_EXPORT`） |

部门信息 `/api/departments`（P_DEPARTMENT / B_DEPT_* / B_DEPT_STATUS / B_DEPT_DELETE）：
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/departments/tree` | 部门树（`keyword`/`status` 过滤） |
| GET | `/api/departments/{id}` | 详情 |
| POST | `/api/departments` | 新增（记录祖先路径） |
| PUT | `/api/departments` | 编辑 |
| PUT | `/api/departments/{id}/status` | 停用/启用 |
| DELETE | `/api/departments/{id}`（及 `/batch`） | 删除 |

员工信息 `/api/employees`（P_EMPLOYEE / B_EMP_ADD / B_EMP_EDIT / B_EMP_LEAVE / B_EMP_IMPORT / B_EMP_DELETE）：
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/employees/page` | 分页（返回 VO：补充分管部门名、关联登录用户名） |
| GET | `/api/employees/list-working` | 在职员工列表 |
| GET | `/api/employees/{id}` | 详情 |
| POST | `/api/employees` | 新增 |
| PUT | `/api/employees` | 编辑 |
| PUT | `/api/employees/{id}/leave` | 员工离职 |
| POST | `/api/employees/import` | 批量导入（入参为表单数组，返回 total/success/fail/errors 统计） |
| DELETE | `/api/employees/{id}`（及 `/batch`） | 删除 |

角色维护 `/api/roles`（P_ROLE / B_ROLE_ADD / B_ROLE_EDIT / B_ROLE_STATUS / B_ROLE_DELETE / B_ROLE_COPY）：
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/roles/page` | 分页（返回用户数、资源数、资源ID集合、数据范围标签） |
| GET | `/api/roles/list` | 启用角色列表 |
| GET | `/api/roles/{id}` | 详情（含已授权资源） |
| POST | `/api/roles` | 新增 |
| PUT | `/api/roles` | 编辑（内置角色编码锁定） |
| PUT | `/api/roles/{id}/status` | 停用/启用（内置 SUPER_ADMIN 不可停用） |
| POST | `/api/roles/{id}/copy` | 复制角色（含数据范围与功能权限；内置角色不可复制） |
| DELETE | `/api/roles/{id}`（及 `/batch`） | 删除（有关联用户时拒绝） |

资源维护 `/api/resources`（P_RESOURCE / B_RES_ADD / B_RES_EDIT / B_RES_STATUS / B_RES_DELETE）：
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/resources/tree` | 资源树（菜单/页面/按钮/接口） |
| GET | `/api/resources/{id}` | 详情 |
| POST | `/api/resources` | 新增（含接口类型时校验路径与方法唯一） |
| PUT | `/api/resources` | 编辑 |
| PUT | `/api/resources/{id}/status` | 停用/启用 |
| DELETE | `/api/resources/{id}`（及 `/batch`） | 删除（有下级或被角色引用时拒绝） |

仓库信息 `/api/warehouses`（P_WAREHOUSE / B_WH_ADD / B_WH_EDIT / B_WH_STATUS / B_WH_DELETE）：
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/warehouses/page` | 分页查询 |
| GET | `/api/warehouses/list` | 启用仓库列表 |
| GET | `/api/warehouses/{id}` | 详情 |
| POST | `/api/warehouses` | 新增（负责人仅可选未离职员工） |
| PUT | `/api/warehouses` | 编辑 |
| PUT | `/api/warehouses/{id}/status` | 停用/启用 |
| DELETE | `/api/warehouses/{id}`（及 `/batch`） | 删除 |

### 6.3 角色权限
用户账号 `/api/users`（P_USER / B_USER_ADD / B_USER_EDIT / B_USER_STATUS / B_USER_RESET / B_USER_ROLE / B_USER_DELETE）：
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/users/page` | 分页（可按角色 roleId 过滤） |
| GET | `/api/users/{id}` | 详情 |
| POST | `/api/users` | 新增账号 |
| PUT | `/api/users` | 编辑 |
| PUT | `/api/users/{id}/status` | 停用/启用（内置账号不可停用；禁止停用自己） |
| PUT | `/api/users/{id}/password` | 重置密码 |
| PUT | `/api/users/{id}/roles` | 分配角色（覆盖式，含防权限提升校验） |
| DELETE | `/api/users/{id}`（及 `/batch`） | 删除（内置账号不可删除） |

角色授权 `/api/role-grant`（B_GRANT_FUNC / B_GRANT_DATA / B_GRANT_USER）：
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| PUT | `/api/role-grant/resources` | 功能权限授权（覆盖式：`roleId` + `resourceIds`） |
| PUT | `/api/role-grant/data-scope` | 数据权限设置（`roleId` + `dataScope` + 可选 `dataScopeIds`） |
| PUT | `/api/role-grant/users` | 用户分配（覆盖式：`roleId` + `userIds`） |

审计日志 `/api/audit`：
| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/audit/page` | P_AUDIT | 分页（支持 keyword/module/actionType/username/startTime/endTime 过滤） |

---

## 7. 前端说明

前端为 **Vue 3 + Element Plus** 静态站点，无构建步骤；依赖库放在 `lib/`（vue.global.prod.js、axios.min.js、element-plus.index.full.min.js、element-plus.index.css、icons-vue.global.js），由后端 `WebMvcConfig` 的 ResourceHandler 从 `../erp-frontend/` 伺服。

### 7.1 页面与路由（Hash 路由）

| 路由 | 页面 | 权限码（页面级菜单） |
| --- | --- | --- |
| `/login` | 登录页（用户名+密码，无验证码） | 无需登录 |
| `/company` | 公司信息 | P_COMPANY |
| `/department` | 部门信息 | P_DEPARTMENT |
| `/employee` | 员工信息 | P_EMPLOYEE |
| `/role` | 角色维护 | P_ROLE |
| `/resource` | 资源维护 | P_RESOURCE |
| `/warehouse` | 仓库信息 | P_WAREHOUSE |
| `/user` | 用户账号 | P_USER |
| `/role-grant` | 角色授权 | P_ROLE_GRANT（按钮级 B_GRANT_FUNC/B_GRANT_DATA/B_GRANT_USER） |
| `/audit` | 审计日志 | P_AUDIT |

### 7.2 RBAC 适配

- 登录接口返回菜单树与角色信息；`store.js` 提供全局 `AppStore`（token、userInfo、menus、permissions）
- `app.js` 依据菜单构建侧边栏，路由守卫对每个页面做 RBAC 判断，无页面权限跳转 401 页
- 页面内按钮通过 `hasPerm('B_XXXX')` 控制显隐（如新增/编辑/删除/导出按钮）
- `api.js` 的 Axios 拦截器统一附加 `Authorization: Bearer`，401 时自动跳转登录，业务 code 非 200 统一 `ElMessage` 提示；下载接口（导出 CSV）走 blob 处理

### 7.3 业务页面要点（对接真实接口）

- 公司：分页表格 + 新增/编辑/停用启用/删除/批量删除 + CSV 导出
- 部门：树形展示（Element Plus tree），支持关键字/状态过滤、增删改、停用启用
- 员工：分页列表（展示员工编号、姓名、手机号（脱敏）、岗位职级、入职日期、所属部门与关联登录名），支持新增/编辑/离职/删除、批量导入
- 角色：分页 + 新增/编辑/复制/停用启用/删除；详情展示用户数与资源数并回显授权资源
- 资源：资源树维护（菜单/页面/按钮/接口四种类型），新增/编辑/停用启用/删除
- 仓库：分页 + 新增/编辑/停用启用/删除，负责人下拉取在职员工
- 用户账号：分页 + 新增/编辑/停用启用/重置密码/分配角色/删除；内置账号保护（不可停用/删除）
- 角色授权：选择角色后分别配置**功能权限树**（勾选资源，覆盖式保存）、**数据权限**（单选 1-5 档 + 可选部门/仓库明细）、**用户分配**（弹窗勾选用户，覆盖式保存）
- 审计日志：分页查询 + 条件过滤 + 详情弹窗（展示变更前后快照、操作人、IP、时间）

---

## 8. 内置账号（初始化数据）

| 登录名 | 密码 | 角色 | 数据范围 | 说明 |
| --- | --- | --- | --- | --- |
| `admin` | `Admin123456` | SUPER_ADMIN（系统管理员） | 全部 | 内置账号，权限只读，不可删除/停用 |
| `zhangsan` | `Admin123456` | HR（人事专员） | 本部门 | 演示：员工档案维护 |
| `lisi` | `Admin123456` | WH_MANAGER（仓库主管） | 本仓库 | 演示：仓库信息维护 |

> 密码均以 BCrypt 加密存储；内置账号 `is_builtin=1`，后端强制不可删除/停用（SUPER_ADMIN 角色编码不允许修改）。

---

## 9. 构建与运行

### 9.1 前置要求
- JDK 17、Maven 3.6+
- MySQL 8.x，服务运行中；数据源账号 `root` / `syz20051026`（可在 `application.yml` 修改）
- 启动时 `schema.sql` / `data.sql` 自动执行（`spring.sql.init.mode=always`），库不存在自动创建

### 9.2 编译（仅编译，不启动）
```bash
cd erp-backend
mvn clean compile
```
> 已通过 `mvn compile` 校验，编译成功（EXIT=0）。

### 9.3 启动后端
```bash
cd erp-backend
mvn spring-boot:run
```
启动后：
- 后端 API：`http://localhost:8080/api/**`
- 前端页面：`http://localhost:8080/`（由后端伺服 `../erp-frontend/`；若前端目录不在该相对位置，可把 `erp-frontend` 内容复制到 `erp-backend/src/main/resources/static/`）

### 9.4 独立调试前端（可选）
前端为静态站点且后端已放开 CORS，也可直接用浏览器打开 `erp-frontend/index.html`，接口默认指向 `http://localhost:8080`（见 `js/api.js` 的 `baseURL`）。

---

## 10. 关键配置说明（application.yml）

| 配置项 | 值 | 说明 |
| --- | --- | --- |
| `server.port` | 8080 | 服务端口 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/j180?createDatabaseIfNotExist=true&...` | 数据源（自动建库） |
| `spring.datasource.username/password` | `root` / `syz20051026` | 数据库账号 |
| `spring.sql.init.mode` | `always` | 每次启动执行建表与种子脚本（幂等） |
| `erp.jwt.secret` | j180-...（示例密钥） | JWT 签名密钥，**生产环境务必更换** |
| `erp.jwt.expire-hours` | 8 | Token 有效时长（小时） |
| `mybatis-plus.configuration.map-underscore-to-camel-case` | true | 下划线转驼峰映射 |

---

## 11. 已完成的本次变更摘要

1. **移除图形验证码**：删除 `CaptchaService`、`/api/auth/captcha` 接口、`LoginForm` 验证码字段、`AuthInterceptor`/`AuthService` 验证码校验逻辑、`WebMvcConfig` 排除项、`application.yml` 的 `erp.captcha` 配置，前端登录页同步移除验证码输入。
2. **修复部门树 NPE**：`DepartmentService.buildTree()` 中 `DeptTreeNode.children` 初始化，部门树接口可正常返回。
3. **后端补齐并编译通过**：service（AuditLogService/AuthService/Company/Department/Employee/Role/Resource/Warehouse/SysUser/RoleGrant/InitDataService）、validator（7 个）、controller（11 个）齐全，`mvn clean compile` 通过（EXIT=0）。
4. **前端从零搭建完成**：登录、基础维护 6 模块、角色权限 3 模块全部对接后端接口，适配 RBAC 与数据权限。
5. **前端由后端伺服**：`WebMvcConfig` 增加 ResourceHandler 指向 `../erp-frontend/`，浏览器直接访问 `http://localhost:8080/` 即可使用。
*（内容由AI生成，仅供参考）*
