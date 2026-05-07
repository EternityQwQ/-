# ThermalFaker

A modern Android app for spoofing device temperatures using Shizuku.

## Features

- **Battery Temperature Spoofing**: Modify reported battery temperature using `dumpsys` commands via Shizuku
- **CPU Temperature Support (Root Required)**: Attempts to write to thermal zone sysfs nodes (requires root access via Shizuku)
- **Quick Reset**: Easily revert to real temperature values
- **Modern Material 3 UI**: Beautiful, dynamic theming with dark mode support
- **Real-time Temperature Display**: Shows current temperature with animated thermometer arc

## How it Works

ThermalFaker uses Shizuku to execute shell commands at ADB level:

- **Battery Temperature**: Uses `dumpsys battery set temp <value>` to change the reported battery temperature (value is in tenths of a degree Celsius)
- **Reset**: Uses `dumpsys battery reset` to revert all battery properties to real values
- **CPU Temperature**: Tries to write to `/sys/class/thermal/thermal_zone*/temp` files (requires root access)

## Requirements

- **Android 12+** (API 31 or higher)
- **Shizuku** installed and running on your device
- **ADB/Shizuku Permission** granted to the app
- **Root Access (Optional)**: For CPU temperature spoofing

## Getting Started

### Prerequisites

1. Install Shizuku from [GitHub](https://github.com/RikkaApps/Shizuku) or Google Play
2. Start Shizuku and grant it necessary permissions
3. Download and install ThermalFaker

### Building from Source

```bash
# Clone the repository
git clone https://github.com/EternityQwQ/-.git
cd -

# Open in Android Studio or build with Gradle
./gradlew assembleDebug
```

## Usage

1. Open ThermalFaker
2. Grant Shizuku permission when prompted
3. Enter your target temperature in Celsius
4. Tap "Apply Spoof" to set the temperature
5. To revert, tap "Reset"

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | MVVM |
| Dependency Injection | Hilt |
| Shizuku Integration | Shizuku API 13.1.5 |
| Local Storage | DataStore (Optional) |
| Target SDK | 34 |
| Minimum SDK | 31 |

## Project Structure

```
com.thermalfaker.app/
├── ThermalFakerApp.kt              # Application class with Hilt setup
├── core/
│   ├── shizuku/
│   │   ├── ShizukuManager.kt       # Shizuku permission & service management
│   │   ├── TemperatureManager.kt   # Temperature command execution
│   │   └── ShellService.kt         # Shell service implementation
│   └── util/
│       └── Logger.kt               # Simple logging utility
├── data/
│   └── model/
│       └── AppModels.kt            # Data models for UI state & settings
├── di/
│   └── AppModule.kt                # Hilt dependency injection module
└── ui/
    ├── MainActivity.kt             # Main activity with edge-to-edge
    ├── navigation/
    │   ├── Screen.kt               # Navigation routes
    │   └── ThermalFakerNavHost.kt  # Compose navigation setup
    ├── screens/
    │   ├── MainScreen.kt           # Home screen with temperature controls
    │   └── InfoScreen.kt           # About/info screen
    ├── theme/
    │   ├── Color.kt                # App colors
    │   ├── Theme.kt                # Material 3 theme
    │   └── Type.kt                 # Typography
    └── viewmodel/
        └── MainViewModel.kt        # ViewModel for main screen
```

## License

This project is provided for educational purposes only. Temperature spoofing may violate terms of service of other apps. Use responsibly.

---

# 简体中文文档

## 应用简介

**ThermalFaker** 是一款现代化的 Android 设备温度伪装应用，通过 Shizuku 授予的 ADB 权限来执行温度伪装命令。

## 功能特点

- **电池温度伪装**：使用 `dumpsys` 命令通过 Shizuku 修改报告的电池温度
- **CPU 温度支持（需要 Root）**：尝试写入热区 sysfs 节点（需要通过 Shizuku 获取 root 权限）
- **快速重置**：轻松恢复真实温度值
- **现代 Material 3 UI**：美观的动态主题，支持深色模式
- **实时温度显示**：带动画温度计圆弧的当前温度展示

## 工作原理

ThermalFaker 使用 Shizuku 以 ADB 级别执行 shell 命令：

- **电池温度**：使用 `dumpsys battery set temp <值>` 更改报告的电池温度（值为摄氏度十分之一）
- **重置**：使用 `dumpsys battery reset` 将所有电池属性恢复为真实值
- **CPU 温度**：尝试写入 `/sys/class/thermal/thermal_zone*/temp` 文件（需要 root 权限）

## 系统要求

- **Android 12+**（API 31 或更高）
- 设备上已安装并运行 **Shizuku**
- 已向应用授予 **ADB/Shizuku 权限**
- **Root 访问权限（可选）**：用于 CPU 温度伪装

## 快速开始

### 前置条件

1. 从 [GitHub](https://github.com/RikkaApps/Shizuku) 或 Google Play 安装 Shizuku
2. 启动 Shizuku 并授予必要的权限
3. 下载并安装 ThermalFaker

### 从源码构建

```bash
# 克隆仓库
git clone https://github.com/EternityQwQ/-.git
cd -

# 在 Android Studio 中打开，或使用 Gradle 构建
./gradlew assembleDebug
```

## 使用方法

1. 打开 ThermalFaker 应用
2. 当提示时授予 Shizuku 权限
3. 输入目标温度（摄氏度）
4. 点击"应用伪装"按钮设置温度
5. 如需恢复，点击"重置"按钮

### Shizuku 授权步骤

1. 确保设备上已安装并激活 Shizuku
2. 打开 ThermalFaker
3. 点击主界面中的"授予权限"按钮
4. 在弹出的 Shizuku 授权对话框中选择"允许"

## 技术栈

| 组件 | 技术 |
|------|------|
| 编程语言 | Kotlin |
| UI 框架 | Jetpack Compose |
| 架构模式 | MVVM |
| 依赖注入 | Hilt |
| Shizuku 集成 | Shizuku API 13.1.5 |
| 本地存储 | DataStore（可选） |
| 目标 SDK | 34 |
| 最低 SDK | 31 |

## 项目结构

```
com.thermalfaker.app/
├── ThermalFakerApp.kt              # 应用入口，Hilt 配置
├── core/
│   ├── shizuku/
│   │   ├── ShizukuManager.kt       # Shizuku 权限与服务管理
│   │   ├── TemperatureManager.kt   # 温度命令执行
│   │   └── ShellService.kt         # Shell 服务实现
│   └── util/
│       └── Logger.kt               # 日志工具类
├── data/
│   └── model/
│       └── AppModels.kt           # UI 状态与设置数据模型
├── di/
│   └── AppModule.kt               # Hilt 依赖注入模块
├── ui/
│   ├── MainActivity.kt            # 主活动，边缘到边缘显示
│   ├── navigation/
│   │   ├── Screen.kt              # 导航路由定义
│   │   └── ThermalFakerNavHost.kt # Compose 导航配置
│   ├── screens/
│   │   ├── MainScreen.kt          # 主界面，温度控制
│   │   └── InfoScreen.kt          # 关于/信息页面
│   ├── theme/
│   │   ├── Color.kt               # 应用配色
│   │   ├── Theme.kt               # Material 3 主题
│   │   └── Type.kt                # 字体排版
│   └── viewmodel/
│       └── MainViewModel.kt       # 主界面 ViewModel
└── aidl/
    └── IShellService.aidl         # AIDL 接口定义
```

## 许可证

本项目仅供教育目的。温度伪装可能违反其他应用的服务条款。请负责任地使用。
