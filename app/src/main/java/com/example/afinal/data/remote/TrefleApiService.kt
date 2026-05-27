package com.example.afinal.data.remote

import com.example.afinal.data.model.TrefleDetailResponse
import com.example.afinal.data.model.TrefleResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Trefle 公共植物数据库 API 接口定义类
 * 【功能说明】：对接全球权威的 Trefle 开放植物数据库平台，获取标准化的植物数据
 * 【适用场景】：App 中展示海量植物基础信息、按关键词搜索植物、查看植物详细资料
 * 【权限要求】：所有请求必须携带 Trefle 平台申请的授权 Token，否则接口会返回权限错误
 * 【技术实现】：基于 Retrofit 框架，通过注解声明 HTTP 请求，无需手动编写网络请求代码
 */
interface TrefleApiService {

    /**
     * GET请求：获取分页的植物列表
     * 【接口用途】：加载植物列表数据，支持分页加载（避免一次性加载过多数据导致卡顿）
     * 【请求地址】：拼接在 Trefle 基础地址后 → https://trefle.io/api/v1/plants
     * 【请求参数说明】：
     *   - token：Trefle 平台的授权令牌（必填），需提前在 Trefle 官网注册申请
     *   - page：页码，默认第1页，用于分页加载下一页数据
     *   - pageSize：每页展示的植物数量，默认20条，可根据需求调整（如改为10条/页）
     * 【返回值】：TrefleResponse 对象，包含当前页的植物列表 + 分页元数据（总页数、总条数等）
     */
    @GET("api/v1/plants")
    suspend fun getPlants(
        @Query("token") token: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): TrefleResponse

    /**
     * GET请求：按关键词搜索植物
     * 【接口用途】：实现植物搜索功能，支持模糊匹配（比如搜"玫瑰"能找到相关的所有玫瑰品种）
     * 【匹配规则】：会同时匹配植物的通用名（如"月季"）和学名（如"Rosa chinensis"）
     * 【请求地址】：https://trefle.io/api/v1/plants/search
     * 【请求参数说明】：
     *   - token：授权令牌（必填）
     *   - q：搜索关键词（比如"多肉"、"apple"），参数名"q"是 Trefle 接口规定的固定字段
     * 【返回值】：TrefleResponse 对象，包含所有匹配关键词的植物列表
     */
    @GET("api/v1/plants/search")
    suspend fun searchPlants(
        @Query("token") token: String,
        @Query("q") query: String
    ): TrefleResponse

    /**
     * GET请求：根据植物 Slug 获取完整详情
     * 【接口用途】：查看某一株植物的完整信息（比列表接口返回的字段更多，如生长环境、养护方法等）
     * 【Slug 说明】：是植物的 URL 友好型标识（比如"rosa-chinensis"对应月季花），
     *              特点是无特殊字符、全小写，适合放在 URL 中传递
     * 【请求地址】：动态拼接 Slug → https://trefle.io/api/v1/plants/[slug]（例：rosa-chinensis）
     * 【请求参数说明】：
     *   - slug：植物的唯一 Slug 标识（路径参数，嵌在 URL 中）
     *   - token：授权令牌（查询参数，拼在 URL 末尾 → ?token=xxx）
     * 【返回值】：TrefleDetailResponse 对象，包含植物的所有详细字段
     */
    @GET("api/v1/plants/{slug}")
    suspend fun getPlantBySlug(
        @Path("slug") slug: String,
        @Query("token") token: String
    ): TrefleDetailResponse
}