package com.todo.android.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.todo.android.data.room.entity.Task

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    // 根据 ID 查询任务
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): Task

    // 根据完成情况查询任务（支持完成/未完成/所有--null），未完成排在完成的前面，未完成的日期从小到大，完成的日期从大到小
    @Query(
        ("SELECT * FROM tasks WHERE :isFinish IS null OR is_finish = :isFinish ORDER BY " +
                "CASE WHEN is_finish = 0 THEN 0 ELSE 1 END ASC, " +
                "CASE WHEN is_finish = 0 THEN due_date END ASC, " +
                "CASE WHEN is_finish = 1 THEN due_date END DESC")
    )
    suspend fun getTasksByFinish(isFinish: Boolean): List<Task>

    // 根据标题查询任务（支持完成/未完成/所有--null），按日期排序，未完成任务优先
    @Query(
        ("SELECT * FROM tasks WHERE title LIKE '%' ||:title || '%' AND ( :isFinish IS null OR is_finish = :isFinish ) ORDER BY " +
                "CASE WHEN is_finish = 0 THEN 0 ELSE 1 END ASC, " +
                "CASE WHEN is_finish = 0 THEN due_date END ASC, " +
                "CASE WHEN is_finish = 1 THEN due_date END DESC")
    )
    suspend fun getTasksByTitleAndFinish(title: String, isFinish: Boolean): List<Task>

    // 根据日期查询任务（支持完成/未完成/所有--null），按日期排序，未完成任务优先
    @Query(
        ("SELECT * FROM tasks WHERE due_date BETWEEN :startDate AND :endDate AND ( :isFinish IS null OR is_finish = :isFinish ) ORDER BY " +
                "CASE WHEN is_finish = 0 THEN 0 ELSE 1 END ASC, " +
                "CASE WHEN is_finish = 0 THEN due_date END ASC, " +
                "CASE WHEN is_finish = 1 THEN due_date END DESC")
    )
    suspend fun getTasksByDueDateAndFinish(startDate: Long, endDate: Long, isFinish: Boolean): List<Task>

    // 根据标签查询任务（支持完成/未完成/所有--null），按日期排序，未完成任务优先
    @Query(
        ("SELECT * FROM tasks WHERE tag LIKE :tag AND ( :isFinish IS null OR is_finish = :isFinish ) ORDER BY " +
                "CASE WHEN is_finish = 0 THEN 0 ELSE 1 END ASC, " +
                "CASE WHEN is_finish = 0 THEN due_date END ASC, " +
                "CASE WHEN is_finish = 1 THEN due_date END DESC")
    )
    suspend fun getTasksByTagAndFinish(tag: String, isFinish: Boolean): List<Task>
}
