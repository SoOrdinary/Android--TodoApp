package com.example.todo.data.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.todo.data.room.entity.Todo;
import com.example.todo.data.repository.TodoRepository;

import java.util.List;

public class TodoViewModel extends AndroidViewModel {

    private final TodoRepository repository;

    public TodoViewModel(@NonNull Application application) {
        super(application);
        repository = new TodoRepository(application);
    }

    // 根据完成情况查询任务
    public LiveData<List<Todo>> getTodosByFinish(Boolean isFinish){
        return repository.getTodosByFinish(isFinish);
    }

    // 根据标题和完成状态查询任务
    public LiveData<List<Todo>> getTodosByTitleAndFinish(String title, Boolean isFinish) {
        return repository.getTodosByTitleAndFinish(title, isFinish);
    }

    // 根据日期和完成状态查询任务
    public LiveData<List<Todo>> getTodosByDueDateAndFinish(Long startDate, Long endDate, Boolean isFinish) {
        return repository.getTodosByDueDateAndFinish(startDate, endDate,isFinish);
    }

    // 根据标签和完成状态查询任务
    public LiveData<List<Todo>> getTodosByTagAndFinish(String tag, Boolean isFinish) {
        return repository.getTodosByTagAndFinish(tag, isFinish);
    }

    // 插入任务
    public void insert(Todo todo) {
        repository.insert(todo);
    }

    // 更新任务
    public void update(Todo todo) {
        repository.update(todo);
    }

    // 删除任务
    public void delete(Todo todo) {
        repository.delete(todo);
    }
}
