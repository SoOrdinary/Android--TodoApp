package com.example.todo.data.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.todo.data.room.entity.Chat;
import com.example.todo.data.repository.ChatRepository;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository repository;
    private LiveData<List<Chat>> allChats;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        repository = new ChatRepository(application);
        allChats = repository.getAllChats();
    }

    // 获取所有聊天记录
    public LiveData<List<Chat>> getAllChats() {
        return allChats;
    }

    // 根据发送者ID获取聊天记录
    public LiveData<List<Chat>> getChatsBySenderId(Long senderId) {
        return repository.getChatsBySenderId(senderId);
    }

    // 根据聊天内容中的关键字获取聊天记录
    public LiveData<List<Chat>> getChatsByTextKeyword(String keyword) {
        return repository.getChatsByTextKeyword(keyword);
    }

    // 插入聊天记录
    public void insert(Chat chat) {
        repository.insert(chat);
    }

    // 更新聊天记录
    public void update(Chat chat) {
        repository.update(chat);
    }

    // 删除聊天记录
    public void delete(Chat chat) {
        repository.delete(chat);
    }

    // 删除所有聊天记录
    public void deleteAll() {
        repository.deleteAll();
    }
}
