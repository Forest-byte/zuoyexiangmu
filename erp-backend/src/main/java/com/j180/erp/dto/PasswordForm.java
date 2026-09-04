package com.j180.erp.dto;

import lombok.Data;

/**
 * 修改密码表单
 */
@Data
public class PasswordForm {

    private String oldPassword;

    private String newPassword;
}
