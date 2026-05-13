package com.example.afinal.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * PlantDao单元测试 —— 测试数据库增删改查
 * 
 * 测试环境说明：
 * - 使用内存数据库（inMemoryDatabaseBuilder），测试结束后自动销毁
 * - runBlocking 用于在测试中以阻塞方式运行协程代码
 * - Flow需要使用.first()获取当前值
 */
@RunWith(AndroidJUnit4::class)
class PlantDaoTest {

    private lateinit var plantDao: PlantDao
    private lateinit var db: PlantDatabase

    /**
     * @Before —— 每个测试方法执行前运行
     * 创建内存数据库和DAO实例
     */
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // inMemoryDatabaseBuilder：内存数据库，进程结束即销毁，适合测试
        db = Room.inMemoryDatabaseBuilder(context, PlantDatabase::class.java)
            .allowMainThreadQueries()  // 测试时允许在主线程运行（简化测试）
            .build()
        plantDao = db.plantDao()
    }

    /**
     * @After —— 每个测试方法执行后运行
     * 关闭数据库，释放资源
     */
    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    // ========== 测试1：插入和查询（基本CRUD）==========

    @Test
    fun insertPlant_and_getById() = runBlocking {
        // Arrange（准备测试数据）
        val plant = PlantEntity(
            id = "test-001",
            name = "测试绿萝",
            alias = "测试别名",
            family = "测试科",
            category = "测试分类",
            imageUrl = "http://test.com/image.jpg",
            desc = "测试描述",
            feature = "测试特征",
            habit = "测试习性",
            care = "测试养护"
        )

        // Act（执行操作）
        plantDao.insertPlant(plant)

        // Assert（验证结果）
        val result = plantDao.getPlantById("test-001")
        
        assertNotNull("插入后应该能查到数据", result)
        assertEquals("名称应该匹配", "测试绿萝", result?.name)
        assertEquals("别名应该匹配", "测试别名", result?.alias)
        assertEquals("科属应该匹配", "测试科", result?.family)
    }

    // ========== 测试2：批量插入和查询所有 ==========

    @Test
    fun insertAllPlants_and_getAll() = runBlocking {
        // Arrange
        val plants = listOf(
            PlantEntity("1", "植物A", "别名A", "科A", "绿植", "", "描述A", "特征A", "习性A", "养护A"),
            PlantEntity("2", "植物B", "别名B", "科B", "花卉", "", "描述B", "特征B", "习性B", "养护B"),
            PlantEntity("3", "植物C", "别名C", "科C", "绿植", "", "描述C", "特征C", "习性C", "养护C")
        )
        
        // Act
        plantDao.insertAllPlants(plants)
        val result = plantDao.getAllPlants().first()  // Flow取第一个值

        // Assert
        assertEquals("应该有3条数据", 3, result.size)
        assertEquals("第一条应该是植物A", "植物A", result[0].name)
        assertEquals("第二条应该是植物B", "植物B", result[1].name)
    }

    // ========== 测试3：删除操作 ==========

    @Test
    fun deletePlant_shouldRemoveFromDb() = runBlocking {
        // Arrange
        val plant = PlantEntity(
            id = "delete-test",
            name = "待删除植物",
            alias = "",
            family = "",
            category = "",
            imageUrl = "",
            desc = "",
            feature = "",
            habit = "",
            care = ""
        )
        plantDao.insertPlant(plant)
        assertNotNull("插入后应该存在", plantDao.getPlantById("delete-test"))

        // Act
        plantDao.deletePlant(plant)

        // Assert
        val result = plantDao.getPlantById("delete-test")
        assertNull("删除后应该查不到", result)
    }

    // ========== 测试4：搜索功能 ==========

    @Test
    fun searchPlants_shouldReturnMatchingResults() = runBlocking {
        // Arrange
        val plants = listOf(
            PlantEntity("1", "玫瑰花", "", "", "", "", "", "", "", ""),
            PlantEntity("2", "月季花", "", "", "", "", "", "", "", ""),
            PlantEntity("3", "绿萝", "", "", "", "", "", "", "", ""),
            PlantEntity("4", "荷花", "", "", "", "", "", "", "", "")
        )
        plantDao.insertAllPlants(plants)

        // Act
        val results = plantDao.searchPlants("花")

        // Assert：应该返回3条（玫瑰、月季、荷花）
        assertEquals("搜索'花'应该返回3条", 3, results.size)
        assertTrue("结果应该包含玫瑰花", results.any { it.name == "玫瑰花" })
        assertTrue("结果应该包含月季花", results.any { it.name == "月季花" })
        assertTrue("结果应该包含荷花", results.any { it.name == "荷花" })
    }

    // ========== 测试5：更新操作 ==========

    @Test
    fun updatePlant_shouldModifyData() = runBlocking {
        // Arrange
        val plant = PlantEntity(
            id = "update-test",
            name = "旧名称",
            alias = "旧别名",
            family = "旧科",
            category = "旧分类",
            imageUrl = "",
            desc = "",
            feature = "",
            habit = "",
            care = ""
        )
        plantDao.insertPlant(plant)

        // Act
        val updatedPlant = plant.copy(
            name = "新名称",
            alias = "新别名",
            family = "新科"
        )
        plantDao.updatePlant(updatedPlant)

        // Assert
        val result = plantDao.getPlantById("update-test")
        assertEquals("名称应该更新", "新名称", result?.name)
        assertEquals("别名应该更新", "新别名", result?.alias)
        assertEquals("科属应该更新", "新科", result?.family)
    }

    // ========== 测试6：按分类查询 ==========

    @Test
    fun getPlantsByCategory_shouldFilterCorrectly() = runBlocking {
        // Arrange
        val plants = listOf(
            PlantEntity("1", "绿萝", "", "", "常见绿植", "", "", "", "", ""),
            PlantEntity("2", "月季", "", "", "户外花卉", "", "", "", "", ""),
            PlantEntity("3", "吊兰", "", "", "常见绿植", "", "", "", "", ""),
            PlantEntity("4", "茉莉", "", "", "户外花卉", "", "", "", "", "")
        )
        plantDao.insertAllPlants(plants)

        // Act
        val greenPlants = plantDao.getPlantsByCategory("常见绿植").first()
        val flowers = plantDao.getPlantsByCategory("户外花卉").first()

        // Assert
        assertEquals("绿植应该有2个", 2, greenPlants.size)
        assertEquals("花卉应该有2个", 2, flowers.size)
        assertTrue("绿植应该包含绿萝", greenPlants.any { it.name == "绿萝" })
        assertTrue("花卉应该包含月季", flowers.any { it.name == "月季" })
    }
}
