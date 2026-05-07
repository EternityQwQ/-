# ThermalFaker

A modern Android app for spoofing device temperatures using Shizuku.

## Features

- **Battery Temperature Spoofing**: Modify reported battery temperature using `dumpsys` commands via Shizuku
- **Hardware Dashboard**: Real-time monitoring of CPU, GPU, battery, and ambient temperatures
- **Multi-Hardware Temperature Spoofing**: Support for spoofing CPU, GPU, battery, and ambient sensor temperatures
- **Global Temperature Control**: Set all hardware temperatures at once with a single slider
- **CPU/GPU Frequency Monitoring**: Display real-time CPU core frequencies and GPU clock speeds
- **Quick Reset**: Easily revert to real temperature values for individual or all hardware
- **Modern Material 3 UI**: Beautiful, dynamic theming with dark mode support
- **Real-time Temperature Display**: Shows current temperature with animated thermometer arc
- **Bilingual Support**: English and Chinese (Simplified) localization
- **Debug Log Export**: Real-time logging with export to download directory

## Debug Log Features

### Log Collection
- **Shizuku Events**: Permission requests, binding status changes
- **ADB Commands**: Every shell command executed and its output
- **Temperature Spoofing**: Spoof apply and reset operations
- **Hardware Detection**: Success/failure of sensor readings
- **Error Tracking**: All exceptions and error messages

### Log Management
- **Real-time Display**: Live log stream with scrollable list
- **Level Filtering**: Color-coded DEBUG/INFO/WARN/ERROR levels
- **Export Function**: Export logs to `.txt` file in Download directory
- **Clear Function**: Clear all logs with single tap

## Hardware Dashboard Features

### Real-time Monitoring
- **Battery Temperature**: Current battery temperature in Celsius
- **CPU Temperature**: Read from thermal zones or dumpsys thermalservice
- **GPU Temperature**: GPU thermal zone readings (device dependent)
- **Ambient Temperature**: Ambient sensor readings (device dependent)
- **CPU Frequencies**: Per-core frequency display with utilization bars
- **GPU Frequency**: GPU clock speed (if available on device)

### Temperature Spoofing
- **Individual Control**: Set different temperatures for each hardware type
- **Global Control**: Apply the same temperature to all hardware at once
- **Status Indicators**: Clear "Real" vs "SPOOFED" badges on each card
- **Graceful Degradation**: Shows "Not supported on this device" for unavailable sensors

## How it Works

ThermalFaker uses Shizuku to execute shell commands at ADB level:

### Battery Temperature
- Uses `dumpsys battery set temp <value>` to change the reported battery temperature
- Value is in tenths of a degree Celsius
- Reset with `dumpsys battery reset`

### CPU/GPU/Ambient Temperature Reading
- Reads from `/sys/class/thermal/thermal_zone*/temp` sysfs nodes
- Falls back to `dumpsys thermalservice` if sysfs unavailable
- Gracefully handles unsupported devices

### CPU Frequency Reading
- Reads from `/sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq`
- Displays current and max frequency for each core

### GPU Frequency Reading
- Attempts to read from various GPU driver paths:
  - `/sys/class/kgsl/kgsl-3d0/gpuclk` (Adreno)
  - `/sys/class/misc/mali0/device/clock` (Mali)
  - `/sys/kernel/gpu/gpu_clock` (Generic)

## Requirements

- **Android 12+** (API 31 or higher)
- **Shizuku** installed and running on your device
- **ADB/Shizuku Permission** granted to the app
- **Root Access (Optional)**: For some advanced temperature spoofing features

## Getting Started

### Prerequisites

