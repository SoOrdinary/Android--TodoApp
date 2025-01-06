package com.todo.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * 定义一些应用全程跟随的变量
 *
 * @role1 建立全局context，方便调用
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
    }
}
