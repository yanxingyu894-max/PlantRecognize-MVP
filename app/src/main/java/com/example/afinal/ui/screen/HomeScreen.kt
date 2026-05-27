package com.example.afinal.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.afinal.ui.theme.PlantBackground
import com.example.afinal.ui.theme.PlantGreenDark
import com.example.afinal.ui.theme.PlantGreenPrimary
import com.example.afinal.ui.utils.clickableOnce
import com.example.afinal.ui.viewmodel.PlantViewModel

/**
 * 应用的首页屏幕组件
 * 核心功能：展示植物助手的主界面，包含导航栏、功能菜单、每日植物养护指南
 * @param onNavigateToList 跳转到植物列表页面的回调函数
 * @param onNavigateToFav 跳转到我的收藏（花园）页面的回调函数
 * @param onNavigateToRecognition 跳转到AI植物识别（相机）页面的回调函数
 * @param onNavigateToCategory 跳转到植物分类页面的回调函数
 * @param onNavigateToMy 跳转到个人中心页面的回调函数
 * @param onNavigateToDetail 跳转到植物详情页面的回调函数（需要传入植物ID）
 * @param viewModel 植物相关的视图模型，用于获取每日推荐植物数据
 */
@Composable
fun HomeScreen(
    onNavigateToList: () -> Unit,
    onNavigateToFav: () -> Unit,
    onNavigateToRecognition: () -> Unit,
    onNavigateToCategory: () -> Unit,
    onNavigateToMy: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: PlantViewModel
) {
    // 获取当前设备的屏幕配置（用于判断横竖屏）
    val configuration = LocalConfiguration.current
    // 判断是否为横屏模式：Configuration.ORIENTATION_LANDSCAPE 代表横屏，PORTRAIT 代表竖屏
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Scaffold是Compose的基础布局组件，提供顶部栏、底部栏、内容区域等标准布局结构
    Scaffold(
        // 底部导航栏配置
        bottomBar = {
            NavigationBar(
                containerColor = Color.White, // 导航栏背景色为白色
                tonalElevation = 8.dp // 导航栏阴影高度，增加立体感
            ) {
                // 首页导航项（选中状态）
                NavigationBarItem(
                    selected = true, // 当前页面是首页，所以选中
                    onClick = { }, // 点击首页不跳转（已经在首页）
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") }, // 首页图标
                    label = { Text("Home") }, // 首页文字标签
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PlantGreenPrimary, // 选中状态图标颜色（植物主题绿色）
                        selectedTextColor = PlantGreenPrimary // 选中状态文字颜色
                    )
                )
                // 个人中心导航项（未选中状态）
                NavigationBarItem(
                    selected = false, // 不是个人中心页面，未选中
                    onClick = onNavigateToMy, // 点击跳转到个人中心
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }, // 个人中心图标
                    label = { Text("Profile") } // 个人中心文字标签
                )
            }
        }
    ) { padding ->
        // Box是Compose的基础容器，用于叠加布局，这里作为整个页面的根容器
        Box(
            modifier = Modifier
                .fillMaxSize() // 填充整个屏幕
                .background(PlantBackground) // 设置页面背景色（植物主题背景色）
                .padding(padding) // 适配Scaffold的内边距（避免内容被底部导航栏遮挡）
        ) {
            // 根据横竖屏加载不同的布局组件
            if (isLandscape) {
                // 横屏布局
                HomeLandscape(
                    onNavigateToList,
                    onNavigateToFav,
                    onNavigateToRecognition,
                    onNavigateToCategory
                )
            } else {
                // 竖屏布局（默认）
                HomePortrait(
                    onNavigateToList,
                    onNavigateToFav,
                    onNavigateToRecognition,
                    onNavigateToCategory,
                    onNavigateToDetail,
                    viewModel
                )
            }
        }
    }
}

