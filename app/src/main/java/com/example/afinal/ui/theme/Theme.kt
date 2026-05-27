package com.example.afinal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 定义植物助手App的专属配色方案（浅色模式）
// lightColorScheme是Material3提供的浅色主题配色模板，参数对应不同UI元素的颜色
private val PlantColorScheme = lightColorScheme(
    primary = PlantGreenPrimary,      // 主色调：用于按钮、导航栏等核心交互元素
    onPrimary = PlantSurface,         // 主色调背景上的文字/图标颜色
    secondary = PlantGreenSecondary,  // 次要色调：用于次要按钮、强调标签等
    background = PlantBackground,     // App整体背景色
    surface = PlantSurface,           // 卡片、弹窗等表面容器的背景色
    onSurface = PlantGreenDark        // 卡片/表面容器上的文字/图标颜色
)

/**
 * 植物助手App的全局主题包装器
 * 作用：为整个App统一设置配色方案、文字样式、形状等主题属性
 * @param content 包裹的Compose内容（整个App的UI布局）
 * 使用方式：在App入口处用该函数包裹所有界面内容，即可应用统一主题
 */
@Composable
fun PlantAssistantTheme(content: @Composable () -> Unit) {
    // MaterialTheme是Compose Material3的核心主题组件，提供全局样式上下文
    MaterialTheme(
        colorScheme = PlantColorScheme, // 应用自定义的植物主题配色
        content = content               // 展示用户传入的UI内容
    )
}