package com.soordinary.todo.utils

import android.app.ActivityManager
import android.content.Context

object ProcessUtils {
    /**
     * 获取当前进程名称
     */
    fun getProcessName(context: Context): String? {
        val pid = android.os.Process.myPid()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // 遍历所有运行中的进程，找到当前PID对应的进程名称
        activityManager.runningAppProcesses?.forEach { processInfo ->
            if (processInfo.pid == pid) {
                return processInfo.processName
            }
        }
        return null
    }

    /**
     * 判断当前进程是否为主进程
     */
    fun isMainProcess(context: Context): Boolean {
        return context.packageName == getProcessName(context)
    }
}