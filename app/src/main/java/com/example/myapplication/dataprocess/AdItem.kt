package com.example.myapplication.dataprocess

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

// 表名：ad_items
@Entity(tableName = "ad_items")
@TypeConverters(ListConverter::class) // 支持 List<String> 标签存储
data class AdItem(
    @PrimaryKey val id: String,        // 唯一ID
    val title: String,                 // 标题
    val desc: String,                  // 简介（列表用）
    val detailContent: String,        // 详情内容（详情页用）
    val label: List<String>,           // 1~3个标签
    val imgUrl: String,                // 图片
    val videoUrl: String?,             // 视频（可为null）
    val type: Int,                     // 0大图 1小图 2视频
    val channelType: Int,              // 0频道1 1频道2 2频道3 → 核心！
    var like: Boolean = false,         // 点赞（布尔更合适）
    var star: Boolean = false,         // 收藏
    var likeCount: Int = 0,            // 点赞数
    var starCount: Int = 0,            // 收藏数
    val createTime: Long = System.currentTimeMillis() // 排序用
)

// 用户实体类
@Entity(tableName = "users")
@TypeConverters(ListConverter::class)
data class User(
    @PrimaryKey val username: String,  // 用户名称（作为主键）
    val password: String,              // 密码
    var browseHistory: List<String> = emptyList(), // 浏览历史（存储广告ID列表）
    var likeList: List<String> = emptyList(),      // 点赞列表
    var starList: List<String> = emptyList(),      // 收藏列表
    var preferenceTags: List<String> = emptyList() // 用户喜好标签
)