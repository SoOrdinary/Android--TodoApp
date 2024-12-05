package com.example.todo.data.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.todo.data.room.entity.Chat;

import java.util.List;

@Dao
public interface ChatDao {

    @Insert
    void insert(Chat chat);

    @Update
    void update(Chat chat);

    @Delete
    void delete(Chat chat);

    // 删除所有聊天记录
    @Query("DELETE FROM chats")
    void deleteAll();

    // 查询所有聊天记录，按时间戳降序排序
    @Query("SELECT * FROM chats ORDER BY timestamp ASC")
    LiveData<List<Chat>> getAllChats();

    // 根据 senderId 查找聊天记录，按时间戳降序排序
    @Query("SELECT * FROM chats WHERE sender_id = :senderId ORDER BY timestamp ASC")
    LiveData<List<Chat>> getChatsBySenderId(Long senderId);

    // 根据 chatText 中的关键字查找聊天记录，按时间戳降序排序
    @Query("SELECT * FROM chats WHERE chat_text LIKE '%' || :keyword || '%' ORDER BY timestamp ASC")
    LiveData<List<Chat>> getChatsByTextKeyword(String keyword);

}
