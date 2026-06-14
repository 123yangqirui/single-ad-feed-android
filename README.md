# My Application

一个基于 Android / Kotlin 的智能广告信息流应用。项目围绕“内容推荐 + 本地数据管理 + AI 对话检索”展开，提供登录注册、三频道广告流、图文/视频内容展示、标签过滤、详情页互动、个人中心和 DeepSeek 智能检索等功能。

---

## 一、项目介绍

### 1.1 项目定位

**My Application** 是一个Android平台单列广告信息流的Demo 应用。它通过本地 Room 数据库存储广告内容和用户数据，在首页以信息流形式展示不同频道的广告，同时集成 DeepSeek API，让用户可以通过自然语言搜索广告内容。

- Android 原生页面开发
- RecyclerView 多类型列表
- ViewPager2 多频道信息流
- Room 本地数据库持久化
- Kotlin 协程异步处理
- Retrofit / OkHttp 网络请求
- AI 对话与本地检索结合
- 图文、视频混合内容展示

### 1.2 核心功能

| 功能 | 说明 |
| --- | --- |
| 登录注册 | 用户可在启动页注册或登录，用户数据保存在本地 Room 数据库中。 |
| 三频道信息流 | 首页使用 `ViewPager2` 管理三个频道，支持按钮点击和左右滑动切换。 |
| 多类型广告卡片 | 列表支持大图、小图、视频三种广告样式，并通过不同布局展示。 |
| 分页加载 | 每次加载 11 条数据，滚动到列表底部附近自动加载下一页。 |
| 下拉刷新 | 每个频道支持下拉刷新，刷新后重新加载当前频道内容。 |
| 标签过滤 | 点击广告标签可筛选包含该标签的内容，已选标签会展示在首页顶部。 |
| 详情页 | 展示广告完整内容，支持点赞、收藏、分享、视频播放、静音、进度拖动和全屏。 |
| 状态同步 | 从详情页返回后，列表中的点赞、收藏状态和数量会同步更新。 |
| 个人中心 | 展示当前用户的浏览历史、点赞列表和收藏列表，支持退出登录。 |
| AI 对话检索 | 用户可输入自然语言，DeepSeek 返回检索方式后，应用查询本地广告数据并展示结果。 |
| 本地 Mock 数据 | 首次运行时自动初始化广告数据，无需后端服务即可体验主要功能。 |

### 1.3 技术栈

| 类型 | 技术 |
| --- | --- |
| 开发语言 | Kotlin |
| 构建工具 | Gradle Kotlin DSL |
| Android 插件 | Android Gradle Plugin 9.2.1 |
| UI 体系 | XML Layout、AppCompat、Material Components、ConstraintLayout |
| 列表组件 | RecyclerView、ListAdapter、DiffUtil |
| 页面切换 | ViewPager2、Fragment |
| 数据库 | Room 2.6.1 |
| 异步处理 | Kotlin Coroutines |
| 图片加载 | Glide 4.16.0 |
| 视频播放 | VideoView |
| 视频缓存 | VideoCache 2.7.1 |
| 网络请求 | Retrofit 2.9.0、OkHttp 4.12.0 |
| JSON 解析 | Gson |
| AI 服务 | DeepSeek API |

### 1.4 整体流程

```text
登录界面
   |
   v
主界面
   |
   +-- ViewPager2
   |     +-- 频道一
   |     +-- 频道二
   |     +-- 频道三
   |
   +-- AI对话框 -> 调用大模型 -> 检索本地数据并返回
   |
   +-- 个人中心

```

### 1.5 数据模型

项目主要使用两个 Room 实体：

| 实体 | 表名 | 说明 |
| --- | --- | --- |
| `AdItem` | `ad_items` | 广告内容表，保存标题、简介、详情、标签、图片、视频、频道、点赞收藏状态等。 |
| `User` | `users` | 用户表，保存用户名、密码、浏览历史、点赞列表、收藏列表和偏好标签。 |

`DataInitHelper.initTestData()` 会在首页初始化时检查数据库。如果广告表为空，会自动插入内置 Mock 数据。

---

## 二、如何运行

### 2.1 环境要求

| 环境 | 要求 |
| --- | --- |
| Android Studio | 建议使用较新稳定版 |
| JDK | 11 或更高版本 |
| Gradle | 使用项目自带 Gradle Wrapper |
| minSdk | 24 |
| targetSdk | 36 |
| compileSdk | 36 |

### 2.2 获取项目

```bash
git clone https://github.com/123yangqirui/single-ad-feed-android.git
cd single-ad-feed-android
```

如果你是直接使用当前本地项目，可以跳过克隆步骤，直接用 Android Studio 打开项目根目录。

### 2.3 配置 API Key

项目通过 `local.properties` 读取 DeepSeek API Key。请在项目根目录创建或修改 `local.properties`：

```properties
DEEPSEEK_API_KEY=your_api_key_here
```

说明：

- `local.properties` 只用于本地环境配置
- 不配置 API Key 时，登录、首页、详情页、个人中心、本地 Mock 数据等功能仍可运行。
- AI 对话功能需要有效的 DeepSeek API Key 才能正常请求。

