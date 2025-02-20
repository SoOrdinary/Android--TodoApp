package com.soordinary.todo.view.fragment.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soordinary.todo.repository.TaskRepository
import com.soordinary.todo.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * 界面User的ViewModel
 *
 * @role1 管理Task的Tag增删
 * @role2 管理User的一些信息更改
 */
class UserViewModel : ViewModel() {

    private val taskRepository: TaskRepository = TaskRepository()
    private val userRepository: UserRepository = UserRepository()

    // 增加当前的task标签
    fun insertTaskTag(newTag: String) = viewModelScope.launch {
        taskRepository.insertTaskTag(newTag)
    }

    // 删除某个的task标签
    fun deleteTaskTag(oldTag: String) = viewModelScope.launch {
        taskRepository.deleteTaskTag(oldTag)
    }

    // 判断某个标签是否存在Todo:异步
    fun isContain(tag: String) = taskRepository.isContain(tag)

    // 修改当前的个人头像
    fun updateIconUri(newIconUri: String) = viewModelScope.launch {
        userRepository.updateUserIcon(newIconUri)
    }

    // 修改当前的个人昵称
    fun updateName(newName: String) = viewModelScope.launch {
        userRepository.updateUserName(newName)
    }

    // 修改当前的个人签名
    fun updateSignature(newSignature: String) = viewModelScope.launch {
        userRepository.updateUserSignature(newSignature)
    }

    // 修改当前的密码
    fun updatePassword(newPassword: String) = viewModelScope.launch {
        userRepository.updateUserPassword(newPassword)
    }

    // 获取当前的个人头像LiveData
    fun getIconUriLiveData() = userRepository.userIconUriLiveData

    // 获取当前的个人昵称LiveData
    fun getNameLiveData() = userRepository.userNameLiveData

    // 获取当前的个人签名LiveData
    fun getSignatureLiveData() = userRepository.userSignatureLiveData

    // 获取当前的用户密码的LiveData
    fun getPasswordLiveData() = userRepository.userPasswordLiveData
}