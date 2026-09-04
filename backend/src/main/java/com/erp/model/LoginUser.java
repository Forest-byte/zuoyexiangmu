package com.erp.model;

import lombok.Data;

import java.util.List;

/**
 * 登录用户上下文信息（存入 JWT / ThreadLocal）
 */
@Data
public class LoginUser {
    private Long userId;
    private String username;
    private String name;
    private String roleCode;
    private List<String> roles;
    private List<String> permissions;
    private Boolean isAdmin;
}
