package com.example.myapplication.dataprocess

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ListConverter {
    @TypeConverter
    fun fromList(list: List<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toList(json: String): List<String> {
        return Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)
    }
}

// 用户管理单例类
object UserManager {
    private var currentUser: User? = null
    private val userDao by lazy { AppDatabase.instance.userDao() }

    fun getCurrentUser(): User? = currentUser

    fun getCurrentUsername(): String = currentUser?.username ?: ""

    fun isLoggedIn(): Boolean = currentUser != null

    // 登录（同步版本，用于需要立即返回结果的场景）
    fun login(username: String, password: String): Boolean = runBlocking(Dispatchers.IO) {
        val user = userDao.getUserByUsername(username)
        if (user != null && user.password == password) {
            currentUser = user
            return@runBlocking true
        }
        return@runBlocking false
    }

    // 注册（同步版本，用于需要立即返回结果的场景）
    fun register(username: String, password: String): Boolean = runBlocking(Dispatchers.IO) {
        if (userDao.checkUsernameExists(username) > 0) {
            return@runBlocking false // 用户名已存在
        }
        val newUser = User(
            username = username,
            password = password,
            browseHistory = emptyList(),
            likeList = emptyList(),
            starList = emptyList(),
            preferenceTags = emptyList()
        )
        userDao.insertUser(newUser)
        currentUser = newUser
        return@runBlocking true
    }

    // 登出
    fun logout() {
        GlobalScope.launch(Dispatchers.IO) {
            if (currentUser != null) {
                userDao.updateUser(currentUser!!)
            }
        }
        currentUser = null
    }

    // 添加浏览记录
    fun addBrowseHistory(itemId: String) {
        currentUser?.let { user ->
            val history = user.browseHistory.toMutableList()
            // 移除重复项并添加到开头
            history.remove(itemId)
            history.add(0, itemId)
            // 保留最近20条记录
            if (history.size > 20) {
                history.removeLast()
            }
            user.browseHistory = history
            GlobalScope.launch(Dispatchers.IO) {
                userDao.updateUser(user)
            }
        }
    }

    // 添加点赞
    fun addLike(itemId: String) {
        currentUser?.let { user ->
            val likes = user.likeList.toMutableList()
            if (!likes.contains(itemId)) {
                likes.add(itemId)
                user.likeList = likes
                GlobalScope.launch(Dispatchers.IO) {
                    userDao.updateUser(user)
                }
            }
        }
    }

    // 移除点赞
    fun removeLike(itemId: String) {
        currentUser?.let { user ->
            val likes = user.likeList.toMutableList()
            if (likes.contains(itemId)) {
                likes.remove(itemId)
                user.likeList = likes
                GlobalScope.launch(Dispatchers.IO) {
                    userDao.updateUser(user)
                }
            }
        }
    }

    // 添加收藏
    fun addStar(itemId: String) {
        currentUser?.let { user ->
            val stars = user.starList.toMutableList()
            if (!stars.contains(itemId)) {
                stars.add(itemId)
                user.starList = stars
                GlobalScope.launch(Dispatchers.IO) {
                    userDao.updateUser(user)
                }
            }
        }
    }

    // 移除收藏
    fun removeStar(itemId: String) {
        currentUser?.let { user ->
            val stars = user.starList.toMutableList()
            if (stars.contains(itemId)) {
                stars.remove(itemId)
                user.starList = stars
                GlobalScope.launch(Dispatchers.IO) {
                    userDao.updateUser(user)
                }
            }
        }
    }

    // 添加喜好标签
    fun addPreferenceTag(tag: String) {
        currentUser?.let { user ->
            val tags = user.preferenceTags.toMutableList()
            if (!tags.contains(tag)) {
                tags.add(tag)
                user.preferenceTags = tags
                GlobalScope.launch(Dispatchers.IO) {
                    userDao.updateUser(user)
                }
            }
        }
    }
}