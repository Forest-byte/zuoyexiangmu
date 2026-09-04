# -*- coding: utf-8 -*-
# ERP 进销存一体化管理系统 - 启动脚本（由 start.bat 调用）
$ErrorActionPreference = "Continue"
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  ERP 进销存一体化管理系统（7 大子系统 / 63 功能点）" -ForegroundColor Cyan
Write-Host "  一键启动：后端 Jar（内置前端页面 + API）" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查 MySQL80 服务
Write-Host "[1/3] 检查 MySQL 服务..." -ForegroundColor Yellow
$svc = Get-Service -Name "MySQL80" -ErrorAction SilentlyContinue
if ($null -eq $svc) {
    Write-Host "      未发现 MySQL80 服务，请确认已安装并启动 MySQL。" -ForegroundColor Red
} elseif ($svc.Status -ne "Running") {
    try { Start-Service -Name "MySQL80" -ErrorAction Stop; Write-Host "      MySQL80 已启动。" }
    catch { Write-Host "      无法自动启动 MySQL80（可能需管理员权限），请手动启动后重试。" -ForegroundColor Red }
} else {
    Write-Host "      MySQL80 运行中。"
}

# 2. 启动后端
$jar = Join-Path $PSScriptRoot "backend\target\erp-backend-1.0.0.jar"
Write-Host "[2/3] 启动后端服务 http://localhost:8080 ..." -ForegroundColor Yellow
if (-not (Test-Path $jar)) {
    Write-Host "      未找到 $jar" -ForegroundColor Red
    Write-Host "      请先在 backend 目录执行 mvn -DskipTests package 打包。" -ForegroundColor Red
    Read-Host "      按回车退出"
    exit 1
}
Start-Process -FilePath "java" -ArgumentList "-jar `"$jar`"" -WindowStyle Normal
Start-Sleep -Seconds 6

# 3. 打开浏览器
Write-Host "[3/3] 打开浏览器..." -ForegroundColor Yellow
Start-Process "http://localhost:8080"

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  系统已启动，浏览器访问：http://localhost:8080" -ForegroundColor Green
Write-Host "  演示账号：" -ForegroundColor Green
Write-Host "    admin     / admin123   系统管理员（全部权限）"
Write-Host "    purchase  / 123456     采购员"
Write-Host "    saler     / 123456     销售员"
Write-Host "    warehouse / 123456     仓管员"
Write-Host "    finance   / 123456     财务"
Write-Host "  提示：首次部署请先执行 sql 目录下 4 个初始化脚本建库。"
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""
Read-Host "按回车键退出本窗口（后端服务保持运行）"