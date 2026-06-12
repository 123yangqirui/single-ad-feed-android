package com.example.myapplication.dialog

import com.google.gson.annotations.SerializedName

/**
 * AI 响应数据类
 */
data class AiResponse(
    @SerializedName("reply")
    val reply: String,
    @SerializedName("search_method")
    val searchMethod: String,
    @SerializedName("tags")
    val tags: List<String>
)

/**
 * DeepSeek API 请求体
 */
data class DeepSeekRequest(
    @SerializedName("model")
    val model: String = "deepseek-chat",
    @SerializedName("messages")
    val messages: List<MessageData>
)

/**
 * 消息数据
 */
data class MessageData(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)

/**
 * DeepSeek API 响应体
 */
data class DeepSeekResponse(
    @SerializedName("choices")
    val choices: List<Choice>,
    @SerializedName("usage")
    val usage: Usage
)

data class Choice(
    @SerializedName("message")
    val message: MessageData
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)
