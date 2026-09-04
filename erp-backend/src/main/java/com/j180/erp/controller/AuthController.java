package com.j180.erp.controller;

import com.j180.erp.common.Result;
import com.j180.erp.dto.LoginForm;
import com.j180.erp.dto.LoginResult;
import com.j180.erp.dto.PasswordForm;
import com.j180.erp.dto.UserInfoVO;
import com.j180.erp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证接口：登录 / 当前用户 / 修改密码 / 登出
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody LoginForm form, HttpServletRequest request) {
        return Result.ok(authService.login(form, request));
    }

    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        return Result.ok(authService.info());
    }

    @PostMapping("/password")
    public Result<Void> changePassword(@RequestBody PasswordForm form) {
        authService.changePassword(form);
        return Result.ok();
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }
}
