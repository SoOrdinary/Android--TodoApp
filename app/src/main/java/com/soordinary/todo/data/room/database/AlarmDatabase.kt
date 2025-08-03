package com.soordinary.todo.data.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.soordinary.todo.data.room.dao.AlarmDao
import com.soordinary.todo.data.room.entity.Alarm
import kotlin.concurrent.Volatile

/**
 * Alarm的数据库
 */
@Database(version = 1, entities = [Alarm::class], exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao // 获取 Dao

    companion object {
        @Volatile
        private var instance: AlarmDatabase? = null // 单例实例

        // 获取 TodoDatabase 实例
        @Synchronized
        fun getDatabase(context: Context): AlarmDatabase {
            instance?.let { return it }
            databaseBuilder(context.applicationContext, AlarmDatabase::class.java, "alarm_database")
                .enableMultiInstanceInvalidation()
                .build()
                .apply { instance = this }
            return instance as AlarmDatabase
        }
    }
}