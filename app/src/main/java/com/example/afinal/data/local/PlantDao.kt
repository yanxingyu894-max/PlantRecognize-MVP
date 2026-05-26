package com.example.afinal.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 植物数据访问接口，定义了所有针对 "plants" 表的数据库操作。
 * 已更新以支持多用户数据隔离。
 */
@Dao
interface PlantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPlants(plants: List<PlantEntity>)

    @Update
    suspend fun updatePlant(plant: PlantEntity)

    @Delete
    suspend fun deletePlant(plant: PlantEntity)

    @Query("SELECT * FROM plants WHERE slug = :slug AND ownerId = :ownerId")
    suspend fun getPlantBySlug(slug: String, ownerId: String): PlantEntity?

    @Query("SELECT * FROM plants WHERE id = :plantId AND ownerId = :ownerId")
    suspend fun getPlantById(plantId: String, ownerId: String): PlantEntity?

    /**
     * 按最后更新时间降序获取所有植物。
     * 仅获取属于当前 ownerId 的数据。
     */
    @Query("SELECT * FROM plants WHERE ownerId = :ownerId ORDER BY slug ASC")
    fun getAllPlants(ownerId: String): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE category = :category AND ownerId = :ownerId ORDER BY name ASC")
    fun getPlantsByCategory(category: String, ownerId: String): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE season = :season AND ownerId = :ownerId ORDER BY name ASC")
    fun getPlantsBySeason(season: String, ownerId: String): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE name LIKE '%' || :keyword || '%' AND ownerId = :ownerId ORDER BY lastUpdate DESC")
    suspend fun searchPlants(keyword: String, ownerId: String): List<PlantEntity>

    @Query("SELECT * FROM plants WHERE ownerId = :ownerId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomPlants(limit: Int, ownerId: String): List<PlantEntity>

    @Query("DELETE FROM plants WHERE ownerId = :ownerId")
    suspend fun clearAllPlants(ownerId: String)

    @Query("SELECT COUNT(*) FROM plants WHERE ownerId = :ownerId")
    suspend fun getPlantCount(ownerId: String): Int

    /** 局部更新：仅修改收藏状态 */
    @Query("UPDATE plants SET isFavorite = :isFavorite WHERE slug = :slug AND ownerId = :ownerId")
    suspend fun updateFavoriteStatus(slug: String, isFavorite: Boolean, ownerId: String)

    @Query("SELECT * FROM plants WHERE isFavorite = 1 AND ownerId = :ownerId ORDER BY lastUpdate DESC")
    fun getFavoritePlants(ownerId: String): Flow<List<PlantEntity>>

    @Query("SELECT slug FROM plants WHERE isFavorite = 1 AND ownerId = :ownerId")
    suspend fun getFavoriteIds(ownerId: String): List<String>
}
