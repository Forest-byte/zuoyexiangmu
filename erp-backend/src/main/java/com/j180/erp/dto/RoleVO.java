package com.j180.erp.dto;

import com.j180.erp.entity.Role;
import lombok.Data;

import java.util.List;

/**
 * 角色视图（含已授权用户数/资源数统计）
 */
@Data
public class RoleVO {

    private Long id;

    private String roleCode;

    private String roleName;

    private String description;

    private Integer dataScope;

    private String dataScopeIds;

    private Integer isBuiltin;

    private Integer status;

    private String remark;

    /** 已授权用户数 */
    private Long userCount;

    /** 已配置资源数 */
    private Long resourceCount;

    /** 已授权资源ID集合（用于授权回显） */
    private List<Long> resourceIds;

    /** 数据范围名称 */
    private String dataScopeLabel;

    public static RoleVO from(Role role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDescription(role.getDescription());
        vo.setDataScope(role.getDataScope());
        vo.setDataScopeIds(role.getDataScopeIds());
        vo.setIsBuiltin(role.getIsBuiltin());
        vo.setStatus(role.getStatus());
        vo.setRemark(role.getRemark());
        return vo;
    }
}
