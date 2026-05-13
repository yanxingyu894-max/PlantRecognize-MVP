package com.example.afinal.data.remote
import com.example.afinal.data.model.PlantDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API接口 —— 定义所有网络请求方法
 * 你只需要写接口+注解，Retrofit自动生成实现类
 */
interface PlantApiService {
    /**
     * GET请求 —— 获取所有植物列表
     * @GET 标记这是一个GET请求，括号内是API路径（相对路径）
     */
    @GET("plants")
    suspend fun getAllPlants(): List<PlantDto>

    /**
     * GET请求 —— 根据ID获取单个植物详情
     * @Path("id") 将URL中的{id}替换为传入的参数值
     * 例如：传入 "123" → 请求路径变成 plants/123
     */
    @GET("plants/{id}")
    suspend fun getPlantById(@Path("id") id: String): PlantDto

    /**
     * GET请求 —— 按分类搜索植物
     * @Query("category") 会生成 ?category=xxx 的查询参数
     * 例如：传入 "绿植" → 请求路径变成 plants?category=绿植
     */
    @GET("plants")
    suspend fun getPlantsByCategory(@Query("category") category: String): List<PlantDto>

    /**
     * POST请求 —— 添加新植物（模拟）
     * @Body 将对象转成JSON字符串作为请求体发送
     */
    @POST("plants")
    suspend fun createPlant(@Body plant: PlantDto): PlantDto
}