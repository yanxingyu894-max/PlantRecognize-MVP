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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PlantGreenPrimary,
                        selectedTextColor = PlantGreenPrimary
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToMy,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PlantBackground)
                .padding(padding)
        ) {
            if (isLandscape) {
                HomeLandscape(
                    onNavigateToList,
                    onNavigateToFav,
                    onNavigateToRecognition,
                    onNavigateToCategory
                )
            } else {
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

@Composable
fun HomePortrait(
    onNavigateToList: () -> Unit,
    onNavigateToFav: () -> Unit,
    onNavigateToRecognition: () -> Unit,
    onNavigateToCategory: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: PlantViewModel
) {
    val dailyPlant by viewModel.dailyPlant.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Plant Assistant", fontSize = 36.sp, fontWeight = FontWeight.Black, color = PlantGreenDark)
        Text("Making care easy, coloring life", fontSize = 15.sp, color = PlantGreenPrimary, modifier = Modifier.padding(top = 8.dp))
        Spacer(modifier = Modifier.height(16.dp))

        MenuTile(
            title = "AI Camera",
            subtitle = "Identify plant species intelligently",
            icon = Icons.Default.CameraAlt,
            gradientColors = listOf(Color(0xFF52B788), PlantGreenPrimary),
            height = 140.dp,
            onClick = onNavigateToRecognition
        )
        Spacer(modifier = Modifier.height(16.dp))

        MenuTile(
            title = "Categories",
            subtitle = "Explore seasons and families",
            icon = Icons.Default.Category,
            gradientColors = listOf(Color(0xFF74C69D), Color(0xFF2D6A4F)),
            height = 110.dp,
            onClick = onNavigateToCategory
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MenuTile(
                title = "Library",
                subtitle = "100+ Plants",
                icon = Icons.Default.Search,
                gradientColors = listOf(Color(0xFF95D5B2), Color(0xFF40916C)),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToList
            )
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
        Text(
            text = "Daily Care Guide",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PlantGreenDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (dailyPlant != null) {
            DailyCareCard(dailyPlant!!) {
                onNavigateToDetail(dailyPlant!!.id)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PlantGreenPrimary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DailyCareCard(plant: com.example.afinal.data.model.Plant, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            AsyncImage(
                model = plant.imageUrl,
                contentDescription = plant.name,
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                error = painterResource(id = android.R.drawable.ic_dialog_alert)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Surface(
                        color = PlantGreenPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            " ${plant.name} ",
                            style = MaterialTheme.typography.labelSmall,
                            color = PlantGreenDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(plant.family, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PlantGreenPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    val conciseTip = plant.care.ifBlank { "Provide sufficient lighting and proper watering." }
                    Text(
                        text = conciseTip,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        lineHeight = 16.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun HomeLandscape(
    onNavigateToList: () -> Unit,
    onNavigateToFav: () -> Unit,
    onNavigateToRecognition: () -> Unit,
    onNavigateToCategory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Plant Assistant", fontSize = 32.sp, fontWeight = FontWeight.Black, color = PlantGreenDark)
            Text("Making care easy", fontSize = 14.sp, color = PlantGreenPrimary, modifier = Modifier.padding(top = 8.dp))
        }

        Column(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuTile(
                title = "AI Camera",
                subtitle = "Identify plant species intelligently",
                icon = Icons.Default.CameraAlt,
                gradientColors = listOf(Color(0xFF52B788), PlantGreenPrimary),
                height = 100.dp,
                onClick = onNavigateToRecognition,
                isLandscape = true
            )
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(colors = gradientColors))
            .clickableOnce { onClick() }
            .padding(16.dp)
    ) {
        if (isLandscape) {
            // Optimized Landscape layout to avoid position conflict and multi-line titles
            // Subtitle and Icon are placed at the top end (aligned right)
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(24.dp))
            }
            // Title is anchored at the bottom start to prevent overlapping
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomStart),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }
    }
}
