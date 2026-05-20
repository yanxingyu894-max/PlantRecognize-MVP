package com.example.afinal.data.repository

import com.example.afinal.data.local.PlantDao
import com.example.afinal.data.local.PlantEntity
import com.example.afinal.data.model.Plant
import com.example.afinal.data.model.TreflePlantData
import com.example.afinal.data.remote.PlantNetApiService
import com.example.afinal.data.remote.TrefleApiService
import com.google.gson.JsonElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PlantRepository(
    private val plantDao: PlantDao,
    private val trefleApi: TrefleApiService,
    private val plantNetApi: PlantNetApiService
) {
    private val TREFLE_TOKEN = "usr-uR7Pwb7ku_SiptQTrntLgOF2F1UsH0aoWLam67uROe8"
    private val PLANTNET_KEY = "2b10jZ1ZMitX6HCPM0jqVlWjhO"

    val favoritePlants: Flow<List<Plant>> = plantDao.getFavoritePlants().map { entities ->
        entities.map { it.toPlant() }
    }

    val allPlants: Flow<List<Plant>> = plantDao.getAllPlants().map { entities ->
        entities.map { it.toPlant() }
    }

    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean) {
        plantDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun getPlantById(id: String): Plant? {
        val local = plantDao.getPlantById(id)
        if (local != null) return local.toPlant()

        return try {
            val response = trefleApi.getPlantById(id.toInt(), TREFLE_TOKEN)
            val entity = response.data.toEntity()
            plantDao.insertPlant(entity)
            entity.toPlant()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun identifyAndFetchDetails(imageFile: File): Result<Plant> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("images", imageFile.name, requestFile)
            val organPart = MultipartBody.Part.createFormData("organs", "leaf")

            val idResult = plantNetApi.identifyPlant(
                images = listOf(imagePart),
                organs = listOf(organPart),
                apiKey = PLANTNET_KEY
            )

            val bestMatch = idResult.results.firstOrNull() ?: return Result.failure(Exception("未识别到植物"))
            val scientificName = bestMatch.species.scientificNameWithoutAuthor

            val searchResult = trefleApi.searchPlants(TREFLE_TOKEN, scientificName)
            val treflePlant = searchResult.data.firstOrNull() ?: return Result.failure(Exception("百科库暂未收录该植物详情"))

            // 获取详情以获得 growth 和 specifications
            val detailResponse = trefleApi.getPlantById(treflePlant.id, TREFLE_TOKEN)
            val finalData = detailResponse.data
            
            val entity = finalData.toEntity()
            plantDao.insertPlant(entity)
            
            Result.success(entity.toPlant())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshPlants(): Result<Unit> {
        return try {
            val response = trefleApi.getPlants(TREFLE_TOKEN)
            val entities = response.data.map { it.toEntity() }
            plantDao.insertAllPlants(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 转换与修复逻辑 ====================

    /**
     * 核心修复：处理 Trefle API 中动态类型的 family 和 genus
     */
    private fun JsonElement?.extractName(): String? {
        if (this == null || this.isJsonNull) return null
        return try {
            if (this.isJsonObject) {
                this.asJsonObject.get("name")?.asString
            } else {
                this.asString
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun TreflePlantData.toEntity(): PlantEntity {
        val spec = mainSpecies?.specifications
        val growth = mainSpecies?.growth
        
        val finalName = commonName ?: scientificName
        val familyName = family.extractName() ?: familyCommonName ?: "未知科属"
        val genusName = genus.extractName() ?: "植物"

        // 描述补全：如果 API 没给描述，自动生成一段基于学名和科属的描述
        val description = growth?.description ?: "该植物在生物学上被归类为 $familyName 下的 $genusName 属。学名为 $scientificName。由于其独特的生长特性，在自然界中占有一席之地。"
        
        val height = spec?.averageHeight?.cm?.let { "$it" } ?: "未知"
        
        // 将数字等级转化为用户友好的描述
        val lightReq = when(growth?.light) {
            in 0..3 -> "耐阴"
            in 4..6 -> "半日照"
            in 7..10 -> "喜光"
            else -> "未知"
        }
        
        val humidityReq = when(growth?.atmosphericHumidity) {
            in 0..3 -> "耐旱"
            in 4..6 -> "适中"
            in 7..10 -> "喜湿"
            else -> "未知"
        }

        return PlantEntity(
            id = id.toString(),
            name = finalName.replaceFirstChar { it.uppercase() },
            alias = scientificName,
            family = familyName,
            category = genusName,
            imageUrl = imageUrl ?: "",
            desc = description,
            feature = "生长形态: ${spec?.growthHabit ?: "直立"}. 平均成熟高度: $height cm",
            habit = "光照: $lightReq. 湿度需求: $humidityReq",
            care = "养护建议: 这种来自 $familyName 的植物毒性评级为 ${spec?.toxicity ?: "未知"}。建议保持环境通风，遵循其生长习性，干透后适量浇水。",
            isFavorite = false
        )
    }

    private fun PlantEntity.toPlant() = Plant(
        id, name, alias, family, category, imageUrl, desc, feature, habit, care, isFavorite
    )
}
