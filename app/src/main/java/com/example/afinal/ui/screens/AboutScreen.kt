@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.afinal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.ui.theme.PlantGreenDark
import com.example.afinal.ui.theme.PlantGreenPrimary

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            TopAppBar(
                title = { Text("About App", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PlantGreenPrimary
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // App Logo and Title
                Surface(
                    modifier = Modifier
                        .size(80.dp),
                    color = PlantGreenPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "App Icon",
                        tint = PlantGreenPrimary,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Plant Assistant",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = PlantGreenDark
                )

                Text(
                    "v1.0.0",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Info Cards
                InfoCard(
                    title = "Introduction",
                    description = "Plant Assistant is a smart recognition app designed for plant lovers. Using advanced AI technology, you can easily identify various plant species and get detailed information and care guides. Whether you're a gardening beginner or an expert, we're here to provide professional help."
                )

                Spacer(modifier = Modifier.height(16.dp))

                InfoCard(
                    title = "Core Features",
                    description = "✓ AI Recognition - Identify plants via smart AI\n✓ Detailed Info - Full biological information\n✓ Category Exploration - Browse by season and family\n✓ My Garden - Save your favorite plants\n✓ Care Guide - Get professional care tips"
                )

                Spacer(modifier = Modifier.height(16.dp))

                InfoCard(
                    title = "Tech Support",
                    description = "This app is built with Kotlin + Jetpack Compose and integrates AI engines from Trefle and PlantNet. All data comes from authoritative plant databases to ensure accuracy and professionalism."
                )

                Spacer(modifier = Modifier.height(16.dp))

                InfoCard(
                    title = "Our Promise",
                    description = "We are committed to providing the most accurate and comprehensive plant information service to enthusiasts worldwide. We continuously improve our features for a better user experience."
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Contact Info
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text(
                    "Contact Us",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PlantGreenDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                ContactItem(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = "support@plantrecognizexxx.app"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "© 2026 Plant Assistant. All rights reserved.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
                )

                // Back Button
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PlantGreenPrimary
                    )
                ) {
                    Text("Back", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PlantGreenDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                description,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun ContactItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = PlantGreenPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
    }
}
