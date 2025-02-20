package com.soordinary.todo.repository

import androidx.lifecycle.MutableLiveData
import com.soordinary.todo.data.shared.UserInfoSharedPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 个人信息提取仓库
 */
class UserRepository {

    companion object {
        private val _userIconUriLiveData = MutableLiveData<String>(UserInfoSharedPreference.userIconUri)
        private val _userNameLiveData = MutableLiveData<String>(UserInfoSharedPreference.userName)
        private val _userSignatureLiveData = MutableLiveData<String>(UserInfoSharedPreference.userSignature)
        private val _userPasswordLiveData = MutableLiveData<String>(UserInfoSharedPreference.userPassword)
    }

    // 获取相应的liveData
    val userIconUriLiveData get() = _userIconUriLiveData
    val userNameLiveData get() = _userNameLiveData
    val userSignatureLiveData get() = _userSignatureLiveData
    val userPasswordLiveData get() = _userPasswordLiveData

    // 更新个人图标
    suspend fun updateUserIcon(newIconUri: String) {
        withContext(Dispatchers.IO) {
            UserInfoSharedPreference.userIconUri = newIconUri
            _userIconUriLiveData.postValue(newIconUri)
        }
    }

    // 更新个人姓名
    suspend fun updateUserName(newName: String) {
        withContext(Dispatchers.IO) {
            UserInfoSharedPreference.userName = newName
            _userNameLiveData.postValue(newName)
        }
    }

    // 更新个人签名
    suspend fun updateUserSignature(newSignature: String) {
        withContext(Dispatchers.IO) {
            UserInfoSharedPreference.userSignature = newSignature
            _userSignatureLiveData.postValue(newSignature)
        }
    }

    suspend fun updateUserPassword(newPassword: String) {
        withContext(Dispatchers.IO) {
            UserInfoSharedPreference.userPassword = newPassword
            _userPasswordLiveData.postValue(newPassword)
        }
    }
}