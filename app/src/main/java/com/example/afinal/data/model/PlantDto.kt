package com.example.afinal.data.model

/**
 * 网络层植物基础数据类
 * 作用：仅解析网络API返回的"基础植物字段"，是网络数据和应用层数据的中间载体
 * 设计说明：
 * 1. 只保留核心基础字段，不包含本地扩展字段（比如收藏状态）和复杂详情字段
 * 2. 网络请求成功后，先映射为该类，再转换为应用层的Plant类供业务逻辑和UI使用
 * 3. 外行人理解：可以把此类看作"从服务器拿到的最基础植物信息模板"，只存核心信息
 */
data class PlantDto(
    // 植物的唯一标识ID（字符串型，兼容不同API的ID格式）
    val id: String,

    // 植物的主名称（通用名/俗名，比如"玫瑰"）
    val name: String,

    // 植物的别名（比如"月季"是玫瑰的别名，多个别名可用逗号分隔）
    val alias: String,

    // 植物所属的科（比如"蔷薇科"）
    val family: String,

    // 植物的分类（比科更细的分类，比如"蔷薇属玫瑰种"或"观赏花卉"）
    val category: String,

    // 植物图片的网络地址（可以直接在App/网页中加载显示）
    val imageUrl: String,

    // 植物的基础描述信息（比如"玫瑰是蔷薇科落叶灌木，原产中国"）
    val desc: String,

    // 植物的核心特征（比如"茎有刺、花瓣重瓣、气味芳香"）
    val feature: String,

    // 植物的生长习性（比如"喜阳光、耐寒、耐旱，适合疏松土壤"）
    val habit: String,

    // 植物的养护方法（比如"每周浇水1次，每月施肥1次，修剪残花"）
    val care: String
)