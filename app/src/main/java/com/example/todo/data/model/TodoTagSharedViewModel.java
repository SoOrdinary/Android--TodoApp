package com.example.todo.data.model;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.todo.data.repository.TodoTagSharedRepository;

import java.util.Set;

public class TodoTagSharedViewModel extends AndroidViewModel {

    private final TodoTagSharedRepository todoTagRepository;
    private final LiveData<Set<String>> tagListLiveData;

    public TodoTagSharedViewModel(Application application) {
        super(application);
        todoTagRepository = new TodoTagSharedRepository(application.getApplicationContext());
        tagListLiveData = todoTagRepository.getTagListLiveData();  // 获取标签列表的 LiveData
    }

    // 获取标签列表的 LiveData
    public LiveData<Set<String>> getTagListLiveData() {
        return tagListLiveData;
    }

    // 添加标签
    public void addTag(String tag) {
        todoTagRepository.addTag(tag);
    }

    // 删除标签
    public void removeTag(String tag) {
        todoTagRepository.removeTag(tag);
    }


    // 检查标签是否存在
    public boolean isTagExist(String tag) {
        return todoTagRepository.isTagExist(tag);
    }
}
