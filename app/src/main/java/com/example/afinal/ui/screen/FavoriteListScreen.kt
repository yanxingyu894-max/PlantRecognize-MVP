package com.example.afinal.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.ui.components.PlantCard
import com.example.afinal.ui.theme.PlantBackground
import com.example.afinal.ui.theme.PlantGreenDark
import com.example.afinal.ui.theme.PlantGreenPrimary
import com.example.afinal.ui.viewmodel.PlantViewModel

/**
 * Favorites Page (My Garden)
 * Responsibility: Displays all plants favorited by the user, supports adaptive layout, empty state prompts, and navigation to details.
 * Design: Grid layout, favorite status responds in real-time to ViewModel changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    viewModel: PlantViewModel,                // Plant data and state management
    onBack: () -> Unit,                        // Back navigation
    onNavigateToDetail: (String) -> Unit       // Navigate to details page (pass plant ID)
) {
    // Observe favorite plants list
    val favoritePlants by viewModel.favoritePlants.collectAsState()

    // Adaptive layout: 4 columns in landscape, 2 in portrait
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) 4 else 2

    Scaffold(
        topBar = {
            // Top Bar: Title + Back button
            CenterAlignedTopAppBar(
                title = { Text("My Garden", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PlantBackground
                )
            )
        },
        containerColor = PlantBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Empty state: Show guide when no favorites exist
            if (favoritePlants.isEmpty()) {
                EmptyFavoritesView()
            } else {
                // Grid list of favorite plants
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = favoritePlants,
                        key = { it.id }  // Stable key for performance
                    ) { plant ->
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

/**
 * Empty Favorites View
 * Responsibility: Shows a friendly UI when the user hasn't favorited any plants yet.
 */
@Composable
fun EmptyFavoritesView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Heart icon (low opacity)
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = PlantGreenPrimary.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your Garden is Empty",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PlantGreenDark
        )

        Text(
            text = "Look for plants you like in the encyclopedia and click the heart to save them!",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
