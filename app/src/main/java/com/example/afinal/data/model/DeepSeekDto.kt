package com.example.afinal.data.model

import com.google.gson.annotations.SerializedName

/**
 * DeepSeek大模型的请求数据模型
 * 作用：构造发送给DeepSeek API的请求参数，用于调用AI生成/补充植物信息
 * DeepSeek说明：DeepSeek是一个大语言模型（类似ChatGPT），可通过API调用生成文本
 */
data class DeepSeekRequest(
    // 指定调用的模型名称（固定为"deepseek-chat"，代表对话型模型）
    val model: String = "deepseek-chat",
    // 对话消息列表（包含用户提问、AI回复等，模拟聊天场景）
    val messages: List<DeepSeekMessage>,
    // 响应格式要求（比如指定AI返回JSON格式，而非纯文本）
    @SerializedName("response_format") val responseFormat: DeepSeekResponseFormat? = null
)

/**
 * DeepSeek对话消息体
 * 每一条消息包含"角色"和"内容"，模拟人类和AI的对话
 */
data class DeepSeekMessage(
    // 消息角色（"user"=用户/提问者，"assistant"=AI/回答者，"system"=系统提示）
    val role: String,
    // 消息内容（比如用户提问："帮我补充这个植物的养护信息"）
    val content: String
)

/**
 * DeepSeek响应格式要求
 * 用于指定AI返回的内容格式，避免AI返回非结构化文本
 */
data class DeepSeekResponseFormat(
    // 格式类型（比如"json_object"代表要求AI返回JSON对象）
    val type: String
)

/**
 * DeepSeek大模型的响应数据模型
 * 作用：解析DeepSeek API返回的AI回答结果
 */
data class DeepSeekResponse(
    // AI生成的结果列表（通常只有1个元素）
    val choices: List<DeepSeekChoice>
)

/**
 * DeepSeek响应结果项
 * 包装单条AI回答的消息体
 */
data class DeepSeekChoice(
    // AI生成的回答消息（role=assistant，content=回答内容）
    val message: DeepSeekMessage
)

/**
 * DeepSeek返回的植物信息标准化JSON结构
 * 作用：定义AI返回植物信息的固定格式，方便解析为实体类
 * 设计说明：整合多个数据源的植物字段，让AI按照该结构返回，保证数据一致性
 */
data class DeepSeekPlantInfo(
    // 植物通用名称（俗名，比如"玫瑰"）
    val commonName: String?,
    // 植物科学名称（学名，比如"Rosa rugosa"）
    val scientificName: String?,
    // 植物所属科（比如"蔷薇科"）
    val family: String?,
    // 植物所属属（比如"蔷薇属"）
    val genus: String?,
    // 植物详细描述（比基础desc更丰富的介绍）
    val description: String?,
    // 植物养护指南（更详细的养护方法）
    val careGuide: String?,
    // 花色（比如"红色、粉色、白色"）
    val flowerColor: String?,
    // 毒性说明（比如"无毒"、"轻微有毒"）
    val toxicity: String?,
    // 是否可食用（true=可食用，false=不可食用）
    val edible: Boolean?,
    // 原生分布地（比如"中国华北、西北，日本、朝鲜"）
    val nativeDistribution: String?
)