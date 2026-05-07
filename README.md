# HabitFlow - 习惯追踪应用

一款优雅的 Android 习惯追踪应用，帮助你培养良好的生活习惯。

## 功能特性

- **习惯管理** - 添加、编辑、删除习惯，自定义名称、描述和目标天数
- **每日打卡** - 一键记录今日完成情况，直观展示进度
- **数据统计** - 追踪完成天数、当前连续、最长连续等数据
- **进度可视化** - 清晰的进度条展示目标完成情况
- **Material 3 设计** - 现代化界面，符合 Material Design 3 规范
- **深色模式** - 支持跟随系统自动切换深色/浅色主题
- **边到边显示** - 全新的 Edge-to-Edge 显示体验

## 技术栈

| 分类 | 技术 |
|------|------|
| **语言** | Kotlin 1.9.22 |
| **UI 框架** | Jetpack Compose (BOM 2024.01.00) |
| **最低 SDK** | API 26 (Android 8.0) |
| **目标 SDK** | API 34 (Android 14) |
| **架构** | MVVM + Clean Architecture |
| **依赖注入** | Hilt 2.50 |
| **本地存储** | Room 2.6.1 |
| **状态管理** | StateFlow + Compose State |
| **导航** | Compose Navigation 2.7.6 |
| **异步** | Kotlin Coroutines + Flow |

## 项目结构

```
com.habitflow.app/
├── data/
│   ├── local/          # Room 数据库相关
│   │   ├── HabitDao.kt
│   │   ├── HabitDatabase.kt
│   │   ├── HabitEntity.kt
│   │   └── HabitRecordEntity.kt
│   ├── model/          # 数据模型
│   │   └── Habit.kt
│   └── repository/      # 仓库层
│       ├── HabitRepository.kt
│       └── HabitRepositoryImpl.kt
├── di/                 # Hilt 依赖注入模块
│   └── AppModule.kt
├── ui/
│   ├── MainActivity.kt
│   ├── detail/         # 习惯详情
│   │   └── HabitDetailViewModel.kt
│   ├── habit/          # 习惯列表 & 添加/编辑
│   │   ├── AddEditHabitViewModel.kt
│   │   └── HabitListViewModel.kt
│   ├── navigation/     # 导航配置
│   │   ├── HabitNavHost.kt
│   │   └── Screen.kt
│   ├── screens/        # Compose 界面
│   │   ├── AddEditHabitScreen.kt
│   │   ├── HabitDetailScreen.kt
│   │   └── HabitListScreen.kt
│   └── theme/          # 主题配置
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── HabitFlowApp.kt     # Application 类
```

## 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK API 34

### 构建步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd HabitFlow
   ```

2. **使用 Android Studio 打开**
   - 打开 Android Studio
   - 选择 "Open an existing project"
   - 选择项目根目录

3. **同步 Gradle**
   - Android Studio 会自动提示同步 Gradle
   - 点击 "Sync Now" 或等待自动同步完成

4. **运行应用**
   - 连接 Android 设备或启动模拟器
   - 点击 Android Studio 工具栏的 Run 按钮 (▶)
   - 或使用快捷键 `Shift + F10`

### 构建 APK

```bash
./gradlew assembleDebug
```

APK 文件将生成在 `app/build/outputs/apk/debug/` 目录。

## 架构说明

### MVVM 架构

- **Model** - 数据层，包括 Room 数据库、Entity、Repository
- **View** - Compose UI 界面，负责展示数据
- **ViewModel** - 业务逻辑，处理 UI 状态，使用 StateFlow 管理

### 依赖注入

使用 Hilt 进行依赖注入，通过 `@HiltViewModel` 注解自动注入 Repository。

### 状态管理

使用 Kotlin Flow 和 StateFlow 进行响应式状态管理，配合 `collectAsStateWithLifecycle()` 在 Compose 中收集状态。

## 屏幕预览

应用包含以下主要屏幕：

1. **习惯列表** - 显示所有习惯，显示今日打卡状态
2. **添加/编辑习惯** - 创建新习惯或编辑现有习惯
3. **习惯详情** - 查看习惯详细信息、统计数据和打卡记录

## 许可证

本项目仅供学习和参考使用。
