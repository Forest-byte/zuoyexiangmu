-- ============================================================
-- 企业ERP管理系统 数据库初始化脚本（幂等，可重复执行）
-- 数据库：j180   字符集：utf8mb4
-- ============================================================

CREATE TABLE IF NOT EXISTS sys_company (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    company_code VARCHAR(32)  NOT NULL COMMENT '公司编码',
    company_name VARCHAR(128) NOT NULL COMMENT '公司名称',
    credit_code  VARCHAR(18)  NOT NULL COMMENT '统一社会信用代码(18位)',
    legal_person VARCHAR(64)  NULL COMMENT '法定代表人',
    address      VARCHAR(256) NULL COMMENT '注册地址',
    phone        VARCHAR(32)  NULL COMMENT '联系电话',
    email        VARCHAR(128) NULL COMMENT '邮箱',
    bank_name    VARCHAR(128) NULL COMMENT '开户银行',
    bank_account VARCHAR(64)  NULL COMMENT '银行账号',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '0=停用 1=启用',
    remark       VARCHAR(512) NULL COMMENT '备注',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_company_code (company_code),
    UNIQUE KEY uk_company_name (company_name),
    UNIQUE KEY uk_credit_code (credit_code),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公司信息表';

CREATE TABLE IF NOT EXISTS sys_department (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '上级部门ID(0=顶级)',
    ancestors   VARCHAR(500) NOT NULL DEFAULT '0' COMMENT '祖先路径(含自身),如0,1,3',
    dept_name   VARCHAR(64)  NOT NULL COMMENT '部门名称(同级唯一)',
    dept_code   VARCHAR(32)  NOT NULL COMMENT '部门编码(全局唯一)',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '显示排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0=停用 1=启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_code (dept_code),
    KEY idx_parent_id (parent_id),
    KEY idx_ancestors (ancestors(64)),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '部门信息表(树形自关联)';

CREATE TABLE IF NOT EXISTS sys_employee (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    emp_no        VARCHAR(32)  NOT NULL COMMENT '员工编号',
    name          VARCHAR(64)  NOT NULL COMMENT '姓名',
    gender        TINYINT      NULL COMMENT '0=男 1=女',
    id_card       VARCHAR(18)  NULL COMMENT '身份证号(展示脱敏)',
    mobile        VARCHAR(20)  NULL COMMENT '手机号',
    email         VARCHAR(128) NULL COMMENT '邮箱',
    department_id BIGINT       NULL COMMENT '所属部门(sys_department.id)',
    position      VARCHAR(64)  NULL COMMENT '岗位/职位',
    level         VARCHAR(32)  NULL COMMENT '职级',
    hire_date     DATE         NOT NULL COMMENT '入职日期',
    leave_date    DATE         NULL COMMENT '离职日期',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '0=试用 1=在职 2=离职',
    user_id       BIGINT       NULL COMMENT '关联登录账号(sys_user.id,一人一账号)',
    remark        VARCHAR(512) NULL COMMENT '备注',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_emp_no (emp_no),
    UNIQUE KEY uk_id_card (id_card),
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_department_id (department_id),
    KEY idx_name (name),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工信息表';

CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username        VARCHAR(64)  NOT NULL COMMENT '登录名',
    password_hash   VARCHAR(128) NOT NULL COMMENT '密码哈希(BCrypt)',
    employee_id     BIGINT       NULL COMMENT '关联员工(sys_employee.id)',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '0=停用 1=启用',
    is_builtin      TINYINT      NOT NULL DEFAULT 0 COMMENT '1=内置账号(不可删除/停用)',
    last_login_time DATETIME     NULL COMMENT '最后登录时间',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_employee_id (employee_id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '登录账号表';

CREATE TABLE IF NOT EXISTS sys_role (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_code      VARCHAR(32)  NOT NULL COMMENT '角色编码',
    role_name      VARCHAR(64)  NOT NULL COMMENT '角色名称',
    description    VARCHAR(256) NULL COMMENT '角色描述',
    data_scope     TINYINT      NOT NULL DEFAULT 4 COMMENT '1=全部 2=本部门及子部门 3=本部门 4=本人 5=本仓库',
    data_scope_ids VARCHAR(512) NULL COMMENT '数据范围明细ID(逗号分隔,可选)',
    is_builtin     TINYINT      NOT NULL DEFAULT 0 COMMENT '1=内置角色(SUPER_ADMIN不可删/停用)',
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '0=停用 1=启用',
    remark         VARCHAR(512) NULL COMMENT '备注',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code),
    UNIQUE KEY uk_role_name (role_name),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';

CREATE TABLE IF NOT EXISTS sys_resource (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    res_code    VARCHAR(64)  NOT NULL COMMENT '资源编码',
    res_name    VARCHAR(64)  NOT NULL COMMENT '资源名称',
    res_type    TINYINT      NOT NULL COMMENT '1=菜单 2=页面 3=按钮 4=接口',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '上级资源ID(0=顶级)',
    path        VARCHAR(256) NULL COMMENT '资源路径/URL',
    http_method VARCHAR(8)   NULL COMMENT '接口方法(GET/POST/PUT/DELETE,仅接口类型)',
    sort_no     INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    icon        VARCHAR(64)  NULL COMMENT '菜单图标',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0=停用 1=启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_res_code (res_code),
    KEY idx_parent_id (parent_id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '资源表(菜单/页面/按钮/接口)';

CREATE TABLE IF NOT EXISTS wms_warehouse (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    wh_code     VARCHAR(32)  NOT NULL COMMENT '仓库编码',
    wh_name     VARCHAR(64)  NOT NULL COMMENT '仓库名称',
    wh_type     TINYINT      NOT NULL COMMENT '1=原材料仓 2=成品仓 3=半成品仓 4=退货仓 5=其他',
    manager_id  BIGINT       NULL COMMENT '负责人(sys_employee.id,仅可选未离职员工)',
    region      VARCHAR(128) NULL COMMENT '所在地区',
    address     VARCHAR(256) NULL COMMENT '详细地址',
    contact     VARCHAR(64)  NULL COMMENT '联系人',
    phone       VARCHAR(32)  NULL COMMENT '联系电话',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0=停用 1=启用',
    remark      VARCHAR(512) NULL COMMENT '备注',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wh_code (wh_code),
    UNIQUE KEY uk_wh_name (wh_name),
    KEY idx_wh_type (wh_type),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '仓库信息表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT   NOT NULL COMMENT '用户ID(sys_user.id)',
    role_id     BIGINT   NOT NULL COMMENT '角色ID(sys_role.id)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户-角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_resource (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id     BIGINT   NOT NULL COMMENT '角色ID(sys_role.id)',
    resource_id BIGINT   NOT NULL COMMENT '资源ID(sys_resource.id)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_resource (role_id, resource_id),
    KEY idx_resource_id (resource_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色-资源关联表';

CREATE TABLE IF NOT EXISTS sys_employee_warehouse (
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    employee_id  BIGINT   NOT NULL COMMENT '员工ID(sys_employee.id)',
    warehouse_id BIGINT   NOT NULL COMMENT '仓库ID(wms_warehouse.id)',
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_employee_warehouse (employee_id, warehouse_id),
    KEY idx_warehouse_id (warehouse_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工-仓库关联表(数据权限-本仓库维度)';

CREATE TABLE IF NOT EXISTS sys_audit_log (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT       NULL COMMENT '操作人ID',
    username        VARCHAR(64)  NULL COMMENT '操作人登录名(冗余,防删除后无法追溯)',
    module          VARCHAR(64)  NULL COMMENT '所属模块',
    action_type     VARCHAR(32)  NULL COMMENT '操作类型(新增/编辑/停用/授权/登录等)',
    target_type     VARCHAR(32)  NULL COMMENT '操作对象类型',
    target_id       BIGINT       NULL COMMENT '操作对象ID',
    before_snapshot TEXT         NULL COMMENT '变更前快照(JSON)',
    after_snapshot  TEXT         NULL COMMENT '变更后快照(JSON)',
    ip              VARCHAR(64)  NULL COMMENT '来源IP',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_module (module),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '操作/权限审计日志表';
