package com.example.afinal.ui.screens

// 导入安卓系统配置（判断横竖屏）、Compose核心UI库、状态管理、图标等
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // 加载网络图片的组件
import com.example.afinal.data.model.Plant // 植物数据模型（包含名称、图片地址等）
import com.example.afinal.ui.theme.PlantBackground // 自定义背景色
import com.example.afinal.ui.theme.PlantGreenDark // 自定义深植物绿
import com.example.afinal.ui.theme.PlantGreenPrimary // 自定义主植物绿
import com.example.afinal.ui.viewmodel.PlantViewModel // 业务逻辑ViewModel

/**
 * 个人中心页面组件
 * 外行人理解：这是APP的"我的"页面，显示用户信息、收藏的植物、登录/注册/退出等功能，适配横竖屏
 * @param viewModel 处理个人中心业务的核心类（获取收藏、登录状态等）
 * @param onNavigateToHome 点击底部"首页"的回调
 * @param onLogin 点击"登录"的回调（未登录时显示）
 * @param onRegister 点击"注册"的回调（未登录时显示）
 * @param onAbout 点击"关于APP"的回调
 * @param onBack 点击返回的回调
 */
@Composable
fun MyScreen(
    viewModel: PlantViewModel,
    onNavigateToHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onAbout: () -> Unit,
    onBack: () -> Unit
) {
    // 获取设备配置（用于判断横竖屏）
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE // true=横屏，false=竖屏
    // 从ViewModel获取收藏的植物列表（自动监听数据变化，更新UI）
    val favorites by viewModel.favoritePlants.collectAsState()
    // 从ViewModel获取当前登录用户ID（null=未登录）
    val loggedInUserId by viewModel.loggedInUserId.collectAsState()

    // Scaffold：Compose的标准页面骨架（包含顶部栏、底部栏、内容区）
    Scaffold(
        bottomBar = { // 底部导航栏
            NavigationBar(
                containerColor = Color.White, // 底部栏背景白色
                tonalElevation = 8.dp // 阴影高度（增加立体感）
            ) {
                // 首页导航项
                NavigationBarItem(
                    selected = false, // 未选中（当前在个人中心）
                    onClick = onNavigateToHome, // 点击跳首页
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") }, // 首页图标
                    label = { Text("Home") } // 首页文字
                )
                // 个人中心导航项
                NavigationBarItem(
                    selected = true, // 选中（当前页面）
                    onClick = { /* Already here */ }, // 点击无操作（已在当前页）
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }, // 个人中心图标
                    label = { Text("Profile") }, // 个人中心文字
                    // 选中时的颜色：图标/文字为植物绿，背景浅植物绿
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PlantGreenPrimary,
                        selectedTextColor = PlantGreenPrimary,
                        indicatorColor = PlantGreenPrimary.copy(alpha = 0.1f)
                    )
                )
            }
        },
        containerColor = PlantBackground // 页面背景色（自定义）
    ) { padding -> // padding：Scaffold的内边距（避免内容被底部栏遮挡）
        // 根据横竖屏显示不同布局
        if (isLandscape) {
            MyLandscapeContent(
                padding, favorites, loggedInUserId, onLogin, onRegister,
                onLogout = { viewModel.logout() }, // 退出登录回调（调用ViewModel的登出方法）
                onAbout = onAbout
            )
        } else {
            MyPortraitContent(
                padding, favorites, loggedInUserId, onLogin, onRegister,
                onLogout = { viewModel.logout() },
                onAbout = onAbout
            )
        }
    }
}

/**
 * 竖屏布局的个人中心内容
 * @param padding Scaffold的内边距（避免内容被底部栏遮挡）
 * @param favorites 收藏的植物列表
 * @param username 当前登录用户名（null=未登录）
 * @param onLogin 登录回调
 * @param onRegister 注册回调
 * @param onLogout 退出登录回调
 * @param onAbout 关于APP回调
 */
