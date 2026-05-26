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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(viewModel: PlantViewModel, onBack: () -> Unit, onNavigateToDetail: (String) -> Unit) {
    val plants by viewModel.allPlants.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val initialPlants by viewModel.initialPlants.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isExternalSearching by viewModel.isExternalSearching.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) 4 else 2

    val pullRefreshState = rememberPullToRefreshState()
    val gridState = rememberLazyGridState()

    // Automatically clear search query when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onSearchQueryChange("")
        }
    }

    // Smooth scroll to top after refresh finishes
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing && (plants.isNotEmpty() || initialPlants.isNotEmpty())) {
            gridState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = PlantGreenPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    CenterAlignedTopAppBar(
                        title = { Text("Plant Encyclopedia", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search plant name...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank() && plants.isEmpty() && !isExternalSearching) {
                                    TextButton(onClick = { viewModel.performExternalSearch() }) {
                                        Text("AI Search", color = PlantGreenPrimary)
                                    }
                                } else if (isExternalSearching) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        val displayPlants = if (searchQuery.isBlank()) initialPlants else plants

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PlantBackground)
                .padding(top = padding.calculateTopPadding())
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshInitialPlants() },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                if (displayPlants.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (isRefreshing || isExternalSearching) {
                            CircularProgressIndicator(color = PlantGreenPrimary)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (searchQuery.isEmpty()) "Collecting data from nature..." else "No relevant plants found locally.", color = Color.Gray)
                                if (searchQuery.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.performExternalSearch() },
                                        colors = ButtonDefaults.buttonColors(containerColor = PlantGreenPrimary)
                                    ) {
                                        Text("Search with AI & Trefle")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        state = gridState,
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(displayPlants, key = { it.id }) { plant ->
                            PlantCard(
                                plant = plant,
                                onClick = { onNavigateToDetail(plant.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(plant) }
                            )
                        }
                    }
                }
            }
        }
    }
}
