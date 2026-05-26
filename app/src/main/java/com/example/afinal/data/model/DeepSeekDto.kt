package com.example.afinal.data.model

import com.google.gson.annotations.SerializedName

data class DeepSeekRequest(
    val model: String = "deepseek-chat",
    val messages: List<DeepSeekMessage>,
    @SerializedName("response_format") val responseFormat: DeepSeekResponseFormat? = null
)

data class DeepSeekMessage(
    val role: String,
    val content: String
)

data class DeepSeekResponseFormat(
    val type: String
)

data class DeepSeekResponse(
    val choices: List<DeepSeekChoice>
)

data class DeepSeekChoice(
    val message: DeepSeekMessage
)

/**
 * Standardized JSON structure for plant data returned by DeepSeek
 */
data class DeepSeekPlantInfo(
    val commonName: String?,
    val scientificName: String?,
    val family: String?,
    val genus: String?,
    val description: String?,
    val careGuide: String?,
    val flowerColor: String?,
    val toxicity: String?,
    val edible: Boolean?,
    val nativeDistribution: String?
)
