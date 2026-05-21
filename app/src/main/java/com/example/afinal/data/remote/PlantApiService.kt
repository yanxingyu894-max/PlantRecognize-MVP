package com.example.afinal.data.remote

import com.example.afinal.data.model.PlantDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 本地模拟植物数据 API 接口
 * 作用：定义应用内模拟植物数据的所有网络请求方法
 * 采用 Retrofit 注解方式声明请求，无需编写实现类，由框架自动生成
 * 适用于本地调试、模拟接口、无真实后端时的数据交互
 */
interface PlantApiService {

    /**
     * GET 请求：获取全部植物列表
     * 请求路径：plants
     * 无请求参数
     * @return List<PlantDto> 植物基础数据列表，与网络返回 JSON 结构对齐
     */
    @GET("plants")
    suspend fun getAllPlants(): List<PlantDto>

    /**
     * GET 请求：根据植物 ID 获取单条植物详情
     * 路径参数：{id} 会被方法参数自动替换
     * 示例：传入 id = "1001" → 请求路径 plants/1001
     * @param id 植物唯一标识 ID
     * @return PlantDto 对应 ID 的植物基础数据
     */
    @GET("plants/{id}")
    suspend fun getPlantById(@Path("id") id: String): PlantDto

    /**
     * GET 请求：按分类筛选植物列表
     * 查询参数：自动拼接为 ?category=xxx
     * 示例：传入 category = "多肉" → 请求路径 plants?category=多肉
     * @param category 植物分类名称（如多肉、绿植、药用）
     * @return List<PlantDto> 该分类下的植物数据列表
     */
    @GET("plants")
    suspend fun getPlantsByCategory(@Query("category") category: String): List<PlantDto>

    /**
     * POST 请求：新增一条植物数据（模拟）
     * 请求体：将 PlantDto 对象转为 JSON 作为 body 发送
     * 适用于本地模拟添加、测试数据提交逻辑
     * @param plant 待添加的植物基础数据
     * @return PlantDto 添加成功后返回的完整数据
     */
    @POST("plants")
    suspend fun createPlant(@Body plant: PlantDto): PlantDto
}