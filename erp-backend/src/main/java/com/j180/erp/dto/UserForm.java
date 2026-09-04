package com.j180.erp.dto;

import lombok.Data;

/**
 * 登录账号表单
 */
@Data
public class UserForm {

    private Long id;

    /** 登录名（唯一） */
    private String username;

    /** 新增必填；编辑留空则不修改 */
    private String password;

    /** 关联员工ID（一人一账号，可选） */
    private Long employeeId;

    private Integer status;
}
