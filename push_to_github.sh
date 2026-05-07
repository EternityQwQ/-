#!/bin/bash

# ============================================
# ThermalFaker - GitHub 推送脚本 (Linux/Mac)
# 用途：将当前项目初始化为 Git 仓库并强制推送到 GitHub 主分支
# ============================================

# ========== 配置部分 ==========
# 修改为你的 GitHub 用户名和仓库名
GITHUB_USER="EternityQwQ"
REPO_NAME="ThermalFaker"
GITHUB_URL="https://github.com/${GITHUB_USER}/${REPO_NAME}.git"
DEFAULT_BRANCH="main"
COMMIT_MESSAGE="Initial commit: ThermalFaker temperature spoofing app"

# ========== 使用前置说明 ==========
# 在使用此脚本前，请确保已完成以下设置：
#
# 1. 已安装 Git
#    - 运行 git --version 检查是否已安装
#
# 2. 已配置 GitHub 认证（二选一即可）：
#
#    选项 A：使用 GitHub Personal Access Token (推荐)
#    - 访问 https://github.com/settings/tokens 创建 Token
#    - 至少选择 repo 权限
#    - 然后使用 HTTPS URL 格式：
#      https://<your-token>@github.com/${GITHUB_USER}/${REPO_NAME}.git
#
#    选项 B：使用 SSH Key
#    - 确保已生成 SSH Key 并添加到 GitHub
#    - 访问 https://github.com/settings/keys 添加
#    - 然后修改 GITHUB_URL 为 SSH 格式：
#      git@github.com:${GITHUB_USER}/${REPO_NAME}.git
#
# 3. 确保在 GitHub 上已创建名为 ${REPO_NAME} 的仓库（可以是空仓库）
#
# ========== 执行权限 ==========
# 如果提示权限不足，请先执行：
# chmod +x push_to_github.sh
# ============================================

echo "===================================="
echo "  ThermalFaker - GitHub 推送脚本"
echo "===================================="
echo ""

# 检查 Git 是否已安装
if ! command -v git &> /dev/null; then
    echo "❌ 错误：Git 未安装！"
    echo "请先安装 Git：https://git-scm.com/downloads"
    exit 1
fi

echo "✅ Git 检查通过"

# 检查是否已经是 Git 仓库
if [ -d ".git" ]; then
    echo "⚠️  检测到已存在 Git 仓库"
    read -p "是否重新初始化 (这将清除现有 Git 历史)? [y/N]: " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ 操作已取消"
        exit 0
    fi
    echo "🗑️  删除现有 .git 目录..."
    rm -rf .git
fi

# 初始化 Git 仓库
echo ""
echo "📦 正在初始化 Git 仓库..."
git init

# 添加所有文件
echo "📄 正在添加所有文件..."
git add .

# 创建初始提交
echo "📝 正在创建提交..."
git commit -m "$COMMIT_MESSAGE"

# 切换到主分支
echo "🌿 正在切换到 $DEFAULT_BRANCH 分支..."
git branch -M $DEFAULT_BRANCH

# 添加远程仓库
echo "🔗 正在添加远程仓库..."
git remote add origin "$GITHUB_URL"

echo ""
echo "===================================="
echo "🚀 准备推送!"
echo "===================================="
echo "远程仓库: $GITHUB_URL"
echo "分支: $DEFAULT_BRANCH"
echo ""
echo "⚠️  警告：这将执行强制推送，会覆盖远程仓库历史！"
echo ""
read -p "确认推送? [y/N]: " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "☁️  正在强制推送到 GitHub..."
    git push -f origin $DEFAULT_BRANCH
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ 成功！代码已推送到 GitHub!"
        echo ""
        echo "🌐 访问仓库："
        echo "   https://github.com/${GITHUB_USER}/${REPO_NAME}"
    else
        echo ""
        echo "❌ 推送失败！"
        echo ""
        echo "💡 可能的问题："
        echo "1. 未配置 GitHub 认证"
        echo "2. 仓库 URL 不正确"
        echo "3. 没有推送权限"
        echo ""
        echo "请参考脚本开头的配置说明"
        exit 1
    fi
else
    echo "❌ 操作已取消"
fi

echo ""
echo "===================================="
echo "  完成!"
echo "===================================="
