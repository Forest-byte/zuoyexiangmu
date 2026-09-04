package com.j180.erp.security.dataperm;

import lombok.Getter;

/**
 * 数据权限过滤表配置：声明每张业务表各数据维度的过滤列
 * <p>
 * 各业务 Mapper 无需手写权限 SQL，由 DataPermissionInterceptor 统一注入 WHERE 条件；
 * 不在配置内的表（系统配置类）默认放行。
 */
@Getter
public enum TableScope {

    /** 员工表：部门维度按 department_id，本人维度按 id（本人员工档案） */
    EMPLOYEE("sys_employee", "department_id", "id", null, SelfIdType.EMPLOYEE_ID),

    /** 仓库表：本人维度按 manager_id（本人负责的仓库），本仓库维度按 id */
    WAREHOUSE("wms_warehouse", null, "manager_id", "id", SelfIdType.EMPLOYEE_ID),

    /** 审计日志表：本人维度按 user_id（本人操作记录） */
    AUDIT_LOG("sys_audit_log", null, "user_id", null, SelfIdType.USER_ID);

    /** 表名 */
    private final String tableName;
    /** 部门维度过滤列（可空=该表不支持部门维度） */
    private final String deptColumn;
    /** 本人维度过滤列（可空=该表不支持本人维度） */
    private final String selfColumn;
    /** 仓库维度过滤列（可空=该表不支持仓库维度） */
    private final String warehouseColumn;
    /** 本人维度取值类型 */
    private final SelfIdType selfIdType;

    TableScope(String tableName, String deptColumn, String selfColumn, String warehouseColumn, SelfIdType selfIdType) {
        this.tableName = tableName;
        this.deptColumn = deptColumn;
        this.selfColumn = selfColumn;
        this.warehouseColumn = warehouseColumn;
        this.selfIdType = selfIdType;
    }

    /**
     * 根据 Mapper 全限定 statementId 解析所属表配置（如 com.j180.erp.mapper.EmployeeMapper.selectPage）
     */
    public static TableScope resolve(String mappedStatementId) {
        if (mappedStatementId == null) {
            return null;
        }
        for (TableScope scope : values()) {
            if (mappedStatementId.contains(".mapper." + scope.tableNameMapperKeyword() + ".")) {
                return scope;
            }
        }
        return null;
    }

    private String tableNameMapperKeyword() {
        return switch (this) {
            case EMPLOYEE -> "EmployeeMapper";
            case WAREHOUSE -> "WarehouseMapper";
            case AUDIT_LOG -> "AuditLogMapper";
        };
    }

    /** 本人维度取值类型 */
    public enum SelfIdType {
        /** 取当前用户的员工ID */
        EMPLOYEE_ID,
        /** 取当前用户的账号ID */
        USER_ID
    }
}
