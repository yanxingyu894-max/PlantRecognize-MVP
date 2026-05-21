package com.example.afinal.data.model

/**
 * 应用层数据类 —— 业务逻辑和UI层使用
 * 作用：作为页面展示、业务处理的核心数据模型
 * 包含植物基础信息、形态特征、生长参数、环境需求、本地收藏状态等全量字段
 */
data class Plant(
    // 植物唯一标识，与数据库、API保持一致
    val id: String,

    // 植物名称，用于页面主展示
    val name: String,

    // 植物别名，多个用逗号分隔
    val alias: String,

    // 植物所属科（如蔷薇科）
    val family: String,

    // 植物所属属（如蔷薇属），默认空字符串
    val genus: String = "",

    // 植物分类（观赏/药用/多肉等）
    val category: String,

    // 植物图片链接，用于图片加载展示
    val imageUrl: String,

    // 植物简要描述信息
    val desc: String,

    // 植物特征简介
    val feature: String,

    // 植物生长习性
    val habit: String,

    // 植物养护要点
    val care: String,

    // 适配季节（春/夏/秋/冬），默认空字符串
    val season: String = "",

    // 本地收藏状态，控制收藏图标与收藏列表
    val isFavorite: Boolean,

    // 识别置信度，拍照识别时使用，可空
    val confidence: Double? = null,

    // ==================== 详细形态属性 ====================
    // 花朵颜色
    val flowerColor: String = "",

    // 叶片颜色
    val foliageColor: String = "",

    // 叶片质地
    val foliageTexture: String = "",

    // 果实颜色
    val fruitColor: String = "",

    // 果实形状
    val fruitShape: String = "",

    // 生命周期（一年生/多年生）
    val duration: String = "",

    // 生长习性（直立/匍匐/攀援）
    val growthHabit: String = "",

    // 生长速度（快速/中速/慢速）
    val growthRate: String = "",

    // 平均高度，单位米，可空
    val averageHeight: Float? = null,

    // 最大高度，单位米，可空
    val maximumHeight: Float? = null,

    // 毒性说明（无毒/轻微有毒/剧毒）
    val toxicity: String = "",

    // 是否可食用
    val edible: Boolean = false,

    // 可食用部位（果实/叶片/根）
    val ediblePart: String = "",

    // ==================== 环境生长参数 ====================
    // 光照需求等级（1低/2中/3高），可空
    val light: Int? = null,

    // 土壤最低pH值，可空
    val phMinimum: Float? = null,

    // 土壤最高pH值，可空
    val phMaximum: Float? = null,

    // 最低耐受温度（℃），可空
    val minTemp: Int? = null,

    // 最高耐受温度（℃），可空
    val maxTemp: Int? = null,

    // 土壤湿度要求
    val soilHumidity: String = "",

    // 土壤质地（砂质/黏质/壤土）
    val soilTexture: String = "",

    // 土壤养分需求
    val soilNutrients: String = "",

    // 开花月份，如3-5月
    val bloomMonths: String = "",

    // 结果月份，如6-8月
    val fruitMonths: String = ""
)