package com.example.afinal.data.local

import androidx.room.Entity

/**
 * Room数据库实体类（Entity）
 * 作用：映射数据库中的 "plants" 表，每个字段对应表的一列，每个对象对应表的一行数据
 * 核心更新：通过 ownerId 实现多用户数据隔离，联合主键（slug + ownerId）确保同一植物在不同用户下可独立存储
 * 字段设计：覆盖植物的基础信息、形态特征、生长环境、本地扩展属性等，适配Trefle植物API数据
 */
@Entity(
    tableName = "plants", // 指定映射的数据库表名：plants
    primaryKeys = ["slug", "ownerId"] // 联合主键：slug（植物唯一标识）+ ownerId（用户ID），确保数据唯一性
)
data class PlantEntity(
    // 植物唯一字符标识（如 "chinese-rose"），作为联合主键之一
    val slug: String,

    /**
     * 数据所属用户的ID（联合主键之二）
     * 用途：区分不同用户的植物数据，实现多用户隔离
     * 特殊值："guest_user" 用于未登录的游客用户
     */
    val ownerId: String,

    // 植物的官方ID（备用标识，非主键）
    val id: String = "",
    // 植物通用名称（如 "月季花"）
    val commonName: String = "",
    // 植物学名（如 "Rosa chinensis"）
    val scientificName: String = "",
    // 植物所属科（如 "蔷薇科"）
    val family: String = "",
    // 植物所属属（如 "蔷薇属"）
    val genus: String = "",
    // 植物分类（如 "花卉"、"药用植物"）
    val category: String = "",
    // 植物图片网络地址（用于UI展示）
    val imageUrl: String = "",
    // 植物描述信息
    val desc: String = "",
    // 是否加载过详细数据（标记位，避免重复请求网络详情）
    val isDetailLoaded: Boolean = false,

    // --- 形态特征相关字段 ---
    // 花色（如 "红色、粉色"）
    val flowerColor: String = "",
    // 花朵是否显眼（true=显眼，false=不显眼，null=未知）
    val flowerConspicuous: Boolean? = null,

    // 叶片颜色（如 "深绿"）
    val foliageColor: String = "",
    // 叶片质地（如 "革质、纸质"）
    val foliageTexture: String = "",
    // 是否常绿（true=常绿，false=落叶，null=未知）
    val leafRetention: Boolean? = null,

    // 果实颜色（如 "红色"）
    val fruitColor: String = "",
    // 果实形状（如 "球形"）
    val fruitShape: String = "",

    // 生命周期（如 "一年生、多年生"）
    val duration: String = "",
    // 木质化类型（如 "木本、草本"）
    val ligneousType: String = "",
    // 生长习性（如 "直立、匍匐"）
    val growthHabit: String = "",
    // 生长速度（如 "快速、缓慢"）
    val growthRate: String = "",
    // 冠幅（植物横向扩展范围，单位：米）
    val spread: Float? = null,

    // 平均高度（单位：米）
    val averageHeight: Float? = null,
    // 最大高度（单位：米）
    val maximumHeight: Float? = null,

    // 毒性（如 "无毒、微毒、剧毒"）
    val toxicity: String = "",
    // 是否可食用（true=可食用，false=不可食用）
    val edible: Boolean = false,
    // 可食用部位（如 "果实、叶片"）
    val ediblePart: String = "",

    // 原生分布地（如 "中国、欧洲"）
    val nativeDistribution: String = "",
    // 引入分布地（非原生，人为引入的地区）
    val introducedDistribution: String = "",

    // --- 生长环境/参数相关字段 ---
    // 光照需求（数值型，如 1=低光照，5=强光照）
    val light: Int? = null,
    // 土壤pH最小值（适合生长的土壤酸碱度下限）
    val phMinimum: Float? = null,
    // 土壤pH最大值（适合生长的土壤酸碱度上限）
    val phMaximum: Float? = null,
    // 最低耐受温度（单位：摄氏度）
    val minimumTemperature: Int? = null,
    // 最高耐受温度（单位：摄氏度）
    val maximumTemperature: Int? = null,
    // 土壤湿度需求（数值型，如 1=干燥，5=湿润）
    val soilHumidity: Int? = null,
    // 土壤质地需求（数值型，如 1=砂土，5=黏土）
    val soilTexture: Int? = null,
    // 土壤养分需求（数值型，如 1=低养分，5=高养分）
    val soilNutrients: Int? = null,
    // 土壤盐度耐受度（数值型，如 1=低耐盐，5=高耐盐）
    val soilSalinity: Int? = null,

    // 生长月份（如 "3-10月"）
    val growthMonths: String = "",
    // 开花月份（如 "5-6月"）
    val bloomMonths: String = "",
    // 结果月份（如 "8-9月"）
    val fruitMonths: String = "",

    // --- 本地扩展字段（适配APP自定义功能） ---
    // 生长季节（如 "春季、夏季"）
    val season: String = "",
    // 植物名称（本地化显示用）
    val name: String = "",
    // 植物别名
    val alias: String = "",
    // 植物特色（如 "芳香、观花"）
    val feature: String = "",
    // 栽培习性
    val habit: String = "",
    // 养护要点
    val care: String = "",

    // 最后更新时间戳（毫秒），用于排序/判断数据新鲜度
    val lastUpdate: Long = System.currentTimeMillis(),
    // 是否收藏（true=已收藏，false=未收藏）
    val isFavorite: Boolean = false
)