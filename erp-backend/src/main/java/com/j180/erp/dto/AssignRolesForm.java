package com.j180.erp.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户分配角色表单（覆盖式）
 */
@Data
public class AssignRolesForm {

    private Long userId;

    private List<Long> roleIds;
}
