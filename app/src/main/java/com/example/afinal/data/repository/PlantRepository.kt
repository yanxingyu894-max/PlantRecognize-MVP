package com.example.afinal.data.repository

import android.util.Log
import com.example.afinal.data.local.PlantDao
import com.example.afinal.data.local.PlantEntity
import com.example.afinal.data.local.UserDao
import com.example.afinal.data.local.UserEntity
import com.example.afinal.data.model.*
import com.example.afinal.data.remote.PlantNetApiService
import com.example.afinal.BuildConfig
import com.example.afinal.data.remote.TrefleApiService
import com.example.afinal.util.HashUtils
import com.google.gson.JsonElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * 植物应用数据层核心仓库类
 * 作用：统一封装本地Room数据库与远程API数据操作，向ViewModel提供唯一数据入口
 * 遵循单一职责原则：只处理数据获取、缓存、转换，不涉及业务逻辑与UI
 *
 * 依赖注入：通过构造函数传入DAO与API接口，便于单元测试
 */
class PlantRepository(
    private val plantDao: PlantDao,        // 植物表数据访问对象
    private val userDao: UserDao,          // 用户表数据访问对象
    private val trefleApi: TrefleApiService,// Trefle植物百科API服务
    private val plantNetApi: PlantNetApiService // PlantNet植物识别API服务
) {
    // ==================== 常量配置 ====================
    /** Trefle API访问令牌，从BuildConfig读取，保证安全性 */
    private val TREFLE_TOKEN = if (BuildConfig.TREFLE_API_TOKEN.isNotBlank()) BuildConfig.TREFLE_API_TOKEN else ""
    /** PlantNet API密钥，从BuildConfig读取 */
    private val PLANTNET_KEY = if (BuildConfig.PLANTNET_API_KEY.isNotBlank()) BuildConfig.PLANTNET_API_KEY else ""

    // ==================== 可观察数据流 ====================
    /**
     * 观察所有收藏植物
     * 将数据库实体Flow转换为UI层Plant模型Flow
     * 自动响应收藏状态变化、数据更新
     */
    val favoritePlants: Flow<List<Plant>> = plantDao.getFavoritePlants().map { entities ->
        entities.map { it.toPlant() }
    }

    /**
     * 观察所有本地植物数据
     * 用于首页列表、植物库展示，实时响应数据库变化
     */
    val allPlants: Flow<List<Plant>> = plantDao.getAllPlants().map { entities ->
        entities.map { it.toPlant() }
    }

    // ==================== 收藏状态操作 ====================
    /**
     * 更新植物收藏状态
     * @param id 植物唯一ID
     * @param isFavorite 新收藏状态
     */
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean) {
        plantDao.updateFavoriteStatus(id, isFavorite)
    }

    // ==================== 植物详情获取 ====================

    suspend fun getPlantById(id: String, forceRefresh: Boolean = false): Plant? {
        Log.d("getPlantById", "正在通过调用getplantbyid: $id")
        // 1. 如果本地存储以 slug 作为查询条件，需要修改 DAO
        val local = plantDao.getPlantById(id)

        if (local != null && !forceRefresh && local.isDetailLoaded) {
            return local.toPlant()
        }

        return try {

            Log.d("PlantRepository", "正在通过 Slug 发起 API 调用: ${local?.slug}")
            // 使用新定义的接口方法
            val response = trefleApi.getPlantBySlug(local?.slug.toString(), TREFLE_TOKEN)

            var entity = response.data.toEntity(isDetail = true)

            // 确保关联时保持收藏状态
            if (local?.isFavorite == true) {
                entity = entity.copy(isFavorite = true)
            }

            plantDao.insertPlant(entity)
            entity.toPlant()
        } catch (e: Exception) {
            Log.e("PlantRepository", "API 获取详情失败:  ${local?.slug}", e)
            local?.toPlant()
        }
    }

    // ==================== AI植物识别 ====================
    /**
     * AI图片识别植物，并自动拉取完整详情
     * 流程：图片上传识别 → 学名搜索 → 二次详情拉取 → 本地入库
     * @param imageFile 待识别的植物图片文件
     * @return Result<Plant> 包装识别结果，成功返回带置信度的Plant
     */
    suspend fun identifyAndFetchDetails(imageFile: File): Result<Plant> {
        return try {
            // 构建图片上传请求体
            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("images", imageFile.name, requestFile)

            // 识别部位：自动模式
            val organsParts = listOf(
                MultipartBody.Part.createFormData("organs", null, "auto".toRequestBody("text/plain".toMediaTypeOrNull()))
            )

            // 1. 调用PlantNet进行识别
            val idResult = plantNetApi.identifyPlant(listOf(imagePart), organsParts, PLANTNET_KEY)
            val bestMatch = idResult.results.firstOrNull() ?: return Result.failure(Exception("AI 未能识别出该植物"))
            val scientificName = bestMatch.species.scientificNameWithoutAuthor

            // 2. 根据学名从Trefle搜索基础信息
            val searchResult = trefleApi.searchPlants(TREFLE_TOKEN, scientificName)
            val treflePlant = searchResult.data.firstOrNull()

            // 无匹配结果，创建兜底实体
            if (treflePlant == null || treflePlant.slug == null) {
                val fallbackEntity = createFallbackEntity(bestMatch.species, bestMatch.score)
                plantDao.insertPlant(fallbackEntity)
                return Result.success(fallbackEntity.toPlant().copy(confidence = bestMatch.score))
            }

            // 3. 二次调用：获取完整详情
            Log.d("PlantRepository", "AI 识别后发起二次详情调用: ${treflePlant.slug}")
            val detailResponse = trefleApi.getPlantBySlug(treflePlant.slug!!, TREFLE_TOKEN)
            var entity = detailResponse.data.toEntity(isDetail = true)

            // 保留收藏状态
            val existing = plantDao.getPlantById(entity.id)
            if (existing?.isFavorite == true) {
                entity = entity.copy(isFavorite = true)
            }

            // 入库并返回
            plantDao.insertPlant(entity)
            Result.success(entity.toPlant().copy(confidence = bestMatch.score))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 创建AI识别兜底实体（无Trefle数据时使用）
     * 仅包含基础识别信息，标记为未加载详情
     */
    private fun createFallbackEntity(species: PlantNetSpecies, confidence: Double): PlantEntity {
        val name = if (species.commonNames.isNotEmpty()) species.commonNames[0] else species.scientificNameWithoutAuthor
        return PlantEntity(
            id = "fallback_${System.currentTimeMillis()}",
            name = name,
            alias = species.scientificName,
            scientificName = species.scientificName,
            family = species.family.scientificName,
            genus = species.genus.scientificName,
            category = species.family.scientificName,
            desc = "AI 识别结果（置信度：${(confidence * 100).toInt()}%）。详细百科信息正在同步中。",
            care = "建议根据该科属植物的通用原则养护：保持充足的散射光，并确保土壤排水良好。",
            isDetailLoaded = false
        )
    }

    // ==================== 植物列表刷新 ====================
    /**
     * 随机刷新植物列表数据
     * 用于首页植物库加载，随机页码保证内容多样性
     * @param pageSize 每次加载数量
     * @return Result<Unit> 刷新结果
     */
// 在 PlantRepository.kt 中修改 refreshPlants 方法
    suspend fun refreshPlants(pageSize: Int = 20): Result<Unit> {
        return try {
            // 1. 获取随机页码的列表（这一步只拿到 slug 和基础信息）
            val metaResp = trefleApi.getPlants(TREFLE_TOKEN, page = 1, pageSize = 1)
            val total = metaResp.meta.total
            val randomPage = if (total / pageSize > 1) (1..minOf(total / pageSize, 100)).random() else 1
            val listResponse = trefleApi.getPlants(TREFLE_TOKEN, page = randomPage, pageSize = pageSize)

            val favoriteIds = plantDao.getFavoriteIds().toSet()

            // 2. 【核心修改】：针对列表中的每一个植物，发起“二次调用”获取 main_species 详情
            // 使用协程并行处理以提高效率
            val fullEntities = listResponse.data.map { dto ->
                val slug = dto.slug
                if (slug != null) {
                    try {
                        Log.d("PlantRepository", "随机发现：正在为 $slug 发起二次详情调用")
                        // 调用详情接口
                        val detailResp = trefleApi.getPlantBySlug(slug, TREFLE_TOKEN)
                        var entity = detailResp.data.toEntity(isDetail = true)

                        // 保持收藏状态
                        if (favoriteIds.contains(entity.id)) {
                            entity = entity.copy(isFavorite = true)
                        }
                        entity
                    } catch (e: Exception) {
                        // 如果详情获取失败，退而求其次存入基础信息
                        dto.toEntity(isDetail = false)
                    }
                } else {
                    dto.toEntity(isDetail = false)
                }
            }

            // 3. 批量存入数据库。现在存进去的就是带有 PH、土壤、光照等详情的完整数据了。
            plantDao.insertAllPlants(fullEntities)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepository", "随机刷新植物库失败", e)
            Result.failure(e)
        }
    }


    // ==================== 数据转换核心 ====================
    /**
     * 网络DTO → 本地数据库实体 核心转换函数
     * 处理字段映射、空值保护、枚举转文本、养护指南生成
     * @param isDetail 是否为完整详情（决定是否读取main_species）
     */
    private fun TreflePlantData.toEntity(isDetail: Boolean): PlantEntity {
        val main = mainSpecies
        val spec = main?.specifications
        val growth = main?.growth

        // 科属名称解析
        val familyName = extractName(family) ?: familyCommonName ?: "app error001: 无可名"
        val genusName = extractName(genus) ?: "app error002: 无 genus name"

        // 描述信息
        val observations = main?.observations ?: ""
        val finalDesc = if (observations.isNotBlank()) observations else {
            "这是一株属于 ${familyName} 科的植物。app error003: 数据无简介。"
        }

        // 生成养护指南
        val careGuide = buildCareGuide(growth, spec)

        // 多值字段拼接
        val flowerColor = main?.flower?.color?.joinToString("、") ?: ""
        val foliageColor = main?.foliage?.color?.joinToString("、") ?: ""
        val duration = main?.duration?.joinToString("、") ?: ""
        val ediblePart = main?.ediblePart?.joinToString("、") ?: ""
        val bloom = growth?.bloomMonths?.joinToString(",") ?: ""

        // 季节判断
        val season = when {
            bloom.contains("Mar", true) || bloom.contains("Apr", true) -> "春季"
            bloom.contains("Jun", true) || bloom.contains("Jul", true) -> "夏季"
            bloom.contains("Sep", true) || bloom.contains("Oct", true) -> "秋季"
            bloom.contains("Dec", true) || bloom.contains("Jan", true) -> "冬季"
            else -> "四季"
        }

        return PlantEntity(
            id = id.toString(),
            name = commonName ?: scientificName,
            alias = scientificName,
            slug = slug ?: "",
            commonName = commonName ?: "",
            scientificName = scientificName,
            family = familyName,
            genus = genusName,
            category = familyName,
            imageUrl = imageUrl ?: "",
            desc = finalDesc,
            isDetailLoaded = isDetail,
            flowerColor = flowerColor,
            foliageColor = foliageColor,
            duration = duration,
            growthHabit = spec?.growthHabit ?: "",
            growthRate = spec?.growthRate ?: "",
            averageHeight = spec?.averageHeight?.cm,
            maximumHeight = spec?.maximumHeight?.cm,
            toxicity = spec?.toxicity ?: "error",
            edible = main?.edible ?: false,
            ediblePart = ediblePart,
            light = growth?.light,
            phMinimum = growth?.phMinimum,
            phMaximum = growth?.phMaximum,
            minimumTemperature = growth?.minTemp?.deg_c,
            maximumTemperature = growth?.maxTemp?.deg_c,
            soilHumidity = humidityToText(growth?.soilHumidity),
            soilTexture = textureToText(growth?.soilTexture),
            soilNutrients = nutrientsToText(growth?.soilNutrients),
            bloomMonths = growth?.bloomMonths?.joinToString("、") ?: "",
            fruitMonths = growth?.fruitMonths?.joinToString("、") ?: "",
            care = careGuide,
            season = season,
            lastUpdate = System.currentTimeMillis()
        )
    }

    /**
     * 构建标准化养护指南
     * 包含光照、温度、pH、湿度等关键种植参数
     */
    private fun buildCareGuide(growth: TrefleGrowth?, spec: TrefleSpecifications?): String {
        val guide = StringBuilder()
        guide.append("【光照需求】\n")
        growth?.light?.let {
            val levelText = when (it) {
                in 1..3 -> "低光照 (室内间接光)"
                in 4..7 -> "半阴至中等光照"
                else -> "充足阳光"
            }
            guide.append("$levelText\n")
        } ?: guide.append("app error004: 数据无光照需求。\n")

        guide.append("\n【温度范围】\n")
        if (growth?.minTemp != null || growth?.maxTemp != null) {
            val minC = growth.minTemp?.deg_c ?: 10
            val maxC = growth.maxTemp?.deg_c ?: 30
            guide.append("$minC°C - $maxC°C\n")
        } else {
            guide.append("app error005: 数据无温度信息。\n")
        }

        guide.append("\n【土壤要求】\n")
        guide.append("pH值: ${growth?.phMinimum ?: "null"} - ${growth?.phMaximum ?: "app error006: 数据无pH信息。"}\n")
        guide.append("湿度: ${humidityToText(growth?.soilHumidity)}\n")
        return guide.toString()
    }

    /**
     * 安全提取JsonElement中的名称字段
     * 兼容对象/原始值两种结构，防止解析崩溃
     */
    private fun extractName(element: JsonElement?): String? {
        return try {
            when {
                element == null || element.isJsonNull -> null
                element.isJsonPrimitive -> element.asString
                element.isJsonObject -> element.asJsonObject.get("name")?.asString
                else -> null
            }
        } catch (e: Exception) { null }
    }

    /**
     * 安全提取JsonElement中的整型数值
     */
    private fun extractInt(element: JsonElement?): Int? {
        return try {
            if (element != null && element.isJsonPrimitive) element.asInt else null
        } catch (e: Exception) { null }
    }

    /**
     * 土壤湿度枚举 → 中文字符串
     */
    private fun humidityToText(element: JsonElement?): String {
        val level = extractInt(element)
        return when (level) {
            in 1..3 -> "低湿度 (干旱)"
            in 4..6 -> "中等湿度"
            in 7..10 -> "高湿度 (湿润)"
            else -> "适中"
        }
    }

    /**
     * 土壤质地枚举 → 中文字符串
     */
    private fun textureToText(element: JsonElement?): String {
        val level = extractInt(element)
        return when (level) {
            in 1..3 -> "砂质/疏松"
            in 4..7 -> "壤质/适中"
            in 8..10 -> "粘质/紧实"
            else -> "常规土质"
        }
    }

    /**
     * 土壤养分枚举 → 中文字符串
     */
    private fun nutrientsToText(element: JsonElement?): String {
        val level = extractInt(element)
        return when (level) {
            in 1..3 -> "贫瘠/低需求"
            in 4..7 -> "中等肥力"
            in 8..10 -> "肥沃/高需求"
            else -> "常规养分"
        }
    }

    /**
     * 数据库实体 → UI层Plant模型
     * 只保留页面需要的字段，解耦数据层与UI层
     */
    private fun PlantEntity.toPlant() = Plant(
        id = id,
        name = name,
        alias = alias,
        family = family,
        genus = genus,
        category = category,
        imageUrl = imageUrl,
        desc = desc,
        isFavorite = isFavorite,
        feature = "生长速度: ${growthRate.ifBlank { "app error007: 数据无生长速度信息。" }}",
        habit = "习性: ${growthHabit.ifBlank { "app error008: 数据无习性信息。" }}",
        care = care, season = season, flowerColor = flowerColor,
        foliageColor = foliageColor,
        duration = duration,
        edible = edible,
        ediblePart = ediblePart,
        light = light,
        phMinimum = phMinimum,
        phMaximum = phMaximum,
        minTemp = minimumTemperature,
        maxTemp = maximumTemperature,
        soilHumidity = soilHumidity,
        bloomMonths = bloomMonths,
        fruitMonths = fruitMonths
    )

    // ==================== 用户登录注册 ====================
    /**
     * 用户注册
     * 校验用户名唯一性，密码SHA256哈希存储
     */
    suspend fun registerUser(username: String, password: String, agree: Boolean): Result<Unit> {
        val existing = userDao.getUser(username)
        if (existing != null) return Result.failure(Exception("用户名已存在"))
        userDao.insertUser(UserEntity(username = username, passwordHash = HashUtils.sha256(password), agreedToTerms = agree))
        return Result.success(Unit)
    }

    /**
     * 用户登录
     * 比对用户名与哈希密码，返回登录结果
     */
    suspend fun loginUser(username: String, password: String): Result<Unit> {
        val user = userDao.getUser(username) ?: return Result.failure(Exception("用户不存在"))
        return if (user.passwordHash == HashUtils.sha256(password)) Result.success(Unit) else Result.failure(Exception("密码错误"))
    }
}