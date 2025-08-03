package com.soordinary.todo.repository

import androidx.lifecycle.MutableLiveData
import com.soordinary.todo.data.shared.UserMMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 个人信息提取仓库
 */
class UserRepository {

    companion object {
        private val _userIconUriLiveData = MutableLiveData<String>(UserMMKV.userIconUri)
        private val _userNameLiveData = MutableLiveData<String>(UserMMKV.userName)
        private val _userSignatureLiveData = MutableLiveData<String>(UserMMKV.userSignature)
        private val _userPasswordLiveData = MutableLiveData<String>(UserMMKV.userPassword)
    }

    // 获取相应的liveData
    val userIconUriLiveData get() = _userIconUriLiveData
    val userNameLiveData get() = _userNameLiveData
    val userSignatureLiveData get() = _userSignatureLiveData
    val userPasswordLiveData get() = _userPasswordLiveData

    // 更新个人图标
    suspend fun updateUserIcon(newIconUri: String) {
        withContext(Dispatchers.IO) {
            UserMMKV.userIconUri = newIconUri
            _userIconUriLiveData.postValue(newIconUri)
        }
    }

    // 更新个人姓名
    suspend fun updateUserName(newName: String) {
        withContext(Dispatchers.IO) {
            UserMMKV.userName = newName
            _userNameLiveData.postValue(newName)
        }
    }

    // 更新个人签名
    suspend fun updateUserSignature(newSignature: String) {
        withContext(Dispatchers.IO) {
            UserMMKV.userSignature = newSignature
            _userSignatureLiveData.postValue(newSignature)
        }
    }

    suspend fun updateUserPassword(newPassword: String) {
        withContext(Dispatchers.IO) {
            UserMMKV.userPassword = newPassword
            _userPasswordLiveData.postValue(newPassword)
        }
    }
}