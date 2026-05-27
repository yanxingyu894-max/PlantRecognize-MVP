package com.example.afinal.data.repository

import android.util.Log
import com.example.afinal.data.local.PlantDao
import com.example.afinal.data.local.PlantEntity
import com.example.afinal.data.local.UserDao
import com.example.afinal.data.local.UserEntity
import com.example.afinal.data.model.*
import com.example.afinal.data.remote.PlantNetApiService
import com.example.afinal.BuildConfig
import com.example.afinal.data.remote.DeepSeekApiService
import com.example.afinal.data.remote.TrefleApiService
import com.example.afinal.util.HashUtils
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

// 启用协程的实验性API，用于支持flatMapLatest等高级流操作
@OptIn(ExperimentalCoroutinesApi::class)
/**
 * 植物数据仓库类，是数据层的核心入口
 * 负责协调本地数据库(PlantDao/UserDao)和远程API(Trefle/PlantNet/DeepSeek)的数据交互
 * 对外提供植物相关的所有数据操作接口，包括：用户管理、植物增删改查、AI识别、数据增强等
 * @param plantDao 本地植物数据库操作接口
 * @param userDao 本地用户数据库操作接口
 * @param trefleApi Trefle植物百科API服务（提供基础植物数据）
 * @param plantNetApi PlantNet植物识别API服务（图片识别植物）
 * @param deepSeekApi DeepSeek AI API服务（AI增强植物信息）
 */
