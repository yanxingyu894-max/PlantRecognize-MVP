package com.example.afinal.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 植物数据访问接口，定义了所有针对 "plants" 表的数据库操作。
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

    @Query("SELECT * FROM plants WHERE slug = :slug")
    suspend fun getPlantBySlug(slug: String): PlantEntity?

    @Query("SELECT * FROM plants WHERE id = :plantId")
    suspend fun getPlantById(plantId: String): PlantEntity?

    /**
     * 按最后更新时间降序获取所有植物，适用于首页展示（新数据置顶）。
     * 使用 Flow 实现自动响应式更新。
     */
    @Query("SELECT * FROM plants ORDER BY slug ASC")
    fun getAllPlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE category = :category ORDER BY name ASC")
    fun getPlantsByCategory(category: String): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE season = :season ORDER BY name ASC")
    fun getPlantsBySeason(season: String): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE name LIKE '%' || :keyword || '%' ORDER BY lastUpdate DESC")
    suspend fun searchPlants(keyword: String): List<PlantEntity>

    @Query("SELECT * FROM plants ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomPlants(limit: Int): List<PlantEntity>

    @Query("DELETE FROM plants")
    suspend fun clearAllPlants()

    @Query("SELECT COUNT(*) FROM plants")
    suspend fun getPlantCount(): Int

    /** 局部更新：仅修改收藏状态 */
    @Query("UPDATE plants SET isFavorite = :isFavorite WHERE slug = :slug")
    suspend fun updateFavoriteStatus(slug: String, isFavorite: Boolean)

    @Query("SELECT * FROM plants WHERE isFavorite = 1 ORDER BY lastUpdate DESC")
    fun getFavoritePlants(): Flow<List<PlantEntity>>

    @Query("SELECT slug FROM plants WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<String>
}
