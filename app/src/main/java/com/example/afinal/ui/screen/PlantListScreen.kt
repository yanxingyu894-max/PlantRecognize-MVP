package com.example.afinal.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.afinal.ui.components.PlantCard
import com.example.afinal.ui.theme.PlantBackground
import com.example.afinal.ui.theme.PlantGreenPrimary
import com.example.afinal.ui.viewmodel.PlantViewModel

// 启用Material3的实验性API（下拉刷新、顶部应用栏等新特性）
@OptIn(ExperimentalMaterial3Api::class)
/**
 * 植物列表页面
 * 功能说明：展示所有植物的列表，支持搜索、下拉刷新、AI外部搜索，适配横竖屏布局，点击植物卡片可进入详情页
 * @param viewModel 数据管理中心，负责处理植物数据的获取、搜索、刷新等逻辑
 * @param onBack 点击返回按钮的回调函数，用于返回上一级页面
 * @param onNavigateToDetail 点击植物卡片的回调函数，传入植物ID跳转到详情页
 */
@Composable
fun PlantListScreen(viewModel: PlantViewModel, onBack: () -> Unit, onNavigateToDetail: (String) -> Unit) {
    // 从ViewModel中监听植物数据：搜索后的植物列表（实时更新）
    val plants by viewModel.allPlants.collectAsState()
    // 从ViewModel中监听搜索框输入的内容（实时更新）
    val searchQuery by viewModel.searchQuery.collectAsState()
    // 从ViewModel中监听初始的植物列表（未搜索时展示的原始数据）
    val initialPlants by viewModel.initialPlants.collectAsState()
    // 从ViewModel中监听下拉刷新状态（是否正在刷新）
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    // 从ViewModel中监听AI外部搜索状态（是否正在执行外部搜索）
    val isExternalSearching by viewModel.isExternalSearching.collectAsState()

    // 获取当前设备的屏幕配置（用于判断横竖屏）
    val configuration = LocalConfiguration.current
    // 判断是否为横屏模式：横屏返回true，竖屏返回false
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    // 根据横竖屏设置网格列数：横屏4列，竖屏2列（适配不同屏幕布局）
    val columns = if (isLandscape) 4 else 2

    // 创建下拉刷新的状态对象（管理下拉刷新的UI状态）
    val pullRefreshState = rememberPullToRefreshState()
    // 创建网格列表的状态对象（管理网格的滚动、定位等）
    val gridState = rememberLazyGridState()

    // 刷新完成后的滚动逻辑：当刷新结束且有植物数据时，平滑滚动到列表顶部
    // LaunchedEffect：监听isRefreshing状态变化，状态改变时执行代码块
    LaunchedEffect(isRefreshing) {
        // 条件：刷新完成（!isRefreshing）且有植物数据（初始列表或搜索列表非空）
        if (!isRefreshing && (plants.isNotEmpty() || initialPlants.isNotEmpty())) {
            // 平滑滚动到网格的第一个位置
            gridState.animateScrollToItem(0)
        }
    }

    // Scaffold：Material3的标准页面骨架，包含顶部应用栏、内容区域等
    Scaffold(
        // 顶部应用栏配置（包含返回按钮、页面标题、搜索框）
        topBar = {
            // Surface：带阴影的容器，用于包裹顶部栏，提升视觉层级
            Surface(
                shadowElevation = 4.dp, // 阴影高度（4dp）
                color = PlantGreenPrimary, // 背景色（植物主题绿色）
                modifier = Modifier.fillMaxWidth() // 宽度占满屏幕
            ) {
                // Column：垂直排列的布局，包含顶部应用栏和搜索框
                Column(modifier = Modifier.statusBarsPadding()) { // statusBarsPadding：适配状态栏高度，避免内容被遮挡
                    // 居中对齐的顶部应用栏
                    CenterAlignedTopAppBar(
                        // 页面标题：显示"Plant Encyclopedia"，文字白色
                        title = { Text("Plant Encyclopedia", color = Color.White) },
                        // 左侧返回按钮
                        navigationIcon = {
                            IconButton(onClick = {
                                // 返回前清空搜索框内容（避免返回后搜索状态残留）
                                viewModel.onSearchQueryChange("")
                                // 执行返回回调，回到上一级页面
                                onBack()
                            }){
                                // 显示返回图标，图标颜色白色，描述为"Back"
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                            }
                        },
                        // 顶部应用栏颜色配置：容器透明（继承Surface的背景色）
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                    // 搜索框行布局
                    Row(
                        modifier = Modifier
                            .fillMaxWidth() // 宽度占满
                            .padding(horizontal = 16.dp, vertical = 8.dp) // 水平内边距16dp，垂直8dp
                            .padding(bottom = 8.dp), // 额外底部内边距8dp
                        verticalAlignment = Alignment.CenterVertically // 垂直居中对齐
                    ) {
                        // 带边框的文本输入框（搜索框）
                        OutlinedTextField(
                            value = searchQuery, // 输入框当前值（绑定ViewModel的搜索关键词）
                            // 输入框内容变化时的回调：更新ViewModel中的搜索关键词
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            modifier = Modifier.fillMaxWidth(), // 宽度占满父布局
                            placeholder = { Text("Search plant name...") }, // 占位提示文字："搜索植物名称..."
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, // 左侧搜索图标
                            trailingIcon = {
                                // 右侧图标/按钮逻辑：
                                // 1. 如果正在执行AI外部搜索，显示加载转圈图标
                                if (isExternalSearching) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                                // 2. 如果搜索框有内容且未在搜索，显示"AI Search"按钮
                                else if (searchQuery.isNotBlank()) {
                                    TextButton(onClick = { viewModel.performExternalSearch() }) {
                                        Text("AI Search", color = PlantGreenPrimary)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp), // 输入框圆角12dp
                            singleLine = true, // 仅允许单行输入（避免搜索框换行）
                            // 输入框颜色配置：
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White, // 聚焦时背景白色
                                unfocusedContainerColor = Color.White, // 未聚焦时背景白色
                                focusedBorderColor = Color.Transparent, // 聚焦时边框透明
                                unfocusedBorderColor = Color.Transparent // 未聚焦时边框透明
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        // 确定要展示的植物列表：搜索框为空时显示初始列表，否则显示搜索结果列表
        val displayPlants = if (searchQuery.isBlank()) initialPlants else plants

        // 页面主内容容器（背景为主题色，适配顶部栏的内边距）
        Box(
            modifier = Modifier
                .fillMaxSize() // 占满屏幕大小
                .background(PlantBackground) // 背景色（植物主题背景色）
                .padding(top = padding.calculateTopPadding()) // 适配顶部栏的内边距，避免内容被遮挡
        ) {
            // 下拉刷新容器（Material3实验性组件）
            PullToRefreshBox(
                isRefreshing = isRefreshing, // 是否正在刷新（绑定ViewModel的刷新状态）
                onRefresh = { viewModel.refreshInitialPlants() }, // 下拉刷新的回调：刷新初始植物列表
                state = pullRefreshState, // 下拉刷新状态对象
                modifier = Modifier.fillMaxSize() // 占满屏幕
            ) {
                // 空数据处理：展示的植物列表为空时
                if (displayPlants.isEmpty()) {
                    // 居中布局容器
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        // 如果正在刷新或AI搜索中，显示加载转圈
                        if (isRefreshing || isExternalSearching) {
                            CircularProgressIndicator(color = PlantGreenPrimary)
                        }
                        // 否则显示空数据提示
                        else {
                            // 垂直排列的提示文本和按钮
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // 提示文字：搜索框为空时显示"正在从自然中收集数据..."，否则显示"本地未找到相关植物"
                                Text(if (searchQuery.isEmpty()) "Collecting data from nature..." else "No relevant plants found locally.", color = Color.Gray)
                                // 如果搜索框有内容，显示"AI & Trefle搜索"按钮
                                if (searchQuery.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp)) // 8dp的空白间隔
                                    Button(
                                        onClick = { viewModel.performExternalSearch() }, // 点击执行AI外部搜索
                                        colors = ButtonDefaults.buttonColors(containerColor = PlantGreenPrimary) // 按钮背景色（主题绿色）
                                    ) {
                                        Text("Search with AI & Trefle")
                                    }
                                }
                            }
                        }
                    }
                }
                // 有植物数据时，显示网格列表
                else {
                    // 垂直网格列表（LazyVerticalGrid：懒加载，仅渲染可见区域，提升性能）
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns), // 固定列数（横竖屏适配）
                        state = gridState, // 网格状态对象（用于滚动控制）
                        contentPadding = PaddingValues(12.dp), // 列表内边距（12dp）
                        verticalArrangement = Arrangement.spacedBy(12.dp), // 垂直方向的卡片间距（12dp）
                        horizontalArrangement = Arrangement.spacedBy(12.dp), // 水平方向的卡片间距（12dp）
                        modifier = Modifier.fillMaxSize() // 占满屏幕
                    ) {
                        // 遍历要展示的植物列表，生成植物卡片
                        // key = { it.id }：用植物ID作为唯一标识，避免列表刷新时重复渲染
                        items(displayPlants, key = { it.id }) { plant ->
                            // 植物卡片组件（自定义）
                            PlantCard(
                                plant = plant, // 当前植物数据
                                onClick = { onNavigateToDetail(plant.id) }, // 点击卡片跳转到详情页（传入植物ID）
                                onFavoriteClick = { viewModel.toggleFavorite(plant) } // 点击收藏按钮的回调：切换植物的收藏状态
                            )
                        }
                    }
                }
            }
        }
    }
}