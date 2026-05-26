package com.example.afinal.ui.components

import androidx.compose.foundation.clickable
import com.example.afinal.ui.utils.clickableOnce
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.afinal.data.model.Plant
import com.example.afinal.ui.theme.PlantGreenPrimary

/**
 * Plant list item card component
 * Responsibility: Uniformly display plant images, names, families, and favorite buttons. Supports clicking to enter details and toggling favorites.
 * Design: Reused in the plant encyclopedia list, favorites list, and category waterfall flow to ensure UI consistency.
 */
@Composable
fun PlantCard(
    plant: Plant,                     // Plant data model to display
    onClick: () -> Unit,              // Overall card click callback (navigate to details)
    onFavoriteClick: () -> Unit       // Favorite button click callback (toggle favorite status)
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickableOnce { onClick() },  // Prevents double-click
        shape = RoundedCornerShape(16.dp), // Consistent rounded corners
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Slight elevation for depth
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // ------------------------------
            // Section 1: Plant Image (Top rounded corner clipping)
            // Function: Displays the main image of the plant; uses system icons for loading/failure
            // ------------------------------
            AsyncImage(
                model = plant.imageUrl,
                contentDescription = plant.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop,       // Crop to fill
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery), // Placeholder
                error = painterResource(id = android.R.drawable.ic_dialog_alert)        // Error placeholder
            )

            // ------------------------------
            // Section 2: Text Information Area
            // Includes: Name, Favorite button, Family
            // ------------------------------
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Plant Name
                    Text(
                        text = plant.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis  // Ellipsis for long names
                    )

                    // Favorite Button: Toggles between filled and outlined icons
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (plant.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (plant.isFavorite) MaterialTheme.colorScheme.primary else PlantGreenPrimary
                        )
                    }
                }

                // Plant Family (Secondary info, smaller font, theme color)
                Text(
                    text = plant.family,
                    fontSize = 12.sp,
                    color = PlantGreenPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
