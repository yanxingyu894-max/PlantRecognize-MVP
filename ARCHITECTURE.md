# Architecture Design (架构设计)

本项目遵循 Android 官方推荐的**现代 Android 开发架构 (Modern Android Development)**，采用分层设计以确保代码的可测试性、可维护性和扩展性。

## 1. 总体架构模式：MVVM

项目采用 **MVVM (Model-View-ViewModel)** 模式，结合 **Jetpack Compose** 实现声明式 UI 编程。

-   **View (UI 层)**：
    -   使用 `Jetpack Compose` 构建。
    -   包含 `screens/`（页面级组件）和 `component/`（可复用的小组件）。
    -   通过 `State` 观察 `ViewModel` 中的数据变化并自动重绘。
-   **ViewModel (状态管理层)**：
    -   负责处理 UI 逻辑，并将 `Repository` 提供的数据流转换为 UI 可用的状态 (`StateFlow`/`Compose State`)。
    -   保持数据在配置更改（如屏幕旋转）时的持久性。
-   **Model (数据层)**：
    -   包含 `Repository`、`Remote Data Source` 和 `Local Data Source`。

## 2. 数据层设计 (The Data Layer)

项目采用了 **Repository Pattern (仓库模式)**，`PlantRepository` 是应用唯一的真相来源 (Single Source of Truth)。

### 2.1 仓库模式 (Repository)
`PlantRepository` 协调多个数据源：
-   **本地缓存**：使用 `Room` 数据库存储植物详情和用户收藏，支持离线查看。
-   **远程 API**：
    -   `PlantNet API`：负责图像识别。
    -   `Trefle API`：负责获取标准的植物百科数据。
    -   `DeepSeek AI API`：负责对植物描述、养护指南进行智能增强和翻译。

### 2.2 数据流向
1.  **UI 请求数据**：ViewModel 调用 Repository 的方法。
2.  **Repository 处理**：
    -   首先尝试从 `Room` 获取本地缓存。
    -   如果本地缺失或需要更新，则发起网络请求。
    -   将网络数据解析后存入本地 `Room`，并自动触发 `Flow` 更新。
3.  **UI 自动更新**：ViewModel 观察 `Flow` 数据流，UI 层感知到 `State` 变化并重绘。

## 3. 关键技术选型

-   **声明式 UI**: Jetpack Compose (Material 3)
-   **异步编程**: Kotlin Coroutines & Flow (实现响应式编程)
-   **依赖管理**: Version Catalog (`libs.versions.toml`)
-   **网络库**: Retrofit + OkHttp
-   **数据库**: Room (支持多用户数据隔离)
-   **图片加载**: Coil (支持异步加载网络与本地图片)
-   **相机集成**: CameraX (处理拍照与预览)

## 4. 目录结构说明

```text
com.example.afinal
├── ui/                 # 展示层
│   ├── screens/        # 屏幕界面 (如 Home, Detail, Camera)
│   ├── viewmodel/      # 状态持有者 (ViewModels)
│   └── component/      # 基础 UI 组件
├── data/               # 数据层
│   ├── local/          # 数据库定义 (Room DAO, Entity)
│   ├── remote/         # 网络接口定义 (Retrofit)
│   ├── repository/     # 数据中转与业务逻辑封装
│   └── model/          # 统一的领域模型与数据传输对象 (DTO)
└── util/               # 工具类 (哈希、权限处理、日期格式化等)
```

## 5. 多数据源融合逻辑

本项目的一个特色是**三方数据融合**：
-   当用户识别植物时，`PlantNet` 提供分类名称。
-   `Trefle` 根据名称匹配详细的植物学参数（pH值、生长月份等）。
-   `DeepSeek` 将复杂的学名转换为用户友好的中文描述，并生成定制的养护指南。
-   最后由 `Room` 统一持久化。
