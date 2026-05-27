package com.example.afinal.data.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * Trefle植物库列表API的响应数据模型
 * 作用：解析Trefle API返回的"植物列表"类请求结果（比如查询某科植物的列表）
 * Trefle说明：Trefle是一个开源的植物数据库API，提供全球植物的详细信息
 */
data class TrefleResponse(
    // 植物列表数据，每个元素是一个植物的基础信息
    val data: List<TreflePlantData>,
    // 分页元数据（比如总记录数，用于分页加载）
    val meta: TrefleMeta
)

/**
 * Trefle植物详情API的响应数据模型
 * 作用：解析Trefle API返回的"单株植物详情"类请求结果（比如根据植物ID查询详细信息）
 */
data class TrefleDetailResponse(
    // 单株植物的完整详细数据
    val data: TreflePlantData
)

/**
 * Trefle单株植物的基础数据
 * 包含植物的核心标识、名称、分类、图片等基础信息
 */
data class TreflePlantData(
    // 植物在Trefle数据库中的唯一ID（整数型）
    val id: Int,
    // 植物的URL友好别名（用于前端路由/链接，比如"pinus-sylvestris"）
    val slug: String?,
    // 植物的通用名称（俗名，比如"樟子松"，@SerializedName用于映射JSON中的下划线字段）
    @SerializedName("common_name") val commonName: String?,
    // 植物的科学名称（学名，比如"Pinus sylvestris"）
    @SerializedName("scientific_name") val scientificName: String,
    // 植物所属科的通用名称（比如"松科"）
    @SerializedName("family_common_name") val familyCommonName: String?,
    // 植物所属科的详细信息（JsonElement类型，可兼容任意JSON结构，因科信息可能包含多字段）
    val family: JsonElement?,
    // 植物所属属的详细信息（JsonElement类型，兼容任意JSON结构）
    val genus: JsonElement?,
    // 植物的图片地址（可直接用于加载图片）
    @SerializedName("image_url") val imageUrl: String?,
    // 该植物主要物种的详细信息（部分植物可能包含变种，此字段指向核心物种）
    @SerializedName("main_species") val mainSpecies: TrefleMainSpecies?
)

/**
 * 植物主要物种的详细信息
 * 包含植物的生长周期、可食用性、形态特征、生长环境等深度信息
 */
data class TrefleMainSpecies(
    // 主要物种的ID（可为空，部分数据可能无此字段）
    val id: Int?,
    // 主要物种的通用名称
    @SerializedName("common_name") val commonName: String?,
    // 主要物种的科学名称
    @SerializedName("scientific_name") val scientificName: String?,
    // 该物种的观测记录说明（比如分布范围、数量等）
    val observations: String?,
    // 生长周期类型（比如"多年生"、"一年生"，列表形式可包含多种）
    val duration: List<String>?,
    // 是否可食用（true=可食用，false=不可食用，null=未知）
    val edible: Boolean?,
    // 可食用部位列表（比如"果实"、"种子"、"嫩叶"）
    @SerializedName("edible_part") val ediblePart: List<String>?,
    // 植物规格参数（比如高度、生长习性等）
    val specifications: TrefleSpecifications?,
    // 生长环境参数（比如光照、温度、土壤要求等）
    val growth: TrefleGrowth?,
    // 花朵特征（颜色、是否显眼等）
    val flower: TrefleFlower?,
    // 叶片特征（颜色、质地等）
    val foliage: TrefleFoliage?,
    // 果实/种子特征（颜色、形状等）
    @SerializedName("fruit_or_seed") val fruitOrSeed: TrefleFruitOrSeed?,
    // 地理分布信息（原生地、引入地等）
    val distributions: TrefleDistributions?
)

/**
 * 植物地理分布信息
 * 区分原生分布地和引入分布地（比如熊猫原生在中国，引入到其他国家）
 */
data class TrefleDistributions(
    // 原生分布地列表（该植物自然生长的地区）
    val native: List<TrefleLocation>?,
    // 引入分布地列表（人为引入种植的地区）
    val introduced: List<TrefleLocation>?
)

/**
 * 地理位置数据
 * 代表一个地区/国家的名称（比如"中国"、"四川省"、"欧洲"）
 */
data class TrefleLocation(
    val name: String?
)

/**
 * 植物规格参数信息
 * 包含植物的高度、生长习性、毒性等物理特征
 */
