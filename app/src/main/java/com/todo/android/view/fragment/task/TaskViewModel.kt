package com.todo.android.view.fragment.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.data.repository.TaskRepository
import com.todo.android.data.room.entity.Task
import kotlinx.coroutines.launch

/**
 * Task的ViewModel层，保存临时数据
 */
class TaskViewModel: ViewModel() {

    private var repository: TaskRepository = TaskRepository()

    // 缓存数据
    val taskList = ArrayList<Task>()

    // 提供可观察对像
    lateinit var taskLiveData:LiveData<List<Task>>

    // 插入任务
    fun insert(task: Task) = viewModelScope.launch{
        repository.insert(task)
    }

    // 更新任务
    fun update(task: Task) = viewModelScope.launch{
        repository.update(task)
    }


    // 删除任务
    fun delete(task: Task) = viewModelScope.launch{
        repository.delete(task)
    }

    // 根据完成情况查询任务
    fun getTasksByFinish(isFinish: Boolean) {
        taskLiveData = repository.getTasksByFinish(isFinish)
    }

    // 根据标题和完成状态查询任务
    fun getTasksByTitleAndFinish(title: String, isFinish: Boolean){
        taskLiveData = repository.getTasksByTitleAndFinish(title,isFinish)
    }

    // 根据日期和完成状态查询任务
    fun getTasksByDueDateAndFinish(startDate: Long, endDate: Long, isFinish: Boolean) {
        taskLiveData = repository.getTasksByDueDateAndFinish(startDate, endDate, isFinish)
    }

    // 根据标签和完成状态查询任务
    fun getTasksByTagAndFinish(tag: String, isFinish: Boolean) {
        taskLiveData = repository.getTasksByTagAndFinish(tag, isFinish)
    }

}