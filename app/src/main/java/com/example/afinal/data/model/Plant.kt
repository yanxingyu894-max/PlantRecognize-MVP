package com.example.afinal.data.model

/**
 * 应用层完整植物数据类
 * 作用：供业务逻辑层（比如ViewModel）和UI层（比如Activity/Fragment）使用的最终数据模型
 * 设计说明：
 * 1. 整合了所有数据源（PlantNet/Trefle/DeepSeek/本地）的字段，是"最全的植物信息模板"
 * 2. 包含基础信息、形态特征、生长参数、环境需求、本地状态（收藏）等所有维度
 * 3. 外行人理解：此类是App中展示、操作植物信息的"最终模板"，所有植物相关的UI和逻辑都用这个类
 */
data class Plant(
    // 植物唯一ID（贯穿整个App的核心标识）
    val id: String,
    // 植物主名称（俗名/通用名）
    val name: String,
    // 植物别名（多个别名用逗号分隔）
    val alias: String,
    // 植物通用名称（和name字段冗余但区分场景，比如name是"玫瑰"，commonName是"玫瑰花"）
    val commonName: String,
    // 植物科学名称（学名）
    val scientificName: String,
    // 植物所属科
    val family: String,
    // 植物所属属（默认空字符串，避免空指针）
    val genus: String = "",
    // 植物分类（比如"观赏花卉"、"药用植物"）
    val category: String,
    // 植物图片地址
    val imageUrl: String,
    // 植物基础描述
    val desc: String,
    // 植物核心特征
    val feature: String,
    // 植物生长习性
    val habit: String,
    // 植物养护方法
    val care: String,
    // 植物生长季节（比如"春夏季生长，秋季结果"，默认空）
    val season: String = "",
    // 本地收藏状态（true=已收藏，false=未收藏，用于App的收藏功能）
    val isFavorite: Boolean,
    // 识别置信度（来自PlantNet API，0-1之间，null代表非识别结果）
    val confidence: Double? = null,

    // ==================== 形态特征相关字段 ====================
    // 花色（比如"红色、粉色"）
    val flowerColor: String = "",
    // 花朵是否显眼（true=显眼，false=不显眼，null=未知）
    val flowerConspicuous: Boolean? = null,

    // 叶色（比如"绿色、紫红色"）
    val foliageColor: String = "",
    // 叶片质地（比如"革质、纸质"）
    val foliageTexture: String = "",
    // 保叶性（是否常绿，true=常绿，false=落叶，null=未知）
    val leafRetention: Boolean? = null,

    // 果实颜色（比如"黑色、红色"）
    val fruitColor: String = "",
    // 果实形状（比如"球形、椭圆形"）
    val fruitShape: String = "",

    // 生长周期（比如"多年生、一年生"）
    val duration: String = "",
    // 木质类型（比如"硬木、软木、藤本"）
    val ligneousType: String = "",
    // 生长习性（比如"乔木、灌木、草本"）
    val growthHabit: String = "",
    // 生长速度（比如"快速、中等、缓慢"）
    val growthRate: String = "",
    // 植株扩展范围（冠幅，单位：厘米，null=未知）
    val spread: Float? = null,

    // 平均高度（单位：厘米，null=未知）
    val averageHeight: Float? = null,
    // 最大高度（单位：厘米，null=未知）
    val maximumHeight: Float? = null,

    // 毒性说明（比如"无毒、轻微有毒、剧毒"）
    val toxicity: String = "",
    // 是否可食用（默认false，避免空指针）
    val edible: Boolean = false,
    // 可食用部位（比如"果实、嫩叶"）
    val ediblePart: String = "",

    // 原生分布地（比如"中国华北、西北"）
    val nativeDistribution: String = "",
    // 引入分布地（比如"欧洲、美洲"）
    val introducedDistribution: String = "",

    // ==================== 环境与生长参数相关字段 ====================
    // 光照需求（数值型，代表光照强度需求，null=未知）
    val light: Int? = null,
    // 土壤pH最小值（比如4.5，null=未知）
    val phMinimum: Float? = null,
    // 土壤pH最大值（比如7.5，null=未知）
    val phMaximum: Float? = null,
    // 最低生长温度（摄氏度，null=未知）
    val minTemp: Int? = null,
    // 最高生长温度（摄氏度，null=未知）
    val maxTemp: Int? = null,
    // 土壤湿度要求（数值型，代表湿度需求等级，null=未知）
    val soilHumidity: Int? = null,
    // 土壤质地要求（数值型，代表质地适配等级，null=未知）
    val soilTexture: Int? = null,
    // 土壤养分要求（数值型，代表养分需求等级，null=未知）
    val soilNutrients: Int? = null,
    // 土壤盐度要求（数值型，代表盐度耐受等级，null=未知）
    val soilSalinity: Int? = null,

    // 生长月份（比如"3-5月"）
    val growthMonths: String = "",
    // 开花月份（比如"4-6月"）
    val bloomMonths: String = "",
    // 结果月份（比如"9-10月"）
    val fruitMonths: String = ""
)