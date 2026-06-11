package com.example.myapplication.dataprocess

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE username = :username")
    fun getUserByUsername(username: String): User?

    @Insert
    fun insertUser(user: User)

    @Update
    fun updateUser(user: User)

    @Query("DELETE FROM users WHERE username = :username")
    fun deleteUser(username: String)

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    fun checkUsernameExists(username: String): Int
}