class PlantRepository(
    private val plantDao: PlantDao,
    private val userDao: UserDao,
    private val trefleApi: TrefleApiService,
    private val plantNetApi: PlantNetApiService,
    private val deepSeekApi: DeepSeekApiService
) {
    // 伴生对象：定义常量
    companion object {
        // 游客用户ID，未登录时默认使用
        const val GUEST_USER_ID = "guest_user"
    }

    // 从BuildConfig中读取API密钥（BuildConfig是Android编译时生成的配置类，存放环境变量）
    // Trefle API令牌，用于访问植物百科数据
    private val TREFLE_TOKEN = if (BuildConfig.TREFLE_API_TOKEN.isNotBlank()) BuildConfig.TREFLE_API_TOKEN else ""
    // PlantNet API密钥，用于植物图片识别
    private val PLANTNET_KEY = if (BuildConfig.PLANTNET_API_KEY.isNotBlank()) BuildConfig.PLANTNET_API_KEY else ""
    // DeepSeek API密钥（带Bearer前缀），用于AI生成植物信息
    private val DEEPSEEK_KEY = if (BuildConfig.DEEPSEEK_API_KEY.isNotBlank()) "Bearer ${BuildConfig.DEEPSEEK_API_KEY}" else ""

    // 可修改的当前用户ID数据流（MutableStateFlow是协程的状态流，用于实时更新用户状态）
    private val _currentUserId = MutableStateFlow(GUEST_USER_ID)
    // 对外暴露只读的当前用户ID数据流，外部只能观察不能修改
    val currentUserId: Flow<String> = _currentUserId

    /**
     * 设置当前登录用户ID
     * @param userId 用户ID，为空则设置为游客ID
     */
    fun setUserId(userId: String?) {
        _currentUserId.value = userId ?: GUEST_USER_ID
        Log.d("PlantRepository", "User set to: ${_currentUserId.value}")
    }

    /**
     * 获取当前用户ID（非流式，直接获取当前值）
     * @return 当前用户ID（游客/登录用户）
     */
    fun getUserId(): String = _currentUserId.value

    /**
     * 观察当前用户的收藏植物列表
     * 功能说明：
     * 1. 监听当前用户ID变化（flatMapLatest：用户ID变化时重新请求数据）
     * 2. 从本地数据库获取该用户的收藏植物
     * 3. 将数据库实体(PlantEntity)转换为对外展示的模型(Plant)
     * @return 植物列表数据流，实时更新
     */
    val favoritePlants: Flow<List<Plant>> = _currentUserId.flatMapLatest { userId ->
        plantDao.getFavoritePlants(userId).map { entities ->
            entities.map { it.toPlant() }
        }
    }

    /**
     * 观察当前用户的所有植物列表（包含收藏和非收藏）
     * 逻辑同favoritePlants，只是查询范围是所有植物
     * @return 所有植物列表数据流
     */
    val allPlants: Flow<List<Plant>> = _currentUserId.flatMapLatest { userId ->
        plantDao.getAllPlants(userId).map { entities ->
            entities.map { it.toPlant() }
        }
    }

    /**
     * 观察是否有未加载完整详情的植物
     * 用途：用于后台自动补全植物的详细信息
     * @return 布尔值数据流，true表示有未加载详情的植物
     */
    val hasPendingDetails: Flow<Boolean> = _currentUserId.flatMapLatest { userId ->
        plantDao.getAllPlants(userId).map { entities ->
            entities.any { !it.isDetailLoaded }
        }
    }

    /**
     * 更新植物的收藏状态
     * @param slug 植物唯一标识（URL友好的名称）
     * @param isFavorite 是否收藏
     */
    suspend fun updateFavoriteStatus(slug: String, isFavorite: Boolean) {
        plantDao.updateFavoriteStatus(slug, isFavorite, getUserId())
    }

    /**
     * 根据植物slug获取植物详情
     * 核心逻辑：
     * 1. 先查本地数据库，有数据且详情已加载则直接返回
     * 2. 无数据/需要刷新则调用Trefle API获取详情
     * 3. 调用DeepSeek AI增强植物信息（补充描述、养护指南等）
     * 4. 保存到本地数据库，保留原收藏状态
     * @param slug 植物唯一标识
     * @param forceRefresh 是否强制刷新（忽略本地缓存）
     * @return 植物详情模型，失败则返回本地缓存（如果有）
     */
    suspend fun getPlantBySlug(slug: String, forceRefresh: Boolean = false): Plant? {
        val userId = getUserId()
        // 从本地数据库查询植物
        val local = plantDao.getPlantBySlug(slug, userId)

        // 本地有数据且不需要刷新、详情已加载 → 直接返回
        if (local != null && !forceRefresh && local.isDetailLoaded) {
            return local.toPlant()
        }

        return try {
            // 调用Trefle API获取植物详情
            val response = trefleApi.getPlantBySlug(slug, TREFLE_TOKEN)
            // 将API返回的DTO转换为本地数据库实体
            var entity = response.data.toEntity(isDetail = true, userId = userId)

            // 调用DeepSeek AI补充植物信息
            val aiInfo = fetchDeepSeekInfo(entity.commonName.ifBlank { entity.scientificName })
            if (aiInfo != null) {
                // 仅当原有数据为空/默认值时，替换为AI生成的信息
                entity = entity.copy(
                    desc = if (entity.desc.contains("This is a plant belonging to the")) aiInfo.description ?: entity.desc else entity.desc,
                    care = if (entity.care.isBlank()) aiInfo.careGuide ?: "" else entity.care,
                    flowerColor = if (entity.flowerColor.isBlank()) aiInfo.flowerColor ?: "" else entity.flowerColor,
                    toxicity = if (entity.toxicity.isBlank()) aiInfo.toxicity ?: "" else entity.toxicity,
                    edible = if (!entity.edible) aiInfo.edible ?: false else true,
                    nativeDistribution = if (entity.nativeDistribution.isBlank()) aiInfo.nativeDistribution ?: "" else entity.nativeDistribution
                )
            }

            // 保留本地的收藏状态
            if (local?.isFavorite == true) {
                entity = entity.copy(isFavorite = true)
            }

            // 保存到本地数据库
            plantDao.insertPlant(entity)
            // 转换为对外模型并返回
            entity.toPlant()
        } catch (e: Exception) {
            // API请求失败时，打印日志并返回本地缓存（如果有）
            Log.e("PlantRepository", "API detail fetch failed: $slug", e)
            local?.toPlant()
        }
    }

    /**
     * 调用DeepSeek AI获取植物的详细信息
     * 功能：通过自然语言提示词让AI返回标准化的植物信息JSON
     * @param plantName 植物名称（通用名/学名）
     * @return 标准化的AI植物信息模型，失败返回null
     */
    private suspend fun fetchDeepSeekInfo(plantName: String): DeepSeekPlantInfo? {
        // 校验API密钥和植物名称是否有效
        if (DEEPSEEK_KEY.isBlank() || plantName.isBlank()) return null
        return try {
            // 构建AI提示词：要求返回指定字段的JSON格式数据
            val prompt = """
                Provide detailed information about the plant '$plantName' in standardized JSON format in English.
                Fields: commonName, scientificName, family, genus, description, careGuide, flowerColor, toxicity, edible (boolean), nativeDistribution.
                Respond ONLY with the JSON object.
            """.trimIndent()

            // 构建DeepSeek API请求体
            val request = DeepSeekRequest(
                messages = listOf(DeepSeekMessage(role = "user", content = prompt)),
                responseFormat = DeepSeekResponseFormat(type = "json_object") // 指定返回JSON格式
            )
            // 调用DeepSeek API
            val response = deepSeekApi.chatCompletions(DEEPSEEK_KEY, request)
            // 提取AI返回的JSON字符串
            val jsonString = response.choices.firstOrNull()?.message?.content ?: return null
            // 将JSON字符串转换为实体类
            Gson().fromJson(jsonString, DeepSeekPlantInfo::class.java)
        } catch (e: Exception) {
            // AI请求失败时打印日志
            Log.e("PlantRepository", "DeepSeek fetch failed", e)
            null
        }
    }

    /**
     * 核心功能：图片识别植物并获取完整详情
     * 执行流程：
     * 1. 将图片文件上传到PlantNet API进行识别
     * 2. 获取识别结果中置信度最高的植物物种
     * 3. 通过Trefle API查询该物种的详细信息
     * 4. 调用DeepSeek AI增强植物信息
     * 5. 保存到本地数据库，保留收藏状态
     * 6. 返回识别结果
     * @param imageFile 植物图片文件
     * @return 识别结果（成功返回Plant模型，失败返回异常）
     */
    suspend fun identifyAndFetchDetails(imageFile: File): Result<Plant> {
        val userId = getUserId()
        return try {
            // 1. 构建图片上传请求体（Multipart格式，符合PlantNet API要求）
            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("images", imageFile.name, requestFile)
            val organsParts = listOf(
                MultipartBody.Part.createFormData("organs", null, "auto".toRequestBody("text/plain".toMediaTypeOrNull()))
            )

            // 2. 调用PlantNet API识别植物
            val idResult = plantNetApi.identifyPlant(listOf(imagePart), organsParts, PLANTNET_KEY)
            // 获取置信度最高的识别结果
            val bestMatch = idResult.results.firstOrNull() ?: return Result.failure(Exception("AI could not identify this plant."))
            // 提取植物学名（去掉作者名）
            val scientificName = bestMatch.species.scientificNameWithoutAuthor

            // 3. 调用Trefle API搜索该植物
            val searchResult = trefleApi.searchPlants(TREFLE_TOKEN, scientificName)
            val treflePlant = searchResult.data.firstOrNull()

            // 3.1 Trefle无结果时，创建本地兜底实体（仅保存识别结果，无详细信息）
            if (treflePlant == null || treflePlant.slug == null) {
                val fallbackEntity = createFallbackEntity(bestMatch.species, bestMatch.score, userId)
                plantDao.insertPlant(fallbackEntity)
                return Result.success(fallbackEntity.toPlant().copy(confidence = bestMatch.score))
            }

            // 3.2 Trefle有结果时，获取植物完整详情
            val detailResponse = trefleApi.getPlantBySlug(treflePlant.slug!!, TREFLE_TOKEN)
            var entity = detailResponse.data.toEntity(isDetail = true, userId = userId)

            // 4. 调用DeepSeek AI增强植物信息（同getPlantBySlug逻辑）
            val aiInfo = fetchDeepSeekInfo(entity.commonName.ifBlank { entity.scientificName })
            if (aiInfo != null) {
                entity = entity.copy(
                    desc = if (entity.desc.contains("This is a plant belonging to the")) aiInfo.description ?: entity.desc else entity.desc,
                    care = if (entity.care.isBlank()) aiInfo.careGuide ?: "" else entity.care,
                    flowerColor = if (entity.flowerColor.isBlank()) aiInfo.flowerColor ?: "" else entity.flowerColor,
                    toxicity = if (entity.toxicity.isBlank()) aiInfo.toxicity ?: "" else entity.toxicity,
                    edible = if (!entity.edible) aiInfo.edible ?: false else true,
                    nativeDistribution = if (entity.nativeDistribution.isBlank()) aiInfo.nativeDistribution ?: "" else entity.nativeDistribution
                )
            }

            // 5. 保留本地收藏状态
            val existing = plantDao.getPlantBySlug(entity.slug, userId)
            if (existing?.isFavorite == true) {
                entity = entity.copy(isFavorite = true)
            }

            // 6. 保存到数据库并返回结果（包含识别置信度）
            plantDao.insertPlant(entity)
            Result.success(entity.toPlant().copy(confidence = bestMatch.score))
        } catch (e: Exception) {
            // 任何步骤失败时，返回异常结果
            Result.failure(e)
        }
    }

    /**
     * 创建兜底植物实体（当Trefle API无结果时使用）
     * 用途：保证图片识别后至少有基础的植物信息展示
     * @param species PlantNet识别的物种信息
     * @param confidence 识别置信度（0-1）
     * @param userId 当前用户ID
     * @return 本地兜底的植物数据库实体
     */
    private fun createFallbackEntity(species: PlantNetSpecies, confidence: Double, userId: String): PlantEntity {
        // 优先使用通用名，无则使用学名
        val name = if (species.commonNames.isNotEmpty()) species.commonNames[0] else species.scientificNameWithoutAuthor
        // 生成兜底的slug（使用学名哈希值）
        val slug = "fallback_${species.scientificName.hashCode()}"
        return PlantEntity(
            slug = slug,
            ownerId = userId,
            id = slug,
            name = name,
            alias = species.scientificName,
            scientificName = species.scientificName,
            family = species.family.scientificName,
            genus = species.genus.scientificName,
            category = species.family.scientificName,
            // 兜底描述：包含识别置信度，提示详情正在同步
            desc = "AI Identification result (Confidence: ${(confidence * 100).toInt()}%). Detailed encyclopedia info is syncing.",
            // 兜底养护指南：基于科/属的通用建议
            care = "Care recommendation based on family/genus: Maintain adequate indirect light and ensure well-draining soil.",
            isDetailLoaded = false // 标记为未加载完整详情
        )
    }

    /**
     * 刷新植物列表（随机获取Trefle API的植物数据）
     * 功能：为用户加载新的随机植物列表，用于首页展示
     * @param pageSize 每页加载数量，默认20
     * @return 刷新结果（成功/失败）
     */
    suspend fun refreshPlants(pageSize: Int = 20): Result<Unit> {
        val userId = getUserId()
        return try {
            // 1. 先请求1条数据，获取总植物数量
            val metaResp = trefleApi.getPlants(TREFLE_TOKEN, page = 1, pageSize = 1)
            val total = metaResp.meta.total
            // 2. 随机生成页码（最多100页，避免页数过大）
            val randomPage = if (total / pageSize > 1) (1..minOf(total / pageSize, 100)).random() else 1
            // 3. 请求随机页的植物列表
            val listResponse = trefleApi.getPlants(TREFLE_TOKEN, page = randomPage, pageSize = pageSize)

            // 4. 获取当前用户的收藏植物slug，用于保留收藏状态
            val favoriteSlugs = plantDao.getFavoriteIds(userId).toSet()

            // 5. 将API数据转换为本地实体，并保留收藏状态
            val fullEntities = listResponse.data.map { dto ->
                var entity = dto.toEntity(isDetail = false, userId = userId)
                if (favoriteSlugs.contains(entity.slug)) {
                    entity = entity.copy(isFavorite = true)
                }
                entity
            }

            // 6. 批量插入数据库
            plantDao.insertAllPlants(fullEntities)
            Result.success(Unit)
        } catch (e: Exception) {
            // 刷新失败时打印日志
            Log.e("PlantRepository", "Random plant fetch failed", e)
            Result.failure(e)
        }
    }

    /**
     * 搜索外部植物（通过关键词）
     * 执行流程：
     * 1. 调用DeepSeek AI将关键词转换为准确的植物学名
     * 2. 调用Trefle API搜索该学名的植物
     * 3. Trefle无结果时，直接使用AI生成的植物信息
     * 4. 保存到本地数据库，保留收藏状态
     * @param query 搜索关键词（通用名/俗称/学名）
     * @return 搜索结果列表（成功返回Plant列表，失败返回异常）
     */
    suspend fun searchExternalPlant(query: String): Result<List<Plant>> {
        val userId = getUserId()
        return try {
            // 1. 将用户输入的关键词转换为准确的学名（提升搜索准确率）
            val scientificName = fetchScientificNameFromDeepSeek(query) ?: query
            // 2. 调用Trefle API搜索植物
            val trefleResults = trefleApi.searchPlants(TREFLE_TOKEN, scientificName)

            // 3. 获取当前用户的收藏slug，用于保留收藏状态
            val favoriteSlugs = plantDao.getFavoriteIds(userId).toSet()

            // 4. Trefle无结果时，使用AI生成植物信息
            if (trefleResults.data.isEmpty()) {
                val aiInfo = fetchDeepSeekInfo(query)
                if (aiInfo != null) {
                    var entity = aiInfo.toEntity(userId)
                    // 保留收藏状态
                    if (favoriteSlugs.contains(entity.slug)) {
                        entity = entity.copy(isFavorite = true)
                    }
                    plantDao.insertPlant(entity)
                    return Result.success(listOf(entity.toPlant()))
                }
                return Result.success(emptyList())
            }

            // 5. Trefle有结果时，转换为本地实体并保存
            val plants = trefleResults.data.map { dto ->
                var entity = dto.toEntity(isDetail = false, userId = userId)
                // 保留收藏状态
                if (favoriteSlugs.contains(entity.slug)) {
                    entity = entity.copy(isFavorite = true)
                }
                plantDao.insertPlant(entity)
                entity.toPlant()
            }

            Result.success(plants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 调用DeepSeek AI将用户输入的关键词转换为准确的植物学名
     * 用途：提升Trefle API搜索的准确率（用户可能输入俗称/错别字）
     * @param query 用户输入的关键词
     * @return 准确的植物学名，失败返回null
     */
    private suspend fun fetchScientificNameFromDeepSeek(query: String): String? {
        if (DEEPSEEK_KEY.isBlank()) return null
        return try {
            // 构建提示词：仅返回学名
            val prompt = "Provide the most accurate scientific name for the plant '$query'. Respond ONLY with the scientific name."
            val request = DeepSeekRequest(messages = listOf(DeepSeekMessage(role = "user", content = prompt)))
            val response = deepSeekApi.chatCompletions(DEEPSEEK_KEY, request)
            // 提取并清理学名（去除多余空格）
            response.choices.firstOrNull()?.message?.content?.trim()
        } catch (e: Exception) { null }
    }

    /**
     * 将DeepSeek AI返回的植物信息转换为本地数据库实体
     * @param userId 当前用户ID
     * @return 植物数据库实体
     */
    private fun DeepSeekPlantInfo.toEntity(userId: String): PlantEntity {
        // 生成AI数据的slug（使用学名/通用名的哈希值）
        val generatedSlug = "ds_${(scientificName ?: commonName).hashCode()}"
        return PlantEntity(
            slug = generatedSlug,
            ownerId = userId,
            id = generatedSlug,
            name = commonName ?: scientificName ?: "Unknown",
            commonName = commonName ?: "",
            scientificName = scientificName ?: "",
            family = family ?: "",
            genus = genus ?: "",
            desc = description ?: "",
            care = careGuide ?: "",
            flowerColor = flowerColor ?: "",
            toxicity = toxicity ?: "",
            edible = edible ?: false,
            nativeDistribution = nativeDistribution ?: "",
            isDetailLoaded = true // AI数据标记为已加载完整详情
        )
    }

    /**
     * 后台加载未完成的植物详情
     * 用途：补全之前图片识别生成的兜底植物信息
     * @param immediate 是否立即加载（true：无延迟；false：每1秒加载一个，避免API限流）
     */
    suspend fun fetchPendingDetails(immediate: Boolean) {
        val userId = getUserId()
        try {
            // 1. 查询当前用户所有未加载详情的植物
            val pending = plantDao.searchPlants("", userId).filter { !it.isDetailLoaded && it.slug.isNotBlank() }

            // 2. 逐个加载详情
            for (entity in pending) {
                try {
                    // 调用Trefle API获取详情
                    val detailResp = trefleApi.getPlantBySlug(entity.slug, TREFLE_TOKEN)
                    var updatedEntity = detailResp.data.toEntity(isDetail = true, userId = userId)

                    // 调用AI增强信息
                    val aiInfo = fetchDeepSeekInfo(updatedEntity.commonName.ifBlank { updatedEntity.scientificName })
                    if (aiInfo != null) {
                        updatedEntity = updatedEntity.copy(
                            desc = if (updatedEntity.desc.contains("belonging to the")) aiInfo.description ?: updatedEntity.desc else updatedEntity.desc,
                            care = if (updatedEntity.care.isBlank()) aiInfo.careGuide ?: "" else updatedEntity.care
                        )
                    }

                    // 保留收藏状态
                    if (entity.isFavorite) {
                        updatedEntity = updatedEntity.copy(isFavorite = true)
                    }
                    // 保存更新后的实体
                    plantDao.insertPlant(updatedEntity)
                    // 非立即加载时，延迟1秒（避免API请求过快被限流）
                    if (!immediate) {
                        delay(1000)
                    }
                } catch (e: CancellationException) {
                    // 协程取消时重新抛出异常
                    throw e
                } catch (e: Exception) {
                    // 单个植物加载失败时打印日志，继续处理下一个
                    Log.e("PlantRepository", "Background detail fetch failed: ${entity.slug}", e)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 整体加载失败时打印日志
            Log.e("PlantRepository", "Failed to fetch pending details", e)
        }
    }

    /**
     * 将Trefle API返回的植物数据转换为本地数据库实体
     * 核心转换逻辑：将多层嵌套的API数据映射为扁平的本地实体，方便数据库存储和使用
     * @param isDetail 是否是完整详情（true：已加载所有信息；false：仅基础信息）
     * @param userId 当前用户ID
     * @return 植物数据库实体
     */
    private fun TreflePlantData.toEntity(isDetail: Boolean, userId: String): PlantEntity {
        // 提取核心数据字段（Trefle API返回的是嵌套结构）
        val main = mainSpecies
        val spec = main?.specifications
        val growth = main?.growth

        // 提取科/属名称（优先显示通用名，无则显示学名）
        val familyName = extractName(family) ?: familyCommonName ?: "Unknown Family"
        val genusName = extractName(genus) ?: "Unknown Genus"

        // 构建植物描述（优先使用观测信息，无则使用默认描述）
        val observations = main?.observations ?: ""
        val finalDesc = if (observations.isNotBlank()) observations else "This is a plant belonging to the $familyName family."
        // 构建养护指南（基于生长环境数据）
        val careGuide = buildCareGuide(growth)

        // 提取各类特征字段（处理空值和集合转换）
        val flowerColor = main?.flower?.color?.joinToString(", ") ?: ""
        val foliageColor = main?.foliage?.color?.joinToString(", ") ?: ""
        val duration = main?.duration?.joinToString(", ") ?: ""
        val ediblePart = main?.ediblePart?.joinToString(", ") ?: ""

        // 提取分布信息（本地/引入）
        val nativeDist = main?.distributions?.native?.mapNotNull { it.name }?.joinToString(", ") ?: ""
        val introducedDist = main?.distributions?.introduced?.mapNotNull { it.name }?.joinToString(", ") ?: ""

        // 提取花期并转换为季节
        val bloom = growth?.bloomMonths?.joinToString(",") ?: ""
        val season = when {
            bloom.contains("Mar", true) || bloom.contains("Apr", true) -> "Spring"
            bloom.contains("Jun", true) || bloom.contains("Jul", true) -> "Summer"
            bloom.contains("Sep", true) || bloom.contains("Oct", true) -> "Autumn"
            bloom.contains("Dec", true) || bloom.contains("Jan", true) -> "Winter"
            else -> "All Seasons"
        }

        // 构建并返回本地实体
        return PlantEntity(
            slug = slug ?: "",
            ownerId = userId,
            id = id.toString(),
            name = commonName ?: scientificName,
            alias = scientificName,
            commonName = commonName ?: "",
            scientificName = scientificName,
            family = familyName,
            genus = genusName,
            category = familyName,
            imageUrl = imageUrl ?: "",
            desc = finalDesc,
            isDetailLoaded = isDetail,
            flowerColor = flowerColor,
            flowerConspicuous = main?.flower?.conspicuous,
            foliageColor = foliageColor,
            foliageTexture = main?.foliage?.texture ?: "",
            leafRetention = main?.foliage?.leafRetention,
            fruitColor = main?.fruitOrSeed?.color?.joinToString(", ") ?: "",
            fruitShape = main?.fruitOrSeed?.shape ?: "",
            duration = duration,
            ligneousType = spec?.ligneousType ?: "",
            growthHabit = spec?.growthHabit ?: "",
            growthRate = spec?.growthRate ?: "",
            spread = growth?.spread?.cm,
            averageHeight = spec?.averageHeight?.cm,
            maximumHeight = spec?.maximumHeight?.cm,
            toxicity = spec?.toxicity ?: "",
            edible = main?.edible ?: false,
            ediblePart = ediblePart,
            nativeDistribution = nativeDist,
            introducedDistribution = introducedDist,
            light = growth?.light,
            phMinimum = growth?.phMinimum,
            phMaximum = growth?.phMaximum,
            minimumTemperature = growth?.minTemp?.deg_c,
            maximumTemperature = growth?.maxTemp?.deg_c,
            soilHumidity = extractInt(growth?.soilHumidity),
            soilTexture = extractInt(growth?.soilTexture),
            soilNutrients = extractInt(growth?.soilNutrients),
            soilSalinity = extractInt(growth?.soilSalinity),
            growthMonths = growth?.growthMonths?.joinToString(", ") ?: "",
            bloomMonths = growth?.bloomMonths?.joinToString(", ") ?: "",
            fruitMonths = growth?.fruitMonths?.joinToString(", ") ?: "",
            care = careGuide,
            season = season,
            lastUpdate = System.currentTimeMillis() // 记录最后更新时间
        )
    }

    /**
     * 构建植物养护指南
     * 功能：将Trefle API返回的量化生长数据转换为人类可读的文字描述
     * @param growth Trefle API返回的生长环境数据
     * @return 格式化的养护指南字符串
     */
    private fun buildCareGuide(growth: TrefleGrowth?): String {
        val guide = StringBuilder()

        // 光照要求（1-10级转换为文字描述）
        growth?.light?.let {
            val levelText = when (it) {
                in 1..3 -> "Low light (Indoor indirect)"
                in 4..7 -> "Partial shade to medium light"
                else -> "Full sun (Direct sunlight)"
            }
            guide.append("Light Requirements: $levelText (Level $it/10)\n\n")
        }

        // 土壤pH值
        if (growth?.phMinimum != null && growth?.phMaximum != null) {
            guide.append("Soil pH: ${growth.phMinimum} - ${growth.phMaximum}\n\n")
        }

        // 温度耐受性
        if (growth?.minTemp?.deg_c != null || growth?.maxTemp?.deg_c != null) {
            val min = growth.minTemp?.deg_c?.toString() ?: "N/A"
            val max = growth.maxTemp?.deg_c?.toString() ?: "N/A"
            guide.append("Temperature Tolerance: $min°C to $max°C\n\n")
        }

        // 土壤湿度（1-10级转换为文字描述）
        growth?.soilHumidity?.let { element ->
            extractInt(element)?.let {
                val text = when (it) {
                    in 1..3 -> "Dry (Drought-tolerant)"
                    in 4..6 -> "Moderate moisture"
                    in 7..10 -> "High moisture (Wet/Bog)"
                    else -> "Average moisture"
                }
                guide.append("Soil Moisture: $text (Level $it/10)\n\n")
            }
        }

        // 土壤质地（1-10级转换为文字描述）
        growth?.soilTexture?.let { element ->
            extractInt(element)?.let {
                val text = when (it) {
                    in 1..3 -> "Sandy, loose, well-draining"
                    in 4..7 -> "Loamy, balanced texture"
                    in 8..10 -> "Clay, heavy, moisture-retaining"
                    else -> "Standard potting mix"
                }
                guide.append("Soil Texture: $text (Level $it/10)\n\n")
            }
        }

        // 土壤肥力（1-10级转换为文字描述）
        growth?.soilNutrients?.let { element ->
            extractInt(element)?.let {
                val text = when (it) {
                    in 1..3 -> "Low fertility (Poor soil tolerant)"
                    in 4..7 -> "Moderate fertility"
                    in 8..10 -> "High fertility (Requires rich soil)"
                    else -> "Standard nutrients requirements"
                }
                guide.append("Soil Fertility: $text (Level $it/10)\n\n")
            }
        }

        // 土壤盐度耐受性（1-10级转换为文字描述）
        growth?.soilSalinity?.let { element ->
            extractInt(element)?.let {
                val text = when (it) {
                    in 1..3 -> "Low salinity tolerance"
                    in 4..7 -> "Moderate salinity tolerance"
                    in 8..10 -> "High salinity tolerance (Coastal suitable)"
                    else -> "Unknown salinity tolerance"
                }
                guide.append("Soil Salinity: $text (Level $it/10)\n\n")
            }
        }

        // 去除末尾多余的换行和空格
        return guide.toString().trim()
    }

    /**
     * 从JsonElement中提取名称字符串
     * 处理Trefle API返回的嵌套JSON结构（可能是字符串/对象/空值）
     * @param element JSON元素
     * @return 提取的名称字符串，失败返回null
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
     * 从JsonElement中提取整数值
     * 处理Trefle API返回的量化数据（如光照等级、湿度等级）
     * @param element JSON元素
     * @return 提取的整数值，失败返回null
     */
    private fun extractInt(element: JsonElement?): Int? {
        return try {
            if (element != null && element.isJsonPrimitive) element.asInt else null
        } catch (e: Exception) { null }
    }

    /**
     * 将本地数据库实体转换为对外展示的植物模型
     * 用途：隔离数据库层和UI层的数据结构，避免直接暴露数据库实体
     * @return 对外展示的Plant模型
     */
    private fun PlantEntity.toPlant() = Plant(
        id = slug,
        name = name,
        alias = alias,
        commonName = commonName,
        scientificName = scientificName,
        family = family,
        genus = genus,
        category = category,
        imageUrl = imageUrl,
        desc = desc,
        feature = feature,
        habit = habit,
        care = care,
        season = season,
        isFavorite = isFavorite,
        flowerColor = flowerColor,
        flowerConspicuous = flowerConspicuous,
        foliageColor = foliageColor,
        foliageTexture = foliageTexture,
        leafRetention = leafRetention,
        fruitColor = fruitColor,
        fruitShape = fruitShape,
        duration = duration,
        ligneousType = ligneousType,
        growthHabit = growthHabit,
        growthRate = growthRate,
        spread = spread,
        averageHeight = averageHeight,
        maximumHeight = maximumHeight,
        toxicity = toxicity,
        edible = edible,
        ediblePart = ediblePart,
        nativeDistribution = nativeDistribution,
        introducedDistribution = introducedDistribution,
        light = light,
        phMinimum = phMinimum,
        phMaximum = phMaximum,
        minTemp = minimumTemperature,
        maxTemp = maximumTemperature,
        soilHumidity = soilHumidity,
        soilTexture = soilTexture,
        soilNutrients = soilNutrients,
        soilSalinity = soilSalinity,
        growthMonths = growthMonths,
        bloomMonths = bloomMonths,
        fruitMonths = fruitMonths
    )

    /**
     * 注册新用户
     * 安全说明：密码使用SHA256哈希存储，不存储明文
     * @param username 用户名
     * @param password 密码（明文，内部会哈希处理）
     * @param agree 是否同意用户协议
     * @return 注册结果（成功/失败：用户已存在）
     */
    suspend fun registerUser(username: String, password: String, agree: Boolean): Result<Unit> {
        // 检查用户是否已存在
        val existing = userDao.getUser(username)
        if (existing != null) return Result.failure(Exception("User already exists"))
        // 哈希密码并插入数据库
        userDao.insertUser(UserEntity(username = username, passwordHash = HashUtils.sha256(password), agreedToTerms = agree))
        return Result.success(Unit)
    }

    /**
     * 用户登录
     * 验证逻辑：对比输入密码的哈希值和数据库中存储的哈希值
     * @param username 用户名
     * @param password 密码（明文）
     * @return 登录结果（成功/失败：用户不存在/密码错误）
     */
    suspend fun loginUser(username: String, password: String): Result<Unit> {
        // 检查用户是否存在
        val user = userDao.getUser(username) ?: return Result.failure(Exception("User does not exist"))
        // 验证密码哈希
        return if (user.passwordHash == HashUtils.sha256(password)) {
            // 登录成功，设置当前用户ID
            setUserId(username)
            Result.success(Unit)
        } else Result.failure(Exception("Incorrect password"))
    }

    /**
     * 用户登出
     * 功能：将当前用户ID重置为游客ID
     */
    fun logout() {
        setUserId(null)
    }
}