data class TrefleSpecifications(
    // 平均高度（包含厘米/英尺两种单位）
    @SerializedName("average_height") val averageHeight: TrefleMeasurement?,
    // 最大高度（包含厘米/英尺两种单位）
    @SerializedName("maximum_height") val maximumHeight: TrefleMeasurement?,
    // 生长习性（比如"乔木"、"灌木"、"草本"）
    @SerializedName("growth_habit") val growthHabit: String?,
    // 生长速度（比如"快速"、"中等"、"缓慢"）
    @SerializedName("growth_rate") val growthRate: String?,
    // 毒性说明（比如"无毒"、"轻微有毒"、"剧毒"）
    val toxicity: String?,
    // 木质类型（比如"硬木"、"软木"、"藤本"）
    @SerializedName("ligneous_type") val ligneousType: String?
)

/**
 * 植物生长环境参数
 * 包含光照、酸碱度、温度、土壤等生长所需条件
 */
data class TrefleGrowth(
    // 光照需求（数值型，代表光照强度需求，范围/单位由Trefle定义）
    val light: Int?,
    // 土壤pH最小值（比如4.5，代表适合生长的最低酸碱度）
    @SerializedName("ph_minimum") val phMinimum: Float?,
    // 土壤pH最大值（比如7.5，代表适合生长的最高酸碱度）
    @SerializedName("ph_maximum") val phMaximum: Float?,
    // 最低生长温度（包含摄氏度/华氏度）
    @SerializedName("minimum_temperature") val minTemp: TrefleTemperature?,
    // 最高生长温度（包含摄氏度/华氏度）
    @SerializedName("maximum_temperature") val maxTemp: TrefleTemperature?,
    // 土壤湿度要求（JsonElement类型，兼容任意JSON结构）
    @SerializedName("soil_humidity") val soilHumidity: JsonElement?,
    // 土壤质地要求（比如"砂土"、"黏土"、"壤土"，JsonElement兼容多字段结构）
    @SerializedName("soil_texture") val soilTexture: JsonElement?,
    // 土壤养分要求（比如"高氮"、"低磷"，JsonElement兼容多字段结构）
    @SerializedName("soil_nutriments") val soilNutrients: JsonElement?,
    // 土壤盐度要求（比如"耐盐"、"不耐盐"，JsonElement兼容多字段结构）
    @SerializedName("soil_salinity") val soilSalinity: JsonElement?,
    // 生长月份列表（比如["3月","4月","5月"]，代表主要生长季）
    @SerializedName("growth_months") val growthMonths: List<String>?,
    // 开花月份列表（比如["4月","5月"]）
    @SerializedName("bloom_months") val bloomMonths: List<String>?,
    // 结果/结籽月份列表（比如["9月","10月"]）
    @SerializedName("fruit_months") val fruitMonths: List<String>?,
    // 植株扩展范围（比如冠幅，单位：厘米/英尺）
    val spread: TrefleMeasurement?
)

/**
 * 温度数据模型
 * 包含摄氏度和华氏度两种单位，适配不同地区的使用习惯
 */
data class TrefleTemperature(
    // 摄氏度（℃）
    @SerializedName("deg_c") val deg_c: Int?,
    // 华氏度（℉）
    @SerializedName("deg_f") val deg_f: Int?
)

/**
 * 尺寸测量数据模型
 * 包含厘米和英尺两种单位，用于描述植物高度、冠幅等尺寸
 */
data class TrefleMeasurement(
    // 厘米（cm）
    val cm: Float?,
    // 英尺（ft）
    val ft: Float?
)

/**
 * 花朵特征信息
 */
data class TrefleFlower(
    // 花色列表（比如["红色","粉色","白色"]）
    val color: List<String>?,
    // 花朵是否显眼（true=显眼/易观察，false=不显眼，比如小花/隐蔽花）
    val conspicuous: Boolean?
)

/**
 * 叶片特征信息
 */
data class TrefleFoliage(
    // 叶色列表（比如["绿色","黄绿色","紫红色"]）
    val color: List<String>?,
    // 叶片质地（比如"革质"、"纸质"、"肉质"）
    val texture: String?,
    // 保叶性（是否常绿，true=常绿，false=落叶）
    @SerializedName("leaf_retention") val leafRetention: Boolean?
)

/**
 * 果实/种子特征信息
 */
data class TrefleFruitOrSeed(
    // 果实/种子颜色列表（比如["黑色","棕色","红色"]）
    val color: List<String>?,
    // 果实/种子形状（比如"球形"、"椭圆形"、"扁圆形"）
    val shape: String?
)

/**
 * 分页元数据
 * 用于列表请求的分页控制，比如总记录数决定分页数量
 */
data class TrefleMeta(
    // 符合查询条件的总记录数
    val total: Int
)