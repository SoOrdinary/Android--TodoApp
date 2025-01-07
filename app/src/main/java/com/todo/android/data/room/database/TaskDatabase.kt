package com.todo.android.data.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.todo.android.data.room.dao.TaskDao
import com.todo.android.data.room.entity.Task
import kotlin.concurrent.Volatile

@Database(version = 1, entities = [Task::class])
abstract class TodoDatabase : RoomDatabase() {

    abstract fun todoDao(): TaskDao // 获取 Dao

    companion object {
        @Volatile
        private var instance: TodoDatabase? = null // 单例实例
        private const val NUMBER_OF_THREADS = 4 // 线程池大小

        // 获取 TodoDatabase 实例
        @Synchronized
        fun getDatabase(context: Context): TodoDatabase {
            instance?.let {
                return it
            }
            databaseBuilder(context.applicationContext, TodoDatabase::class.java, "todo_database")
                .build()
                .apply {
                    instance = this
                }
            return instance as TodoDatabase
        }
    }
}

