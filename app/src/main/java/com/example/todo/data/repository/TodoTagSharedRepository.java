package com.example.todo.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.todo.data.shared.TodoTagShared;

import java.util.Set;

public class TodoTagSharedRepository {

    private final TodoTagShared todoTagShared;
    private final MutableLiveData<Set<String>> tagListLiveData = new MutableLiveData<>();

    // 构造方法，初始化 TodoTagShared 实例和 LiveData
    public TodoTagSharedRepository(Context context) {
        todoTagShared = TodoTagShared.getInstance(context);
        tagListLiveData.setValue(todoTagShared.getTags());
    }

    // 获取标签列表的 LiveData，用于 UI 更新
    public LiveData<Set<String>> getTagListLiveData() {
        return tagListLiveData;
    }

    // 添加标签
    public void addTag(String tag) {
        if (todoTagShared.addTag(tag)) {
            tagListLiveData.setValue(todoTagShared.getTags()); // 更新 LiveData，通知 UI 更新
        }
    }

    // 删除标签
    public void removeTag(String tag) {
        if (todoTagShared.removeTag(tag)) {
            tagListLiveData.setValue(todoTagShared.getTags()); // 更新 LiveData，通知 UI 更新
        }
    }


    // 检查标签是否存在
    public boolean isTagExist(String tag) {
        return todoTagShared.isTagExist(tag);
    }
}
