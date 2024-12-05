package com.example.todo.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.todo.data.room.entity.Todo;
import com.example.todo.data.room.dao.TodoDao;
import com.example.todo.data.room.database.TodoDatabase;

import java.util.List;

public class TodoRepository {

    private final TodoDao todoDao;

    public TodoRepository(Application application) {
        TodoDatabase db = TodoDatabase.getDatabase(application);
        todoDao = db.todoDao();
    }

    // 根据完成情况查询任务
    public LiveData<List<Todo>> getTodosByFinish(Boolean isFinish){
        return todoDao.getTodosByFinish(isFinish);
    }

    // 根据标题和完成状态查询任务
    public LiveData<List<Todo>> getTodosByTitleAndFinish(String title, Boolean isFinish) {
        return todoDao.getTodosByTitleAndFinish(title, isFinish);
    }

    // 根据日期和完成状态查询任务
    public LiveData<List<Todo>> getTodosByDueDateAndFinish(Long startDate, Long endDate, Boolean isFinish) {
        return todoDao.getTodosByDueDateAndFinish(startDate, endDate,isFinish);
    }

    // 根据标签和完成状态查询任务
    public LiveData<List<Todo>> getTodosByTagAndFinish(String tag, Boolean isFinish) {
        return todoDao.getTodosByTagAndFinish(tag, isFinish);
    }


    // 插入任务
    public void insert(Todo todo) {
        TodoDatabase.databaseWriteExecutor.execute(() -> todoDao.insert(todo));
    }

    // 更新任务
    public void update(Todo todo) {
        TodoDatabase.databaseWriteExecutor.execute(() -> todoDao.update(todo));
    }

    // 删除任务
    public void delete(Todo todo) {
        TodoDatabase.databaseWriteExecutor.execute(() -> todoDao.delete(todo));
    }
}
