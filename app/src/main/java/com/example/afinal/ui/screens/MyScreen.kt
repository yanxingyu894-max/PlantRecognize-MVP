package com.example.afinal.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.afinal.ui.viewmodel.PlantViewModel

@Composable
fun MyScreen(
    viewModel: PlantViewModel,
    onNavigateToHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onAbout: () -> Unit,
    onBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val favorites by viewModel.favoritePlants.collectAsState()
    val loggedInUserId by viewModel.loggedInUserId.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already here */ },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PlantGreenPrimary,
                        selectedTextColor = PlantGreenPrimary,
                        indicatorColor = PlantGreenPrimary.copy(alpha = 0.1f)
                    )
                )
            }
        },
        containerColor = PlantBackground
    ) { padding ->
        if (isLandscape) {
            MyLandscapeContent(
                padding, favorites, loggedInUserId, onLogin, onRegister, 
                onLogout = { viewModel.logout() }, 
                onAbout = onAbout
            )
        } else {
            MyPortraitContent(
                padding, favorites, loggedInUserId, onLogin, onRegister, 
                onLogout = { viewModel.logout() }, 
                onAbout = onAbout
            )
        }
    }
}

@Composable
fun MyPortraitContent(
    padding: PaddingValues,
    favorites: List<Plant>,
    username: String?,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
    onAbout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        ProfileHeader(username)

        Spacer(modifier = Modifier.height(32.dp))

        // Horizontal scrollable collection gallery
        if (favorites.isNotEmpty()) {
            FavoritesSection(favorites)
            Spacer(modifier = Modifier.height(24.dp))
        }

        MenuCard(username, onLogin, onRegister, onLogout, onAbout)
        
        Spacer(modifier = Modifier.weight(1f))
        
        VersionInfo()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MyLandscapeContent(
    padding: PaddingValues,
    favorites: List<Plant>,
    username: String?,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
    onAbout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(username)
        }
        
        Spacer(modifier = Modifier.width(32.dp))
        
        Column(modifier = Modifier.weight(1.5f)) {
            if (favorites.isNotEmpty()) {
                FavoritesSection(favorites)
                Spacer(modifier = Modifier.height(16.dp))
            }

            MenuCard(username, onLogin, onRegister, onLogout, onAbout)
            
            Spacer(modifier = Modifier.height(16.dp))
            VersionInfo()
        }
    }
}

@Composable
fun ProfileHeader(username: String? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = PlantGreenPrimary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = PlantGreenPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = username ?: "Plant Explorer",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PlantGreenDark
        )
        
        Text(
            text = if (username != null) "Welcome back to nature" else "Discover the secrets of nature",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun FavoritesSection(favorites: List<Plant>) {
    Column {
        Text(
            text = "My Collection",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = PlantGreenDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            favorites.forEach { plant ->
                FavoriteThumbnail(plant)
            }
        }
    }
}

@Composable
fun FavoriteThumbnail(plant: Plant) {
    Card(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        AsyncImage(
            model = plant.imageUrl,
            contentDescription = plant.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
            error = painterResource(id = android.R.drawable.ic_dialog_alert)
        )
    }
}

@Composable
fun MenuCard(
    username: String?,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
    onAbout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (username == null) {
                MyMenuItem(
                    icon = Icons.Default.Login,
                    title = "Login",
                    onClick = onLogin
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PlantBackground, thickness = 1.dp)
                MyMenuItem(
                    icon = Icons.Default.AppRegistration,
                    title = "Register Now",
                    onClick = onRegister
                )
            } else {
                MyMenuItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = "Logout",
                    onClick = onLogout
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = PlantBackground, thickness = 1.dp)
            MyMenuItem(
                icon = Icons.Default.Info,
                title = "About App",
                onClick = onAbout
            )
        }
    }
}

@Composable
fun VersionInfo() {
    Text(
        text = "Version 1.0.0",
        fontSize = 12.sp,
        color = Color.LightGray,
        modifier = Modifier.fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
fun MyMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PlantGreenPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PlantGreenPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = PlantGreenDark,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
