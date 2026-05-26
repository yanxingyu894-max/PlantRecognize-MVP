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

@Composable
fun DetailScreen(plantId: String, viewModel: PlantViewModel, onBack: () -> Unit) {
    var plantState by remember { mutableStateOf<Plant?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 核心修改：进入详情页时，统一调用 getPlantById
    // 即使列表页有缓存，我们也需要通过这个方法检查 isDetailLoaded 标志位
    LaunchedEffect(plantId) {
        isLoading = true
        val result = viewModel.getPlantById(plantId)
        plantState = result
        isLoading = false
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val plant = plantState

    // 展示加载转圈：当正在联网补全详情，或刚进入页面时
    if (isLoading) {
        Box(Modifier.fillMaxSize().background(PlantBackground), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = PlantGreenPrimary)
                Spacer(Modifier.height(16.dp))
                Text("Fetching rich plant details...", color = PlantGreenPrimary, fontSize = 14.sp)
            }
        }
        return
    }

    // 错误处理：如果最终没拿到数据
    if (plant == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Oops, this plant is missing...", color = Color.Gray)
                Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Go Back") }
            }
        }
        return
    }

    // 定义收藏切换逻辑，手动更新本地状态以触发 UI 重绘
    val onToggleFavorite = {
        viewModel.toggleFavorite(plant)
        plantState = plantState?.copy(isFavorite = !plant.isFavorite)
    }

    // 根据屏幕方向展示详情
    if (isLandscape) {
        DetailLandscape(plant, onBack, onToggleFavorite)
    } else {
        DetailPortrait(plant, onBack, onToggleFavorite)
    }
}

@Composable
fun DetailPortrait(plant: Plant, onBack: () -> Unit, onToggleFavorite: () -> Unit) {
    val listState = rememberLazyListState()
    var scrolledY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        scrolledY = listState.firstVisibleItemScrollOffset.toFloat()
    }

    Box(modifier = Modifier.fillMaxSize().background(PlantBackground)) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            item {
                AsyncImage(
                    model = plant.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .graphicsLayer {
                            translationY = scrolledY * 0.5f
                            alpha = 1f - (scrolledY / 800f).coerceIn(0f, 1f)
                        },
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = painterResource(id = android.R.drawable.ic_dialog_alert)
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = PlantBackground,
                    modifier = Modifier.fillMaxWidth().offset(y = (-32).dp)
                ) {
                    DetailContent(plant)
                }
            }
        }
        DetailTopBar(onBack, isFavorite = plant.isFavorite, onToggleFavorite = onToggleFavorite)
    }
}

@Composable
fun DetailLandscape(plant: Plant, onBack: () -> Unit, onToggleFavorite: () -> Unit) {
    Row(modifier = Modifier.fillMaxSize().background(PlantBackground)) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            AsyncImage(
                model = plant.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                error = painterResource(id = android.R.drawable.ic_dialog_alert)
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier.statusBarsPadding().padding(16.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
        }
        Box(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(plant.name, fontSize = 28.sp, fontWeight = FontWeight.Black, color = PlantGreenDark)
                            if (plant.scientificName.isNotBlank()) {
                                Text("Scientific Name: ${plant.scientificName}", color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.background(PlantGreenPrimary.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                if (plant.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                "Favorite",
                                tint = if (plant.isFavorite) Color(0xFFFF5252) else PlantGreenPrimary
                            )
                        }
                    }
                    DetailContent(plant, showTitle = false)
                }
            }
        }
    }
}

