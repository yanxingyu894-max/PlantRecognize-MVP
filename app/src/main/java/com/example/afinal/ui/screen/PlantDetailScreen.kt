package com.example.afinal.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import com.example.afinal.ui.theme.PlantBackground
import com.example.afinal.ui.theme.PlantGreenDark
import com.example.afinal.ui.theme.PlantGreenPrimary
import com.example.afinal.ui.viewmodel.PlantViewModel
import com.example.afinal.data.model.Plant

/**
 * 植物详情页面的主入口组件
 * @param plantId 植物的唯一标识ID，用于从ViewModel获取对应植物数据
 * @param viewModel 数据管理的ViewModel，负责处理植物数据的获取、收藏状态切换等逻辑
 * @param onBack 点击返回按钮时的回调函数，用于返回上一级页面
 */
@Composable
fun DetailScreen(plantId: String, viewModel: PlantViewModel, onBack: () -> Unit) {
    // 定义可观察的状态变量：存储当前展示的植物数据，初始值为null
    var plantState by remember { mutableStateOf<Plant?>(null) }
    // 定义加载状态变量：标记是否正在获取植物详情数据，初始为true（刚进入页面需要加载）
    var isLoading by remember { mutableStateOf(true) }

    // 核心逻辑：当plantId发生变化时（比如进入不同植物的详情页），执行数据加载逻辑
    // LaunchedEffect是Compose的副作用处理，确保在协程中执行耗时操作（如网络请求）
    LaunchedEffect(plantId) {
        // 开始加载，设置加载状态为true
        isLoading = true
        // 从ViewModel获取指定ID的植物详情数据
        val result = viewModel.getPlantById(plantId)
        // 将获取到的植物数据赋值给状态变量，触发UI刷新
        plantState = result
        // 加载完成，设置加载状态为false
        isLoading = false
    }

    // 获取当前设备的屏幕配置信息，用于判断横竖屏
    val configuration = LocalConfiguration.current
    // 判断是否为横屏模式：Configuration.ORIENTATION_LANDSCAPE表示横屏
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    // 简化变量：获取当前的植物数据（非空判断在后续处理）
    val plant = plantState

    // 加载状态展示：当正在获取数据时，显示加载动画和提示文字
    if (isLoading) {
        // 全屏背景容器，居中展示内容
        Box(Modifier.fillMaxSize().background(PlantBackground), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 圆形加载进度条，使用主题主色
                CircularProgressIndicator(color = PlantGreenPrimary)
                // 间距：垂直方向16dp
                Spacer(Modifier.height(16.dp))
                // 加载提示文字，告知用户正在获取植物详情
                Text("Fetching rich plant details...", color = PlantGreenPrimary, fontSize = 14.sp)
            }
        }
        // 加载状态下直接返回，不执行后续UI渲染
        return
    }

    // 错误处理：如果最终没有获取到植物数据（plant为null），显示错误提示
    if (plant == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 错误提示文字
                Text("Oops, this plant is missing...", color = Color.Gray)
                // 返回按钮：点击后执行onBack回调返回上一页
                Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Go Back") }
            }
        }
        // 无数据时返回，不执行后续UI渲染
        return
    }

    // 定义收藏状态切换的逻辑函数
    val onToggleFavorite = {
        // 调用ViewModel的方法，修改该植物的收藏状态（数据层）
        viewModel.toggleFavorite(plant)
        // 手动更新本地状态变量，触发UI刷新（界面层）
        // copy方法创建新的Plant对象，修改isFavorite字段为相反值
        plantState = plantState?.copy(isFavorite = !plant.isFavorite)
    }

    // 根据屏幕方向展示不同的布局：横屏/竖屏
    if (isLandscape) {
        DetailLandscape(plant, onBack, onToggleFavorite)
    } else {
        DetailPortrait(plant, onBack, onToggleFavorite)
    }
}

/**
 * 竖屏模式下的植物详情布局
 * @param plant 要展示的植物数据对象
 * @param onBack 返回按钮回调
 * @param onToggleFavorite 收藏状态切换回调
 */
