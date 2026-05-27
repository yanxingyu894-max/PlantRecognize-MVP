package com.example.afinal.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 植物数据访问接口（DAO）
 * 作用：定义所有针对数据库中 "plants" 表的操作方法，Room框架会自动生成该接口的实现类
 * 核心特性：支持多用户数据隔离，所有操作都会通过 ownerId（用户ID）筛选数据，确保不同用户数据不混淆
 * 注意：所有方法均为挂起函数（suspend）或返回Flow，避免阻塞主线程，符合Android异步编程规范
 */
@Dao // Room注解，标记这是数据访问接口
interface PlantDao {

    /**
     * 插入单条植物数据
     * @param plant 要插入的植物实体对象
     * 冲突策略：REPLACE（如果主键重复，覆盖原有数据）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantEntity)

    /**
     * 批量插入植物数据
     * @param plants 植物实体对象列表
     * 场景：一次性导入多个植物数据（如从网络接口批量获取后入库）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPlants(plants: List<PlantEntity>)

    /**
     * 更新单条植物数据
     * @param plant 要更新的植物实体（需包含完整主键信息：slug + ownerId）
     * 说明：会根据主键匹配数据，更新所有字段
     */
    @Update
    suspend fun updatePlant(plant: PlantEntity)

    /**
     * 删除单条植物数据
     * @param plant 要删除的植物实体（需包含完整主键信息）
     */
    @Delete
    suspend fun deletePlant(plant: PlantEntity)

    /**
     * 根据植物唯一标识（slug）和用户ID查询植物
     * @param slug 植物的唯一字符标识（类似网址别名，如 "rose"）
     * @param ownerId 用户ID（区分不同用户的植物数据）
     * @return 匹配的植物实体，无匹配则返回null
     */
    @Query("SELECT * FROM plants WHERE slug = :slug AND ownerId = :ownerId")
    suspend fun getPlantBySlug(slug: String, ownerId: String): PlantEntity?

    /**
     * 根据植物ID和用户ID查询植物
     * @param plantId 植物的数字/字符串ID
     * @param ownerId 用户ID
     * @return 匹配的植物实体，无匹配则返回null
     */
    @Query("SELECT * FROM plants WHERE id = :plantId AND ownerId = :ownerId")
    suspend fun getPlantById(plantId: String, ownerId: String): PlantEntity?

    /**
     * 获取指定用户的所有植物数据
     * @param ownerId 用户ID
     * @return Flow流（可观察的数据源），包含植物列表，按slug升序排列
     * 优势：数据变化时自动通知UI更新，无需手动刷新
     */
    @Query("SELECT * FROM plants WHERE ownerId = :ownerId ORDER BY slug ASC")
    fun getAllPlants(ownerId: String): Flow<List<PlantEntity>>

    /**
     * 按植物分类查询指定用户的植物
     * @param category 分类名称（如 "花卉"、"乔木"）
     * @param ownerId 用户ID
     * @return Flow流，包含该分类下的植物列表，按名称升序排列
     */
    @Query("SELECT * FROM plants WHERE category = :category AND ownerId = :ownerId ORDER BY name ASC")
    fun getPlantsByCategory(category: String, ownerId: String): Flow<List<PlantEntity>>

    /**
     * 按生长季节查询指定用户的植物
     * @param season 季节（如 "春季"、"夏季"）
     * @param ownerId 用户ID
     * @return Flow流，包含该季节的植物列表，按名称升序排列
     */
    @Query("SELECT * FROM plants WHERE season = :season AND ownerId = :ownerId ORDER BY name ASC")
    fun getPlantsBySeason(season: String, ownerId: String): Flow<List<PlantEntity>>

    /**
     * 按关键词搜索指定用户的植物（模糊匹配名称）
     * @param keyword 搜索关键词（如 "玫瑰"、"rose"）
     * @param ownerId 用户ID
     * @return 匹配的植物列表，按最后更新时间降序排列（最新修改的在前）
     * 说明：LIKE '%' || :keyword || '%' 表示关键词可出现在名称任意位置
     */
    @Query("SELECT * FROM plants WHERE name LIKE '%' || :keyword || '%' AND ownerId = :ownerId ORDER BY lastUpdate DESC")
    suspend fun searchPlants(keyword: String, ownerId: String): List<PlantEntity>

    /**
     * 随机获取指定用户的若干植物
     * @param limit 要获取的植物数量（如 limit=5 表示随机返回5个）
     * @param ownerId 用户ID
     * @return 随机筛选的植物列表
     * 场景：首页推荐、随机展示植物等
     */
    @Query("SELECT * FROM plants WHERE ownerId = :ownerId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomPlants(limit: Int, ownerId: String): List<PlantEntity>

    /**
     * 清空指定用户的所有植物数据
     * @param ownerId 用户ID
     * 注意：执行后该用户的植物数据将全部删除，不可恢复
     */
    @Query("DELETE FROM plants WHERE ownerId = :ownerId")
    suspend fun clearAllPlants(ownerId: String)

    /**
     * 统计指定用户的植物总数
     * @param ownerId 用户ID
     * @return 植物数量（整数）
     * 场景：个人中心展示 "我的植物总数" 等
     */
    @Query("SELECT COUNT(*) FROM plants WHERE ownerId = :ownerId")
    suspend fun getPlantCount(ownerId: String): Int

    /**
     * 局部更新植物的收藏状态（无需修改整个植物实体）
     * @param slug 植物唯一标识
     * @param isFavorite 收藏状态（true=收藏，false=取消收藏）
     * @param ownerId 用户ID
     * 优势：相比updatePlant更高效，仅修改单个字段
     */
    @Query("UPDATE plants SET isFavorite = :isFavorite WHERE slug = :slug AND ownerId = :ownerId")
    suspend fun updateFavoriteStatus(slug: String, isFavorite: Boolean, ownerId: String)

    /**
     * 获取指定用户收藏的所有植物
     * @param ownerId 用户ID
     * @return Flow流，包含收藏的植物列表，按最后更新时间降序排列
     */
    @Query("SELECT * FROM plants WHERE isFavorite = 1 AND ownerId = :ownerId ORDER BY lastUpdate DESC")
    fun getFavoritePlants(ownerId: String): Flow<List<PlantEntity>>

    /**
     * 获取指定用户收藏的所有植物slug
     * @param ownerId 用户ID
     * @return 收藏植物的slug列表
     * 场景：快速判断某个植物是否被收藏、批量处理收藏数据等
     */
    @Query("SELECT slug FROM plants WHERE isFavorite = 1 AND ownerId = :ownerId")
    suspend fun getFavoriteIds(ownerId: String): List<String>
}