### 2.4 Android Studio 运行

1. 打开 Android Studio。
2. 选择 `Open`，打开项目根目录。
3. 等待 Gradle Sync 完成。
4. 启动模拟器或连接 Android 真机。
5. 选择 `app` 运行配置。
6. 点击 `Run` 启动应用。

### 2.5 命令行构建

macOS / Linux：

```bash
./gradlew assembleDebug
```

Windows PowerShell：

```powershell
.\gradlew.bat assembleDebug
```

构建成功后，Debug APK 通常位于：

```text
app/build/outputs/apk/debug/
```



### 2.6 首次运行说明

首次进入首页时，项目会执行数据初始化：

```text
MainActivity -> DataInitHelper.initTestData() -> Room(ad_database)
```

如果数据库中没有广告数据，会插入内置 Mock 数据；如果已有数据，则不会重复插入。开发阶段数据库版本不匹配时，`AppDatabase` 使用 `fallbackToDestructiveMigration()` 自动重建数据库。

---

## 三、模块划分

### 3.1 根目录结构

```text
.
├── app/                         # Android 应用主模块
├── gradle/                      # Gradle Wrapper 和版本管理文件
├── build.gradle.kts             # 根项目构建配置
├── settings.gradle.kts          # 项目名称、模块和仓库配置
├── gradle.properties            # Gradle 全局属性
├── local.properties             # 本地配置，存放 SDK 路径和 API Key
└── README.md                    # 项目说明文档
```

### 3.2 app 模块结构

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/example/myapplication/
│   ├── LoginActivity.kt
│   ├── MainActivity.kt
│   ├── DetailActivity.kt
│   ├── UserProfileActivity.kt
│   ├── BaseChannelFragment.kt
│   ├── ChannelPagerAdapter.kt
│   ├── FeedAdapter.kt
│   ├── FilterManager.kt
│   ├── channels/
│   ├── dataprocess/
│   ├── dialog/
│   ├── ui/theme/
│   └── util/
└── res/
    ├── layout/
    ├── drawable/
    ├── anim/
    ├── values/
    ├── mipmap-*/
    └── xml/
