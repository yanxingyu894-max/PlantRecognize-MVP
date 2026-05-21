package com.example.afinal.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 网络客户端单例
 * 作用：统一管理应用所有网络请求的 Retrofit 实例、OkHttp 配置、BaseURL
 * 采用单例模式，保证全局唯一网络客户端，避免重复创建资源
 * 支持 Trefle 植物库、PlantNet 识别双服务接口创建
 */
object RetrofitClient {

    /**
     * Trefle 植物数据库基础地址
     * 用于获取海量植物公开数据、搜索、详情查询
     */
    private const val TREFLE_BASE_URL = "https://trefle.io/"

    /**
     * PlantNet 植物识别基础地址
     * 用于图像上传、AI 识别植物种类
     */
    private const val PLANTNET_BASE_URL = "https://my-api.plantnet.org/"

    /**
     * 日志拦截器：打印完整请求/响应信息
     * 级别 BODY：输出请求头、请求体、响应头、响应体
     * 仅在 Debug 环境开启，Release 应关闭以提升性能与安全
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttp 客户端配置
     * 统一设置超时、拦截器、连接池等全局网络策略
     * 连接超时：30s
     * 读取超时：30s
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 通用 Retrofit 创建方法
     * 根据不同 BaseURL 生成对应服务实例
     * @param baseUrl 目标服务基础地址
     * @return Retrofit 配置完成的实例
     */
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Trefle 植物库 API 服务实例
     * 懒加载：首次使用时创建，全局单例
     */
    val trefleApiService: TrefleApiService by lazy {
        createRetrofit(TREFLE_BASE_URL).create(TrefleApiService::class.java)
    }

    /**
     * PlantNet 植物识别 API 服务实例
     * 懒加载：首次使用时创建，全局单例
     */
    val plantNetApiService: PlantNetApiService by lazy {
        createRetrofit(PLANTNET_BASE_URL).create(PlantNetApiService::class.java)
    }
}