package com.soordinary.todo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.soordinary.todo.data.shared.UserInfoSharedPreference

/**
 * 定义一些应用全程跟随的变量
 *
 * @role1 建立全局context，方便调用
 * @role2 第一次启动程序时自动录入当前时间作为唯一id
 *
 * @improve1 app的载入照片uri用了Glide，可以跳过权限？
 */
class TodoApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var _context: Context

        // 只允许取，不可修改
        val context get() = _context
    }

    override fun onCreate() {
        super.onCreate()
        _context = applicationContext
        // 获取id，让id初始化一下
        UserInfoSharedPreference.userId
    }
}
