package com.todo.android.repository

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.todo.android.data.shared.UserInfoSharedPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 个人信息提取仓库
 */
class UserRepository {

    companion object{
        private val _userIconUriLiveData = MutableLiveData<Uri>(Uri.parse(UserInfoSharedPreference.userIconUri))
        private val _userNameLiveData = MutableLiveData<String>(UserInfoSharedPreference.userName)
        private val _userSignatureLiveData = MutableLiveData<String>(UserInfoSharedPreference.userSignature)
    }

    val userIconUriLiveData get() = _userIconUriLiveData
    val userNameLiveData get() = _userNameLiveData
    val userSignatureLiveData get() = _userSignatureLiveData

    // 更新个人图标
    suspend fun updateUserIcon(newIconUri:Uri){
        withContext(Dispatchers.IO){
            val newIconUriString = newIconUri.toString()
            UserInfoSharedPreference.userIconUri=newIconUriString
            _userIconUriLiveData.postValue(newIconUri)
        }
    }

    // 更新个人姓名
    suspend fun updateUserName(newName:String){
        withContext(Dispatchers.IO){
            UserInfoSharedPreference.userName=newName
            _userNameLiveData.postValue(newName)
        }
    }

    // 更新个人签名
    suspend fun updateUserSignature(newSignature: String){
        withContext(Dispatchers.IO){
            UserInfoSharedPreference.userSignature=newSignature
            _userSignatureLiveData.postValue(newSignature)
        }
    }
}