@Composable
fun DetailPortrait(plant: Plant, onBack: () -> Unit, onToggleFavorite: () -> Unit) {
    // 记住LazyColumn的滚动状态，用于监听滚动偏移量
    val listState = rememberLazyListState()
    // 存储滚动的垂直偏移量，用于实现图片滚动时的视觉效果（渐变、位移）
    var scrolledY by remember { mutableFloatStateOf(0f) }

    // 监听LazyColumn的滚动偏移变化，更新scrolledY值
    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        scrolledY = listState.firstVisibleItemScrollOffset.toFloat()
    }

    // 根容器：全屏背景，使用主题的PlantBackground颜色
    Box(modifier = Modifier.fillMaxSize().background(PlantBackground)) {
        // 懒加载列表：只渲染可视区域的内容，提升性能
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            // 列表第一个元素：植物主图
            item {
                AsyncImage(
                    // 图片加载地址：从植物数据中获取imageUrl
                    model = plant.imageUrl,
                    // 内容描述：null表示无需无障碍描述（也可根据需求设置）
                    contentDescription = null,
                    // 图片缩放模式：Crop表示裁剪以填充容器，保持比例
                    contentScale = ContentScale.Crop,
                    // 修饰符：设置图片尺寸+滚动视觉效果
                    modifier = Modifier
                        .fillMaxWidth() // 宽度填充父容器
                        .height(350.dp) // 高度固定350dp
                        .graphicsLayer {
                            // 滚动时图片向上位移：偏移量的50%，营造视差效果
                            translationY = scrolledY * 0.5f
                            // 滚动时图片透明度渐变：滚动800dp后完全透明
                            alpha = 1f - (scrolledY / 800f).coerceIn(0f, 1f)
                        },
                    // 加载中占位图：使用系统默认的图库图标
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    // 加载失败占位图：使用系统默认的警告图标
                    error = painterResource(id = android.R.drawable.ic_dialog_alert)
                )
            }
            // 列表第二个元素：植物详情内容区域
            item {
                Surface(
                    // 圆角形状：顶部左右圆角32dp，营造卡片悬浮效果
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    // 背景色：主题的PlantBackground
                    color = PlantBackground,
                    // 偏移：向上偏移32dp，覆盖在图片底部，增强视觉层次
                    modifier = Modifier.fillMaxWidth().offset(y = (-32).dp)
                ) {
                    // 渲染植物详情核心内容
                    DetailContent(plant)
                }
            }
        }
        // 顶部导航栏：返回按钮+收藏按钮（悬浮在列表上方）
        DetailTopBar(onBack, isFavorite = plant.isFavorite, onToggleFavorite = onToggleFavorite)
    }
}

/**
 * 横屏模式下的植物详情布局
 * @param plant 要展示的植物数据对象
 * @param onBack 返回按钮回调
 * @param onToggleFavorite 收藏状态切换回调
 */
