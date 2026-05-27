package com.example.afinal.data.model

import com.google.gson.annotations.SerializedName

/**
 * PlantNet植物识别API的响应数据模型
 * 作用：解析植物识别API返回的JSON数据，包含识别匹配结果、置信度、物种详细信息等核心内容
 * 适用场景：调用PlantNet第三方API后，将返回的JSON数据映射为该数据类，方便代码中读取和使用
 */
data class PlantNetResponse(
    // 植物识别结果列表，每个元素代表一个匹配的植物物种结果
    val results: List<PlantNetResult>,

    // 本次识别请求的查询参数信息（比如上传的图片、指定的植物器官等）
    val query: PlantNetQuery,

    // 响应结果的语言（比如"zh"代表中文，"en"代表英文）
    val language: String,

    // 优先使用的参考数据库名称（PlantNet支持多个植物数据库，此字段标识本次使用的优先库）
    val preferedReferential: String
)

/**
 * 单个植物识别匹配结果
 * 说明：PlantNet API会返回多个匹配度不同的植物结果，此类代表其中一个结果项
 */
data class PlantNetResult(
    // 识别置信度得分（0-1之间，数值越高代表匹配度越高）
    val score: Double,

    // 该匹配结果对应的植物物种详细信息
    val species: PlantNetSpecies,

    // GBIF（全球生物多样性信息机构）数据库的关联信息（可为空，部分物种可能无该数据）
    val gbif: PlantNetGbif?
)

/**
 * 植物物种核心信息
 * 包含物种的学名、俗名、属、科等分类学信息
 */
data class PlantNetSpecies(
    // 去除作者署名的科学名称（纯物种学名，无命名人信息，比如"Pinus sylvestris"）
    val scientificNameWithoutAuthor: String,

    // 科学名称的作者署名（学名命名人的信息，比如"L."代表林奈）
    val scientificNameAuthorship: String,

    // 该物种所属的"属"级分类信息
    val genus: PlantNetGenus,

    // 该物种所属的"科"级分类信息
    val family: PlantNetFamily,

    // 该物种的通用名称/俗名列表（比如"松树"、"樟子松"等，可能包含多语言）
    val commonNames: List<String>,

    // 完整的科学名称（包含作者署名，比如"Pinus sylvestris L."）
    val scientificName: String
)

/**
 * 植物"属"级分类信息
 * 分类学说明：属（Genus）是生物分类法中的一级，介于科和种之间
 */
data class PlantNetGenus(
    // 去除作者署名的属名（比如"Pinus"）
    val scientificNameWithoutAuthor: String,

    // 属名的作者署名（命名人信息）
    val scientificNameAuthorship: String,

    // 完整的属名（包含作者署名）
    val scientificName: String
)

/**
 * 植物"科"级分类信息
 * 分类学说明：科（Family）是生物分类法中的一级，介于目和属之间
 */
data class PlantNetFamily(
    // 去除作者署名的科名（比如"Pinaceae"松科）
    val scientificNameWithoutAuthor: String,

    // 科名的作者署名（命名人信息）
    val scientificNameAuthorship: String,

    // 完整的科名（包含作者署名）
    val scientificName: String
)

/**
 * GBIF数据库关联信息
 * GBIF说明：全球生物多样性信息机构（Global Biodiversity Information Facility），提供全球物种数据
 * 此字段仅存储该物种在GBIF数据库中的唯一ID，可用于后续查询更多数据
 */
data class PlantNetGbif(
    val id: String
)

/**
 * 植物识别请求的查询参数详情
 * 记录本次调用PlantNet API时传入的所有请求参数，用于追溯请求条件
 */
data class PlantNetQuery(
    // 项目标识符（PlantNet API需要指定项目ID才能使用，不同项目对应不同的API权限）
    val project: String,

    // 本次上传的图片地址/标识符列表（识别依据的图片，可能上传多张）
    val images: List<String>,

    // 指定的植物器官类型列表（比如"leaf"叶子、"flower"花、"fruit"果实，帮助API精准识别）
    val organs: List<String>,

    // 是否包含相关图片（返回结果中是否附带该物种的其他参考图片）
    val includeRelatedImages: Boolean
)