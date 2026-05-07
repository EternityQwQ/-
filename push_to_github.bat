@echo off
chcp 65001 >nul
REM ============================================
REM ThermalFaker - GitHub 推送脚本 (Windows)
REM 用途：将当前项目初始化为 Git 仓库并强制推送到 GitHub 主分支
REM ============================================

REM ========== 配置部分 ==========
set GITHUB_USER=EternityQwQ
set REPO_NAME=ThermalFaker
set GITHUB_URL=https://github.com/%GITHUB_USER%/%REPO_NAME%.git
set DEFAULT_BRANCH=main
set COMMIT_MESSAGE=Initial commit: ThermalFaker temperature spoofing app

REM ========== 使用前置说明 ==========
REM 在使用此脚本前，请确保已完成以下设置：
REM
REM 1. 已安装 Git
REM    - 运行 git --version 检查是否已安装
REM
REM 2. 已配置 GitHub 认证（二选一即可）：
REM
REM    选项 A：使用 GitHub Personal Access Token (推荐)
REM    - 访问 https://github.com/settings/tokens 创建 Token
REM    - 至少选择 repo 权限
REM    - 然后修改 GITHUB_URL 为：
REM      https://<your-token>@github.com/%GITHUB_USER%/%REPO_NAME%.git
REM
REM    选项 B：使用 SSH Key
REM    - 确保已生成 SSH Key 并添加到 GitHub
REM    - 访问 https://github.com/settings/keys 添加
REM    - 然后修改 GITHUB_URL 为：
REM      git@github.com:%GITHUB_USER%/%REPO_NAME%.git
REM
REM 3. 确保在 GitHub 上已创建名为 %REPO_NAME% 的仓库（可以是空仓库）
REM
REM ============================================

echo ====================================
echo   ThermalFaker - GitHub 推送脚本
echo ====================================
echo.

REM 检查 Git 是否已安装
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误：Git 未安装！
    echo 请先安装 Git：https://git-scm.com/downloads
    pause
    exit /b 1
)

echo ✅ Git 检查通过

REM 检查是否已经是 Git 仓库
if exist .git (
    echo ⚠️  检测到已存在 Git 仓库
    set /p REINIT="是否重新初始化 (这将清除现有 Git 历史)? [y/N]: "
    if /i not "%REINIT%"=="y" (
        echo ❌ 操作已取消
        pause
        exit /b 0
    )
    echo 🗑️  删除现有 .git 目录...
    rmdir /s /q .git
)

REM 初始化 Git 仓库
echo.
echo 📦 正在初始化 Git 仓库...
git init

REM 添加所有文件
echo 📄 正在添加所有文件...
git add .

REM 创建初始提交
echo 📝 正在创建提交...
git commit -m "%COMMIT_MESSAGE%"

REM 切换到主分支
echo 🌿 正在切换到 %DEFAULT_BRANCH% 分支...
git branch -M %DEFAULT_BRANCH%

REM 添加远程仓库
echo 🔗 正在添加远程仓库...
git remote add origin "%GITHUB_URL%"

echo.
echo ====================================
echo 🚀 准备推送!
echo ====================================
echo 远程仓库: %GITHUB_URL%
echo 分支: %DEFAULT_BRANCH%
echo.
echo ⚠️  警告：这将执行强制推送，会覆盖远程仓库历史！
echo.
set /p CONFIRM="确认推送? [y/N]: "
if /i not "%CONFIRM%"=="y" (
    echo ❌ 操作已取消
    pause
    exit /b 0
)

echo ☁️  正在强制推送到 GitHub...
git push -f origin %DEFAULT_BRANCH%

if %errorlevel% equ 0 (
    echo.
    echo ✅ 成功！代码已推送到 GitHub!
    echo.
    echo 🌐 访问仓库：
    echo    https://github.com/%GITHUB_USER%/%REPO_NAME%
) else (
    echo.
    echo ❌ 推送失败！
    echo.
    echo 💡 可能的问题：
    echo 1. 未配置 GitHub 认证
    echo 2. 仓库 URL 不正确
    echo 3. 没有推送权限
    echo.
    echo 请参考脚本开头的配置说明
    pause
    exit /b 1
)

echo.
echo ====================================
echo   完成!
echo ====================================
pause
