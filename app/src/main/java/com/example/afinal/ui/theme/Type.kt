package com.example.afinal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * App全局文字样式配置（Material3 Typography体系）
 * Typography包含多个预设文字样式，对应不同UI场景（标题、正文、标签等）
 * 可根据需求扩展更多样式，所有Compose文本组件可直接引用该配置
 */
val Typography = Typography(
    // 正文大样式：用于App主要文本内容（如详情页说明、列表描述等）
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,  // 使用系统默认字体（保证跨设备兼容性）
        fontWeight = FontWeight.Normal,   // 字体粗细：常规（400）
        fontSize = 16.sp,                 // 字体大小：16像素（移动端正文标准尺寸）
        lineHeight = 24.sp,               // 行高：24像素（提升文字可读性）
        letterSpacing = 0.5.sp            // 字符间距：0.5像素（优化视觉效果）
    )
    /* 可扩展的其他预设样式示例（默认被注释，可按需启用）
    titleLarge = TextStyle(              // 大标题：页面顶部主标题
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(              // 小标签：按钮文字、表单提示等
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)