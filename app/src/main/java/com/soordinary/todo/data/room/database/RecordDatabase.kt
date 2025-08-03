package com.soordinary.todo.data.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.soordinary.todo.data.room.dao.RecordDao
import com.soordinary.todo.data.room.entity.RecordSo
import kotlin.concurrent.Volatile

/**
 * Record的数据库
 */
@Database(version = 1, entities = [RecordSo::class], exportSchema = false)
abstract class RecordDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao // 获取 Dao

    companion object {
        @Volatile
        private var instance: RecordDatabase? = null // 单例实例

        // 获取 TodoDatabase 实例
        @Synchronized
        fun getDatabase(context: Context): RecordDatabase {
            instance?.let { return it }
            databaseBuilder(context.applicationContext, RecordDatabase::class.java, "record_database")
                .enableMultiInstanceInvalidation()
                .build()
                .apply { instance = this }
            return instance as RecordDatabase
        }
    }
}