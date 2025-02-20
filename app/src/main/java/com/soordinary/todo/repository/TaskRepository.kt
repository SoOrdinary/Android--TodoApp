package com.soordinary.todo.repository

import androidx.lifecycle.MutableLiveData
import com.soordinary.todo.TodoApplication
import com.soordinary.todo.data.room.database.TaskDatabase
import com.soordinary.todo.data.room.entity.Task
import com.soordinary.todo.data.shared.TaskSharedPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Task仓库,增删改查使用liveData自带的协程
 *
 * @role1 为对room数据库Task进行异步查询提供了常用的函数以便调用
 * @role2 管理SharePreference中的taskTag
 *
 * @improve1 Todo:增加缓存
 */
class TaskRepository() {

    // 这样子所有调用该仓库的viewModel才能拿到同一个LiveData Todo:是否需要异步查询？这种还是共享的ViewModel好？
    companion object {
        private val _taskTagsLiveData = MutableLiveData<Set<String>>(TaskSharedPreference.tags)
    }

    private val taskDao = TaskDatabase.getDatabase(TodoApplication.context).taskDao()

    // 可观察的tags
    val taskTagsLiveData = _taskTagsLiveData

    // 插入标签
    suspend fun insertTaskTag(newTag: String) {
        withContext(Dispatchers.IO) {
            if (TaskSharedPreference.addTag(newTag)) {
                _taskTagsLiveData.postValue(TaskSharedPreference.tags)
            }
        }
    }

    // 删除标签
    suspend fun deleteTaskTag(oldTag: String) {
        withContext(Dispatchers.IO) {
            if (TaskSharedPreference.removeTag(oldTag)) {
                _taskTagsLiveData.postValue(TaskSharedPreference.tags)
            }
        }
    }

    // 判断某标签是否包含 Todo:实现异步
    fun isContain(tag: String): Boolean = TaskSharedPreference.isContain(tag)

    // 插入任务
    suspend fun insertTask(task: Task) {
        withContext(Dispatchers.IO) {
            taskDao.insert(task)
        }
    }

    // 更新任务
    suspend fun updateTask(task: Task) {
        withContext(Dispatchers.IO) {
            // 获取数据库中原始封面图片 URL
            val oldTask: Task = taskDao.getTaskById(task.id) // 假设有通过 ID 查询的方法
            val oldCoverImage: String? = oldTask.image
            // 判断封面图是否发生变化，如果发生变化且原有封面图不为空，删除原来的图片
            if (oldCoverImage != null && oldCoverImage != task.image) {
                val oldCoverFile = File(oldCoverImage)
                if (oldCoverFile.exists()) {
                    oldCoverFile.delete() // 删除原有图
                }
            }
            taskDao.update(task)
        }
    }

    // 删除任务
    suspend fun deleteTask(task: Task) {
        withContext(Dispatchers.IO) {
            // 判断是否有图片 URL，如果有则删除文件
            task.image?.let {
                val coverFile: File = File(it)
                if (coverFile.exists()) {
                    coverFile.delete()
                }
            }
            taskDao.delete(task)
        }
    }

    // 根据完成情况查询任务
    fun getTasksByFinish(isFinish: Boolean?) =
        taskDao.getTasksByFinish(isFinish)

    // 根据标题和完成状态查询任务
    fun getTasksByTitleAndFinish(title: String, isFinish: Boolean?) =
        taskDao.getTasksByTitleAndFinish(title, isFinish)

    // 根据日期和完成状态查询任务
    fun getTasksByDueDateAndFinish(startDate: Long, endDate: Long, isFinish: Boolean?) =
        taskDao.getTasksByDueDateAndFinish(startDate, endDate, isFinish)

    // 根据标签和完成状态查询任务
    fun getTasksByTagAndFinish(tag: String, isFinish: Boolean?) =
        taskDao.getTasksByTagAndFinish(tag, isFinish)

    // 用于Service前台调用
    fun getCompletedTaskCount() = taskDao.getCompletedTaskCount()
    fun getTaskCountInTimeRange(startDate: Long, endDate: Long) = taskDao.getTaskCountInTimeRange(startDate, endDate)
    fun getCompletedTaskCountInTimeRange(startDate: Long, endDate: Long) = taskDao.getCompletedTaskCountInTimeRange(startDate, endDate)
    fun getOverdueUncompletedTaskCount(currentTime: Long) = taskDao.getOverdueUncompletedTaskCount(currentTime)
}