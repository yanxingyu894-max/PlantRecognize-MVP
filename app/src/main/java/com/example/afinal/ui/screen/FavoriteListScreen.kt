package com.example.afinal.ui.screens

// 导入安卓系统配置相关类，用于判断屏幕横竖屏
import android.content.res.Configuration
// 导入Jetpack Compose布局相关组件，用于构建UI布局
import androidx.compose.foundation.layout.*
// 导入Compose网格布局相关组件，用于实现瀑布流/网格列表
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
// 导入Compose Material3图标库，提供预设图标
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
// 导入Compose Material3核心组件，如顶部导航栏、按钮、文本等
import androidx.compose.material3.*
// 导入Compose响应式编程相关注解和函数，用于监听数据变化
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
// 导入Compose UI对齐、修饰符相关类，用于控制UI样式和位置
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
// 导入当前设备配置信息类，用于获取屏幕方向
import androidx.compose.ui.platform.LocalConfiguration
// 导入文本样式相关属性，用于设置字体粗细
import androidx.compose.ui.text.font.FontWeight
// 导入尺寸相关单位，用于设置UI元素大小
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// 导入自定义的植物卡片组件，用于展示单株植物信息
import com.example.afinal.ui.components.PlantCard
// 导入自定义主题颜色，统一APP视觉风格
import com.example.afinal.ui.theme.PlantBackground
import com.example.afinal.ui.theme.PlantGreenDark
import com.example.afinal.ui.theme.PlantGreenPrimary
// 导入植物数据视图模型，用于管理植物数据和业务逻辑
import com.example.afinal.ui.viewmodel.PlantViewModel

/**
 * 收藏页面（我的花园）
 * 核心功能：
 * 1. 展示用户收藏的所有植物
 * 2. 适配横竖屏不同的布局（横屏4列、竖屏2列）
 * 3. 无收藏时显示空状态提示
 * 4. 支持返回上一页、点击植物进入详情页
 * 设计逻辑：采用网格布局展示植物卡片，收藏状态实时响应ViewModel的数据变化
 */
// 声明这是一个Compose可组合函数（用于构建UI的核心函数）
// OptIn注解：表示使用Material3的实验性API（目前已稳定，仅为兼容标记）
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    viewModel: PlantViewModel,                // 植物数据视图模型：管理所有植物数据、收藏状态等业务逻辑
    onBack: () -> Unit,                        // 返回按钮点击事件：点击后执行的返回上一页逻辑
    onNavigateToDetail: (String) -> Unit       // 跳转到详情页事件：接收植物ID，点击植物卡片后跳转到对应植物的详情页
) {
    // 监听收藏植物列表的变化：将ViewModel中的可观察数据转换为Compose状态
    // 当ViewModel中的收藏列表更新时，此UI会自动刷新
    val favoritePlants by viewModel.favoritePlants.collectAsState()

    // 适配性布局：根据屏幕方向调整网格列数
    // 1. 获取当前设备的配置信息（包含屏幕方向、尺寸等）
    val configuration = LocalConfiguration.current
    // 2. 判断是否为横屏：对比设备当前方向与系统横屏常量
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    // 3. 动态设置列数：横屏显示4列，竖屏显示2列
    val columns = if (isLandscape) 4 else 2

    // Scaffold：Compose的核心布局容器，提供标准化的APP页面结构（顶部导航栏、内容区、底部导航等）
    Scaffold(
        // 顶部导航栏配置
        topBar = {
            // 居中对齐的顶部导航栏：Material3推荐的顶部导航样式
            CenterAlignedTopAppBar(
                // 导航栏标题：显示"My Garden"（我的花园），字体加粗
                title = { Text("My Garden", fontWeight = FontWeight.Bold) },
                // 导航栏左侧返回按钮
                navigationIcon = {
                    // 图标按钮：点击触发返回事件
                    IconButton(onClick = onBack) {
                        // 显示返回箭头图标，contentDescription为无障碍服务提供描述（读屏功能）
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // 导航栏颜色配置：使用自定义主题的背景色
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PlantBackground
                )
            )
        },
        // 页面整体背景色：使用自定义主题的背景色
        containerColor = PlantBackground
    ) { padding ->
        // Box：Compose基础布局容器，用于堆叠/包裹子元素，此处作为内容区根容器
        // Modifier：修饰符，用于设置UI元素的大小、内边距、样式等
        // fillMaxSize()：占满父容器所有空间；padding(padding)：适配Scaffold的内边距（避免内容被顶部导航栏遮挡）
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 空状态判断：如果收藏列表为空，显示空状态提示；否则显示植物网格列表
            if (favoritePlants.isEmpty()) {
                // 显示空收藏视图：引导用户添加收藏
                EmptyFavoritesView()
            } else {
                // 垂直网格列表：懒加载模式（仅渲染屏幕可见的项，提升性能）
                LazyVerticalGrid(
                    // 网格列数：使用之前根据屏幕方向计算的列数
                    columns = GridCells.Fixed(columns),
                    // 修饰符：占满所有可用空间
                    modifier = Modifier.fillMaxSize(),
                    // 内边距：网格整体的内边距（上下左右各12dp）
                    contentPadding = PaddingValues(12.dp),
                    // 水平间距：列之间的间距（8dp）
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    // 垂直间距：行之间的间距（8dp）
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 遍历收藏植物列表，生成对应的植物卡片
                    items(
                        items = favoritePlants,          // 要展示的数据源：用户收藏的植物列表
                        key = { it.id }                  // 唯一标识：使用植物ID作为Key，提升列表刷新性能（避免重复渲染）
                    ) { plant ->                         // 遍历每一个植物对象，命名为plant
                        // 自定义植物卡片组件：展示单株植物的信息
                        PlantCard(
                            plant = plant,                          // 当前要展示的植物数据
                            onClick = { onNavigateToDetail(plant.id) },  // 卡片点击事件：跳转到该植物的详情页
                            onFavoriteClick = { viewModel.toggleFavorite(plant) }  // 收藏按钮点击事件：切换该植物的收藏状态
                        )
                    }
                }
            }
        }
    }
}

