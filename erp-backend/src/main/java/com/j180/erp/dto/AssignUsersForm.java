package com.j180.erp.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色-用户分配表单（覆盖式）
 */
@Data
public class AssignUsersForm {

    private Long roleId;

    private List<Long> userIds;
}