@Composable
fun DetailLandscape(plant: Plant, onBack: () -> Unit, onToggleFavorite: () -> Unit) {
    // 根布局：水平行布局，分为图片区和内容区
    Row(modifier = Modifier.fillMaxSize().background(PlantBackground)) {
        // 左侧图片区：占1份宽度，填充高度
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            AsyncImage(
                model = plant.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(), // 全屏填充左侧区域
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                error = painterResource(id = android.R.drawable.ic_dialog_alert)
            )
            // 返回按钮：悬浮在图片左上角
            IconButton(
                onClick = onBack,
                // 修饰符：状态栏内边距+16dp内边距+半透明黑色圆形背景
                modifier = Modifier.statusBarsPadding().padding(16.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                // 返回图标：白色箭头
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
        }
        // 右侧内容区：占1.2份宽度，填充高度（比图片区宽）
        Box(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
            // 懒加载列表：垂直展示详情内容
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // 内边距：24dp，避免内容贴边
                contentPadding = PaddingValues(24.dp)
            ) {
                item {
                    // 顶部行：植物名称 + 收藏按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween, // 两端对齐
                        verticalAlignment = Alignment.CenterVertically // 垂直居中
                    ) {
                        // 左侧：植物名称+学名
                        Column {
                            Text(plant.name, fontSize = 28.sp, fontWeight = FontWeight.Black, color = PlantGreenDark)
                            // 如果有学名，展示学名
                            if (plant.scientificName.isNotBlank()) {
                                Text("Scientific Name: ${plant.scientificName}", color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                        // 右侧：收藏按钮
                        IconButton(
                            onClick = onToggleFavorite,
                            // 浅绿色半透明圆形背景
                            modifier = Modifier.background(PlantGreenPrimary.copy(alpha = 0.1f), CircleShape)
                        ) {
                            // 收藏图标：已收藏显示实心红心，未收藏显示空心绿心
                            Icon(
                                if (plant.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                "Favorite",
                                tint = if (plant.isFavorite) Color(0xFFFF5252) else PlantGreenPrimary
                            )
                        }
                    }
                    // 渲染植物详情核心内容（不重复显示标题，因为顶部已展示）
                    DetailContent(plant, showTitle = false)
                }
            }
        }
    }
}

/**
 * 详情页面的顶部导航栏（仅竖屏模式使用）
 * @param onBack 返回按钮回调
 * @param isFavorite 当前植物是否被收藏
 * @param onToggleFavorite 收藏状态切换回调
 */
@Composable
fun DetailTopBar(onBack: () -> Unit, isFavorite: Boolean, onToggleFavorite: () -> Unit) {
    // 水平行布局：两端对齐，包含返回和收藏按钮
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 返回按钮
        IconButton(
            onClick = onBack,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
        // 收藏按钮
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(
                if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                "Favorite",
                tint = if (isFavorite) Color(0xFFFF5252) else Color.White
            )
        }
    }
}

/**
 * 植物详情的核心内容展示组件
 * @param plant 植物数据对象
 * @param showTitle 是否显示植物名称标题（竖屏显示，横屏不显示，因为横屏顶部已展示）
 */
@Composable
fun DetailContent(plant: Plant, showTitle: Boolean = true) {
    // 垂直列布局：包含标题、标签、描述、各类详情板块
    Column(modifier = Modifier.padding(24.dp)) {
        // 显示标题区域（可选）
        if (showTitle) {
            // 植物名称：大号粗体字
            Text(plant.name, fontSize = 32.sp, fontWeight = FontWeight.Black, color = PlantGreenDark)
            // 学名：灰色小字，有值时显示
            if (plant.scientificName.isNotBlank()) {
                Text("Scientific Name: ${plant.scientificName}", color = Color.Gray, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
            }
        }

        // 标签行：展示科、属、分类等标签（横向排列，间距8dp）
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            // 科标签：有值时显示
            if (plant.family.isNotBlank()) InfoChip(plant.family)
            // 属标签：有值时显示
            if (plant.genus.isNotBlank()) InfoChip(plant.genus)
            // 分类标签：有值且不等于科名时显示（避免重复）
            if (plant.category.isNotBlank() && plant.category != plant.family) InfoChip(plant.category)
        }

        // 植物描述：有值时显示，常规字号，深灰色
        if (plant.desc.isNotBlank()) {
            Text(plant.desc, fontSize = 15.sp, lineHeight = 24.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 16.dp))
        }

        // 展示“植物简介”板块：由buildIntroduction构建列表数据
        DetailSectionList("Plant Introduction", buildIntroduction(plant))
        // 展示“形态特征”板块：由buildMorphology构建列表数据
        DetailSectionList("Morphological Characteristics", buildMorphology(plant))
        // 展示“生长习性”板块：由buildHabit构建列表数据
        DetailSectionList("Growth Habits", buildHabit(plant))

        // 展示“养护小贴士”板块：有值时显示，高亮样式
        if (plant.care.isNotBlank()) {
            DetailSectionText("Care & Maintenance Tips", plant.care, isHighlight = true)
        }
    }
}

/**
 * 小型信息标签组件（用于展示科、属、分类等短文本）
 * @param label 标签显示的文字内容
 */
@Composable
fun InfoChip(label: String) {
    // 表面容器：浅绿色半透明背景，圆角12dp
    Surface(color = PlantGreenPrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
        // 标签文字：主题色，粗体，内边距保证文字不贴边
        Text(label, color = PlantGreenPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

/**
 * 列表型详情板块组件（用于展示多行列表数据，如植物简介、形态特征）
 * @param title 板块标题
 * @param bulletPoints 列表数据（每行一个条目）
 */
@Composable
fun DetailSectionList(title: String, bulletPoints: List<String>) {
    // 如果列表为空，不渲染该板块
    if (bulletPoints.isEmpty()) return
    // 卡片容器：白色背景，轻微阴影，圆角（默认）
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 板块标题：粗体，深绿色
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PlantGreenDark)
            // 标题与列表间距8dp
            Spacer(Modifier.height(8.dp))
            // 遍历列表数据，逐行显示（带项目符号•）
            bulletPoints.forEach { point ->
                Text(
                    text = "• $point",
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

/**
 * 文本型详情板块组件（用于展示大段文本，如养护小贴士）
 * @param title 板块标题
 * @param content 文本内容
 * @param isHighlight 是否高亮（高亮时背景为浅绿色半透明，无阴影）
 */
@Composable
fun DetailSectionText(title: String, content: String, isHighlight: Boolean = false) {
    // 如果内容为空，不渲染该板块
    if (content.isBlank()) return
    // 卡片容器：根据是否高亮设置背景色和阴影
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isHighlight) PlantGreenPrimary.copy(alpha = 0.08f) else Color.White),
        elevation = CardDefaults.cardElevation(if (isHighlight) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 板块标题：高亮时为主题色，否则为深绿色
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = if (isHighlight) PlantGreenPrimary else PlantGreenDark)
            // 标题与内容间距8dp
            Spacer(Modifier.height(8.dp))
            // 文本内容：常规字号，深灰色
            Text(content, fontSize = 14.sp, lineHeight = 22.sp, color = Color.DarkGray)
        }
    }
}

/**
 * 构建“植物简介”板块的列表数据
 * @param plant 植物数据对象
 * @return 拼接好的简介列表（每行一个信息项）
 */
fun buildIntroduction(plant: Plant): List<String> {
    // 可变列表：存储简介的各个信息项
    val parts = mutableListOf<String>()

    // 通用名：有值时添加
    if (plant.commonName.isNotBlank()) parts.add("Common Name: ${plant.commonName}")
    // 科：有值时添加
    if (plant.family.isNotBlank()) parts.add("Family: ${plant.family}")
    // 属：有值时添加
    if (plant.genus.isNotBlank()) parts.add("Genus: ${plant.genus}")
    // 生长周期：有值时添加（如一年生、多年生）
    if (plant.duration.isNotBlank()) parts.add("Duration: ${plant.duration}")

    // 可食用性：如果是可食用植物
    if (plant.edible) {
        // 可食用部位：有值时拼接，无值时仅显示“Yes”
        val edibleStr = if (plant.ediblePart.isNotBlank()) " (Parts: ${plant.ediblePart})" else ""
        parts.add("Edible: Yes$edibleStr")
    }

    // 毒性：有值时添加（如无毒、轻微有毒、剧毒）
    if (plant.toxicity.isNotBlank()) parts.add("Toxicity: ${plant.toxicity}")
    // 原生分布地：有值时添加
    if (plant.nativeDistribution.isNotBlank()) parts.add("Native to: ${plant.nativeDistribution}")
    // 引入分布地：有值时添加
    if (plant.introducedDistribution.isNotBlank()) parts.add("Introduced to: ${plant.introducedDistribution}")

    return parts
}

/**
 * 构建“形态特征”板块的列表数据
 * @param plant 植物数据对象
 * @return 拼接好的形态特征列表（每行一个信息项）
 */
fun buildMorphology(plant: Plant): List<String> {
    val parts = mutableListOf<String>()

    // 木质类型：有值时添加（如乔木、灌木、草本）
    if (plant.ligneousType.isNotBlank()) parts.add("Ligneous Type: ${plant.ligneousType}")
    // 生长速度：有值时添加（如快速、中等、缓慢）
    if (plant.growthRate.isNotBlank()) parts.add("Growth Rate: ${plant.growthRate}")
    // 扩展范围：非空时添加（单位：厘米）
    plant.spread?.let { parts.add("Spread: $it cm") }

    // 花色：有值时添加，附带是否显眼的标注
    if (plant.flowerColor.isNotBlank()) {
        val conspicuous = if (plant.flowerConspicuous == true) " (Conspicuous)" else ""
        parts.add("Flower Color: ${plant.flowerColor}$conspicuous")
    }

    // 叶色：有值时添加，附带是否常绿的标注
    if (plant.foliageColor.isNotBlank()) {
        val retention = if (plant.leafRetention == true) " (Evergreen/Retained)" else ""
        parts.add("Foliage Color: ${plant.foliageColor}$retention")
    }

    // 叶片质地：有值时添加（如光滑、粗糙、革质）
    if (plant.foliageTexture.isNotBlank()) parts.add("Foliage Texture: ${plant.foliageTexture}")
    // 果实/种子颜色：有值时添加
    if (plant.fruitColor.isNotBlank()) parts.add("Fruit/Seed Color: ${plant.fruitColor}")
    // 果实/种子形状：有值时添加
    if (plant.fruitShape.isNotBlank()) parts.add("Fruit/Seed Shape: ${plant.fruitShape}")

    return parts
}

/**
 * 构建“生长习性”板块的列表数据
 * @param plant 植物数据对象
 * @return 拼接好的生长习性列表（每行一个信息项）
 */
fun buildHabit(plant: Plant): List<String> {
    val parts = mutableListOf<String>()

    // 生长习性：有值时添加（如直立、匍匐、攀援）
    if (plant.growthHabit.isNotBlank()) parts.add("Growth Habit: ${plant.growthHabit}")
    // 光照需求：非空时添加（满分10分）
    plant.light?.let { parts.add("Light Level: $it/10") }

    // 土壤pH值范围：最小值和最大值都有值时添加
    if (plant.phMinimum != null && plant.phMaximum != null) {
        parts.add("Soil pH Range: ${plant.phMinimum} - ${plant.phMaximum}")
    }

    // 温度范围：最小值或最大值有值时添加（单位：摄氏度）
    if (plant.minTemp != null || plant.maxTemp != null) {
        val min = plant.minTemp?.toString() ?: "N/A" // 无最小值时显示N/A
        val max = plant.maxTemp?.toString() ?: "N/A" // 无最大值时显示N/A
        parts.add("Temperature Range: $min°C to $max°C")
    }

    // 土壤湿度：非空时添加（满分10分）
    plant.soilHumidity?.let { parts.add("Soil Moisture Level: $it/10") }
    // 土壤质地：非空时添加（满分10分）
    plant.soilTexture?.let { parts.add("Soil Texture Level: $it/10") }
    // 土壤养分：非空时添加（满分10分）
    plant.soilNutrients?.let { parts.add("Soil Nutrients Level: $it/10") }
    // 土壤盐度耐受性：非空时添加（满分10分）
    plant.soilSalinity?.let { parts.add("Soil Salinity Tolerance: $it/10") }

    // 生长月份：有值时添加（如1-3月，4-10月）
    if (plant.growthMonths.isNotBlank()) parts.add("Growth Months: ${plant.growthMonths}")
    // 开花月份：有值时添加
    if (plant.bloomMonths.isNotBlank()) parts.add("Bloom Months: ${plant.bloomMonths}")
    // 结果/结籽月份：有值时添加
    if (plant.fruitMonths.isNotBlank()) parts.add("Fruit/Seed Months: ${plant.fruitMonths}")

    return parts
}