@Composable
fun DetailContent(plant: Plant, showTitle: Boolean = true) {
    Column(modifier = Modifier.padding(24.dp)) {
        if (showTitle) {
            Text(plant.name, fontSize = 32.sp, fontWeight = FontWeight.Black, color = PlantGreenDark)
            if (plant.scientificName.isNotBlank()) {
                Text("Scientific Name: ${plant.scientificName}", color = Color.Gray, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            if (plant.family.isNotBlank()) InfoChip(plant.family)
            if (plant.genus.isNotBlank()) InfoChip(plant.genus)
            if (plant.category.isNotBlank() && plant.category != plant.family) InfoChip(plant.category)
        }

        if (plant.desc.isNotBlank()) {
            Text(plant.desc, fontSize = 15.sp, lineHeight = 24.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 16.dp))
        }

        DetailSectionList("Plant Introduction", buildIntroduction(plant))
        DetailSectionList("Morphological Characteristics", buildMorphology(plant))
        DetailSectionList("Growth Habits", buildHabit(plant))

        if (plant.care.isNotBlank()) {
            DetailSectionText("Care & Maintenance Tips", plant.care, isHighlight = true)
        }
    }
}

fun buildIntroduction(plant: Plant): List<String> {
    val parts = mutableListOf<String>()

    if (plant.commonName.isNotBlank()) parts.add("Common Name: ${plant.commonName}")
    if (plant.family.isNotBlank()) parts.add("Family: ${plant.family}")
    if (plant.genus.isNotBlank()) parts.add("Genus: ${plant.genus}")
    if (plant.duration.isNotBlank()) parts.add("Duration: ${plant.duration}")

    if (plant.edible) {
        val edibleStr = if (plant.ediblePart.isNotBlank()) " (Parts: ${plant.ediblePart})" else ""
        parts.add("Edible: Yes$edibleStr")
    }

    if (plant.toxicity.isNotBlank()) parts.add("Toxicity: ${plant.toxicity}")
    if (plant.nativeDistribution.isNotBlank()) parts.add("Native to: ${plant.nativeDistribution}")
    if (plant.introducedDistribution.isNotBlank()) parts.add("Introduced to: ${plant.introducedDistribution}")

    return parts
}

fun buildMorphology(plant: Plant): List<String> {
    val parts = mutableListOf<String>()

    if (plant.ligneousType.isNotBlank()) parts.add("Ligneous Type: ${plant.ligneousType}")
    if (plant.growthRate.isNotBlank()) parts.add("Growth Rate: ${plant.growthRate}")
    plant.spread?.let { parts.add("Spread: $it cm") }

    if (plant.flowerColor.isNotBlank()) {
        val conspicuous = if (plant.flowerConspicuous == true) " (Conspicuous)" else ""
        parts.add("Flower Color: ${plant.flowerColor}$conspicuous")
    }

    if (plant.foliageColor.isNotBlank()) {
        val retention = if (plant.leafRetention == true) " (Evergreen/Retained)" else ""
        parts.add("Foliage Color: ${plant.foliageColor}$retention")
    }

    if (plant.foliageTexture.isNotBlank()) parts.add("Foliage Texture: ${plant.foliageTexture}")
    if (plant.fruitColor.isNotBlank()) parts.add("Fruit/Seed Color: ${plant.fruitColor}")
    if (plant.fruitShape.isNotBlank()) parts.add("Fruit/Seed Shape: ${plant.fruitShape}")

    return parts
}

fun buildHabit(plant: Plant): List<String> {
    val parts = mutableListOf<String>()

    if (plant.growthHabit.isNotBlank()) parts.add("Growth Habit: ${plant.growthHabit}")
    plant.light?.let { parts.add("Light Level: $it/10") }

    if (plant.phMinimum != null && plant.phMaximum != null) {
        parts.add("Soil pH Range: ${plant.phMinimum} - ${plant.phMaximum}")
    }

    if (plant.minTemp != null || plant.maxTemp != null) {
        val min = plant.minTemp?.toString() ?: "N/A"
        val max = plant.maxTemp?.toString() ?: "N/A"
        parts.add("Temperature Range: $min°C to $max°C")
    }

    plant.soilHumidity?.let { parts.add("Soil Moisture Level: $it/10") }
    plant.soilTexture?.let { parts.add("Soil Texture Level: $it/10") }
    plant.soilNutrients?.let { parts.add("Soil Nutrients Level: $it/10") }
    plant.soilSalinity?.let { parts.add("Soil Salinity Tolerance: $it/10") }

    if (plant.growthMonths.isNotBlank()) parts.add("Growth Months: ${plant.growthMonths}")
    if (plant.bloomMonths.isNotBlank()) parts.add("Bloom Months: ${plant.bloomMonths}")
    if (plant.fruitMonths.isNotBlank()) parts.add("Fruit/Seed Months: ${plant.fruitMonths}")

    return parts
}

@Composable
fun DetailTopBar(onBack: () -> Unit, isFavorite: Boolean, onToggleFavorite: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
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

@Composable
fun InfoChip(label: String) {
    Surface(color = PlantGreenPrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
        Text(label, color = PlantGreenPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
fun DetailSectionList(title: String, bulletPoints: List<String>) {
    if (bulletPoints.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PlantGreenDark)
            Spacer(Modifier.height(8.dp))
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

@Composable
fun DetailSectionText(title: String, content: String, isHighlight: Boolean = false) {
    if (content.isBlank()) return
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isHighlight) PlantGreenPrimary.copy(alpha = 0.08f) else Color.White),
        elevation = CardDefaults.cardElevation(if (isHighlight) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = if (isHighlight) PlantGreenPrimary else PlantGreenDark)
            Spacer(Modifier.height(8.dp))
            Text(content, fontSize = 14.sp, lineHeight = 22.sp, color = Color.DarkGray)
        }
    }
}
