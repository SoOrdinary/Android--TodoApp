package com.example.todo.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.todo.data.room.entity.Todo;
import com.example.todo.data.room.dao.TodoDao;
import com.example.todo.data.room.database.TodoDatabase;

import java.io.File;
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
        // 执行更新任务操作
        TodoDatabase.databaseWriteExecutor.execute(() -> {
            // 获取数据库中原始封面图片 URL
            Todo oldTodo = todoDao.getTodoById(todo.getId());  // 假设有通过 ID 查询的方法
            String oldCoverImage = (oldTodo != null ? oldTodo.getCoverImage() : null);
            // 判断封面图是否发生变化，如果发生变化且原有封面图不为空，删除原来的图片
            if (oldCoverImage != null && !oldCoverImage.equals(todo.getCoverImage())) {
                File oldCoverFile = new File(oldCoverImage);
                if (oldCoverFile.exists()) {
                    oldCoverFile.delete();  // 删除原有封面图
                }
            }
            todoDao.update(todo);
        });
    }

    // 删除任务
    public void delete(Todo todo) {
        // 执行删除任务操作
        TodoDatabase.databaseWriteExecutor.execute(() -> {
            // 判断是否有封面图片 URL，如果有则删除文件
            if (todo.getCoverImage() != null && !todo.getCoverImage().isEmpty()) {
                File coverFile = new File(todo.getCoverImage());
                if (coverFile.exists()) {
                    coverFile.delete();
                }
            }

            todoDao.delete(todo);
        });
    }
}
