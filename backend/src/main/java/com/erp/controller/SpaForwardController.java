package com.erp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端 SPA 路由回退：非 /api 的页面路径统一回退到 index.html
 * 使后端同源托管前端构建产物时，刷新 /crm 等前端路由不 404
 */
@Controller
public class SpaForwardController {

    @GetMapping(value = {"/", "/login", "/home", "/base", "/permission", "/rules", "/crm",
            "/inventory", "/warehouse", "/reports", "/jobs", "/finance"})
    public String forwardRoot() {
        return "forward:/index.html";
    }

    @GetMapping(value = {"/crm/**", "/inventory/**", "/warehouse/**", "/reports/**", "/jobs/**", "/finance/**",
            "/base/**", "/permission/**", "/rules/**"})
    public String forwardNested() {
        return "forward:/index.html";
    }
}
