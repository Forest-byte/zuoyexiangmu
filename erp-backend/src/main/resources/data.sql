-- ============================================================
-- 企业ERP管理系统 初始化种子数据（幂等：INSERT IGNORE，可重复执行）
-- 内置账号（admin/zhangsan/lisi）及其密码、角色绑定由后端启动时
-- 通过 InitDataService 以 BCrypt 加密方式自动创建/修复。
-- ============================================================

-- 公司信息（默认主体）
INSERT IGNORE INTO sys_company (id, company_code, company_name, credit_code, legal_person, address, phone, email, bank_name, bank_account, status, remark) VALUES
(1, 'C001', '示例智能科技有限公司', '91330100MA27X8YT9A', '张示例', '浙江省杭州市西湖区文一西路100号', '0571-88888888', 'contact@erp-demo.com', '中国银行杭州分行', '123456789012345678', 1, '系统预置默认主体');

-- 部门（ancestors 含自身）
INSERT IGNORE INTO sys_department (id, parent_id, ancestors, dept_name, dept_code, sort, status) VALUES
(1, 0, '0,1',     '总公司', 'D001', 0, 1),
(2, 1, '0,1,2',   '技术部', 'D002', 1, 1),
(3, 1, '0,1,3',   '人事部', 'D003', 2, 1),
(4, 1, '0,1,4',   '仓储部', 'D004', 3, 1),
(5, 1, '0,1,5',   '财务部', 'D005', 4, 1);

-- 员工（0=试用 1=在职 2=离职）
INSERT IGNORE INTO sys_employee (id, emp_no, name, gender, id_card, mobile, email, department_id, position, level, hire_date, leave_date, status, user_id, remark) VALUES
(1, 'E0001', '系统管理员', 0, NULL, '13800000001', 'admin@erp-demo.com',  1, '系统管理员', 'P10', '2020-01-01', NULL, 1, NULL, '内置管理员对应员工档案'),
(2, 'E0002', '张三',       0, '330106199001011234', '13800000002', 'zhangsan@erp-demo.com', 3, '人事专员', 'P5', '2022-03-15', NULL, 1, NULL, '演示账号：人事专员(本部门数据权限)'),
(3, 'E0003', '李四',       1, '330106199202023456', '13800000003', 'lisi@erp-demo.com',     4, '仓库主管', 'P5', '2021-06-20', NULL, 1, NULL, '演示账号：仓库主管(本仓库数据权限)'),
(4, 'E0004', '王五',       0, NULL, '13800000004', NULL, 2, 'Java工程师', 'P4', '2026-08-01', NULL, 0, NULL, '试用期员工'),
(5, 'E0005', '赵六',       1, NULL, '13800000005', NULL, 1, '总经理', 'P9', '2019-05-10', NULL, 1, NULL, NULL),
(6, 'E0006', '钱七',       1, NULL, '13800000006', NULL, 3, '人事助理', 'P3', '2024-09-01', NULL, 1, NULL, NULL);

-- 角色（1=全部 2=本部门及子部门 3=本部门 4=本人 5=本仓库）
INSERT IGNORE INTO sys_role (id, role_code, role_name, description, data_scope, data_scope_ids, is_builtin, status, remark) VALUES
(1, 'SUPER_ADMIN', '系统管理员', '内置超级管理员，默认拥有全部权限，权限配置只读', 1, NULL, 1, 1, '内置角色，不可删除/停用'),
(2, 'HR',          '人事专员',   '负责员工档案维护', 3, NULL, 0, 1, '演示：本部门数据权限'),
(3, 'WH_MANAGER',  '仓库主管',   '负责仓库信息维护', 5, NULL, 0, 1, '演示：本仓库数据权限');

