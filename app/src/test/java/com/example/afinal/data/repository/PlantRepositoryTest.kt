package com.example.afinal.data.repository

import com.example.afinal.data.local.PlantDao
import com.example.afinal.data.local.PlantEntity
import com.example.afinal.data.model.PlantDto
import com.example.afinal.data.remote.PlantApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantRepositoryTest {

    private val mockDao: PlantDao = mockk(relaxed = true)
    private val mockApi: PlantApiService = mockk()

    @Test
    fun getAllPlants_shouldReturnMappedPlants() = runBlocking {
        val mockEntities = listOf(
            PlantEntity("1", "绿萝", "别名1", "天南星科", "绿植", "", "描述1", "特征1", "习性1", "养护1"),
            PlantEntity("2", "月季", "别名2", "蔷薇科", "花卉", "", "描述2", "特征2", "习性2", "养护2")
        )
        every { mockDao.getAllPlants() } returns flowOf(mockEntities)

        val repository = PlantRepository(mockDao, mockApi)
        val result = repository.allPlants.first()

        assertEquals(2, result.size)
        assertEquals("绿萝", result[0].name)
    }

    @Test
    fun refreshPlants_shouldFetchFromApi_andSaveToDb() = runBlocking {
        val mockDtos = listOf(
            PlantDto("1", "网络绿萝", "网别名", "网科", "绿植", "", "网描述", "网特征", "网习性", "网养护")
        )
        coEvery { mockApi.getAllPlants() } returns mockDtos
        
        val repository = PlantRepository(mockDao, mockApi)
        val result = repository.refreshPlants()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockDao.insertAllPlants(any()) }
    }

    @Test
    fun refreshPlants_whenNetworkFails_shouldReturnFailure() = runBlocking {
        coEvery { mockApi.getAllPlants() } throws Exception("网络连接失败")

        val repository = PlantRepository(mockDao, mockApi)
        val result = repository.refreshPlants()

        assertTrue(result.isFailure)
        assertEquals("网络连接失败", result.exceptionOrNull()?.message)
    }

    @Test
    fun getPlantById_whenLocalExists_shouldReturnWithoutNetwork() = runBlocking {
        val localEntity = PlantEntity("1", "本地绿萝", "", "", "", "", "", "", "", "")
        coEvery { mockDao.getPlantById("1") } returns localEntity
        
        val repository = PlantRepository(mockDao, mockApi)
        val result = repository.getPlantById("1")

        assertNotNull(result)
        assertEquals("本地绿萝", result?.name)
        coVerify(exactly = 0) { mockApi.getPlantById(any()) }
    }

    @Test
    fun getPlantById_whenLocalMissing_shouldFetchFromNetwork() = runBlocking {
        coEvery { mockDao.getPlantById("2") } returns null
        val remoteDto = PlantDto("2", "网络月季", "", "", "", "", "", "", "", "")
        coEvery { mockApi.getPlantById("2") } returns remoteDto
        
        val repository = PlantRepository(mockDao, mockApi)
        val result = repository.getPlantById("2")

        assertNotNull(result)
        assertEquals("网络月季", result?.name)
        coVerify { mockDao.insertPlant(any()) }
    }
}