@Composable
fun MyPortraitContent(
    padding: PaddingValues,
    favorites: List<Plant>,
    username: String?,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
    onAbout: () -> Unit
) {
    // 垂直列布局
    Column(
        modifier = Modifier
            .fillMaxSize() // 占满屏幕
            .padding(padding) // 应用Scaffold的内边距
            .padding(horizontal = 20.dp) // 左右内边距20dp
            .verticalScroll(rememberScrollState()), // 内容超出时垂直滚动
        horizontalAlignment = Alignment.CenterHorizontally // 子组件水平居中
    ) {
        // 顶部空白占位符
        Spacer(modifier = Modifier.height(40.dp))

        // 个人资料头部（头像、用户名）
        ProfileHeader(username)

        // 空白占位符
        Spacer(modifier = Modifier.height(32.dp))

        // 收藏植物展示区：有收藏时才显示
        if (favorites.isNotEmpty()) {
            FavoritesSection(favorites)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 功能菜单卡片（登录/注册/退出/关于）
        MenuCard(username, onLogin, onRegister, onLogout, onAbout)

        // 权重1：占满剩余空间（让版本信息靠底部）
        Spacer(modifier = Modifier.weight(1f))

        // 版本信息
        VersionInfo()
        // 底部空白
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * 横屏布局的个人中心内容
 * 逻辑和竖屏一致，只是改成左右分栏布局
 */
@Composable
fun MyLandscapeContent(
    padding: PaddingValues,
    favorites: List<Plant>,
    username: String?,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
    onAbout: () -> Unit
) {
    // 水平行布局（左右分栏）
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically // 垂直居中
    ) {
        // 左侧栏：占1份宽度，显示个人资料
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(username)
        }

        // 左右栏之间的空白
        Spacer(modifier = Modifier.width(32.dp))

        // 右侧栏：占1.5份宽度，显示收藏和功能菜单
        Column(modifier = Modifier.weight(1.5f)) {
            if (favorites.isNotEmpty()) {
                FavoritesSection(favorites)
                Spacer(modifier = Modifier.height(16.dp))
            }

            MenuCard(username, onLogin, onRegister, onLogout, onAbout)

            Spacer(modifier = Modifier.height(16.dp))
            VersionInfo()
        }
    }
}

/**
 * 个人资料头部（头像+用户名+欢迎语）
 * @param username 当前登录用户名（null=未登录）
 */
@Composable
fun ProfileHeader(username: String? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 头像容器（圆形，浅植物绿背景）
        Surface(
            modifier = Modifier.size(100.dp), // 大小100dp
            shape = CircleShape, // 圆形
            color = PlantGreenPrimary.copy(alpha = 0.1f) // 浅植物绿背景
        ) {
            Box(contentAlignment = Alignment.Center) { // 内部居中
                Icon(
                    imageVector = Icons.Default.AccountCircle, // 默认头像图标
                    contentDescription = null, // 无辅助描述
                    modifier = Modifier.size(80.dp), // 图标大小80dp
                    tint = PlantGreenPrimary // 图标颜色植物绿
                )
            }
        }

        // 头像和用户名之间的空白
        Spacer(modifier = Modifier.height(16.dp))

        // 用户名/默认名称
        Text(
            text = username ?: "Plant Explorer", // 有用户名显示用户名，否则显示"Plant Explorer"
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PlantGreenDark
        )

        // 欢迎语
        Text(
            text = if (username != null) "Welcome back to nature" else "Discover the secrets of nature",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

/**
 * 收藏植物展示区
 * @param favorites 收藏的植物列表
 */
@Composable
fun FavoritesSection(favorites: List<Plant>) {
    Column {
        // 标题："我的收藏"
        Text(
            text = "My Collection",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = PlantGreenDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        // 水平滚动的收藏植物缩略图
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()), // 水平滚动
            horizontalArrangement = Arrangement.spacedBy(12.dp) // 缩略图之间间距12dp
        ) {
            // 遍历收藏列表，每个植物显示一个缩略图
            favorites.forEach { plant ->
                FavoriteThumbnail(plant)
            }
        }
    }
}

/**
 * 收藏植物的缩略图卡片
 * @param plant 单个植物数据（包含图片地址、名称等）
 */
@Composable
fun FavoriteThumbnail(plant: Plant) {
    Card(
        modifier = Modifier.size(80.dp), // 卡片大小80dp
        shape = RoundedCornerShape(16.dp), // 圆角16dp
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // 轻微阴影
    ) {
        // 异步加载网络图片（植物图片）
        AsyncImage(
            model = plant.imageUrl, // 图片地址（来自植物数据）
            contentDescription = plant.name, // 辅助描述：植物名称
            modifier = Modifier.fillMaxSize(), // 占满卡片
            contentScale = ContentScale.Crop, // 裁剪图片（填满卡片，保持比例）
            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery), // 加载中显示默认图库图标
            error = painterResource(id = android.R.drawable.ic_dialog_alert) // 加载失败显示警告图标
        )
    }
}

/**
 * 功能菜单卡片（登录/注册/退出/关于）
 * @param username 当前登录用户名（null=未登录）
 */
@Composable
fun MenuCard(
    username: String?,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
    onAbout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(), // 占满宽度
        shape = RoundedCornerShape(24.dp), // 圆角24dp
        colors = CardDefaults.cardColors(containerColor = Color.White), // 白色背景
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // 阴影
    ) {
        Column(modifier = Modifier.padding(8.dp)) { // 内边距8dp
            // 未登录时显示"登录"和"注册"
            if (username == null) {
                MyMenuItem(
                    icon = Icons.Default.Login, // 登录图标
                    title = "Login", // 文字"登录"
                    onClick = onLogin // 点击执行登录回调
                )
                // 分割线
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PlantBackground, thickness = 1.dp)
                MyMenuItem(
                    icon = Icons.Default.AppRegistration, // 注册图标
                    title = "Register Now", // 文字"立即注册"
                    onClick = onRegister // 点击执行注册回调
                )
            } else {
                // 已登录时显示"退出登录"
                MyMenuItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp, // 退出图标
                    title = "Logout", // 文字"退出"
                    onClick = onLogout // 点击执行退出回调
                )
            }

            // 分割线
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PlantBackground, thickness = 1.dp)
            // "关于APP"菜单
            MyMenuItem(
                icon = Icons.Default.Info, // 信息图标
                title = "About App", // 文字"关于APP"
                onClick = onAbout // 点击执行关于回调
            )
        }
    }
}

