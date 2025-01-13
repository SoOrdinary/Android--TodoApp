package com.todo.android.data.shared

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import com.todo.android.TodoApplication


/**
 * 个人信息的存储类
 */
object UserInfoSharedPreference {

    private const val SP_NAME: String = "user_info" // sharedPreferences文件名称
    private const val USER_ID: String = "user_id" // 个人ID，全局唯一，自动生成且不可更改
    private const val USER_ICON_URI: String = "user_icon_uri" // 个人头像的Uri
    private const val USER_NAME: String = "user_name" // 个人昵称
    private const val USER_SIGNATURE: String = "user_signature" // 个人签名

    private val sharedPreferences: SharedPreferences = TodoApplication.context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)

    // get
    val userId :Long get() = sharedPreferences.getLong(USER_ID,uniqueId())
    private fun uniqueId():Long{
        val id :Long=System.currentTimeMillis()
        sharedPreferences.edit().putLong(USER_ID, id).commit()
        return id
    }

    // 委托属性来处理 get 和 set
    var userIconUri: String?
        get() = sharedPreferences.getString(USER_ICON_URI, "android.resource://com.todo.android/drawable/app_icon")
        set(value) = sharedPreferences.edit().putString(USER_ICON_URI, value).apply()

    var userName: String?
        get() = sharedPreferences.getString(USER_NAME, "SoOrdinary")
        set(value) = sharedPreferences.edit().putString(USER_NAME, value).apply()

    var userSignature: String?
        get() = sharedPreferences.getString(USER_SIGNATURE, "Tencent School Enterprise Joint Project")
        set(value) = sharedPreferences.edit().putString(USER_SIGNATURE, value).apply()

}