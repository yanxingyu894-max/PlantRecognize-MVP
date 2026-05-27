package com.example.afinal.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.afinal.data.model.Plant
import com.example.afinal.ui.theme.PlantBackground
import com.example.afinal.ui.theme.PlantGreenDark
import com.example.afinal.ui.theme.PlantGreenPrimary
import com.example.afinal.ui.utils.clickableOnce
import com.example.afinal.ui.viewmodel.PlantViewModel

// 筛选模式枚举：定义两种分类方式
enum class CategoryFilterType {
    CATEGORY, // 按植物科属/类别分类
    SEASON    // 按生长季节分类
}

/**
 * 植物分类浏览页面
 * 功能说明：按"科属"或"季节"对植物进行分组展示，使用瀑布流布局，适配横竖屏，点击卡片可进入详情页
 * @param viewModel 数据管理中心，负责处理植物数据的获取、分组、同步等逻辑
 * @param onBack 点击返回按钮的回调函数，用于返回上一级页面
 * @param onNavigateToDetail 点击植物卡片的回调函数，传入植物ID跳转到详情页
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlantCategoryScreen(
    viewModel: PlantViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    // 从ViewModel中监听所有植物数据（实时更新）
    val plants by viewModel.allPlants.collectAsState()
    // 从ViewModel中监听是否有未同步的植物详情数据（比如季节信息）
    val hasPendingDetails by viewModel.hasPendingDetails.collectAsState()
    // 从ViewModel中监听是否正在同步植物详情数据
    val isSyncingDetails by viewModel.isSyncingDetails.collectAsState()

    // 获取当前设备的屏幕配置（判断横竖屏）
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    // 根据横竖屏设置瀑布流列数：横屏4列，竖屏2列
    val columns = if (isLandscape) 4 else 2

    // 筛选状态：默认选中"按科属分类"，remember用于保存状态（避免重组时重置）
    var activeFilter by remember { mutableStateOf(CategoryFilterType.CATEGORY) }

    // 按选中的筛选方式分组植物数据：
    // remember(plants, activeFilter)：仅当plants或activeFilter变化时重新计算分组，提升性能
    val groupedPlants = remember(plants, activeFilter) {
        if (activeFilter == CategoryFilterType.CATEGORY) {
            // 按科属分组：如果植物的category为空，归为"Other Families"（其他科属）
            plants.groupBy { it.category.ifBlank { "Other Families" } }
        } else {
            // 按季节分组：如果植物的season为空，归为"All Seasons"（全季节）
            plants.groupBy { it.season.ifBlank { "All Seasons" } }
        }
    }

    // Scaffold：Material3标准页面骨架
    Scaffold(
        // 顶部应用栏配置
        topBar = {
            CenterAlignedTopAppBar(
                // 页面标题："Explore Categories"，加粗，深绿色
                title = {
                    Text(
                        "Explore Categories",
                        fontWeight = FontWeight.Black,
                        color = PlantGreenDark
                    )
                },
                // 左侧返回按钮
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back", // 辅助功能描述（无障碍）
                            tint = PlantGreenDark // 图标颜色（深绿色）
                        )
                    }
                },
                // 顶部应用栏颜色：透明（继承页面背景）
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        // 页面主内容容器（垂直布局）
        Column(
            modifier = Modifier
                .fillMaxSize() // 占满屏幕
                .background(PlantBackground) // 背景色（植物主题背景）
                .padding(padding) // 适配顶部栏内边距
        ) {
            // 筛选按钮行：切换"科属"和"季节"筛选
            Row(
                modifier = Modifier
                    .fillMaxWidth() // 占满宽度
                    .padding(horizontal = 24.dp, vertical = 12.dp), // 水平24dp，垂直12dp内边距
                horizontalArrangement = Arrangement.spacedBy(16.dp) // 按钮之间间距16dp
            ) {
                // 科属筛选按钮
                Button(
                    onClick = { activeFilter = CategoryFilterType.CATEGORY }, // 点击切换为"科属分类"
                    // 按钮颜色：选中时背景绿色、文字白色；未选中时背景白色、文字深绿色
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeFilter == CategoryFilterType.CATEGORY)
                            PlantGreenPrimary else Color.White,
                        contentColor = if (activeFilter == CategoryFilterType.CATEGORY)
                            Color.White else PlantGreenDark
                    ),
                    shape = RoundedCornerShape(16.dp), // 按钮圆角16dp
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp), // 按钮阴影2dp
                    modifier = Modifier
                        .weight(1f) // 占行的一半宽度
                        .height(48.dp) // 按钮高度48dp
                ) {
                    Text("Families", fontSize = 16.sp, fontWeight = FontWeight.Bold) // 按钮文字："Families"（科属）
                }

                // 季节筛选按钮
                Button(
                    onClick = {
                        activeFilter = CategoryFilterType.SEASON // 点击切换为"季节分类"
                        // 如果有未同步的植物详情（比如季节信息），立即触发同步
                        if (hasPendingDetails) {
                            viewModel.syncAllPendingDetailsNow()
                        }
                    },
                    // 按钮颜色：选中时背景绿色、文字白色；未选中时背景白色、文字深绿色
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeFilter == CategoryFilterType.SEASON)
                            PlantGreenPrimary else Color.White,
                        contentColor = if (activeFilter == CategoryFilterType.SEASON)
                            Color.White else PlantGreenDark
                    ),
                    shape = RoundedCornerShape(16.dp), // 按钮圆角16dp
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp), // 按钮阴影2dp
                    modifier = Modifier
                        .weight(1f) // 占行的一半宽度
                        .height(48.dp) // 按钮高度48dp
                ) {
                    Text("Seasons", fontSize = 16.sp, fontWeight = FontWeight.Bold) // 按钮文字："Seasons"（季节）
                }
            }

            // 数据加载/空数据处理
            // 场景1：选中季节筛选且有未同步的详情数据 → 显示同步加载中
            if (activeFilter == CategoryFilterType.SEASON && hasPendingDetails) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center // 居中显示
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PlantGreenPrimary) // 绿色加载转圈
                        Spacer(Modifier.height(16.dp)) // 16dp空白间隔
                        Text("Syncing seasonal data...", color = PlantGreenDark, fontWeight = FontWeight.Bold) // 同步提示文字
                    }
                }
            }
            // 场景2：植物列表为空 → 显示加载中
            else if (plants.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center // 居中显示
                ) {
                    CircularProgressIndicator(color = PlantGreenPrimary) // 绿色加载转圈
                }
            }
            // 场景3：有数据 → 显示瀑布流分组列表
            else {
                // 垂直瀑布流网格（LazyVerticalStaggeredGrid：懒加载，卡片高度自适应，形成瀑布流效果）
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(columns), // 固定列数（横竖屏适配）
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), // 内边距：左右16dp，底部24dp
                    horizontalArrangement = Arrangement.spacedBy(12.dp), // 水平卡片间距12dp
                    verticalItemSpacing = 12.dp, // 垂直卡片间距12dp
                    modifier = Modifier.fillMaxSize() // 占满屏幕
                ) {
                    // 遍历分组后的植物数据
                    groupedPlants.forEach { (groupName, itemsInGroup) ->
                        // 分组标题项：占满整行
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Text(
                                text = groupName, // 分组名称（比如"菊科"、"夏季"）
                                fontSize = 22.sp, // 字体大小22sp
                                fontWeight = FontWeight.Black, // 加粗
                                color = PlantGreenDark, // 深绿色
                                modifier = Modifier
                                    .fillMaxWidth() // 占满宽度
                                    .padding(top = 20.dp, bottom = 8.dp) // 顶部20dp，底部8dp内边距
                            )
                        }

                        // 分组内的植物卡片：瀑布流展示
                        // key = "${groupName}_${it.id}"：用分组名+植物ID作为唯一标识，避免重复渲染
                        items(itemsInGroup, key = { "${groupName}_${it.id}" }) { plant ->
                            // 瀑布流植物卡片（自定义）
                            WaterfallPlantCard(plant) {
                                // 点击卡片跳转到详情页（传入植物ID）
                                onNavigateToDetail(plant.id)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 瀑布流专用植物卡片
 * 功能说明：适配瀑布流的自适应高度卡片，包含植物图片和名称，点击可跳转详情页
 * @param plant 当前植物数据
 * @param onClick 点击卡片的回调函数
 */
