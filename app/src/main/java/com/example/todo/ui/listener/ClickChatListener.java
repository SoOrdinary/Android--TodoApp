package com.example.todo.ui.listener;

import android.view.View;

import com.example.todo.data.room.entity.Chat;

public interface ClickChatListener {
    void onClickChatOwnPicture(View view);
    void onLongClickChat(View view, Chat chat);
    void onLongClickChatOwnPicture(View view);
}
