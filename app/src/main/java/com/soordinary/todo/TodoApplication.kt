package com.soordinary.todo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import com.soordinary.todo.data.shared.UserMMKV
import com.soordinary.todo.utils.ProcessUtils
import com.tencent.mmkv.MMKV

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
        // todo：前台服务也需要初始化，每个单独进程都会有一个application创建并create，看后续有没有什么优化手段
        // 初始化MMKV
        MMKV.initialize(context)
        // 获取id，让id初始化一下
        UserMMKV.userId
        Log.d("TodoApplication", "onCreate: ${UserMMKV.userId}")
    }
}
