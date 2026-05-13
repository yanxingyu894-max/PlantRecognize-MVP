package com.example.afinal.data.model


/**
 * 网络层数据类 —— 对应API返回的JSON格式
 */
data class PlantDto(
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