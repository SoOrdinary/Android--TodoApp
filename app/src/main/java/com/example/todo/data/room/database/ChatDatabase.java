package com.example.todo.data.room.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.todo.data.room.dao.ChatDao;
import com.example.todo.data.room.entity.Chat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Chat.class}, version = 3, exportSchema = false)
public abstract class ChatDatabase extends RoomDatabase {

    public abstract ChatDao chatDao(); // 获取 Dao

    private static volatile ChatDatabase INSTANCE; // 单例实例
    private static final int NUMBER_OF_THREADS = 4; // 线程池大小
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS); // 线程池

    // 获取 ChatDatabase 实例
    public static ChatDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ChatDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ChatDatabase.class, "chat_database")
                            .fallbackToDestructiveMigration() // 允许数据库迁移失败时销毁数据库
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
