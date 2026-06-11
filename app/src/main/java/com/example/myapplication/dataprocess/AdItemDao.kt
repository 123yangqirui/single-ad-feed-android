package com.example.myapplication.dataprocess

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AdItemDao {


    @Query("SELECT * FROM ad_items WHERE channelType = :channel ORDER BY createTime DESC LIMIT 11 OFFSET :offset")
    fun getItemsByChannel(channel: Int, offset: Int): Flow<List<AdItem>>

    @Query("SELECT * FROM ad_items WHERE id = :id")
    fun getItemById(id: String): AdItem?

    @Query("UPDATE ad_items SET like = :isLiked WHERE id = :id")
    fun updateLike(id: String, isLiked: Boolean)

    @Query("UPDATE ad_items SET star = :isStarred WHERE id = :id")
    fun updateStar(id: String, isStarred: Boolean)

    @Query("UPDATE ad_items SET likeCount = :count WHERE id = :id")
    fun updateLikeCount(id: String, count: Int)

    @Query("UPDATE ad_items SET starCount = :count WHERE id = :id")
    fun updateStarCount(id: String, count: Int)

    @Query("UPDATE ad_items SET like = :isLiked, likeCount = :count WHERE id = :id")
    fun updateLikeAndCount(id: String, isLiked: Boolean, count: Int)

    @Query("UPDATE ad_items SET star = :isStarred, starCount = :count WHERE id = :id")
    fun updateStarAndCount(id: String, isStarred: Boolean, count: Int)

    @Query("SELECT * FROM ad_items WHERE channelType = :channel ORDER BY createTime DESC LIMIT :pageSize OFFSET :offset")
    fun getItemsSync(channel: Int, offset: Int, pageSize: Int): List<AdItem>

    @Query("SELECT * FROM ad_items WHERE channelType = :channel ORDER BY createTime DESC")
    fun getAllItemsByChannel(channel: Int): List<AdItem>

    @Insert
    fun insertAll(items: List<AdItem>)

    @Query("DELETE FROM ad_items")
    fun clearAll()

    @Query("UPDATE ad_items SET like = :isLiked, star = :isStarred")
    fun resetAllLikeAndStar(isLiked: Boolean = false, isStarred: Boolean = false)

    @Query("SELECT * FROM ad_items WHERE id IN (:ids)")
    fun getItemsByIds(ids: List<String>): List<AdItem>
}