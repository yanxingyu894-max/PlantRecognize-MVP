package com.example.afinal.data.remote

import com.example.afinal.data.model.PlantDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 本地模拟植物数据 API 接口定义类
 * 【核心作用】：无真实后端服务器时，模拟植物数据的网络请求（仅用于本地调试、测试）
 * 【使用场景】：开发初期后端接口未完成、离线调试功能、测试数据提交逻辑
 * 【技术实现】：基于 Retrofit 注解声明请求，框架会自动生成请求实现类，无需手动写网络请求
 * 【数据对齐】：返回的 PlantDto 类结构与真实网络接口的 JSON 数据完全一致，便于后续切换到真实接口
 */
interface PlantApiService {

    /**
     * GET请求：获取全部植物列表
     * 【接口用途】：加载本地模拟的所有植物基础数据（名称、分类、图片地址等）
     * 【请求地址】：模拟路径 → plants（无域名，仅本地模拟）
     * 【请求参数】：无，直接请求即可获取所有数据
     * 【返回值】：植物数据列表（List<PlantDto>），每个 PlantDto 对应一株植物的基础信息
     */
    @GET("plants")
    suspend fun getAllPlants(): List<PlantDto>

    /**
     * GET请求：根据植物 ID 获取单条详情
     * 【接口用途】：查看某一株植物的详细信息（比如点击列表中的植物，进入详情页时调用）
     * 【路径参数说明】：{id} 是动态占位符，会被方法参数 id 的值替换
     * 【示例】：调用 getPlantById("1001") → 实际请求路径为 plants/1001
     * 【参数说明】：id 是植物的唯一标识（比如"1001"、"2002"），确保每个植物 ID 不重复
     * 【返回值】：对应 ID 的植物完整数据（PlantDto）
     */
    @GET("plants/{id}")
    suspend fun getPlantById(@Path("id") id: String): PlantDto

    /**
     * GET请求：按分类筛选植物列表
     * 【接口用途】：实现植物分类筛选功能（比如筛选出"多肉"、"绿植"、"药用"类植物）
     * 【查询参数说明】：category 参数会自动拼接到请求路径后，格式为 ?category=xxx
     * 【示例】：调用 getPlantsByCategory("多肉") → 请求路径 plants?category=多肉
     * 【参数说明】：category 是分类名称（字符串），需与本地模拟数据中的分类字段一致
     * 【返回值】：该分类下的所有植物数据列表（List<PlantDto>）
     */
    @GET("plants")
    suspend fun getPlantsByCategory(@Query("category") category: String): List<PlantDto>

    /**
     * POST请求：新增一条植物数据（模拟）
     * 【接口用途】：测试"添加植物"功能的逻辑（比如用户手动录入新植物信息后提交）
     * 【请求体说明】：将 PlantDto 对象自动转为 JSON 格式，作为请求体发送
     * 【模拟逻辑】：本地不会真正保存数据，仅模拟"提交成功"并返回传入的植物数据
     * 【参数说明】：plant 是待添加的植物数据（包含名称、分类、描述等字段）
     * 【返回值】：添加成功后的植物数据（与传入的 plant 一致，模拟后端返回的结果）
     */
    @POST("plants")
    suspend fun createPlant(@Body plant: PlantDto): PlantDto
}