package com.example.afinal.ui.theme

import androidx.compose.ui.graphics.Color

// 植物助手App的森林系配色体系（所有颜色均为十六进制RGB值，对应UI视觉规范）
// 配色设计思路：贴合植物/自然主题，以绿色系为主，兼顾护眼和视觉层次

val PlantGreenDark = Color(0xFF1B4332)      // 深林绿：用于重要标题、关键文字（视觉权重最高）
val PlantGreenPrimary = Color(0xFF2D6A4F)   // 核心绿：主按钮、导航栏、重点交互元素（品牌核心色）
val PlantGreenSecondary = Color(0xFF52B788) // 柔和绿：次要按钮、辅助标签、分割线等（视觉权重中等）
val PlantGreenLight = Color(0xFF95D5B2)     // 浅绿：细分标签、提示文字、背景点缀（视觉权重最低）
val PlantBackground = Color(0xFFF1F8E9)     // 薄荷白：App全屏背景色，浅绿调护眼，贴合自然主题
val PlantSurface = Color(0xFFFFFFFF)        // 纯白：卡片、列表项等容器背景，突出内容
val PlantFavorite = Color(0xFFD62828)       // 红色：收藏按钮选中态，高对比度吸引注意力