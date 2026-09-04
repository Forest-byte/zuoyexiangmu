package com.j180.erp.security;

import com.j180.erp.common.enums.DataScopeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * 当前登录用户上下文（由鉴权拦截器构建，随请求生命周期存于 ThreadLocal）
 */
@Data
@Builder
public class UserContext {

    private Long userId;

    private String username;

    private Long employeeId;

    private Long departmentId;

    /** 角色ID集合 */
    private Set<Long> roleIds;

    /** 是否超级管理员（拥有全部功能权限与全部数据权限） */
    private boolean superAdmin;

    /** 数据权限范围编码（多角色取最宽），见 DataScopeEnum */
    private Integer dataScope;

    /** 数据权限为部门类范围时的部门ID集合（含子部门展开结果，可能为空） */
    private Set<Long> deptScopeIds;

    /** 数据权限为本仓库时的仓库ID集合 */
    private Set<Long> warehouseScopeIds;

    /** 拥有的资源编码集合；null 表示拥有全部（超级管理员） */
    private Set<String> resCodes;

    /**
     * 功能权限校验
     */
    public boolean hasPermission(String resCode) {
        if (superAdmin) {
            return true;
        }
        return resCodes != null && resCodes.contains(resCode);
    }

    /**
     * 是否需要注入数据权限过滤（超级管理员/全部数据 不过滤）
     */
    public boolean needDataFilter() {
        return !superAdmin && dataScope != null && dataScope != DataScopeEnum.ALL.getCode();
    }

    /**
     * 当前数据范围的宽度排名（越大越宽）
     */
    public int dataScopeRank() {
        if (superAdmin || dataScope == null) {
            return DataScopeEnum.ALL.getRank();
        }
        return DataScopeEnum.of(dataScope).getRank();
    }
}
