package com.example.afinal.data.remote

import com.example.afinal.data.model.DeepSeekRequest
import com.example.afinal.data.model.DeepSeekResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DeepSeekApiService {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") token: String,
        @Body request: DeepSeekRequest
    ): DeepSeekResponse
}
