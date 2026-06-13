# My Application

一个基于 Android 平台的智能内容推荐应用，集成 AI 对话功能和内容检索能力。

---

## 🌟 醒目介绍

### 项目简介

**My Application** 是一款现代化的 Android 内容推荐应用，具备以下核心特性：

- **多频道内容展示**：支持三个独立频道的内容浏览，通过 ViewPager2 实现流畅的页面切换
- **智能搜索与 AI 对话**：集成 DeepSeek 大模型，支持自然语言查询和智能内容检索
- **内容过滤系统**：基于标签的内容过滤机制，帮助用户精准定位感兴趣的内容
- **本地数据持久化**：使用 Room 数据库实现本地数据存储，支持离线浏览
- **多媒体内容支持**：支持图片和视频内容的展示与播放

### 技术亮点

| 特性 | 技术实现 |
|------|----------|
| 架构模式 | MVVM + Repository Pattern |
| 数据库 | Room 2.6.1 |
| 网络请求 | Retrofit 2.9.0 + OkHttp 4.12.0 |
| 图片加载 | Glide 4.16.0 |
| 视频缓存 | VideoCache 2.7.1 |
| 页面导航 | ViewPager2 + Fragment |
| AI 集成 | DeepSeek API |

---

## 🚀 如何运行

### 环境要求

- **Android Studio**：Giraffe (2022.3.1) 或更高版本
- **JDK 版本**：11+
- **Gradle 版本**：8.5+
- **最低 SDK**：API 24 (Android 7.0)
- **目标 SDK**：API 36 (Android 15)

### 配置步骤

1. **克隆项目**

```bash
git clone https://github.com/123yangqirui/single-ad-feed-android.git
cd single-ad-feed-android
```

2. **配置 API Key**

在项目根目录创建 `local.properties` 文件，并添加您的 DeepSeek API Key：

```properties
# local.properties
DEEPSEEK_API_KEY=your_api_key_here
```

> **获取 API Key**：访问 [DeepSeek Console](https://platform.deepseek.com/) 注册账号并获取 API Key

3. **同步项目**

打开 Android Studio，导入项目后等待 Gradle 同步完成。

### 运行项目

1. 连接 Android 设备或启动模拟器
2. 在 Android Studio 中点击 **Run** 按钮（绿色三角形）
3. 选择目标设备后等待应用安装启动

### Mock 数据

项目已内置 Mock 数据，无需额外配置即可运行：
- `app\src\main\java\com\example\myapplication\dataprocess\DataInitHelper.kt` - 数据初始化助手，负责初始化数据库和插入 Mock 数据

---

## 📦 模块划分

```
app/src/main/java/com/example/myapplication/
├── channels/          # 频道模块
│   ├── Channel_1.kt   # 频道 1 Fragment
│   ├── Channel_2.kt   # 频道 2 Fragment
│   └── Channel_3.kt   # 频道 3 Fragment
├── dataprocess/       # 数据处理模块
│   ├── AdItem.kt      # 广告数据模型
│   ├── AdItemDao.kt   # 数据访问接口
│   ├── AppDatabase.kt # 数据库配置
│   ├── DataInitHelper.kt # 数据初始化助手
│   ├── ListConverter.kt  # 类型转换器
│   ├── MyApplication.kt  # 应用入口
│   └── UserDao.kt     # 用户数据访问接口
├── dialog/            # 对话模块
│   ├── AiClient.kt    # AI 客户端
│   ├── AiResponse.kt  # AI 响应模型
│   ├── AiService.kt   # AI 服务接口
│   ├── DialogActivity.kt  # 对话页面
│   ├── Message.kt     # 消息模型
│   ├── MessageAdapter.kt  # 消息列表适配器
│   └── SearchService.kt   # 搜索服务
├── ui/theme/          # UI 主题配置
│   ├── Color.kt       # 颜色定义
│   ├── Theme.kt       # 主题配置
│   └── Type.kt        # 字体样式
├── util/              # 工具模块
│   └── VideoPlaybackManager.kt  # 视频播放管理
├── BaseChannelFragment.kt   # 频道 Fragment 基类
├── ChannelPagerAdapter.kt   # ViewPager2 适配器
├── DetailActivity.kt        # 详情页面
├── FeedAdapter.kt           # 内容列表适配器
├── FilterManager.kt         # 过滤器管理器
├── LoginActivity.kt         # 登录页面
├── MainActivity.kt          # 主页面
└── UserProfileActivity.kt   # 用户个人中心
```

### 模块职责说明

| 模块 | 职责 | 核心文件 |
|------|------|----------|
| **channels** | 内容频道展示 | Channel_1/2/3.kt |
| **dataprocess** | 数据持久化与管理 | AppDatabase.kt, AdItemDao.kt |
| **dialog** | AI 对话与搜索 | AiClient.kt, SearchService.kt |
| **ui/theme** | 主题与样式配置 | Color.kt, Theme.kt |
| **util** | 工具类 | VideoPlaybackManager.kt |

---

## 📋 开发规范

### 代码风格

1. **命名规范**
   - 类名：采用 PascalCase，如 `MainActivity`
   - 方法名：采用 camelCase，如 `setupSearchBox`
   - 变量名：采用 camelCase，如 `searchHintText`
   - 常量名：采用 UPPER_CASE，如 `MAX_ITEMS`
   - 资源文件：采用 snake_case，如 `bg_channel_btn_selected.xml`

2. **代码结构**
   - 每个类职责单一，遵循 SRP 原则
   - 方法长度不超过 50 行
   - 使用 Kotlin 特性：空安全、扩展函数、协程

### 架构规范

1. **分层架构**
   - **UI 层**：Activity/Fragment/Adapter，负责界面展示和用户交互
   - **数据层**：Repository/Dao，负责数据存取
   - **业务层**：Service/Manager，负责业务逻辑处理

2. **数据流向**
   - 单向数据流：UI → ViewModel → Repository → DataSource
   - 使用 Coroutine 处理异步操作
   - 使用 LiveData/Flow 进行数据观察

### 资源管理

1. **资源命名**
   - 布局文件：`activity_*.xml`, `fragment_*.xml`, `item_*.xml`
   - 动画文件：`anim_*.xml`
   - 样式文件：`style_*.xml`, `theme_*.xml`

2. **资源组织**
   - 图片资源按功能分类存放
   - Drawable 资源使用 Vector 格式优先
   - 颜色和字符串统一在 `res/values/` 中管理

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

*Made with ❤️ for Android Development*