-- 资源树（1=菜单 2=页面 3=按钮 4=接口）
INSERT IGNORE INTO sys_resource (id, res_code, res_name, res_type, parent_id, path, http_method, sort_no, icon, status) VALUES
-- 一级菜单：基础维护
(1,  'M_BASE',          '基础维护',     1, 0,  '/base',       NULL,   1, 'Setting',        1),
(2,  'P_COMPANY',       '公司信息',     2, 1,  '/company',    NULL,   1, 'OfficeBuilding', 1),
(3,  'B_COMPANY_ADD',   '新增公司',     3, 2,  NULL,          NULL,   1, NULL,            1),
(4,  'B_COMPANY_EDIT',  '编辑公司',     3, 2,  NULL,          NULL,   2, NULL,            1),
(5,  'B_COMPANY_STATUS','停用启用公司', 3, 2,  NULL,          NULL,   3, NULL,            1),
(6,  'B_COMPANY_DELETE','删除公司',     3, 2,  NULL,          NULL,   4, NULL,            1),
(7,  'B_COMPANY_EXPORT','导出公司',     3, 2,  NULL,          NULL,   5, NULL,            1),
(8,  'P_DEPARTMENT',    '部门信息',     2, 1,  '/department', NULL,   2, 'Connection',     1),
(9,  'B_DEPT_ADD',      '新增部门',     3, 8,  NULL,          NULL,   1, NULL,            1),
(10, 'B_DEPT_EDIT',     '编辑部门',     3, 8,  NULL,          NULL,   2, NULL,            1),
(11, 'B_DEPT_STATUS',   '停用启用部门', 3, 8,  NULL,          NULL,   3, NULL,            1),
(12, 'B_DEPT_DELETE',   '删除部门',     3, 8,  NULL,          NULL,   4, NULL,            1),
(13, 'P_EMPLOYEE',      '员工信息',     2, 1,  '/employee',   NULL,   3, 'User',           1),
(14, 'B_EMP_ADD',       '新增员工',     3, 13, NULL,          NULL,   1, NULL,            1),
(15, 'B_EMP_EDIT',      '编辑员工',     3, 13, NULL,          NULL,   2, NULL,            1),
(16, 'B_EMP_LEAVE',     '员工离职',     3, 13, NULL,          NULL,   3, NULL,            1),
(17, 'B_EMP_IMPORT',    '批量导入员工', 3, 13, NULL,          NULL,   4, NULL,            1),
(18, 'B_EMP_DELETE',    '删除员工',     3, 13, NULL,          NULL,   5, NULL,            1),
(19, 'P_ROLE',          '角色维护',     2, 1,  '/role',       NULL,   4, 'Avatar',         1),
(20, 'B_ROLE_ADD',      '新增角色',     3, 19, NULL,          NULL,   1, NULL,            1),
(21, 'B_ROLE_EDIT',     '编辑角色',     3, 19, NULL,          NULL,   2, NULL,            1),
(22, 'B_ROLE_STATUS',   '停用启用角色', 3, 19, NULL,          NULL,   3, NULL,            1),
(23, 'B_ROLE_DELETE',   '删除角色',     3, 19, NULL,          NULL,   4, NULL,            1),
(24, 'B_ROLE_COPY',     '复制角色',     3, 19, NULL,          NULL,   5, NULL,            1),
(25, 'P_RESOURCE',      '资源维护',     2, 1,  '/resource',   NULL,   5, 'Menu',           1),
(26, 'B_RES_ADD',       '新增资源',     3, 25, NULL,          NULL,   1, NULL,            1),
(27, 'B_RES_EDIT',      '编辑资源',     3, 25, NULL,          NULL,   2, NULL,            1),
(28, 'B_RES_STATUS',    '停用启用资源', 3, 25, NULL,          NULL,   3, NULL,            1),
(29, 'B_RES_DELETE',    '删除资源',     3, 25, NULL,          NULL,   4, NULL,            1),
(30, 'P_WAREHOUSE',     '仓库信息',     2, 1,  '/warehouse',  NULL,   6, 'Box',            1),
(31, 'B_WH_ADD',        '新增仓库',     3, 30, NULL,          NULL,   1, NULL,            1),
(32, 'B_WH_EDIT',       '编辑仓库',     3, 30, NULL,          NULL,   2, NULL,            1),
(33, 'B_WH_STATUS',     '停用启用仓库', 3, 30, NULL,          NULL,   3, NULL,            1),
(34, 'B_WH_DELETE',     '删除仓库',     3, 30, NULL,          NULL,   4, NULL,            1),
-- 一级菜单：角色权限
(35, 'M_PERM',          '角色权限',     1, 0,  '/perm',       NULL,   2, 'Key',            1),
(36, 'P_USER',          '用户账号',     2, 35, '/user',       NULL,   1, 'UserFilled',     1),
(37, 'B_USER_ADD',      '新增账号',     3, 36, NULL,          NULL,   1, NULL,            1),
(38, 'B_USER_EDIT',     '编辑账号',     3, 36, NULL,          NULL,   2, NULL,            1),
(39, 'B_USER_STATUS',   '停用启用账号', 3, 36, NULL,          NULL,   3, NULL,            1),
(40, 'B_USER_RESET',    '重置密码',     3, 36, NULL,          NULL,   4, NULL,            1),
(41, 'B_USER_ROLE',     '分配角色',     3, 36, NULL,          NULL,   5, NULL,            1),
(42, 'B_USER_DELETE',   '删除账号',     3, 36, NULL,          NULL,   6, NULL,            1),
(43, 'P_ROLE_GRANT',    '角色授权',     2, 35, '/role-grant', NULL,   2, 'Lock',           1),
(44, 'B_GRANT_FUNC',    '功能权限授权', 3, 43, NULL,          NULL,   1, NULL,            1),
(45, 'B_GRANT_DATA',    '数据权限设置', 3, 43, NULL,          NULL,   2, NULL,            1),
(46, 'B_GRANT_USER',    '用户分配',     3, 43, NULL,          NULL,   3, NULL,            1),
(47, 'P_AUDIT',         '审计日志',     2, 35, '/audit',      NULL,   3, 'Document',       1);

-- 角色-资源授权（SUPER_ADMIN 走代码内置全量权限，无需配置）
-- 人事专员 HR：基础维护菜单 + 员工信息页 + 新增/编辑/离职/导入
INSERT IGNORE INTO sys_role_resource (role_id, resource_id) VALUES
(2, 1), (2, 13), (2, 14), (2, 15), (2, 16), (2, 17);
-- 仓库主管 WH_MANAGER：基础维护菜单 + 仓库信息页 + 新增/编辑/停用启用
INSERT IGNORE INTO sys_role_resource (role_id, resource_id) VALUES
(3, 1), (3, 30), (3, 31), (3, 32), (3, 33);

-- 仓库
INSERT IGNORE INTO wms_warehouse (id, wh_code, wh_name, wh_type, manager_id, region, address, contact, phone, status, remark) VALUES
(1, 'WH001', '华东成品仓', 2, 3, '浙江省杭州市西湖区', '文一西路100号1号库', '李四', '0571-87654321', 1, NULL),
(2, 'WH002', '原材料仓',   1, NULL, '浙江省杭州市余杭区', '仓前街道2号库',     '王五', '0571-87654322', 1, NULL);

-- 员工-仓库绑定（李四=员工3 绑定 华东成品仓=仓库1，用于"本仓库"数据权限演示）
INSERT IGNORE INTO sys_employee_warehouse (employee_id, warehouse_id) VALUES
(3, 1);
