package com.erp.controller;

import com.erp.common.Result;
import com.erp.model.LoginUser;
import com.erp.service.AuthService;
import com.erp.util.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证接口：登录/登出/当前用户信息/修改密码
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Result.fail("用户名或密码不能为空");
        }
        return Result.ok(authService.login(username, password));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        LoginUser u = UserContext.get();
        return Result.ok(null);
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        LoginUser u = UserContext.get();
        return Result.ok(authService.info(u.getUserId()));
    }

    @PostMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        LoginUser u = UserContext.get();
        authService.changePassword(u.getUserId(), body.get("oldPassword"), body.get("newPassword"));
        return Result.ok(null);
    }
}
