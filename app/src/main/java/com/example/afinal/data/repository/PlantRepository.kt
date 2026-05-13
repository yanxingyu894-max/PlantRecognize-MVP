package com.example.afinal.data.repository

import com.example.afinal.data.local.PlantDao
import com.example.afinal.data.local.PlantEntity
import com.example.afinal.data.model.Plant
import com.example.afinal.data.model.PlantDto
import com.example.afinal.data.remote.PlantApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * PlantRepository —— 单一数据源（Single Source of Truth）
 *
 * 核心逻辑：
 * 1. UI层只订阅数据库的 Flow，实现响应式更新。
 * 2. 刷新操作从 API 获取数据并存入数据库，数据库变化后 UI 会自动刷新。
 */
class PlantRepository(
    private val plantDao: PlantDao,
    private val plantApiService: PlantApiService
) {

    // ==================== 1. 查询操作（从本地数据库读取） ====================

    /**
     * 获取所有植物 —— 返回 Flow，自动响应数据库变化
     */
    val allPlants: Flow<List<Plant>> = plantDao.getAllPlants().map { entities ->
        entities.map { it.toPlant() }
    }

    /**
     * 按分类获取植物
     */
    fun getPlantsByCategory(category: String): Flow<List<Plant>> =
        plantDao.getPlantsByCategory(category).map { entities ->
            entities.map { it.toPlant() }
        }

    /**
     * 根据ID获取单个植物详情（核心逻辑：本地优先）
     */
    suspend fun getPlantById(id: String): Plant? {
        // 先查本地
        val local = plantDao.getPlantById(id)
        if (local != null) return local.toPlant()

        // 本地没有，尝试从网络获取并缓存
        return try {
            val remoteDto = plantApiService.getPlantById(id)
            plantDao.insertPlant(remoteDto.toEntity())
            remoteDto.toPlant()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ==================== 2. 同步操作（从网络更新数据库） ====================

    /**
     * 刷新所有植物数据
     */
    suspend fun refreshPlants(): Result<List<Plant>> {
        return try {
            val dtos = plantApiService.getAllPlants()
            // 将网络对象转换为数据库实体并保存
            val entities = dtos.map { it.toEntity() }
            plantDao.insertAllPlants(entities)
            // 返回转换后的业务模型
            Result.success(dtos.map { it.toPlant() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 按分类刷新数据
     */
    suspend fun refreshPlantsByCategory(category: String): Result<List<Plant>> {
        return try {
            val dtos = plantApiService.getPlantsByCategory(category)
            plantDao.insertAllPlants(dtos.map { it.toEntity() })
            Result.success(dtos.map { it.toPlant() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 3. 内部转换方法（私有扩展函数） ====================

    /**
     * 数据库实体 (PlantEntity) -> 业务模型 (Plant)
     */
    private fun PlantEntity.toPlant(): Plant {
        return Plant(
            id = id,
            name = name,
            alias = alias,
            family = family,
            category = category,
            imageUrl = imageUrl,
            desc = desc,
            feature = feature,
            habit = habit,
            care = care
        )
    }

    /**
     * 网络模型 (PlantDto) -> 数据库实体 (PlantEntity)
     */
    private fun PlantDto.toEntity(): PlantEntity {
        return PlantEntity(
            id = id,
            name = name,
            alias = alias,
            family = family,
            category = category,
            imageUrl = imageUrl,
            desc = desc,
            feature = feature,
            habit = habit,
            care = care,
            lastUpdate = System.currentTimeMillis()
        )
    }

    /**
     * 网络模型 (PlantDto) -> 业务模型 (Plant)
     */
    private fun PlantDto.toPlant(): Plant {
        return Plant(
            id = id,
            name = name,
            alias = alias,
            family = family,
            category = category,
            imageUrl = imageUrl,
            desc = desc,
            feature = feature,
            habit = habit,
            care = care
        )
    }
}