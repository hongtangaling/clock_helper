# Clock Helper - 智能闹钟助手

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue?logo=kotlin)
![Android](https://img.shields.io/badge/Android-8.0+-green?logo=android)
![Compose](https://img.shields.io/badge/Jetpack_Compose-latest-brightgreen?logo=jetpackcompose)
![License](https://img.shields.io/badge/License-MIT-yellow)

一款现代化的 Android 智能闹钟应用，采用 Material Design 3 设计语言，提供丰富的闹钟功能和优雅的用户体验。

</div>

## 📱 功能特性

### 核心功能
- ⏰ **灵活的闹钟设置**
  - 支持每日提醒或自定义频率提醒
  - 多时间段免打扰模式
  - 节假日自动跳过功能
  - 星期排除功能（可选择特定日期不响铃）

- 🎵 **个性化铃声**
  - 自定义闹钟音乐选择
  - 可调节响铃次数
  - 震动开关控制

- 🌙 **免打扰时段**
  - 支持多个免打扰时间段设置
  - 智能静音管理

- 📅 **节假日管理**
  - 内置中国节假日数据库
  - 自动识别并跳过节假日闹钟

### 技术亮点
- 🚀 **现代化架构**: MVVM + Clean Architecture
- 💉 **依赖注入**: Hilt
- 💾 **本地存储**: Room Database
- 🎨 **UI 框架**: Jetpack Compose + Material Design 3
- 🔧 **后台任务**: WorkManager
- 📡 **精确闹钟**: AlarmManager API
- 🎭 **媒体播放**: Media3 (ExoPlayer)

## 🏗️ 技术架构

### 技术栈
- **开发语言**: Kotlin
- **最低 SDK**: 26 (Android 8.0)
- **目标 SDK**: 36
- **UI 框架**: Jetpack Compose
- **架构模式**: MVVM + Repository Pattern
- **依赖注入**: Hilt
- **数据库**: Room
- **异步处理**: Kotlin Coroutines

### 主要依赖
```kotlin
// Core & Lifecycle
androidx.core.ktx
androidx.lifecycle.runtime.ktx
androidx.lifecycle.viewmodel.compose

// Compose UI
compose.material3
compose.material.icons.extended

// Navigation
navigation.compose

// Database
room-runtime, room.ktx

// Background Tasks
work.runtime.ktx

// Dependency Injection
hilt-android, hilt-navigation-compose, hilt-work

// Media
media3.exoplayer, media3.ui

// Coroutines
coroutines-core, coroutines-android
```

## 📂 项目结构

```
app/src/main/java/com/kongshuo/clock_helper/
├── data/                          # 数据层
│   ├── dao/                      # 数据访问对象
│   │   ├── AlarmDao.kt           # 闹钟数据访问
│   │   └── HolidayDao.kt         # 节假日数据访问
│   ├── entity/                   # 数据实体
│   │   ├── AlarmEntity.kt        # 闹钟实体
│   │   ├── HolidayEntity.kt      # 节假日实体
│   │   └── QuietTimePeriod.kt    # 免打扰时段
│   └── repository/               # 数据仓库
│       ├── AlarmRepository.kt
│       └── HolidayRepository.kt
├── di/                           # 依赖注入模块
│   └── DatabaseModule.kt
├── domain/                       # 业务逻辑层
│   ├── calculator/
│   │   └── NextFireTimeCalculator.kt  # 下次触发时间计算
│   └── scheduler/
│       └── AlarmScheduler.kt          # 闹钟调度器
├── permission/                   # 权限管理
│   └── PermissionManager.kt
├── service/                      # 后台服务
│   ├── AlarmPlayer.kt            # 闹钟播放器
│   ├── AlarmReceiver.kt          # 闹钟广播接收器
│   ├── AlarmService.kt           # 闹钟前台服务
│   └── BootReceiver.kt           # 开机启动接收器
├── ui/                           # 界面层
│   ├── editor/                   # 闹钟编辑界面
│   │   ├── components/           # 编辑组件
│   │   ├── AlarmEditorScreen.kt
│   │   └── AlarmEditorViewModel.kt
│   ├── home/                     # 主界面
│   │   ├── components/           # 主页组件
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── navigation/               # 导航
│   │   ├── NavGraph.kt
│   │   └── Screen.kt
│   ├── settings/                 # 设置界面
│   └── theme/                    # 主题配置
├── util/                         # 工具类
│   └── ChineseHolidayProvider.kt # 中国节假日提供者
├── worker/                       # 后台工作
│   └── DailyRescheduleWorker.kt  # 每日重新调度工作器
├── ClockApp.kt                   # Application 类
└── MainActivity.kt               # 主 Activity
```

## ✨ 主要功能说明

### 1. 闹钟类型
- **每日提醒**: 每天固定时间响铃
- **频率提醒**: 按设定的分钟间隔重复响铃

### 2. 节假日跳过
启用后，闹钟会在以下情况自动跳过：
- 国家法定节假日
- 周末（可通过星期排除功能自定义）

### 3. 免打扰时段
支持设置多个免打扰时间段，在此期间闹钟不会响铃：
- 可设置多个时间段
- 以 JSON 格式存储在数据库中
- 灵活的时间段管理

### 4. 星期排除
通过位掩码实现，可以选择在一周中的某些天不响铃：
- Sunday (星期日): Bit 0
- Monday (星期一): Bit 1
- Tuesday (星期二): Bit 2
- Wednesday (星期三): Bit 3
- Thursday (星期四): Bit 4
- Friday (星期五): Bit 5
- Saturday (星期六): Bit 6

## 🔐 权限说明

| 权限 | 用途 |
|------|------|
| `POST_NOTIFICATIONS` | 发送通知 |
| `USE_EXACT_ALARM` | 使用精确闹钟 |
| `SCHEDULE_EXACT_ALARM` | 调度精确闹钟 |
| `RECEIVE_BOOT_COMPLETED` | 开机自启动 |
| `VIBRATE` | 震动提醒 |
| `FOREGROUND_SERVICE` | 前台服务运行 |
| `WAKE_LOCK` | 唤醒设备 |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | 请求忽略电池优化 |

## 🛠️ 构建与运行

### 前置要求
- Android Studio Hedgehog 或更高版本
- JDK 17 或更高版本
- Android SDK (最低 API 26, 目标 API 36)

### 构建步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd clock_helper
```

2. **同步 Gradle**
```bash
./gradlew build
```

3. **在 Android Studio 中打开**
   - File → Open → 选择项目目录
   - 等待 Gradle 同步完成

4. **运行应用**
   - 点击 Run 按钮或按 Shift+F10
   - 选择模拟器或真机运行

### 生成 APK
```bash
# Debug 版本
./gradlew assembleDebug

# Release 版本
./gradlew assembleRelease
```

生成的 APK 文件位于 `app/build/outputs/apk/` 目录

## 📊 数据库设计

### alarms 表
存储所有闹钟信息，包括：
- 基本字段: id, hour, minute, isEnabled
- 提醒配置: isDailyReminder, reminderFrequencyMinutes, ringCount
- 个性化: alarmMusicUri, label, isVibrate
- 高级功能: isHolidaySkipped, excludedDaysBitmask, quietTimePeriodsJson
- 时间戳: createdAt, updatedAt, lastFiredAt

### holidays 表
存储节假日信息，用于节假日跳过功能

## 🧪 测试

项目包含单元测试和仪器化测试：

```bash
# 运行单元测试
./gradlew test

# 运行仪器化测试
./gradlew connectedAndroidTest
```

测试文件位置：
- 单元测试: `src/test/java/`
- 仪器化测试: `src/androidTest/java/`

## 📝 开发指南

### 代码规范
- 遵循 Kotlin 官方代码风格
- 使用 Compose 最佳实践
- 采用 MVVM 架构模式
- 使用 Hilt 进行依赖注入

### 添加新功能
1. 在 `data` 层定义数据模型和 DAO
2. 在 `domain` 层实现业务逻辑
3. 在 `ui` 层创建 Compose 界面
4. 使用 Hilt 进行依赖注入

### 调试技巧
- 使用 Logcat 过滤标签查看日志
- 启用严格模式检测问题
- 使用 Compose Layout Inspector 检查 UI

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📮 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 Issue
- 发送邮件至项目维护者

## 🙏 致谢

感谢以下开源项目：
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Hilt](https://dagger.dev/hilt/)
- [Room](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Media3](https://developer.android.com/guide/topics/media/media3)

---

<div align="center">

Made with ❤️ using Kotlin & Jetpack Compose

</div>
