package com.j180.erp.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色授权表单
 */
@Data
public class RoleGrantForm {

    private Long roleId;

    /** 功能权限资源ID集合（覆盖式） */
    private List<Long> resourceIds;
}
