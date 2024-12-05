package com.example.todo.data.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.todo.data.room.entity.Todo;

import java.util.List;

@Dao
public interface TodoDao {

    @Insert
    void insert(Todo todo);

    @Update
    void update(Todo todo);

    @Delete
    void delete(Todo todo);

    // 根据 ID 查询任务
    @Query("SELECT * FROM todos WHERE id = :id")
    Todo getTodoById(int id);

    // 根据完成情况查询任务（支持完成/未完成/所有--null），未完成排在完成的前面，未完成的日期从小到大，完成的日期从大到小
    @Query("SELECT * FROM todos WHERE :isFinish IS null OR is_finish = :isFinish ORDER BY "+
            "CASE WHEN is_finish = 0 THEN 0 ELSE 1 END ASC, "+
            "CASE WHEN is_finish = 0 THEN due_date END ASC, "+
            "CASE WHEN is_finish = 1 THEN due_date END DESC")
    LiveData<List<Todo>> getTodosByFinish(Boolean isFinish);

    // 根据标题查询任务（支持完成/未完成/所有--null），按日期排序，未完成任务优先
    @Query("SELECT * FROM todos WHERE title LIKE '%' ||:title || '%' AND ( :isFinish IS null OR is_finish = :isFinish ) ORDER BY "+
            "CASE WHEN is_finish = 0 THEN 0 ELSE 1 END ASC, "+
            "CASE WHEN is_finish = 0 THEN due_date END ASC, "+
            "CASE WHEN is_finish = 1 THEN due_date END DESC")
    LiveData<List<Todo>> getTodosByTitleAndFinish(String title, Boolean isFinish);

    // 根据日期查询任务（支持完成/未完成/所有--null），按日期排序，未完成任务优先
    @Query("SELECT * FROM todos WHERE due_date BETWEEN :startDate AND :endDate AND ( :isFinish IS null OR is_finish = :isFinish ) ORDER BY "+
            "CASE WHEN is_finish = 0 THEN 0 ELSE 1 END ASC, "+
            "CASE WHEN is_finish = 0 THEN due_date END ASC, "+
            "CASE WHEN is_finish = 1 THEN due_date END DESC")
    LiveData<List<Todo>> getTodosByDueDateAndFinish(Long startDate, Long endDate, Boolean isFinish);

    // 根据标签查询任务（支持完成/未完成/所有--null），按日期排序，未完成任务优先
    @Query("SELECT * FROM todos WHERE tag LIKE :tag AND ( :isFinish IS null OR is_finish = :isFinish ) ORDER BY "+
            "CASE WHEN is_finish = 0 THEN 0 ELSE 1 END ASC, "+
            "CASE WHEN is_finish = 0 THEN due_date END ASC, "+
            "CASE WHEN is_finish = 1 THEN due_date END DESC")
    LiveData<List<Todo>> getTodosByTagAndFinish(String tag, Boolean isFinish);

}
