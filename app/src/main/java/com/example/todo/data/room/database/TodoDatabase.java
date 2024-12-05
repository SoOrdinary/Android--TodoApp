package com.example.todo.data.room.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.todo.data.room.dao.TodoDao;
import com.example.todo.data.room.entity.Todo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Todo.class}, version = 1, exportSchema = false)
public abstract class TodoDatabase extends RoomDatabase {

    public abstract TodoDao todoDao(); // 获取 Dao

    private static volatile TodoDatabase INSTANCE; // 单例实例
    private static final int NUMBER_OF_THREADS = 4; // 线程池大小
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS); // 线程池

    // 获取 TodoDatabase 实例
    public static TodoDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TodoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    TodoDatabase.class, "todo_database")
                            .fallbackToDestructiveMigration() // 允许数据库迁移失败时销毁数据库
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}