/**
 * 版本信息文字
 */
@Composable
fun VersionInfo() {
    Text(
        text = "Version 1.0.0", // 版本号
        fontSize = 12.sp,
        color = Color.LightGray,
        modifier = Modifier.fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center // 文字居中
    )
}

/**
 * 通用菜单子项（比如登录、注册、关于）
 * @param icon 左侧图标
 * @param title 菜单文字
 * @param onClick 点击回调
 */
@Composable
fun MyMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    // Surface：可点击的容器（点击无波纹，透明背景）
    Surface(
        onClick = onClick, // 点击执行回调
        color = Color.Transparent, // 透明背景
        modifier = Modifier.fillMaxWidth() // 占满宽度
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp), // 内边距
            verticalAlignment = Alignment.CenterVertically // 垂直居中
        ) {
            // 左侧图标容器（圆形背景）
            Box(
                modifier = Modifier
                    .size(40.dp) // 大小40dp
                    .background(PlantGreenPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), // 浅植物绿背景，圆角
                contentAlignment = Alignment.Center // 图标居中
            ) {
                Icon(icon, contentDescription = null, tint = PlantGreenPrimary, modifier = Modifier.size(20.dp))
            }
            // 图标和文字之间的空白
            Spacer(modifier = Modifier.width(16.dp))
            // 菜单文字（占满剩余宽度）
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = PlantGreenDark,
                modifier = Modifier.weight(1f)
            )
            // 右侧箭头（指示可点击）
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}