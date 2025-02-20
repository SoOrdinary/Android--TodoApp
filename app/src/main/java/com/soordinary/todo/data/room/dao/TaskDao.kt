package com.soordinary.todo.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.soordinary.todo.data.room.entity.Task
import kotlinx.coroutines.flow.Flow

/**
 * room数据库的Dao层--Task
 */
@Dao
interface TaskDao {
    @Insert
    fun insert(task: Task): Unit

    @Update
    fun update(task: Task): Unit

    @Delete
    fun delete(task: Task): Unit

    // 根据 ID 查询任务
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Task

    // 根据完成情况查询任务（支持完成/未完成/所有--null），未完成排在完成的前面，未完成的日期从小到大，完成的日期从大到小
    @Query(
        "SELECT * FROM tasks WHERE :isFinish IS null OR is_finish = :isFinish ORDER BY " +
                "CASE WHEN is_finish = 0 THEN 0 ELSE 1 END ASC, " +
                "CASE WHEN is_finish = 0 THEN due_date END ASC, " +
                "CASE WHEN is_finish = 1 THEN due_date END DESC"
    )
    fun getTasksByFinish(isFinish: Boolean?): LiveData<List<Task>>

    // 根据标题查询任务（支持完成/未完成/所有--null），按日期排序，未完成任务优先
    @Query(
        "SELECT * FROM tasks WHERE title LIKE '%' || :title || '%' AND ( :isFinish IS null OR is_finish = :isFinish ) ORDER BY " +
                "CASE WHEN is_finish = 0 THEN 0 ELSE 1 END ASC, " +
                "CASE WHEN is_finish = 0 THEN due_date END ASC, " +
                "CASE WHEN is_finish = 1 THEN due_date END DESC"
    )
    fun getTasksByTitleAndFinish(title: String, isFinish: Boolean?): LiveData<List<Task>>

    // 根据日期查询任务（支持完成/未完成/所有--null），按日期排序，未完成任务优先
    @Query(
        "SELECT * FROM tasks WHERE due_date BETWEEN :startDate AND :endDate AND ( :isFinish IS null OR is_finish = :isFinish ) ORDER BY " +
                "CASE WHEN is_finish = 0 THEN 0 ELSE 1 END ASC, " +
                "CASE WHEN is_finish = 0 THEN due_date END ASC, " +
                "CASE WHEN is_finish = 1 THEN due_date END DESC"
    )
    fun getTasksByDueDateAndFinish(startDate: Long, endDate: Long, isFinish: Boolean?): LiveData<List<Task>>

    // 根据标签查询任务（支持完成/未完成/所有--null），按日期排序，未完成任务优先
    @Query(
        "SELECT * FROM tasks WHERE tag LIKE :tag AND ( :isFinish IS null OR is_finish = :isFinish ) ORDER BY " +
                "CASE WHEN is_finish = 0 THEN 0 ELSE 1 END ASC, " +
                "CASE WHEN is_finish = 0 THEN due_date END ASC, " +
                "CASE WHEN is_finish = 1 THEN due_date END DESC"
    )
    fun getTasksByTagAndFinish(tag: String, isFinish: Boolean?): LiveData<List<Task>>

    // 查询数据库内完成任务数量
    @Query("SELECT COUNT(*) FROM tasks WHERE is_finish = 1")
    fun getCompletedTaskCount(): Flow<Int>

    // 查询某一时间段任务总数
    @Query("SELECT COUNT(*) FROM tasks WHERE due_date BETWEEN :startDate AND :endDate")
    fun getTaskCountInTimeRange(startDate: Long, endDate: Long): Flow<Int>

    // 查询某一时间段完成任务数量
    @Query("SELECT COUNT(*) FROM tasks WHERE due_date BETWEEN :startDate AND :endDate AND is_finish = 1")
    fun getCompletedTaskCountInTimeRange(startDate: Long, endDate: Long): Flow<Int>

    // 查询超时未完成任务数量
    @Query("SELECT COUNT(*) FROM tasks WHERE due_date < :currentTime AND is_finish = 0")
    fun getOverdueUncompletedTaskCount(currentTime: Long): Flow<Int>
}
