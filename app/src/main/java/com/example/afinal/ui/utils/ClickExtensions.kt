package com.example.afinal.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Modifier扩展函数：防止视图被快速重复点击（防抖/节流）
 * 解决问题：用户快速连续点击按钮时，避免多次触发点击事件（如重复跳转、重复提交）
 * @param interval 点击间隔阈值（毫秒），默认500ms（即0.5秒内只能点击一次）
 * @param onClick 真正要执行的点击逻辑
 * 使用方式：替代普通的clickable，如 Modifier.clickableOnce { 点击逻辑 }
 */
fun Modifier.clickableOnce(
    interval: Long = 500L,
    onClick: () -> Unit
): Modifier = composed {
    // 记住上次点击的时间戳（remember保证重组时不重置，mutableLongStateOf是Compose的长整型状态）
    var lastClickTime by remember { mutableLongStateOf(0L) }

    // 基于原生clickable封装，添加时间判断逻辑
    this.clickable {
        // 获取当前系统时间（毫秒）
        val currentTime = System.currentTimeMillis()
        // 判断：当前时间 - 上次点击时间 > 间隔阈值 → 才执行点击逻辑
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime // 更新上次点击时间
            onClick() // 执行用户传入的点击逻辑
        }
    }
}