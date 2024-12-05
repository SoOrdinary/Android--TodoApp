package com.example.todo.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.todo.data.room.entity.Chat;
import com.example.todo.data.room.dao.ChatDao;
import com.example.todo.data.room.database.ChatDatabase;

import java.util.List;

public class ChatRepository {

    private final ChatDao chatDao;
    private final LiveData<List<Chat>> allChats; // 所有聊天记录

    public ChatRepository(Application application) {
        ChatDatabase db = ChatDatabase.getDatabase(application);
        chatDao = db.chatDao();
        allChats = chatDao.getAllChats();
    }

    // 获取所有聊天记录
    public LiveData<List<Chat>> getAllChats() {
        return allChats;
    }

    // 根据 senderId 获取聊天记录
    public LiveData<List<Chat>> getChatsBySenderId(Long senderId) {
        return chatDao.getChatsBySenderId(senderId);
    }

    // 根据关键字查找聊天记录
    public LiveData<List<Chat>> getChatsByTextKeyword(String keyword) {
        return chatDao.getChatsByTextKeyword(keyword);
    }

    // 插入聊天记录
    public void insert(Chat chat) {
        ChatDatabase.databaseWriteExecutor.execute(() -> chatDao.insert(chat));
    }

    // 更新聊天记录
    public void update(Chat chat) {
        ChatDatabase.databaseWriteExecutor.execute(() -> chatDao.update(chat));
    }

    // 删除聊天记录
    public void delete(Chat chat) {
        ChatDatabase.databaseWriteExecutor.execute(() -> chatDao.delete(chat));
    }

    // 删除所有聊天记录
    public void deleteAll() {
        ChatDatabase.databaseWriteExecutor.execute(() -> chatDao.deleteAll());
    }
}
