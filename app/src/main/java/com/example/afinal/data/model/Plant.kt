package com.example.afinal.data.model

/**
 * 应用层数据类 —— 业务逻辑和UI层使用
 */
data class Plant(
    val id: String,
    val name: String,
    val alias: String,
    val family: String,
    val category: String,
    val imageUrl: String,
    val desc: String,
    val feature: String,
    val habit: String,
    val care: String
)