1. Install Shizuku from [GitHub](https://github.com/RikkaApps/Shizuku) or Google Play
2. Start Shizuku and grant it necessary permissions
3. Download and install ThermalFaker

### Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/thermalfaker.git
cd thermalfaker

# Open in Android Studio or build with Gradle
./gradlew assembleDebug
```

## Usage

### Main Screen (Battery Temperature)
1. Open ThermalFaker
2. Grant Shizuku permission when prompted
3. Enter your target temperature in Celsius
4. Tap "Apply Spoof" to set the battery temperature
5. To revert, tap "Reset"

### Hardware Dashboard
1. Tap the "Hardware Dashboard" card on the main screen
2. View real-time hardware information
3. Use sliders or input fields to set target temperatures
4. Tap "Set" to apply spoofing for individual hardware
5. Use "Global Temperature Control" to set all at once
6. Tap "Reset" or "Reset All" to revert to real values

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | MVVM |
| Dependency Injection | Hilt |
| Shizuku Integration | Shizuku API 13.1.5 |
| Navigation | Compose Navigation |
| Local Storage | (Optional - DataStore/Room) |
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
│   │   └── HardwareMonitor.kt      # Hardware detection & monitoring
│   └── util/
│       ├── Logger.kt               # Simple logging utility
│       └── LogManager.kt           # Log collection and export manager
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
    │   ├── MainScreen.kt           # Home screen with battery controls
    │   ├── DashboardScreen.kt      # Hardware monitoring & multi-spoofing
    │   └── InfoScreen.kt           # About/info screen
    ├── theme/
    │   ├── Color.kt                # App colors
    │   ├── Theme.kt                # Material 3 theme
    │   └── Type.kt                 # Typography
    └── viewmodel/
        ├── MainViewModel.kt        # ViewModel for main screen
        └── DashboardViewModel.kt   # ViewModel for hardware dashboard
```

## Localization

The app supports the following languages:
- **English** (default)
- **简体中文** (Simplified Chinese)

## License

This project is provided for educational purposes only. Temperature spoofing may violate terms of service of other apps. Use responsibly.

---

# 设备温度伪装 (ThermalFaker)

一款使用 Shizuku 的现代 Android 设备温度伪装应用。

## 功能特性

- **电池温度伪装**: 通过 Shizuku 使用 `dumpsys` 命令修改报告的电池温度
- **硬件仪表盘**: 实时监控 CPU、GPU、电池和环境温度
- **多硬件温度伪装**: 支持伪装 CPU、GPU、电池和环境传感器温度
- **全局温度控制**: 使用单个滑块一键设置所有硬件温度
- **CPU/GPU 频率监控**: 显示实时 CPU 核心频率和 GPU 时钟速度
- **快速重置**: 轻松恢复单个或所有硬件的真实温度值
- **现代 Material 3 UI**: 美观的动态主题，支持深色模式
- **实时温度显示**: 使用动画温度计弧形显示当前温度
- **双语支持**: 英文和简体中文本地化
- **调试日志导出**: 实时日志记录，可导出到下载目录

## 调试日志功能

### 日志收集
- **Shizuku 事件**: 权限请求、绑定状态变化
- **ADB 命令**: 执行的每个 shell 命令及其输出
- **温度伪装**: 伪装应用和重置操作
- **硬件检测**: 传感器读取成功/失败信息
- **错误追踪**: 所有异常和错误消息

### 日志管理
- **实时显示**: 实时日志流，支持滚动列表
- **级别过滤**: 彩色编码 DEBUG/INFO/WARN/ERROR 级别
- **导出功能**: 导出日志到下载目录的 `.txt` 文件
- **清空功能**: 一键清除所有日志

## 硬件仪表盘功能

### 实时监控
- **电池温度**: 当前电池温度（摄氏度）
- **CPU 温度**: 从 thermal zones 或 dumpsys thermalservice 读取
- **GPU 温度**: GPU 热区读数（取决于设备）
- **环境温度**: 环境传感器读数（取决于设备）
- **CPU 频率**: 每个核心的频率显示，带利用率条
- **GPU 频率**: GPU 时钟速度（如果设备支持）

### 温度伪装
- **独立控制**: 为每种硬件类型设置不同的温度
- **全局控制**: 一次性将相同温度应用到所有硬件
- **状态指示器**: 每个卡片上清晰的"真实"与"已伪装"标识
- **优雅降级**: 对不可用的传感器显示"此设备不支持"

## 工作原理

ThermalFaker 使用 Shizuku 在 ADB 级别执行 shell 命令：

### 电池温度
- 使用 `dumpsys battery set temp <value>` 更改报告的电池温度
- 数值以摄氏度十分之一为单位
- 使用 `dumpsys battery reset` 重置

### CPU/GPU/环境温度读取
- 从 `/sys/class/thermal/thermal_zone*/temp` sysfs 节点读取
- 如果 sysfs 不可用，则回退到 `dumpsys thermalservice`
- 优雅处理不支持的设备

### CPU 频率读取
- 从 `/sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq` 读取
- 显示每个核心的当前频率和最大频率

### GPU 频率读取
- 尝试从各种 GPU 驱动路径读取：
  - `/sys/class/kgsl/kgsl-3d0/gpuclk` (Adreno)
  - `/sys/class/misc/mali0/device/clock` (Mali)
  - `/sys/kernel/gpu/gpu_clock` (通用)

## 系统要求

- **Android 12+** (API 31 或更高)
- **Shizuku** 已安装并在设备上运行
- 授予应用 **ADB/Shizuku 权限**
- **Root 权限（可选）**: 用于某些高级温度伪装功能

## 开始使用

### 前置条件

1. 从 [GitHub](https://github.com/RikkaApps/Shizuku) 或 Google Play 安装 Shizuku
2. 启动 Shizuku 并授予必要的权限
3. 下载并安装 ThermalFaker

### 从源码构建

```bash
# 克隆仓库
git clone https://github.com/yourusername/thermalfaker.git
cd thermalfaker

# 在 Android Studio 中打开或使用 Gradle 构建
./gradlew assembleDebug
```

## 使用方法

### 主屏幕（电池温度）
1. 打开 ThermalFaker
2. 按提示授予 Shizuku 权限
3. 输入目标温度（摄氏度）
4. 点击"应用伪装"设置电池温度
5. 点击"重置"恢复

### 硬件仪表盘
1. 在主屏幕上点击"硬件仪表盘"卡片
2. 查看实时硬件信息
3. 使用滑块或输入框设置目标温度
4. 点击"设置"为单个硬件应用伪装
5. 使用"全局温度控制"一次性设置所有温度
6. 点击"重置"或"全部重置"恢复真实值

### 调试日志
1. 从主屏幕点击右上角"Info"图标进入关于页面
2. 点击"调试日志"卡片进入日志界面
3. 查看实时日志流（最新日志在顶部）
4. 点击下载图标导出日志到下载目录
5. 点击清空图标清除所有日志

## 技术栈

| 组件 | 技术 |
|-----------|------------|
| 语言 | Kotlin |
| UI 框架 | Jetpack Compose |
| 架构 | MVVM |
| 依赖注入 | Hilt |
| Shizuku 集成 | Shizuku API 13.1.5 |
| 导航 | Compose Navigation |
| 本地存储 | （可选 - DataStore/Room） |
| 目标 SDK | 34 |
| 最低 SDK | 31 |

## 项目结构

```
com.thermalfaker.app/
├── ThermalFakerApp.kt              # 带 Hilt 设置的 Application 类
├── core/
│   ├── shizuku/
│   │   ├── ShizukuManager.kt       # Shizuku 权限和服务管理
│   │   ├── TemperatureManager.kt   # 温度命令执行
│   │   └── HardwareMonitor.kt      # 硬件检测和监控
│   └── util/
│       ├── Logger.kt               # 简单日志工具
│       └── LogManager.kt           # 日志收集和导出管理器
├── data/
│   └── model/
│       └── AppModels.kt            # UI 状态和设置的数据模型
├── di/
│   └── AppModule.kt                # Hilt 依赖注入模块
└── ui/
    ├── MainActivity.kt             # 带 Edge-to-Edge 的主 Activity
    ├── navigation/
    │   ├── Screen.kt               # 导航路由
    │   └── ThermalFakerNavHost.kt  # Compose 导航设置
    ├── screens/
    │   ├── MainScreen.kt           # 带电池控制的主屏幕
    │   ├── DashboardScreen.kt      # 硬件监控和多硬件伪装
    │   └── InfoScreen.kt           # 关于/信息屏幕
    ├── theme/
    │   ├── Color.kt                # 应用颜色
    │   ├── Theme.kt                # Material 3 主题
    │   └── Type.kt                 # 字体排版
    └── viewmodel/
        ├── MainViewModel.kt        # 主屏幕的 ViewModel
        └── DashboardViewModel.kt   # 硬件仪表盘的 ViewModel
```

## 本地化

应用支持以下语言：
- **English** (默认)
- **简体中文** (简体中文)

## 许可证

本项目仅供教育目的。温度伪装可能违反其他应用的服务条款。请负责任地使用。
