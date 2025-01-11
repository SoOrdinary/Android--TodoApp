package com.todo.android.data.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.todo.android.data.room.dao.TaskDao
import com.todo.android.data.room.entity.Task
import kotlin.concurrent.Volatile

/**
 * Task的数据库
 */
@Database(version = 1, entities = [Task::class], exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao // 获取 Dao

    companion object {
        @Volatile
        private var instance: TaskDatabase? = null // 单例实例

        // 获取 TodoDatabase 实例
        @Synchronized
        fun getDatabase(context: Context): TaskDatabase {
            instance?.let { return it }
            databaseBuilder(context.applicationContext, TaskDatabase::class.java, "task_database")
                .build()
                .apply { instance = this }
            return instance as TaskDatabase
        }
    }
}

