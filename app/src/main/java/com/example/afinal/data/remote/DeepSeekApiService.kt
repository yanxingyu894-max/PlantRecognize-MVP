package com.example.afinal.data.remote

import com.example.afinal.data.model.DeepSeekRequest
import com.example.afinal.data.model.DeepSeekResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * DeepSeek AI 大模型 API 接口定义类
 * 【功能说明】：对接 DeepSeek 智能对话接口，实现 AI 问答功能（比如让 AI 解答植物养护问题）
 * 【技术特点】：采用 POST 请求 + JSON 请求体，支持传递复杂的对话参数（如提问内容、上下文等）
 * 【权限要求】：请求头必须携带授权 Token，否则接口拒绝访问
 */
interface DeepSeekApiService {
    /**
     * POST请求：调用 DeepSeek 对话接口
     * 【接口用途】：向 AI 发送提问，获取智能回答（例："多肉植物怎么浇水？" → 返回养护建议）
     * 【请求地址】：https://api.deepseek.com/chat/completions
     * 【请求参数说明】：
     *   - token：授权令牌，格式为 "Bearer + 密钥"（例：Bearer sk-xxxxxx），需在 DeepSeek 平台申请
     *   - request：请求体（JSON 格式），包含提问内容、对话历史、模型参数等（封装在 DeepSeekRequest 类中）
     * 【返回值】：DeepSeekResponse 对象，包含 AI 生成的回答内容、回答状态等
     */
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") token: String,
        @Body request: DeepSeekRequest
    ): DeepSeekResponse
}