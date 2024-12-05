package com.example.todo.ui.listener;

import android.view.View;

import com.example.todo.data.room.entity.Todo;
import com.example.todo.ui.fragments.TodoFragment;

public interface ClickTaskListener{
    void onClickTask(View view,Todo todo);
    void onLongClickTask(View view, Todo todo);
    void onClickEdit(View view, Todo todo, TodoFragment.TodoUpdateListener listener);
    void onClickAlarm(View view,Todo todo);
    void onClickPhoto(View view,String name, TodoFragment.TodoEditUpdateListener listener);
}