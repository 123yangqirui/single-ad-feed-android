package com.example.myapplication.dialog

import android.util.Log
import com.example.myapplication.dataprocess.AdItem
import com.example.myapplication.dataprocess.AppDatabase

/**
 * 数据库检索服务
 */
object SearchService {

    private const val TAG = "SearchService"

    /**
     * 通过单个标签检索
     */
    fun searchByTag(tag: String): List<AdItem> {
        return try {
            val adItemDao = AppDatabase.instance.adItemDao()
            val allItems = adItemDao.getAllItemsByChannel(0).toMutableList()
            allItems.addAll(adItemDao.getAllItemsByChannel(1))
            allItems.addAll(adItemDao.getAllItemsByChannel(2))
            val results = allItems.filter { it.label.any { label -> label.contains(tag, ignoreCase = true) } }
            Log.d(TAG, "searchByTag: tag=$tag, found ${results.size} items")
            results
        } catch (e: Exception) {
            Log.e(TAG, "searchByTag error: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 通过多个标签检索
     */
    fun searchByMultipleTags(tags: List<String>): List<AdItem> {
        return try {
            val adItemDao = AppDatabase.instance.adItemDao()
            val allItems = adItemDao.getAllItemsByChannel(0).toMutableList()
            allItems.addAll(adItemDao.getAllItemsByChannel(1))
            allItems.addAll(adItemDao.getAllItemsByChannel(2))
            val results = allItems.filter { adItem ->
                tags.any { tag -> adItem.label.any { label -> label.contains(tag, ignoreCase = true) } }
            }
            Log.d(TAG, "searchByMultipleTags: tags=$tags, found ${results.size} items")
            results
        } catch (e: Exception) {
            Log.e(TAG, "searchByMultipleTags error: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 通过频道类型检索
     */
    fun searchByChannel(channel: Int): List<AdItem> {
        return try {
            val adItemDao = AppDatabase.instance.adItemDao()
            val results = adItemDao.getAllItemsByChannel(channel).take(10)
            Log.d(TAG, "searchByChannel: channel=$channel, found ${results.size} items")
            results
        } catch (e: Exception) {
            Log.e(TAG, "searchByChannel error: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 通过关键词检索（标题和描述）
     */
    fun searchByKeyword(keyword: String): List<AdItem> {
        return try {
            val adItemDao = AppDatabase.instance.adItemDao()
            val allItems = adItemDao.getAllItemsByChannel(0).toMutableList()
            allItems.addAll(adItemDao.getAllItemsByChannel(1))
            allItems.addAll(adItemDao.getAllItemsByChannel(2))
            val results = allItems.filter {
                it.title.contains(keyword, ignoreCase = true) ||
                it.desc.contains(keyword, ignoreCase = true) ||
                it.detailContent.contains(keyword, ignoreCase = true)
            }
            Log.d(TAG, "searchByKeyword: keyword=$keyword, found ${results.size} items")
            results
        } catch (e: Exception) {
            Log.e(TAG, "searchByKeyword error: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 根据 AI 返回的检索方法执行检索
     */
    fun executeSearch(searchMethod: String, tags: List<String>): List<AdItem> {
        Log.d(TAG, "executeSearch: searchMethod=$searchMethod, tags=$tags")
        
        val lowerMethod = searchMethod.lowercase()
        Log.d(TAG, "executeSearch: lowercased method=$lowerMethod")
        
        return when (lowerMethod) {
            "searchbytag", "searchByTag" -> {
                Log.d(TAG, "executeSearch: calling searchByTag")
                if (tags.isNotEmpty()) searchByTag(tags[0]) else emptyList()
            }
            "searchbymultipletags", "searchByMultipleTags" -> {
                Log.d(TAG, "executeSearch: calling searchByMultipleTags")
                searchByMultipleTags(tags)
            }
            "searchbychannel", "searchByChannel" -> {
                Log.d(TAG, "executeSearch: calling searchByChannel")
                if (tags.isNotEmpty()) {
                    val channel = tags[0].toIntOrNull() ?: 0
                    searchByChannel(channel)
                } else {
                    emptyList()
                }
            }
            "searchbykeyword", "searchByKeyword" -> {
                Log.d(TAG, "executeSearch: calling searchByKeyword")
                if (tags.isNotEmpty()) searchByKeyword(tags[0]) else emptyList()
            }
            else -> {
                Log.d(TAG, "executeSearch: unknown method, returning empty")
                emptyList()
            }
        }
    }
}
