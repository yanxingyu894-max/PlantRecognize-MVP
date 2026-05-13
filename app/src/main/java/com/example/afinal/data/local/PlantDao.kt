package com.example.afinal.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO接口 —— 定义对数据库的所有操作
 * @Dao 标记这是一个数据访问对象，Room会自动生成实现类
 */
@Dao
interface PlantDao {

    /**
     * 插入单个植物
     * onConflict = REPLACE：如果ID已存在，替换旧数据
     * suspend = 挂起函数，在协程中运行，不阻塞主线程
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantEntity)

    /**
     * 批量插入植物
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPlants(plants: List<PlantEntity>)

    /**
     * 更新植物信息
     */
    @Update
    suspend fun updatePlant(plant: PlantEntity)

    /**
     * 删除植物
     */
    @Delete
    suspend fun deletePlant(plant: PlantEntity)

    /**
     * 根据ID查询单个植物（suspend，一次性查询）
     */
    @Query("SELECT * FROM plants WHERE id = :plantId")
    suspend fun getPlantById(plantId: String): PlantEntity?

    /**
     * 查询所有植物（返回Flow，数据库变化时自动通知UI）
     * 这是响应式编程的核心：UI订阅Flow，数据自动更新
     */
    @Query("SELECT * FROM plants ORDER BY name ASC")
    fun getAllPlants(): Flow<List<PlantEntity>>

    /**
     * 按分类查询植物
     */
    @Query("SELECT * FROM plants WHERE category = :category ORDER BY name ASC")
    fun getPlantsByCategory(category: String): Flow<List<PlantEntity>>

    /**
     * 搜索植物（名称模糊匹配）
     * SQL语法：'%' || :keyword || '%' 拼接成 %keyword%
     */
    @Query("SELECT * FROM plants WHERE name LIKE '%' || :keyword || '%' ORDER BY name ASC")
    suspend fun searchPlants(keyword: String): List<PlantEntity>

    /**
     * 清空所有植物数据
     */
    @Query("DELETE FROM plants")
    suspend fun clearAllPlants()

    /**
     * 获取植物数量
     */
    @Query("SELECT COUNT(*) FROM plants")
    suspend fun getPlantCount(): Int
}