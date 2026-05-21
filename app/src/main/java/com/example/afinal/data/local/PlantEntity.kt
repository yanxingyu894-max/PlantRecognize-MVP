package com.example.afinal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 植物信息实体类，与数据库中的「plants」表一一映射
 * 包含植物基础信息、生长习性、环境需求、本地扩展字段
 * 设计思路：
 * 1. 基础字段与后端API字段对齐，便于网络数据解析后直接入库
 * 2. 本地扩展字段（如isFavorite、season）满足前端业务逻辑
 * 3. 可选字段使用可空类型（Float?/Int?），避免默认值干扰数据准确性
 */
@Entity(tableName = "plants") // 标记为Room实体，对应数据表名：plants
data class PlantEntity(
    // 主键：植物唯一标识符（与后端API的id一致），保证数据唯一性
    @PrimaryKey
    val id: String,

    // --- 基础字段（与后端API对齐） ---
    // 植物别名缩写（URL友好的字符串，如"rose"）
    val slug: String = "",

    // 植物通用名（如"玫瑰"），用于前端展示
    val commonName: String = "",

    // 植物学名（拉丁名，如"Rosa chinensis"），用于专业展示
    val scientificName: String = "",

    // 植物科（如"蔷薇科"）
    val family: String = "",

    // 植物属（如"蔷薇属"）
    val genus: String = "",

    // 植物分类（如"观赏植物/药用植物"）
    val category: String = "",

    // 植物图片URL，用于加载展示图片
    val imageUrl: String = "",

    // 植物简要描述
    val desc: String = "",

    /**
     * 详情加载标记：标识是否已从后端API获取完整的main_species数据
     * 业务逻辑：
     * - false：仅存储基础信息，需调用/api/v1/plants/{slug}补全详情
     * - true：已加载完整数据，无需重复请求
     * 默认值：false（初始入库仅存基础信息）
     */
    val isDetailLoaded: Boolean = false,

    // --- 形态特征（植物外观相关） ---
    // 花色（如"红色/粉色/白色"）
    val flowerColor: String = "",

    // 叶色（如"绿色/紫红色"）
    val foliageColor: String = "",

    // 叶质（如"革质/纸质"）
    val foliageTexture: String = "",

    // 果色（如"黄色/红色"）
    val fruitColor: String = "",

    // 果形（如"球形/椭圆形"）
    val fruitShape: String = "",

    // 植物生命周期（如"一年生/多年生"）
    val duration: String = "",

    // --- 生长参数（种植相关） ---
    // 生长习性（如"直立/匍匐/攀援"）
    val growthHabit: String = "",

    // 生长速度（如"快速/中速/慢速"）
    val growthRate: String = "",

    // 平均高度（单位：米），可空（部分植物无数据）
    val averageHeight: Float? = null,

    // 最大高度（单位：米），可空
    val maximumHeight: Float? = null,

    // 毒性（如"无毒/轻微有毒/剧毒"）
    val toxicity: String = "",

    // 是否可食用
    val edible: Boolean = false,

    // 可食用部位（如"果实/叶片/根"）
    val ediblePart: String = "",

    // --- 环境需求（种植条件） ---
    // 光照需求（数值型，如1=低光，2=中光，3=高光），可空
    val light: Int? = null,

    // 土壤pH最小值（如5.0），可空
    val phMinimum: Float? = null,

    // 土壤pH最大值（如7.5），可空
    val phMaximum: Float? = null,

    // 最低耐受温度（单位：℃），可空
    val minimumTemperature: Int? = null,

    // 最高耐受温度（单位：℃），可空
    val maximumTemperature: Int? = null,

    // 土壤湿度（如"干燥/湿润/积水"）
    val soilHumidity: String = "",

    // 土壤质地（如"砂质/黏质/壤土"）
    val soilTexture: String = "",

    // 土壤养分（如"低/中/高"）
    val soilNutrients: String = "",

    // 开花月份（如"3-5月"）
    val bloomMonths: String = "",

    // 结果月份（如"6-8月"）
    val fruitMonths: String = "",

    // --- 本地额外字段（前端业务专用，后端无对应字段） ---
    // 季节（如"春季/夏季/秋季/冬季"），用于按季节筛选植物
    val season: String = "",

    // 植物名称（前端自定义展示名）
    val name: String = "",

    // 植物别名（多个别名用逗号分隔）
    val alias: String = "",

    // 植物特征（简短描述）
    val feature: String = "",

    // 植物习性（自定义描述）
    val habit: String = "",

    // 养护要点（自定义描述）
    val care: String = "",


    /**
     * 最后更新时间戳（毫秒）：用于排序（新数据置顶）、判断数据新鲜度
     * 默认值：当前系统时间，入库/更新时自动刷新
     */
    val lastUpdate: Long = System.currentTimeMillis(),

    /**
     * 收藏状态：标记用户是否收藏该植物
     * 业务场景：收藏列表展示、收藏按钮状态切换
     * 默认值：false（初始未收藏）
     */
    val isFavorite: Boolean = false
)