/**
 * 首页竖屏布局组件
 * 包含应用标题、功能菜单（AI相机、分类、图书馆、花园）、每日植物养护指南
 * @param onNavigateToList 跳转到植物列表页面的回调
 * @param onNavigateToFav 跳转到我的收藏页面的回调
 * @param onNavigateToRecognition 跳转到AI识别页面的回调
 * @param onNavigateToCategory 跳转到分类页面的回调
 * @param onNavigateToDetail 跳转到植物详情的回调（需传入植物ID）
 * @param viewModel 植物视图模型，用于获取每日推荐植物数据
 */
@Composable
fun HomePortrait(
    onNavigateToList: () -> Unit,
    onNavigateToFav: () -> Unit,
    onNavigateToRecognition: () -> Unit,
    onNavigateToCategory: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: PlantViewModel
) {
    // 从视图模型中收集每日推荐植物数据（响应式数据，数据变化时自动刷新UI）
    val dailyPlant by viewModel.dailyPlant.collectAsState()

    // Column是垂直排列的布局容器，这里作为竖屏内容的根布局
    Column(
        modifier = Modifier
            .fillMaxSize() // 填充整个屏幕
            .padding(horizontal = 24.dp) // 左右内边距24dp（dp是安卓适配单位，适配不同屏幕）
            .verticalScroll(rememberScrollState()) // 允许垂直滚动（内容超出屏幕时可滑动）
    ) {
        // Spacer是空白占位符，用于控制组件间的间距
        Spacer(modifier = Modifier.height(24.dp))
        // 应用主标题
        Text("Plant Assistant", fontSize = 36.sp, fontWeight = FontWeight.Black, color = PlantGreenDark)
        // 应用副标题
        Text("Making care easy, coloring life", fontSize = 15.sp, color = PlantGreenPrimary, modifier = Modifier.padding(top = 8.dp))
        Spacer(modifier = Modifier.height(16.dp))

        // AI相机功能卡片
        MenuTile(
            title = "AI Camera", // 卡片标题
            subtitle = "Identify plant species intelligently", // 卡片副标题
            icon = Icons.Default.CameraAlt, // 相机图标
            gradientColors = listOf(Color(0xFF52B788), PlantGreenPrimary), // 渐变背景色
            height = 140.dp, // 卡片高度
            onClick = onNavigateToRecognition // 点击跳转到AI识别页面
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 植物分类功能卡片
        MenuTile(
            title = "Categories",
            subtitle = "Explore seasons and families",
            icon = Icons.Default.Category,
            gradientColors = listOf(Color(0xFF74C69D), Color(0xFF2D6A4F)),
            height = 110.dp,
            onClick = onNavigateToCategory
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Row是水平排列的布局容器，用于并列展示两个功能卡片
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // 植物图书馆功能卡片（占1/2宽度）
            MenuTile(
                title = "Library",
                subtitle = "100+ Plants",
                icon = Icons.Default.Search,
                gradientColors = listOf(Color(0xFF95D5B2), Color(0xFF40916C)),
                modifier = Modifier.weight(1f), // weight=1f 表示平分Row宽度
                onClick = onNavigateToList
            )
            // 我的花园功能卡片（占1/2宽度）
            MenuTile(
                title = "Garden",
                subtitle = "My Collection",
                icon = Icons.Default.Favorite,
                gradientColors = listOf(Color(0xFFB7E4C7), Color(0xFF52B788)),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToFav
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        // 每日养护指南标题
        Text(
            text = "Daily Care Guide",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PlantGreenDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 判断每日推荐植物是否加载完成
        if (dailyPlant != null) {
            // 加载完成：展示每日植物养护卡片
            DailyCareCard(dailyPlant!!) {
                // 点击卡片跳转到该植物的详情页面（传入植物ID）
                onNavigateToDetail(dailyPlant!!.id)
            }
        } else {
            // 未加载完成：展示加载中占位框
            Box(
                modifier = Modifier
                    .fillMaxWidth() // 填充宽度
                    .height(180.dp) // 固定高度
                    .clip(RoundedCornerShape(24.dp)) // 圆角24dp
                    .background(Color.White) // 白色背景
            ) {
                // 加载中进度条（居中显示）
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PlantGreenPrimary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * 每日植物养护卡片组件
 * 展示单株植物的名称、科属、养护提示和图片
 * @param plant 植物数据模型（包含id、name、family、care、imageUrl等字段）
 * @param onClick 卡片点击事件回调
 */
@Composable
fun DailyCareCard(plant: com.example.afinal.data.model.Plant, onClick: () -> Unit) {
    // Card是带阴影和圆角的卡片组件，Material Design风格
    Card(
        modifier = Modifier
            .fillMaxWidth() // 填充宽度
            .clip(RoundedCornerShape(24.dp)) // 圆角24dp
            .clickable { onClick() }, // 点击触发回调
        colors = CardDefaults.cardColors(containerColor = Color.White), // 卡片背景白色
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // 卡片阴影高度
    ) {
        // Row水平布局：左侧图片，右侧文字信息
        Row(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            // AsyncImage是Coil库提供的图片加载组件，支持网络图片加载
            AsyncImage(
                model = plant.imageUrl, // 图片网络地址
                contentDescription = plant.name, // 图片描述（无障碍功能）
                modifier = Modifier
                    .width(140.dp) // 图片宽度
                    .fillMaxHeight() // 填充卡片高度
                    .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)), // 仅左上角和左下角圆角
                contentScale = ContentScale.Crop, // 图片裁剪模式（填满容器，保持比例）
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery), // 加载中占位图（安卓系统默认图库图标）
                error = painterResource(id = android.R.drawable.ic_dialog_alert) // 加载失败占位图（安卓系统默认警告图标）
            )

            // 右侧文字信息区域（垂直布局）
            Column(
                modifier = Modifier
                    .weight(1f) // 占剩余宽度
                    .padding(16.dp) // 内边距
                    .fillMaxHeight(), // 填充高度
                verticalArrangement = Arrangement.SpaceBetween // 内容垂直分布（上下间距均分）
            ) {
                Column {
                    // 植物名称标签（带浅色背景）
                    Surface(
                        color = PlantGreenPrimary.copy(alpha = 0.1f), // 半透明绿色背景
                        shape = RoundedCornerShape(6.dp) // 圆角6dp
                    ) {
                        Text(
                            " ${plant.name} ", // 植物名称（前后加空格增加边距）
                            style = MaterialTheme.typography.labelSmall, // 系统预设文字样式
                            color = PlantGreenDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // 植物科属
                    Text(plant.family, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PlantGreenPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    // 植物养护提示（如果为空则显示默认提示）
                    val conciseTip = plant.care.ifBlank { "Provide sufficient lighting and proper watering." }
                    Text(
                        text = conciseTip,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        lineHeight = 16.sp, // 行高
                        maxLines = 3, // 最多显示3行
                        overflow = TextOverflow.Ellipsis // 超出部分显示省略号
                    )
                }
            }
        }
    }
}

/**
 * 首页横屏布局组件
 * 适配横屏时的界面布局，分为左右两栏：左侧标题，右侧功能菜单
 * @param onNavigateToList 跳转到植物列表的回调
 * @param onNavigateToFav 跳转到我的收藏的回调
 * @param onNavigateToRecognition 跳转到AI识别的回调
 * @param onNavigateToCategory 跳转到分类的回调
 */
@Composable
fun HomeLandscape(
    onNavigateToList: () -> Unit,
    onNavigateToFav: () -> Unit,
    onNavigateToRecognition: () -> Unit,
    onNavigateToCategory: () -> Unit
) {
    // Row水平布局：分为左右两栏
    Row(
        modifier = Modifier
            .fillMaxSize() // 填充屏幕
            .padding(24.dp) // 内边距
    ) {
        // 左侧栏：应用标题（占1份宽度）
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center // 标题垂直居中
        ) {
            Text("Plant Assistant", fontSize = 32.sp, fontWeight = FontWeight.Black, color = PlantGreenDark)
            Text("Making care easy", fontSize = 14.sp, color = PlantGreenPrimary, modifier = Modifier.padding(top = 8.dp))
        }

        // 右侧栏：功能菜单（占1.5份宽度）
        Column(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()), // 允许垂直滚动
            verticalArrangement = Arrangement.spacedBy(12.dp) // 组件间间距12dp
        ) {
            // AI相机功能卡片（横屏版）
            MenuTile(
                title = "AI Camera",
                subtitle = "Identify plant species intelligently",
                icon = Icons.Default.CameraAlt,
                gradientColors = listOf(Color(0xFF52B788), PlantGreenPrimary),
                height = 100.dp,
                onClick = onNavigateToRecognition,
                isLandscape = true // 标记为横屏布局
            )
            // 分类+图书馆卡片（水平并列）
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MenuTile(
                    title = "Categories",
                    subtitle = "Explore seasons and families",
                    icon = Icons.Default.Category,
                    gradientColors = listOf(Color(0xFF74C69D), Color(0xFF2D6A4F)),
                    modifier = Modifier.weight(1f),
                    height = 100.dp,
                    onClick = onNavigateToCategory,
                    isLandscape = true
                )
                MenuTile(
                    title = "Library",
                    subtitle = "100+ Plants",
                    icon = Icons.Default.Search,
                    gradientColors = listOf(Color(0xFF95D5B2), Color(0xFF40916C)),
                    modifier = Modifier.weight(1f),
                    height = 100.dp,
                    onClick = onNavigateToList,
                    isLandscape = true
                )
            }
            // 我的花园功能卡片（横屏版）
            MenuTile(
                title = "My Garden",
                subtitle = "My Collection",
                icon = Icons.Default.Favorite,
                gradientColors = listOf(Color(0xFFB7E4C7), Color(0xFF52B788)),
                height = 100.dp,
                onClick = onNavigateToFav,
                isLandscape = true
            )
        }
    }
}

/**
 * 通用功能菜单卡片组件
 * 适配横竖屏的通用卡片，支持渐变背景、图标、标题、副标题
 * @param title 卡片主标题
 * @param subtitle 卡片副标题
 * @param icon 卡片图标（ImageVector是Compose的矢量图标类型）
 * @param gradientColors 渐变背景色列表（从第一个色到最后一个色渐变）
 * @param modifier 布局修饰符（用于自定义大小、边距等）
 * @param height 卡片高度（默认110dp）
 * @param onClick 点击事件回调
 * @param isLandscape 是否为横屏布局（控制内部排版）
 */
@Composable
fun MenuTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 110.dp,
    onClick: () -> Unit,
    isLandscape: Boolean = false
) {
    // Box容器：支持层叠布局，用于放置背景、文字、图标
    Box(
        modifier = modifier
            .fillMaxWidth() // 填充宽度
            .height(height) // 设置高度
            .clip(RoundedCornerShape(24.dp)) // 圆角24dp
            .background(Brush.linearGradient(colors = gradientColors)) // 线性渐变背景
            .clickableOnce { onClick() } // 防重复点击的点击事件（自定义工具类）
            .padding(16.dp) // 内边距
    ) {
        // 横屏布局逻辑
        if (isLandscape) {
            // 文字区域：左侧垂直排列，占80%宽度（避免文字和图标重叠）
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.8f)
                    .align(Alignment.CenterStart), // 靠左垂直居中
                verticalArrangement = Arrangement.Center
            ) {
                // 主标题
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1, // 最多1行
                    overflow = TextOverflow.Ellipsis // 超出省略
                )
                Spacer(modifier = Modifier.height(2.dp))
                // 副标题
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f), // 80%不透明度的白色
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 图标：右上角对齐
            Icon(
                imageVector = icon,
                contentDescription = null, // 无描述（装饰性图标）
                tint = Color.White.copy(alpha = 0.9f), // 90%不透明度的白色
                modifier = Modifier
                    .size(24.dp) // 图标大小
                    .align(Alignment.TopEnd) // 右上角对齐
            )
        } else {
            // 竖屏布局逻辑：图标在上，文字在下，靠左底部对齐
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }
    }
}