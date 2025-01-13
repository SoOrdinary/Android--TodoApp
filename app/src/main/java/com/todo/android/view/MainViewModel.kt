package com.todo.android.view

import androidx.lifecycle.ViewModel
import com.todo.android.repository.TaskRepository
import com.todo.android.repository.UserRepository

class MainViewModel: ViewModel() {

    private val taskRepository: TaskRepository = TaskRepository()
    private val userRepository : UserRepository = UserRepository()

    // 获取当前的taskTags标签
    fun getNowTaskTagsLiveData() = taskRepository.taskTagsLiveData

    // 获取当前的个人头像LiveData
    fun getIconUriLiveData () = userRepository.userIconUriLiveData

    // 获取当前的个人昵称LiveData
    fun getNameLiveData () = userRepository.userNameLiveData

    // 获取当前的个人签名LiveData
    fun getSignatureLiveData () = userRepository.userSignatureLiveData
}