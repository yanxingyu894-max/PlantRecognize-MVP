package com.example.afinal.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 网络客户端单例类
 * 【核心作用】：统一管理 App 所有网络请求的配置（基础地址、超时时间、日志打印等）
 * 【单例模式】：全局只有一个实例，避免重复创建 Retrofit/OkHttp 对象（节省内存、避免资源浪费）
 * 【管理的服务】：Trefle 植物库、PlantNet 识别、DeepSeek AI 三个第三方接口
 * 【核心依赖】：
 *   - Retrofit：处理 HTTP 请求的核心框架（将接口注解转为实际网络请求）
 *   - OkHttp：底层网络请求实现（处理连接、超时、拦截器等）
 *   - GsonConverterFactory：自动将 JSON 响应转为 Kotlin 对象（无需手动解析 JSON）
 */
object RetrofitClient {

    /**
     * Trefle 植物数据库基础地址
     * 【说明】：所有 Trefle 接口的请求地址都基于此拼接（比如 api/v1/plants → 完整地址：https://trefle.io/api/v1/plants）
     * 【用途】：获取海量公开的植物数据（列表、搜索、详情）
     */
    private const val TREFLE_BASE_URL = "https://trefle.io/"

    /**
     * PlantNet 植物识别基础地址
     * 【说明】：植物图片识别接口的基础地址，拼接后如：https://my-api.plantnet.org/v2/identify/all
     * 【用途】：上传植物图片，调用 AI 识别植物种类
     */
    private const val PLANTNET_BASE_URL = "https://my-api.plantnet.org/"

    /**
     * DeepSeek API 基础地址
     * 【说明】：AI 对话接口的基础地址，拼接后如：https://api.deepseek.com/chat/completions
     * 【用途】：调用 DeepSeek 大模型实现智能问答
     */
    private const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/"

    /**
     * 日志拦截器：打印网络请求/响应的详细信息
     * 【作用】：开发阶段调试用，能看到请求头、请求体、响应头、响应体（方便排查接口问题）
     * 【级别说明】：Level.BODY 是最详细的级别，会打印所有内容；Release 版本建议关闭（避免泄露敏感信息、提升性能）
     * 【使用方式】：添加到 OkHttp 客户端中，自动拦截所有网络请求
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttp 客户端配置
     * 【核心作用】：设置网络请求的全局规则（超时时间、拦截器、连接池等）
     * 【超时设置】：
     *   - 连接超时：30秒（请求连接服务器的最长等待时间，超时则判定为网络异常）
     *   - 读取超时：30秒（连接成功后，等待服务器返回数据的最长时间）
     * 【拦截器】：添加日志拦截器，打印请求/响应日志
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 通用 Retrofit 创建方法
     * 【作用】：根据不同的基础地址，创建对应的 Retrofit 实例（避免重复写配置代码）
     * 【参数说明】：baseUrl 是第三方服务的基础地址（如 Trefle/PlantNet/DeepSeek）
     * 【配置项】：
     *   - baseUrl：设置请求的基础地址
     *   - client：绑定上面配置的 OkHttp 客户端（带日志、超时设置）
     *   - GsonConverterFactory：自动将 JSON 转为 Kotlin 对象（比如把接口返回的 JSON 转成 TrefleResponse）
     * 【返回值】：配置完成的 Retrofit 实例
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
     * 【懒加载】：首次使用时才创建（比如调用 getPlants() 时），避免启动 App 就创建（节省内存）
     * 【单例特性】：全局唯一，所有地方调用的都是同一个实例
     * 【创建方式】：通过 Retrofit 实例的 create 方法，将 TrefleApiService 接口转为可调用的实例
     */
    val trefleApiService: TrefleApiService by lazy {
        createRetrofit(TREFLE_BASE_URL).create(TrefleApiService::class.java)
    }

    /**
     * PlantNet 植物识别 API 服务实例
     * 【懒加载】：首次调用 identifyPlant() 时创建
     * 【用途】：调用植物图片识别接口
     */
    val plantNetApiService: PlantNetApiService by lazy {
        createRetrofit(PLANTNET_BASE_URL).create(PlantNetApiService::class.java)
    }

    /**
     * DeepSeek API 服务实例
     * 【懒加载】：首次调用 chatCompletions() 时创建
     * 【用途】：调用 AI 对话接口
     */
    val deepSeekApiService: DeepSeekApiService by lazy {
        createRetrofit(DEEPSEEK_BASE_URL).create(DeepSeekApiService::class.java)
    }
}