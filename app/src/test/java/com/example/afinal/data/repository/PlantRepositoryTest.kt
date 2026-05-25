/*
* 该测试文件已失效（数据库经迭代已变化）
* */


package com.example.afinal.data.repository
//
//import com.example.afinal.data.local.PlantDao
//import com.example.afinal.data.local.PlantEntity
//import com.example.afinal.data.remote.PlantNetApiService
//import com.example.afinal.data.remote.TrefleApiService
//import io.mockk.every
//import io.mockk.mockk
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.runBlocking
//import org.junit.Assert.assertEquals
//import org.junit.Test
//
//class PlantRepositoryTest {
//
//    private val mockDao: PlantDao = mockk(relaxed = true)
//    private val mockTrefle: TrefleApiService = mockk()
//    private val mockPlantNet: PlantNetApiService = mockk()
//
//    @Test
//    fun getAllPlants_shouldReturnMappedPlants() = runBlocking {
//        val mockEntities = listOf(
//            PlantEntity("1", "绿萝", "别名1", "天南星科", "绿植", "", "描述1", "特征1", "习性1", "养护1"),
//            PlantEntity("2", "月季", "别名2", "蔷薇科", "花卉", "", "描述2", "特征2", "习性2", "养护2")
//        )
//        every { mockDao.getAllPlants() } returns flowOf(mockEntities)
//
//        val repository = PlantRepository(mockDao, mockTrefle, mockPlantNet)
//        val result = repository.allPlants.first()
//
//        assertEquals(2, result.size)
//        assertEquals("绿萝", result[0].name)
//    }
//}
