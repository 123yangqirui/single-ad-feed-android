package com.example.myapplication.dialog

import com.example.myapplication.dataprocess.AdItem

/**
 * 消息类型枚举
 */
enum class MessageType {
    USER,          // 用户消息
    AI,            // AI 回复
    AI_WITH_SEARCH // AI 回复（包含搜索结果）
}

/**
 * 消息数据类
 */
data class Message(
    val id: String,
    val type: MessageType,
    val content: String,
    val searchResults: List<AdItem> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
