# 骚扰拦截 (SpamBlocker)

基于 Android 15+ (API 35) 的本地骚扰电话与短信拦截应用。

## 功能特性

- ✅ **来电拦截** — 基于 `CallScreeningService` API，静默拦截黑名单来电
- ✅ **短信拦截** — 监听短信接收广播，自动过滤黑名单号码的短信
- ✅ **本地黑名单管理** — Room 数据库存储，支持添加/删除/搜索
- ✅ **灵活拦截策略** — 每个号码可独立设置是否拦截来电/短信
- ✅ **拦截通知** — 拦截后显示通知提醒
- ✅ **Material Design 3** — Jetpack Compose 现代化 UI
- ✅ **暗色模式** — 自动跟随系统主题

## 技术栈

| 分类 | 技术 |
|------|------|
| 语言 | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| 数据库 | Room 2.6 |
| 来电拦截 | CallScreeningService (API 28+) |
| 短信拦截 | BroadcastReceiver + 默认短信角色 |
| 构建 | Gradle KTS + Version Catalog |
| CI/CD | GitHub Actions 云编译 |

## 项目结构

```
SpamBlocker/
├── .github/workflows/build.yml     # CI/CD 云编译配置
├── app/
│   ├── build.gradle.kts            # 应用构建配置
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/spamblocker/
│       │   ├── MainActivity.kt             # 主界面
│       │   ├── SpamBlockerApp.kt           # Application
│       │   ├── data/
│       │   │   ├── BlockedNumber.kt        # Room 实体
│       │   │   ├── BlockedNumberDao.kt     # Room DAO
│       │   │   └── AppDatabase.kt          # Room 数据库
│       │   ├── service/
│       │   │   ├── SpamCallScreeningService.kt  # 来电筛查
│       │   │   ├── SmsBlockReceiver.kt         # 短信拦截
│       │   │   ├── SmsReceiver.kt              # 默认短信接收
│       │   │   ├── SmsRoleRequestActivity.kt   # 短信角色请求
│       │   │   ├── ComposeSmsActivity.kt       # 快捷短信
│       │   │   └── BootReceiver.kt             # 开机自启
│       │   ├── ui/
│       │   │   ├── MainViewModel.kt
│       │   │   ├── theme/
│       │   │   │   ├── Color.kt / Type.kt / Theme.kt
│       │   │   └── screens/
│       │   │       ├── HomeScreen.kt
│       │   │       └── AddNumberScreen.kt
│       │   └── util/
│       │       └── PhoneNumberUtils.kt
│       └── res/
│           ├── values/   (strings, themes, colors)
│           └── xml/      (call_screening_service)
├── gradle/
│   ├── libs.versions.toml    # 版本目录
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 构建方式

### 本地构建

```bash
# macOS / Linux
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

APK 输出位置: `app/build/outputs/apk/debug/app-debug.apk`

### GitHub Actions 云编译

1. Fork 本仓库
2. 推送代码到 `main` 分支
3. GitHub Actions 自动触发构建
4. 在 Actions 页面下载构建产物 (APK)

也可手动触发:
- 进入 Actions → "Android CI Build" → Run workflow
- 选择 `debug` 或 `release` 构建类型

## 使用说明

### 首次启动

1. 安装 APK 后打开应用
2. **授予通知权限** — 用于显示拦截通知
3. **授权来电筛查角色** — 系统会弹出请求，点击"允许"
4. **（可选）设为默认短信应用** — 以获得完整短信拦截能力

### 添加黑名单

1. 点击右下角 **+** 按钮
2. 输入需要拦截的电话号码
3. 可选填写备注标签（如"骚扰电话"）
4. 选择拦截类型（来电/短信）
5. 点击"添加到黑名单"

### 管理黑名单

- 在主页搜索号码或标签
- 点击拦截开关可临时启用/禁用
- 点击删除图标移除号码

## 权限说明

| 权限 | 用途 | 必需 |
|------|------|------|
| `READ_PHONE_STATE` | 读取来电号码 | ✅ |
| `RECEIVE_SMS` / `READ_SMS` | 拦截短信 | ✅ |
| `POST_NOTIFICATIONS` | 显示拦截通知 | 推荐 |
| `RECEIVE_BOOT_COMPLETED` | 开机自启服务 | 推荐 |
| `BIND_SCREENING_SERVICE` | 来电筛查服务 | ✅ (系统级) |

## 系统要求

- Android 9.0+ (API 28+)
- 推荐 Android 15+ (API 35) 以获得最佳体验
- 来电拦截需要系统授予"来电筛查"角色

## License

MIT License — 开源免费使用
