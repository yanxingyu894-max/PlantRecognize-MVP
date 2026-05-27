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
 * 植物卡片组件（列表项专用）
 * 核心职责：统一展示植物的图片、名称、科属、收藏状态，支持点击进入详情、切换收藏状态
 * 复用场景：植物百科列表、我的收藏列表、分类瀑布流等，保证全App卡片UI风格一致
 *
 * @param plant 要展示的植物数据模型（包含名称、图片、科属、收藏状态等所有必要信息）
 * @param onClick 卡片整体点击的回调（通常用于跳转到植物详情页）
 * @param onFavoriteClick 收藏按钮点击的回调（用于切换植物的收藏状态）
 */
@Composable
fun PlantCard(
    plant: Plant,                     // 植物数据模型
    onClick: () -> Unit,              // 卡片点击事件（跳转详情）
    onFavoriteClick: () -> Unit       // 收藏按钮点击事件（切换收藏）
) {
    // Material3卡片组件：自带阴影、圆角、背景色的容器
    Card(
        modifier = Modifier
            .padding(8.dp)              // 卡片外间距（与其他卡片/元素保持距离）
            .fillMaxWidth()             // 宽度占满父容器
            .clickableOnce { onClick() },  // 防重复点击：替代普通clickable，避免快速双击跳转两次
        shape = RoundedCornerShape(16.dp), // 卡片圆角（16dp符合Material3设计规范）
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // 卡片阴影高度（营造层次感）
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // 卡片背景色（使用主题的表面色：纯白）
    ) {
        // 卡片内部垂直布局：图片 + 文字信息
        Column {
            // ------------------------------
            // 第一部分：植物图片区域（顶部圆角裁剪）
            // 功能：展示植物主图；加载中显示系统图库图标；加载失败显示警告图标
            // ------------------------------
            AsyncImage(
                model = plant.imageUrl,          // 图片网络地址/本地路径
                contentDescription = plant.name, // 图片描述（无障碍服务/屏幕阅读器使用）
                modifier = Modifier
                    .fillMaxWidth()              // 宽度占满卡片
                    .height(140.dp)              // 固定高度（保证所有卡片图片大小一致）
                    // 裁剪：只保留顶部圆角（与卡片圆角匹配，底部直角衔接文字区域）
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop,       // 图片缩放模式：裁剪填充（不拉伸、不挤压）
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery), // 加载中占位图（系统图库图标）
                error = painterResource(id = android.R.drawable.ic_dialog_alert)        // 加载失败占位图（系统警告图标）
            )

            // ------------------------------
            // 第二部分：文字信息区域（包含名称、收藏按钮、科属）
            // ------------------------------
            Column(modifier = Modifier.padding(12.dp)) { // 内间距：文字与卡片边缘保持距离
                // 水平布局：植物名称（左） + 收藏按钮（右）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, // 两端对齐
                    verticalAlignment = Alignment.CenterVertically    // 垂直居中
                ) {
                    // 植物名称文本
                    Text(
                        text = plant.name,                          // 显示植物名称
                        fontSize = 18.sp,                            // 字体大小：18像素（突出名称）
                        fontWeight = FontWeight.Bold,                // 字体加粗（视觉重点）
                        color = MaterialTheme.colorScheme.onSurface, // 文字颜色：主题表面色的对比色（保证可读性）
                        modifier = Modifier.weight(1f),              // 占满左侧剩余空间（挤压收藏按钮到最右）
                        maxLines = 1,                                // 最多显示1行（避免名称过长）
                        overflow = TextOverflow.Ellipsis             // 超出部分显示省略号（...）
                    )

                    // 收藏按钮：可点击的图标按钮
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            // 图标切换：收藏状态显示实心爱心，未收藏显示空心爱心
                            imageVector = if (plant.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",        // 图标描述（无障碍服务使用）
                            // 颜色切换：收藏状态用主题主色，未收藏用核心绿
                            tint = if (plant.isFavorite) MaterialTheme.colorScheme.primary else PlantGreenPrimary
                        )
                    }
                }

                // 植物科属文本（次要信息）
                Text(
                    text = plant.family,                            // 显示植物所属科属（如蔷薇科）
                    fontSize = 12.sp,                                // 字体大小：12像素（次要信息）
                    color = PlantGreenPrimary,                       // 文字颜色：核心绿（贴合主题）
                    modifier = Modifier.padding(top = 4.dp)          // 与上方名称保持4dp间距
                )
            }
        }
    }
}