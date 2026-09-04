@echo off
chcp 65001 >nul
title ERP 进销存一体化管理系统 - 启动器
echo ============================================================
echo   ERP 进销存一体化管理系统（7 大子系统 / 63 功能点）
echo   一键启动：后端 Jar（内置前端页面 + API）
echo ============================================================
echo.

echo [0/2] 检查 MySQL 服务...
sc query MySQL80 | findstr /i "RUNNING" >nul
if %errorlevel% neq 0 (
  echo       MySQL80 未运行，尝试启动...
  net start MySQL80 >nul 2>&1
  if %errorlevel% neq 0 (
    echo       [警告] 无法自动启动 MySQL，请手动启动 MySQL80 服务后重试。
  ) else (
    echo       MySQL80 已启动。
  )
) else (
  echo       MySQL80 运行中。
)

echo [1/2] 启动后端服务（http://localhost:8080）...
cd /d "%~dp0backend"
if not exist "target\erp-backend-1.0.0.jar" (
  echo       [错误] 未找到 target\erp-backend-1.0.0.jar，请先执行打包。
  pause
  exit /b 1
)
start "ERP-Backend" cmd /k "chcp 65001 >nul && java -jar target\erp-backend-1.0.0.jar"

echo [2/2] 打开浏览器访问系统...
timeout /t 6 /nobreak >nul
start http://localhost:8080

echo.
echo ============================================================
echo   系统已启动，浏览器访问：http://localhost:8080
echo.
echo   演示账号：
echo     admin   / admin123   （系统管理员，全部权限）
echo     purchase/ 123456     （采购员：采购/基础/供应商）
echo     saler   / 123456     （销售员：销售/CRM客户信用）
echo     warehouse/123456     （仓管员：仓储/采购销售查看）
echo     finance / 123456     （财务：财务/报表/CRM核销）
echo ============================================================
echo.
pause
