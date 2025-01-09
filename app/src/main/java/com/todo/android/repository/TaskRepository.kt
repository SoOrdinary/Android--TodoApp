package com.example.todo.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.todo.android.TodoApplication
import com.todo.android.data.room.dao.TaskDao
import com.todo.android.data.room.database.TaskDatabase
import com.todo.android.data.room.entity.Task
import kotlinx.coroutines.Dispatchers
import java.io.File

/**
 * Task仓库,增删改查使用协程
 *
 * @role 提供了常用的task的增删改查函数
 */
class TaskRepository() {

    private val taskDao: TaskDao

    init {
        val db: TaskDatabase = TaskDatabase.getDatabase(TodoApplication.context)
        taskDao = db.taskDao()
    }

    // 插入任务
    suspend fun insert(task: Task){
        taskDao.insert(task)
    }

    // 更新任务
    suspend fun update(task: Task){
        // 获取数据库中原始封面图片 URL
        val oldTask: Task = taskDao.getTaskById(task.id) // 假设有通过 ID 查询的方法
        val oldCoverImage: String? = (oldTask.image)
        // 判断封面图是否发生变化，如果发生变化且原有封面图不为空，删除原来的图片
        if (oldCoverImage != null && oldCoverImage != task.image) {
            val oldCoverFile = File(oldCoverImage)
            if (oldCoverFile.exists()) {
                oldCoverFile.delete() // 删除原有封面图
            }
        }
        taskDao.update(task)
    }

    // 删除任务
    suspend fun delete(task: Task) {
        // 判断是否有图片 URL，如果有则删除文件
        task.image?.let {
            val coverFile: File = File(it)
            if (coverFile.exists()) {
                coverFile.delete()
            }
        }
        taskDao.delete(task)
    }

    // 根据完成情况查询任务
    fun getTasksByFinish(isFinish: Boolean): LiveData<List<Task>> = liveData(Dispatchers.IO) {
        val tasks = taskDao.getTasksByFinish(isFinish)
        emit(tasks)
    }

    // 根据标题和完成状态查询任务
    fun getTasksByTitleAndFinish(title: String, isFinish: Boolean): LiveData<List<Task>> = liveData(Dispatchers.IO) {
        val tasks = taskDao.getTasksByTitleAndFinish(title, isFinish)
        emit(tasks)
    }

    // 根据日期和完成状态查询任务
    fun getTasksByDueDateAndFinish(startDate: Long, endDate: Long, isFinish: Boolean): LiveData<List<Task>> = liveData(Dispatchers.IO) {
        val tasks = taskDao.getTasksByDueDateAndFinish(startDate, endDate, isFinish)
        emit(tasks)
    }

    // 根据标签和完成状态查询任务
    fun getTasksByTagAndFinish(tag: String, isFinish: Boolean): LiveData<List<Task>> = liveData(Dispatchers.IO) {
        val tasks = taskDao.getTasksByTagAndFinish(tag, isFinish)
        emit(tasks)
    }
}