/**
 * 空收藏状态视图
 * 功能：当用户还没有收藏任何植物时，显示友好的提示界面，引导用户去收藏植物
 */
@Composable
fun EmptyFavoritesView() {
    // Column：垂直布局容器，子元素从上到下排列
    Column(
        modifier = Modifier
            .fillMaxSize()       // 占满所有可用空间
            .padding(32.dp),     // 内边距：上下左右各32dp，避免内容贴边
        verticalArrangement = Arrangement.Center,    // 垂直方向：子元素居中对齐
        horizontalAlignment = Alignment.CenterHorizontally  // 水平方向：子元素居中对齐
    ) {
        // 收藏图标：空心/低透明度的心形图标，提示收藏功能
        Icon(
            imageVector = Icons.Default.Favorite,    // 图标样式：系统预设的收藏心形图标
            contentDescription = null,                // 无障碍描述：null表示无需读屏（纯装饰性图标）
            modifier = Modifier.size(80.dp),          // 图标大小：宽高80dp
            tint = PlantGreenPrimary.copy(alpha = 0.2f)  // 图标颜色：使用主题绿色，透明度20%（浅淡效果）
        )
        // 空白间隔：垂直方向间隔16dp，分隔图标和文字
        Spacer(modifier = Modifier.height(16.dp))

        // 主提示文字：告知用户当前花园为空
        Text(
            text = "Your Garden is Empty",            // 文字内容：你的花园是空的
            fontSize = 20.sp,                         // 字体大小：20sp（sp适配字体缩放）
            fontWeight = FontWeight.Bold,             // 字体粗细：加粗
            color = PlantGreenDark                    // 字体颜色：主题深绿色
        )

        // 辅助提示文字：引导用户如何操作
        Text(
            text = "Look for plants you like in the encyclopedia and click the heart to save them!",
            // 文字内容：去百科中找到你喜欢的植物，点击心形按钮收藏它们！
            fontSize = 14.sp,                         // 字体大小：14sp
            color = Color.Gray,                       // 字体颜色：灰色（次要文字）
            modifier = Modifier.padding(top = 8.dp),   // 内边距：顶部8dp，与上一行文字分隔
            textAlign = androidx.compose.ui.text.style.TextAlign.Center  // 文字对齐：居中对齐
        )
    }
}