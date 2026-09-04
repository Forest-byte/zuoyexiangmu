package com.j180.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录结果：JWT Token + 用户信息与权限（用于前端渲染菜单/按钮）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult {

    private String token;

    private UserInfoVO user;
}
