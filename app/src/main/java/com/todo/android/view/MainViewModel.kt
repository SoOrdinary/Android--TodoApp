package com.todo.android.view

import androidx.lifecycle.ViewModel
import com.todo.android.repository.TaskRepository

class MainViewModel: ViewModel() {

    private val taskRepository: TaskRepository = TaskRepository()

    // 获取当前的taskTags标签
    fun getNowTaskTagsLiveData() = taskRepository.taskTagsLiveData
}