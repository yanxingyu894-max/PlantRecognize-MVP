package com.example.afinal.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room实体类 —— 对应数据库表 "plants"
 * @Entity 标记这是一个数据库表
 */
@Entity(tableName = "plants")
data class PlantEntity(

    @PrimaryKey
    val id: String,           // 主键，使用API返回的ID

    val name: String,         // 植物名称
    val alias: String,        // 别名
    val family: String,       // 科属
    val category: String,     // 分类：绿植/花卉/多肉
    val imageUrl: String,     // 图片链接
    val desc: String,         // 简介
    val feature: String,      // 形态特征
    val habit: String,        // 生长习性
    val care: String,         // 养护要点
    val lastUpdate: Long = System.currentTimeMillis(),  // 最后更新时间，默认当前时间
    val isFavorite: Boolean = false  // 是否收藏
)