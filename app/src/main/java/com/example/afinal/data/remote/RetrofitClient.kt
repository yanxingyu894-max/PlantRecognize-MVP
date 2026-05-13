package com.example.afinal.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit单例客户端 —— 管理网络请求实例
 * object = Kotlin单例，全局只有一个实例
 */
object RetrofitClient {

    // 使用JSONPlaceholder作为公共Mock API（免费测试用）
    // 注意：必须以 / 结尾！
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    /**
     * 日志拦截器 —— 开发时查看请求/响应详情
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY  // 打印完整body
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    /**
     * 懒加载 by lazy —— 第一次使用时才创建，节省内存
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)                          // 基础URL，所有接口的公共前缀
            .client(okHttpClient)                        // 配置OkHttp客户端（带日志）
            .addConverterFactory(GsonConverterFactory.create())  // JSON自动解析为Kotlin对象
            .build()
    }

    /**
     * 创建API服务实例
     * create() 方法会自动生成 PlantApiService 接口的实现类
     */
    val plantApiService: PlantApiService by lazy {
        retrofit.create(PlantApiService::class.java)
    }
}