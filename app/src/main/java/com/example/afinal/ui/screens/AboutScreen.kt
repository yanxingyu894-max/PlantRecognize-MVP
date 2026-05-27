@file:OptIn(ExperimentalMaterial3Api::class) // 启用Material3的实验性API（TopAppBar）

package com.example.afinal.ui.screens

// 导入Compose核心UI库、图标、主题等
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
import com.example.afinal.ui.theme.PlantGreenDark // 自定义深植物绿
import com.example.afinal.ui.theme.PlantGreenPrimary // 自定义主植物绿

/**
 * 关于APP页面组件
 * 外行人理解：这是APP的"关于"页面，显示APP介绍、功能、技术支持、联系方式等
 * @param onBack 点击返回按钮/左上角返回图标的回调
 */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    // 根容器：Surface（画布）
    Surface(
        modifier = Modifier
            .fillMaxSize() // 占满屏幕
            .background(Color(0xFFF5F5F5)) // 浅灰色背景
    ) {
        // 垂直列布局（可滚动）
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // 内容超出时垂直滚动
        ) {
            // 顶部导航栏（TopAppBar）
            TopAppBar(
                title = { Text("About App", color = Color.White) }, // 标题"关于APP"，白色文字
                navigationIcon = { // 左上角返回图标
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back", // 辅助描述"返回"
                            tint = Color.White // 白色图标
                        )
                    }
                },
                // 导航栏背景色：植物绿
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PlantGreenPrimary
                )
            )

            // 内容区（居中）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp), // 内边距24dp
                horizontalAlignment = Alignment.CenterHorizontally // 子组件水平居中
            ) {
                // 顶部空白
                Spacer(modifier = Modifier.height(24.dp))

                // APP图标容器
                Surface(
                    modifier = Modifier
                        .size(80.dp), // 大小80dp
                    color = PlantGreenPrimary.copy(alpha = 0.15f), // 浅植物绿背景
                    shape = RoundedCornerShape(20.dp) // 圆角20dp
                ) {
                    Icon(
                        Icons.Default.Star, // 星星图标（作为APP默认图标）
                        contentDescription = "App Icon", // 辅助描述"APP图标"
                        tint = PlantGreenPrimary, // 植物绿
                        modifier = Modifier
                            .size(80.dp)
                            .padding(16.dp) // 内边距16dp
                    )
                }

                // 图标和APP名称之间的空白
                Spacer(modifier = Modifier.height(20.dp))

                // APP名称
                Text(
                    "Plant Assistant",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = PlantGreenDark
                )

                // APP版本号
                Text(
                    "v1.0.0",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // 版本号和介绍卡片之间的空白
                Spacer(modifier = Modifier.height(32.dp))

                // 介绍卡片：APP简介
                InfoCard(
                    title = "Introduction",
                    description = "Plant Assistant is a smart recognition app designed for plant lovers. Using advanced AI technology, you can easily identify various plant species and get detailed information and care guides. Whether you're a gardening beginner or an expert, we're here to provide professional help."
                )

                // 卡片之间的空白
                Spacer(modifier = Modifier.height(16.dp))

                // 核心功能卡片
                InfoCard(
                    title = "Core Features",
                    description = "✓ AI Recognition - Identify plants via smart AI\n✓ Detailed Info - Full biological information\n✓ Category Exploration - Browse by season and family\n✓ My Garden - Save your favorite plants\n✓ Care Guide - Get professional care tips"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 技术支持卡片
                InfoCard(
                    title = "Tech Support",
                    description = "This app is built with Kotlin + Jetpack Compose and integrates AI engines from Trefle and PlantNet. All data comes from authoritative plant databases to ensure accuracy and professionalism."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 承诺卡片
                InfoCard(
                    title = "Our Promise",
                    description = "We are committed to providing the most accurate and comprehensive plant information service to enthusiasts worldwide. We continuously improve our features for a better user experience."
                )

                // 承诺卡片和联系方式之间的空白
                Spacer(modifier = Modifier.height(32.dp))

                // 分割线
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // 联系方式标题
                Text(
                    "Contact Us",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PlantGreenDark
                )

                // 标题和邮箱项之间的空白
                Spacer(modifier = Modifier.height(12.dp))

                // 邮箱联系方式项
                ContactItem(
                    icon = Icons.Default.Email, // 邮箱图标
                    label = "Email", // 标签"邮箱"
                    value = "support@plantrecognizexxx.app" // 邮箱地址
                )

                // 邮箱和版权信息之间的空白
                Spacer(modifier = Modifier.height(12.dp))

                // 版权信息
                Text(
                    "© 2026 Plant Assistant. All rights reserved.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
                )

                // 返回按钮
                Button(
                    onClick = onBack, // 点击返回
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp), // 占满宽度，高度50dp
                    shape = RoundedCornerShape(12.dp), // 圆角
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PlantGreenPrimary // 植物绿背景
                    )
                ) {
                    Text("Back", fontSize = 16.sp, fontWeight = FontWeight.Bold) // "返回"文字
                }
            }
        }
    }
}

/**
 * 通用信息卡片（显示标题+描述）
 * @param title 卡片标题
 * @param description 卡片描述文字
 */
@Composable
fun InfoCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(), // 占满宽度
        shape = RoundedCornerShape(16.dp), // 圆角16dp
        colors = CardDefaults.cardColors(containerColor = Color.White), // 白色背景
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // 阴影（立体感）
    ) {
        Column(modifier = Modifier.padding(16.dp)) { // 内边距16dp
            // 卡片标题
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PlantGreenDark
            )
            // 标题和描述之间的空白
            Spacer(modifier = Modifier.height(8.dp))
            // 卡片描述
            Text(
                description,
                fontSize = 13.sp,
                lineHeight = 20.sp, // 行高20sp（提高可读性）
                color = Color.DarkGray
            )
        }
    }
}

/**
 * 联系方式项（图标+标签+值）
 * @param icon 左侧图标
 * @param label 标签（比如"邮箱"）
 * @param value 值（比如邮箱地址）
 */
@Composable
fun ContactItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp)) // 白色背景，圆角
            .padding(12.dp), // 内边距12dp
        verticalAlignment = Alignment.CenterVertically // 垂直居中
    ) {
        // 左侧图标
        Icon(
            icon,
            contentDescription = label, // 辅助描述（标签）
            tint = PlantGreenPrimary, // 植物绿
            modifier = Modifier.size(24.dp) // 大小24dp
        )
        // 图标和文字之间的空白
        Spacer(modifier = Modifier.width(12.dp))
        // 右侧文字（标签+值）
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray) // 标签（灰色小字）
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black) // 值（黑色粗字）
        }
    }
}