```

### 3.3 页面模块

| 文件 | 职责 |
| --- | --- |
| `LoginActivity.kt` | 应用启动页，负责用户登录和注册。 |
| `MainActivity.kt` | 首页，负责频道切换、搜索入口、标签状态展示和右上角菜单。 |
| `DetailActivity.kt` | 详情页，展示广告完整内容，处理点赞、收藏、分享和视频控制。 |
| `UserProfileActivity.kt` | 个人中心，展示浏览历史、点赞列表、收藏列表和退出登录。 |
| `DialogActivity.kt` | AI 对话页，负责消息列表、用户输入、AI 响应和搜索结果展示。 |

### 3.4 频道与信息流模块

| 文件 / 目录 | 职责 |
| --- | --- |
| `channels/Channel_1.kt` | 频道 1 Fragment，传入 `channelType = 0`。 |
| `channels/Channel_2.kt` | 频道 2 Fragment，传入 `channelType = 1`。 |
| `channels/Channel_3.kt` | 频道 3 Fragment，传入 `channelType = 2`。 |
| `BaseChannelFragment.kt` | 频道公共基类，封装列表初始化、刷新、分页、过滤和跳转详情。 |
| `ChannelPagerAdapter.kt` | `ViewPager2` 的 Fragment 适配器，负责创建三个频道。 |
| `FeedAdapter.kt` | 信息流列表适配器，支持大图、小图、视频、底部提示等 ViewType。 |
| `FilterManager.kt` | 标签过滤状态管理，负责维护已选标签并通知监听者刷新列表。 |

### 3.5 数据模块

目录：`app/src/main/java/com/example/myapplication/dataprocess/`

| 文件 | 职责 |
| --- | --- |
| `AdItem.kt` | 定义 `AdItem` 广告实体和 `User` 用户实体。 |
| `AdItemDao.kt` | 广告数据访问接口，包含分页查询、按频道查询、点赞收藏更新等方法。 |
| `UserDao.kt` | 用户数据访问接口，包含查询、插入、更新、删除和用户名校验。 |
| `AppDatabase.kt` | Room 数据库配置，数据库名为 `ad_database`。 |
| `DataInitHelper.kt` | 初始化本地 Mock 广告数据。 |
| `ListConverter.kt` | Room 类型转换器，将 `List<String>` 与 JSON 字符串互转。 |
| `UserManager` | 当前定义在 `ListConverter.kt` 中，负责当前用户状态、登录注册、浏览历史、点赞和收藏。 |

### 3.6 AI 对话与搜索模块

目录：`app/src/main/java/com/example/myapplication/dialog/`

| 文件 | 职责 |
| --- | --- |
| `DialogActivity.kt` | 对话页面，负责发送消息、展示 AI 回复和搜索结果。 |
| `Message.kt` | 消息数据模型和消息类型定义。 |
| `MessageAdapter.kt` | 对话消息列表适配器，支持用户消息、AI 消息和带搜索结果的 AI 消息。 |
| `AiClient.kt` | DeepSeek API 客户端，负责构造提示词、发送请求和解析响应。 |
| `AiService.kt` | Retrofit 接口定义。 |
| `AiResponse.kt` | AI 返回结构定义。 |
| `SearchService.kt` | 本地数据库检索服务，根据 AI 返回的方法和标签查询广告。 |

### 3.7 工具与资源模块

| 路径 / 文件 | 职责 |
| --- | --- |
| `util/VideoPlaybackManager.kt` | 统一管理列表视频播放、暂停、恢复和缓存代理。 |
| `ui/theme/` | Compose 主题相关文件，当前项目主要页面仍使用 XML。 |
| `res/layout/` | Activity、Fragment、列表项、弹窗、对话页布局。 |
| `res/drawable/` | 背景、按钮样式、图标、图片资源。 |
| `res/anim/` | 页面切换、弹窗、按钮缩放等动画。 |
| `res/values/` | 颜色、字符串、主题等资源。 |
| `res/xml/` | 备份和数据导出规则。 |

---

## 五、AI 使用声明

本项目在开发过程中使用了 AI 工具作为辅助，以下为具体分工说明。

### 5.1 AI 辅助完成的部分

- **主界面 + ViewPager2 + Fragment 框架搭建**：首页三频道结构、`ChannelPagerAdapter`、`BaseChannelFragment` 基类骨架由 AI 生成，在此基础上进行了完善和调试。
- **网络请求（Retrofit + OkHttp）**：DeepSeek API 客户端、`AiService` 接口定义、请求头构造与 JSON 响应解析的基础代码由 AI 辅助完成。
- **数据库（Room 实体 + DAO + 数据库配置）**：`AdItem` / `User` 实体、`AdItemDao` / `UserDao` 接口、`AppDatabase` 配置及 `TypeConverter` 由 AI 辅助搭建。
- **README 文档**：项目介绍、技术栈、模块划分、开发规范等章节的初稿由 AI 生成。
- **Bug 调试**：编译错误、数据库版本升级、协程调度问题、API 响应解析异常等问题的排查思路和修复方案参考了 AI 建议。
- **设计方案完善**：页面结构、数据流向、AI 检索策略、接口契约设计在讨论阶段参考了 AI 的方案输出。

### 5.2 人工验证和优化

- 对 AI 给出的方案进行合理性评估，判断是否符合项目需求。
- 手动梳理各页面之间的跳转关系、数据传递字段与状态同步流程。
- 对 AI 生成的代码进行手动走读，检查空值处理、线程切换、生命周期管理是否正确。
- 通过 Android Studio 实际编译并在模拟器 / 真机上运行，验证核心功能。
- 检查数据库初始化、点赞收藏状态更新、标签过滤等关键逻辑的数据库行为是否符合预期。
- 对 AI 生成的网络请求代码进行抓包验证，确认请求头、请求体与响应解析格式正确。
- `MainActivity` 频道切换高亮、搜索提示文字滚动动画、右上角三点菜单 Popup 及动画。
- 首页标签过滤区域的动态渲染（选中标签的展示、移除与状态同步）。
- `DetailActivity` 视频播放控制栏：播放/暂停、进度拖动、静音、全屏切换、播放完成后展示封面图。
- 详情页点赞 / 收藏按钮的点击动画、数量格式化、状态回传给列表。
- 列表项多类型布局（大图、小图、视频）的设计与适配。
- 页面切换动画（`slide_in_left`、`slide_out_right` 等）。

- `LoginActivity` → `MainActivity` → `DetailActivity` → 返回刷新 的完整跳转链路。
- `DetailActivity` 使用 `startActivityForResult` / `ActivityResult` 机制回传点赞收藏状态。
- `MainActivity` 右上角菜单跳转到 `UserProfileActivity` 的逻辑。
- `DialogActivity` 从首页搜索框启动并携带初始查询内容。
- `DataInitHelper` 本地 Mock 数据的构造与初始化时机控制。
- `UserManager` 浏览历史、点赞列表、收藏列表的管理逻辑。
- `AdItemDao` 分页查询、按频道查询、按标签过滤、按关键词搜索等 SQL 的编写与调试验证。
- 数据库版本升级时的 `fallbackToDestructiveMigration` 兜底策略确认。
- 视频播放状态管理：避免多个视频同时播放、页面销毁时释放资源。
- 搜索提示文字滚动：`Handler + Runnable` 定时任务在 `onDestroy` 中正确释放。
- 标签过滤：`FilterManager` 全局状态同步，支持跨页面监听与更新。
- 列表分页与下拉刷新：滚动到底部附近触发下一页加载，刷新时重置分页。
- 代码组织：复杂逻辑从 Activity 拆分到 Manager / Service / Adapter，保持页面类职责清晰。
- 动画与视觉细节：按钮点击缩放、页面切换动画、视频控制栏显示/隐藏等交互效果。