@Composable
fun WaterfallPlantCard(plant: Plant, onClick: () -> Unit) {
    // 卡片容器（Material3 Card组件）
    Card(
        modifier = Modifier
            .fillMaxWidth() // 占满宽度
            .clickableOnce { onClick() }, // 点击回调（clickableOnce：防止重复点击）
        shape = RoundedCornerShape(16.dp), // 卡片圆角16dp
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp), // 卡片阴影3dp
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // 卡片背景色（主题表面色）
    ) {
        // 卡片内容：垂直布局（图片+文字）
        Column {
            // 异步加载植物图片（Coil库：高效加载网络/本地图片）
            AsyncImage(
                model = plant.imageUrl, // 图片地址（网络URL）
                contentDescription = plant.name, // 图片描述（无障碍）
                modifier = Modifier
                    .fillMaxWidth() // 占满宽度
                    .wrapContentHeight() // 高度自适应图片
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)), // 图片顶部圆角16dp
                contentScale = ContentScale.FillWidth, // 图片缩放方式：填充宽度
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery), // 加载中占位图（系统图库图标）
                error = painterResource(id = android.R.drawable.ic_dialog_alert) // 加载失败占位图（系统警告图标）
            )
            // 植物名称文本
            Text(
                text = plant.name, // 植物名称
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), // 内边距：左右14dp，垂直12dp
                fontSize = 15.sp, // 字体大小15sp
                fontWeight = FontWeight.Bold, // 加粗
                color = MaterialTheme.colorScheme.onSurface // 文字颜色（主题表面文字色）
            )
        }
    }
}