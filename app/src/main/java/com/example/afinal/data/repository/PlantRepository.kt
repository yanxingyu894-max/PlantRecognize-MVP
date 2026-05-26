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

@OptIn(ExperimentalCoroutinesApi::class)
class PlantRepository(
    private val plantDao: PlantDao,
    private val userDao: UserDao,
    private val trefleApi: TrefleApiService,
    private val plantNetApi: PlantNetApiService,
    private val deepSeekApi: DeepSeekApiService
) {
    companion object {
        const val GUEST_USER_ID = "guest_user"
    }

    private val TREFLE_TOKEN = if (BuildConfig.TREFLE_API_TOKEN.isNotBlank()) BuildConfig.TREFLE_API_TOKEN else ""
    private val PLANTNET_KEY = if (BuildConfig.PLANTNET_API_KEY.isNotBlank()) BuildConfig.PLANTNET_API_KEY else ""
    private val DEEPSEEK_KEY = if (BuildConfig.DEEPSEEK_API_KEY.isNotBlank()) "Bearer ${BuildConfig.DEEPSEEK_API_KEY}" else ""

    private val _currentUserId = MutableStateFlow(GUEST_USER_ID)
    val currentUserId: Flow<String> = _currentUserId

    fun setUserId(userId: String?) {
        _currentUserId.value = userId ?: GUEST_USER_ID
        Log.d("PlantRepository", "User set to: ${_currentUserId.value}")
    }

    fun getUserId(): String = _currentUserId.value

    val favoritePlants: Flow<List<Plant>> = _currentUserId.flatMapLatest { userId ->
        plantDao.getFavoritePlants(userId).map { entities ->
            entities.map { it.toPlant() }
        }
    }

    val allPlants: Flow<List<Plant>> = _currentUserId.flatMapLatest { userId ->
        plantDao.getAllPlants(userId).map { entities ->
            entities.map { it.toPlant() }
        }
    }

    val hasPendingDetails: Flow<Boolean> = _currentUserId.flatMapLatest { userId ->
        plantDao.getAllPlants(userId).map { entities ->
            entities.any { !it.isDetailLoaded }
        }
    }

    suspend fun updateFavoriteStatus(slug: String, isFavorite: Boolean) {
        plantDao.updateFavoriteStatus(slug, isFavorite, getUserId())
    }

    suspend fun getPlantBySlug(slug: String, forceRefresh: Boolean = false): Plant? {
        val userId = getUserId()
        val local = plantDao.getPlantBySlug(slug, userId)

        if (local != null && !forceRefresh && local.isDetailLoaded) {
            return local.toPlant()
        }

        return try {
            val response = trefleApi.getPlantBySlug(slug, TREFLE_TOKEN)
            var entity = response.data.toEntity(isDetail = true, userId = userId)

            // DeepSeek Augmentation
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

            if (local?.isFavorite == true) {
                entity = entity.copy(isFavorite = true)
            }

            plantDao.insertPlant(entity)
            entity.toPlant()
        } catch (e: Exception) {
            Log.e("PlantRepository", "API detail fetch failed: $slug", e)
            local?.toPlant()
        }
    }

    private suspend fun fetchDeepSeekInfo(plantName: String): DeepSeekPlantInfo? {
        if (DEEPSEEK_KEY.isBlank() || plantName.isBlank()) return null
        return try {
            val prompt = """
                Provide detailed information about the plant '$plantName' in standardized JSON format in English.
                Fields: commonName, scientificName, family, genus, description, careGuide, flowerColor, toxicity, edible (boolean), nativeDistribution.
                Respond ONLY with the JSON object.
            """.trimIndent()

            val request = DeepSeekRequest(
                messages = listOf(DeepSeekMessage(role = "user", content = prompt)),
                responseFormat = DeepSeekResponseFormat(type = "json_object")
            )
            val response = deepSeekApi.chatCompletions(DEEPSEEK_KEY, request)
            val jsonString = response.choices.firstOrNull()?.message?.content ?: return null
            Gson().fromJson(jsonString, DeepSeekPlantInfo::class.java)
        } catch (e: Exception) {
            Log.e("PlantRepository", "DeepSeek fetch failed", e)
            null
        }
    }

    suspend fun identifyAndFetchDetails(imageFile: File): Result<Plant> {
        val userId = getUserId()
        return try {
            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("images", imageFile.name, requestFile)
            val organsParts = listOf(
                MultipartBody.Part.createFormData("organs", null, "auto".toRequestBody("text/plain".toMediaTypeOrNull()))
            )

            val idResult = plantNetApi.identifyPlant(listOf(imagePart), organsParts, PLANTNET_KEY)
            val bestMatch = idResult.results.firstOrNull() ?: return Result.failure(Exception("AI could not identify this plant."))
            val scientificName = bestMatch.species.scientificNameWithoutAuthor

            val searchResult = trefleApi.searchPlants(TREFLE_TOKEN, scientificName)
            val treflePlant = searchResult.data.firstOrNull()

            if (treflePlant == null || treflePlant.slug == null) {
                val fallbackEntity = createFallbackEntity(bestMatch.species, bestMatch.score, userId)
                plantDao.insertPlant(fallbackEntity)
                return Result.success(fallbackEntity.toPlant().copy(confidence = bestMatch.score))
            }

            val detailResponse = trefleApi.getPlantBySlug(treflePlant.slug!!, TREFLE_TOKEN)
            var entity = detailResponse.data.toEntity(isDetail = true, userId = userId)

            val existing = plantDao.getPlantBySlug(entity.slug, userId)
            if (existing?.isFavorite == true) {
                entity = entity.copy(isFavorite = true)
            }

            plantDao.insertPlant(entity)
            Result.success(entity.toPlant().copy(confidence = bestMatch.score))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createFallbackEntity(species: PlantNetSpecies, confidence: Double, userId: String): PlantEntity {
        val name = if (species.commonNames.isNotEmpty()) species.commonNames[0] else species.scientificNameWithoutAuthor
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
            desc = "AI Identification result (Confidence: ${(confidence * 100).toInt()}%). Detailed encyclopedia info is syncing.",
            care = "Care recommendation based on family/genus: Maintain adequate indirect light and ensure well-draining soil.",
            isDetailLoaded = false
        )
    }

    suspend fun refreshPlants(pageSize: Int = 20): Result<Unit> {
        val userId = getUserId()
        return try {
            val metaResp = trefleApi.getPlants(TREFLE_TOKEN, page = 1, pageSize = 1)
            val total = metaResp.meta.total
            val randomPage = if (total / pageSize > 1) (1..minOf(total / pageSize, 100)).random() else 1
            val listResponse = trefleApi.getPlants(TREFLE_TOKEN, page = randomPage, pageSize = pageSize)

            val favoriteSlugs = plantDao.getFavoriteIds(userId).toSet()

            val fullEntities = listResponse.data.map { dto ->
                var entity = dto.toEntity(isDetail = false, userId = userId)
                if (favoriteSlugs.contains(entity.slug)) {
                    entity = entity.copy(isFavorite = true)
                }
                entity
            }

            plantDao.insertAllPlants(fullEntities)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepository", "Random plant fetch failed", e)
            Result.failure(e)
        }
    }

    suspend fun searchExternalPlant(query: String): Result<List<Plant>> {
        val userId = getUserId()
        return try {
            val scientificName = fetchScientificNameFromDeepSeek(query) ?: query
            val trefleResults = trefleApi.searchPlants(TREFLE_TOKEN, scientificName)
            
            if (trefleResults.data.isEmpty()) {
                val aiInfo = fetchDeepSeekInfo(query)
                if (aiInfo != null) {
                    val entity = aiInfo.toEntity(userId)
                    plantDao.insertPlant(entity)
                    return Result.success(listOf(entity.toPlant()))
                }
                return Result.success(emptyList())
            }

            val plants = trefleResults.data.map { dto ->
                val entity = dto.toEntity(isDetail = false, userId = userId)
                plantDao.insertPlant(entity)
                entity.toPlant()
            }
            
            Result.success(plants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchScientificNameFromDeepSeek(query: String): String? {
        if (DEEPSEEK_KEY.isBlank()) return null
        return try {
            val prompt = "Provide the most accurate scientific name for the plant '$query'. Respond ONLY with the scientific name."
            val request = DeepSeekRequest(messages = listOf(DeepSeekMessage(role = "user", content = prompt)))
            val response = deepSeekApi.chatCompletions(DEEPSEEK_KEY, request)
            response.choices.firstOrNull()?.message?.content?.trim()
        } catch (e: Exception) { null }
    }

    private fun DeepSeekPlantInfo.toEntity(userId: String): PlantEntity {
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
            isDetailLoaded = true
        )
    }

    suspend fun fetchPendingDetails(immediate: Boolean) {
        val userId = getUserId()
        try {
            val pending = plantDao.searchPlants("", userId).filter { !it.isDetailLoaded && it.slug.isNotBlank() }

            for (entity in pending) {
                try {
                    val detailResp = trefleApi.getPlantBySlug(entity.slug, TREFLE_TOKEN)
                    var updatedEntity = detailResp.data.toEntity(isDetail = true, userId = userId)

                    val aiInfo = fetchDeepSeekInfo(updatedEntity.commonName.ifBlank { updatedEntity.scientificName })
                    if (aiInfo != null) {
                        updatedEntity = updatedEntity.copy(
                            desc = if (updatedEntity.desc.contains("belonging to the")) aiInfo.description ?: updatedEntity.desc else updatedEntity.desc,
                            care = if (updatedEntity.care.isBlank()) aiInfo.careGuide ?: "" else updatedEntity.care
                        )
                    }

                    if (entity.isFavorite) {
                        updatedEntity = updatedEntity.copy(isFavorite = true)
                    }
                    plantDao.insertPlant(updatedEntity)
                    if (!immediate) {
                        delay(1000)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("PlantRepository", "Background detail fetch failed: ${entity.slug}", e)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("PlantRepository", "Failed to fetch pending details", e)
        }
    }

    private fun TreflePlantData.toEntity(isDetail: Boolean, userId: String): PlantEntity {
        val main = mainSpecies
        val spec = main?.specifications
        val growth = main?.growth

        val familyName = extractName(family) ?: familyCommonName ?: "Unknown Family"
        val genusName = extractName(genus) ?: "Unknown Genus"

        val observations = main?.observations ?: ""
        val finalDesc = if (observations.isNotBlank()) observations else "This is a plant belonging to the $familyName family."
        val careGuide = buildCareGuide(growth)

        val flowerColor = main?.flower?.color?.joinToString(", ") ?: ""
        val foliageColor = main?.foliage?.color?.joinToString(", ") ?: ""
        val duration = main?.duration?.joinToString(", ") ?: ""
        val ediblePart = main?.ediblePart?.joinToString(", ") ?: ""

        val nativeDist = main?.distributions?.native?.mapNotNull { it.name }?.joinToString(", ") ?: ""
        val introducedDist = main?.distributions?.introduced?.mapNotNull { it.name }?.joinToString(", ") ?: ""

        val bloom = growth?.bloomMonths?.joinToString(",") ?: ""
        val season = when {
            bloom.contains("Mar", true) || bloom.contains("Apr", true) -> "Spring"
            bloom.contains("Jun", true) || bloom.contains("Jul", true) -> "Summer"
            bloom.contains("Sep", true) || bloom.contains("Oct", true) -> "Autumn"
            bloom.contains("Dec", true) || bloom.contains("Jan", true) -> "Winter"
            else -> "All Seasons"
        }

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
            lastUpdate = System.currentTimeMillis()
        )
    }

    private fun buildCareGuide(growth: TrefleGrowth?): String {
        val guide = StringBuilder()

        growth?.light?.let {
            val levelText = when (it) {
                in 1..3 -> "Low light (Indoor indirect)"
                in 4..7 -> "Partial shade to medium light"
                else -> "Full sun (Direct sunlight)"
            }
            guide.append("Light Requirements: $levelText (Level $it/10)\n\n")
        }

        if (growth?.phMinimum != null && growth?.phMaximum != null) {
            guide.append("Soil pH: ${growth.phMinimum} - ${growth.phMaximum}\n\n")
        }

        if (growth?.minTemp?.deg_c != null || growth?.maxTemp?.deg_c != null) {
            val min = growth.minTemp?.deg_c?.toString() ?: "N/A"
            val max = growth.maxTemp?.deg_c?.toString() ?: "N/A"
            guide.append("Temperature Tolerance: $min°C to $max°C\n\n")
        }

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

        return guide.toString().trim()
    }

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

    private fun extractInt(element: JsonElement?): Int? {
        return try {
            if (element != null && element.isJsonPrimitive) element.asInt else null
        } catch (e: Exception) { null }
    }

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

    suspend fun registerUser(username: String, password: String, agree: Boolean): Result<Unit> {
        val existing = userDao.getUser(username)
        if (existing != null) return Result.failure(Exception("User already exists"))
        userDao.insertUser(UserEntity(username = username, passwordHash = HashUtils.sha256(password), agreedToTerms = agree))
        return Result.success(Unit)
    }

    suspend fun loginUser(username: String, password: String): Result<Unit> {
        val user = userDao.getUser(username) ?: return Result.failure(Exception("User does not exist"))
        return if (user.passwordHash == HashUtils.sha256(password)) {
            setUserId(username)
            Result.success(Unit)
        } else Result.failure(Exception("Incorrect password"))
    }

    fun logout() {
        setUserId(null)
    }
}
