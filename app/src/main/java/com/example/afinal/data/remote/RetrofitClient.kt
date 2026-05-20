package com.example.afinal.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val TREFLE_BASE_URL = "https://trefle.io/"
    private const val PLANTNET_BASE_URL = "https://my-api.plantnet.org/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val trefleApiService: TrefleApiService by lazy {
        createRetrofit(TREFLE_BASE_URL).create(TrefleApiService::class.java)
    }

    val plantNetApiService: PlantNetApiService by lazy {
        createRetrofit(PLANTNET_BASE_URL).create(PlantNetApiService::class.java)
    }
    
    // 保留旧的，如果还有地方用的话，或者统一替换
    val plantApiService: PlantApiService by lazy {
        createRetrofit("https://jsonplaceholder.typicode.com/").create(PlantApiService::class.java)
    }
}
