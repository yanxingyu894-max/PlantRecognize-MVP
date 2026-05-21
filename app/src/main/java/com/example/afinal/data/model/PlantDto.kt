package com.example.afinal.data.model

/**
 * 网络层数据类 —— 对应API返回的JSON格式
 * 作用：仅解析网络返回的基础植物字段，不包含本地扩展与详情字段
 * 用于网络请求成功后初步数据映射
 */
data class PlantDto(
    // 植物唯一ID
    val id: String,

    // 植物名称
    val name: String,

    // 植物别名
    val alias: String,

    // 植物科
    val family: String,

    // 植物分类
    val category: String,

    // 图片地址
    val imageUrl: String,

    // 描述信息
    val desc: String,

    // 特征
    val feature: String,

    // 习性
    val habit: String,

    // 养护方法
    val care: String
)