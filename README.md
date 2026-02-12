#  coolworker (本地的7*24小时的AI员工)

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-orange.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

因为不想用OpenClaw，且 claude code 本身已经属于比较强的助手了，所以索性做了一个自己的本地+服务器版本。

## 项目简介

EasyWork AI 是一个功能强大的远程服务器管理工具，专为开发者设计。它提供了直观的界面来管理 SSH 连接和 Tmux 会话，让您可以随时随地掌控您的服务器。

### 主要特性

- **SSH 服务器管理**
  - 添加、编辑、删除 SSH 服务器配置
  - 支持自定义端口（默认 22）
  - 保存服务器连接信息
  - 查看连接历史

- **Tmux 会话管理**
  - 列出所有 Tmux 会话
  - 创建新的 Tmux 会话
  - 查看会话状态（活动/非活动）
  - 查看会话窗口数量

- **终端功能**
  - 实时终端输出显示
  - 命令输入和执行
  - 支持 Tab 自动补全
  - 流式命令输出

- **现代化 UI**
  - 基于 Jetpack Compose 的 Material Design 3
  - 支持深色/浅色主题
  - 响应式设计
  - 流畅的动画效果

## 技术栈

### 核心技术
- **Kotlin** - 主要开发语言
- **Jetpack Compose** - 现代化声明式 UI 框架
- **Coroutines** - 异步编程
- **Material Design 3** - UI 设计语言

### 架构组件
- **MVVM** - 架构模式
- **ViewModel** - 管理UI相关数据
- **StateFlow/LiveData** - 响应式数据流
- **Navigation Compose** - 导航管理

### 主要依赖库
- **JSch** - SSH 连接库
- **DataStore** - 数据持久化
- **Lifecycle** - 生命周期管理
- **Navigation** - 导航组件

## 项目结构

```
app/src/main/java/com/easywork/ai/
├── EasyWorkApplication.kt          # Application 类
├── MainActivity.kt                 # 主 Activity
│
├── data/                           # 数据层
│   ├── local/                      # 本地数据
│   │   ├── datastore/              # DataStore 存储
│   │   └── entity/                 # 数据实体
│   └── remote/                     # 远程数据
│       └── ssh/                    # SSH 相关
│           ├── SSHConfig.kt        # SSH 配置
│           └── SSHSession.kt       # SSH 会话管理
│
├── domain/                         # 领域层
│   ├── model/                      # 数据模型
│   │   ├── Server.kt               # 服务器模型
│   │   ├── TmuxSession.kt          # Tmux 会话模型
│   │   └── TerminalLine.kt         # 终端行模型
│   └── repository/                 # 仓库接口
│       ├── IServerRepository.kt    # 服务器仓库接口
│       └── ITmuxRepository.kt      # Tmux 仓库接口
│
├── presentation/                   # 表现层
│   ├── common/                     # 公共组件
│   │   ├── components/             # 通用UI组件
│   │   └── theme/                  # 主题相关
│   ├── navigation/                 # 导航
│   │   ├── AppNavigation.kt        # 导航管理
│   │   └── NavRoute.kt             # 路由定义
│   ├── server/                     # 服务器管理
│   │   ├── ServerListScreen.kt     # 服务器列表
│   │   ├── ServerEditScreen.kt     # 服务器编辑
│   │   ├── ServerListViewModel.kt  # 列表 ViewModel
│   │   └── ServerEditViewModel.kt  # 编辑 ViewModel
│   ├── session/                    # 会话管理
│   │   ├── SessionListScreen.kt    # 会话列表
│   │   ├── SessionViewModel.kt     # 会话 ViewModel
│   │   └── CreateSessionDialog.kt  # 创建会话对话框
│   └── terminal/                   # 终端
│       ├── TerminalScreen.kt       # 终端界面
│       ├── TerminalViewModel.kt    # 终端 ViewModel
│       ├── TerminalOutput.kt       # 终端输出显示
│       └── CommandInput.kt         # 命令输入框
│
└── util/                           # 工具类
    ├── NetworkUtils.kt             # 网络工具
    ├── Result.kt                   # 结果封装
    └── TabCompleter.kt             # Tab 补全
```

## 功能展示

### 服务器管理
- 支持添加多个 SSH 服务器配置
- 显示服务器地址、用户名、最后连接时间
- 快速编辑和删除服务器

### Tmux 会话
- 查看所有 Tmux 会话及其状态
- 创建新的 Tmux 会话
- 查看每个会话的窗口数量

### 终端操作
- 实时终端输出显示
- 支持命令输入和执行
- Tab 键自动补全
- 支持流式命令输出

## 环境要求

- **Android SDK**: API 24+ (Android 7.0+)
- **JDK**: Java 17
- **Kotlin**: 1.9+
- **Gradle**: 8.0+

## 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/wstart/coolworker.git
cd easywork-ai
```

### 2. 打开项目
使用 Android Studio 打开项目：
```bash
open -a "Android Studio" .
```

### 3. 同步依赖
Android Studio 会自动提示同步 Gradle，或手动执行：
```bash
./gradlew build
```

### 4. 运行应用
- 连接 Android 设备或启动模拟器
- 点击 Android Studio 的运行按钮
- 或使用命令：`./gradlew installDebug`

## 构建APK

### Debug版本
```bash
./gradlew assembleDebug
```
APK 位置：`app/build/outputs/apk/debug/app-debug.apk`

### Release版本
```bash
./gradlew assembleRelease
```
APK 位置：`app/build/outputs/apk/release/app-release.apk`

## 权限说明

应用需要以下权限：
- `INTERNET` - 建立 SSH 连接
- `ACCESS_NETWORK_STATE` - 检查网络状态

## 配置说明

### SSH 配置
- 支持标准 SSH 协议
- 默认端口：22
- 支持自定义端口（1-65535）
- 使用密码认证方式

### Tmux 配置
- 要求服务器已安装 Tmux
- 支持 Tmux 基本命令
- 自动检测会话状态

## 开发指南

### 代码规范
- 遵循 Kotlin 官方编码规范
- 使用 MVVM 架构模式
- 保持关注点分离

### 分支管理
- `main` - 主分支，稳定版本
- `develop` - 开发分支
- `feature/*` - 功能分支
- `bugfix/*` - 修复分支

### 提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建/工具相关
```

## 常见问题

### 1. 无法连接到服务器
- 检查网络连接
- 确认服务器地址和端口正确
- 验证用户名和密码
- 检查服务器 SSH 服务是否运行

### 2. Tmux 命令无响应
- 确认服务器已安装 Tmux
- 检查用户是否有 Tmux 执行权限
- 查看终端错误输出

### 3. 应用崩溃
- 查看日志：`adb logcat`
- 清除应用数据重试
- 提交 Issue 并附上日志

## 路线图

- [ ] 支持 SSH 密钥认证
- [ ] 添加 SFTP 文件传输功能
- [ ] 支持多窗口终端
- [ ] 添加命令历史记录
- [ ] 支持自定义终端主题
- [ ] 添加快捷命令
- [ ] 支持端口转发
- [ ] 添加服务器监控功能

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 致谢

- [JSch](http://www.jcraft.com/jsch/) - Java SSH 库
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Android 现代化 UI 工具包
- [Material Design 3](https://m3.material.io/) - 设计系统

## 联系方式

- 项目主页：[GitHub Repository](https://github.com/yourusername/easywork-ai)
- 问题反馈：[Issues](https://github.com/yourusername/easywork-ai/issues)
- 邮箱：your.email@example.com

## Star History

如果这个项目对您有帮助，请给一个 ⭐️ Star！

---

**Made with ❤️ by EasyWork Team**
