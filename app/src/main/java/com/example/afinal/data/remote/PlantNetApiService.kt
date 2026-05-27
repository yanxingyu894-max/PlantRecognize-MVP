package com.example.afinal.data.remote

import com.example.afinal.data.model.PlantNetResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * PlantNet 植物图片识别 API 接口定义类
 * 【核心功能】：对接 PlantNet 第三方服务，实现"上传植物图片 → AI 识别植物种类"的核心功能
 * 【技术特点】：采用 Multipart 表单上传（支持文件+普通参数同时提交）
 * 【返回结果】：包含识别出的植物种类、匹配度（置信度）、学名、通用名等信息
 * 【适用场景】：App 中"拍照识植物"功能的核心网络支撑
 */
interface PlantNetApiService {

    /**
     * POST请求：执行植物图片识别
     * 【接口用途】：上传植物图片（如叶子/花朵照片），让 AI 识别具体是什么植物
     * 【请求方式】：Multipart 多部分表单提交（适合同时上传文件和普通参数）
     * 【请求地址】：拼接后 → https://my-api.plantnet.org/v2/identify/all
     * 【请求参数详细说明】：
     *   1. images：图片文件列表（支持上传多张图片，提高识别准确率）
     *      - 封装方式：需转为 MultipartBody.Part 类型（OkHttp 规定的文件上传格式）
     *      - 示例：拍照后将图片文件转为该类型，传入接口
     *   2. organs：植物器官类型列表（指定图片拍的是植物哪个部位）
     *      - 可选值：flower（花）、leaf（叶）、fruit（果实）、bark（树皮）等
     *      - 作用：帮助 AI 更精准识别（比如拍的是叶子，指定 leaf 能减少误判）
     *   3. apiKey：PlantNet 平台的授权密钥（必填），需提前在 PlantNet 官网申请
     *   4. includeRelatedImages：是否返回相关参考图片（默认 false，节省流量）
     *      - true：返回识别结果的同时，返回该植物的参考图片链接
     *      - false：仅返回识别结果
     *   5. noReject：是否过滤低置信度结果（默认 true）
     *      - true：即使匹配度低，也返回所有可能的结果
     *      - false：只返回高置信度的结果（过滤掉匹配度低的）
     *   6. lang：返回结果的语言（默认 en 英语），可改为 zh（中文）（需 PlantNet 支持）
     * 【返回值】：PlantNetResponse 对象，包含：
     *   - 识别出的植物种类列表（按置信度从高到低排序）
     *   - 每个种类的置信度（0-1，越接近1越准确）
     *   - 植物的学名、通用名、分类信息等
     */
    @Multipart
    @POST("v2/identify/all")
    suspend fun identifyPlant(
        @Part images: @JvmSuppressWildcards List<MultipartBody.Part>,

        @Part organs: @JvmSuppressWildcards List<MultipartBody.Part>,

        @Query("api-key") apiKey: String,

        @Query("include-related-images") includeRelatedImages: Boolean = false,

        @Query("no-reject") noReject: Boolean = true,

        @Query("lang") lang: String = "en"

    ): PlantNetResponse
}