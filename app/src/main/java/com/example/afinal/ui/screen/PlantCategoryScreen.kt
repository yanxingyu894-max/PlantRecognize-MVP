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

// Filter mode: Group by Family or Growing Season
enum class CategoryFilterType {
    CATEGORY, // By Family/Category
    SEASON    // By Season
}

/**
 * Plant Exploration Category Page
 * Responsibility: Aggregates plants by "Category" and "Season" and displays them beautifully in a staggered grid.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlantCategoryScreen(
    viewModel: PlantViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    // Observe plant data from ViewModel
    val plants by viewModel.allPlants.collectAsState()
    val hasPendingDetails by viewModel.hasPendingDetails.collectAsState()
    val isSyncingDetails by viewModel.isSyncingDetails.collectAsState()

    // Adaptive layout: 4 columns in landscape, 2 in portrait
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) 4 else 2

    // Filter state: Default to "CATEGORY"
    var activeFilter by remember { mutableStateOf(CategoryFilterType.CATEGORY) }

    // Group plants based on the selected filter
    val groupedPlants = remember(plants, activeFilter) {
        if (activeFilter == CategoryFilterType.CATEGORY) {
            plants.groupBy { it.category.ifBlank { "Other Families" } }
        } else {
            plants.groupBy { it.season.ifBlank { "All Seasons" } }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Explore Categories",
                        fontWeight = FontWeight.Black,
                        color = PlantGreenDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PlantGreenDark
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PlantBackground)
                .padding(padding)
        ) {
            // Interactive area: Two buttons to switch between Category and Season
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Families Button
                Button(
                    onClick = { activeFilter = CategoryFilterType.CATEGORY },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeFilter == CategoryFilterType.CATEGORY)
                            PlantGreenPrimary else Color.White,
                        contentColor = if (activeFilter == CategoryFilterType.CATEGORY)
                            Color.White else PlantGreenDark
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Families", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                // Seasons Button
                Button(
                    onClick = {
                        activeFilter = CategoryFilterType.SEASON
                        // Trigger accelerated syncing to populate missing Season metadata immediately
                        if (hasPendingDetails) {
                            viewModel.syncAllPendingDetailsNow()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeFilter == CategoryFilterType.SEASON)
                            PlantGreenPrimary else Color.White,
                        contentColor = if (activeFilter == CategoryFilterType.SEASON)
                            Color.White else PlantGreenDark
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Seasons", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Data Content -> Block UI visually if we are querying remaining Season details
            if (activeFilter == CategoryFilterType.SEASON && hasPendingDetails) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PlantGreenPrimary)
                        Spacer(Modifier.height(16.dp))
                        Text("Syncing seasonal data...", color = PlantGreenDark, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (plants.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PlantGreenPrimary)
                }
            } else {
                // Staggered grid layout
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(columns),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Iterate through grouped Map
                    groupedPlants.forEach { (groupName, itemsInGroup) ->
                        // Group header
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Text(
                                text = groupName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = PlantGreenDark,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp, bottom = 8.dp)
                            )
                        }

                        // Plant cards in waterfall flow
                        items(itemsInGroup, key = { "${groupName}_${it.id}" }) { plant ->
                            WaterfallPlantCard(plant) {
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
 * Waterfall specific plant card
 * Responsibility: Adaptive height card for waterfall layout
 */
@Composable
fun WaterfallPlantCard(plant: Plant, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickableOnce { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Adaptive image
            AsyncImage(
                model = plant.imageUrl,
                contentDescription = plant.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.FillWidth,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                error = painterResource(id = android.R.drawable.ic_dialog_alert)
            )
            // Title
            Text(
                text = plant.name,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}