package com.example.myapplication.dataprocess

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context


@Database(
    entities = [AdItem::class, User::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(ListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun adItemDao(): AdItemDao
    abstract fun userDao(): UserDao

    companion object {
        val instance: AppDatabase by lazy {
            Room.databaseBuilder(
                MyApplication.context,
                AppDatabase::class.java,
                "ad_database"
            )
            .fallbackToDestructiveMigration() // 开发阶段版本不匹配时自动重建数据库
            .build()
        }
    }
}