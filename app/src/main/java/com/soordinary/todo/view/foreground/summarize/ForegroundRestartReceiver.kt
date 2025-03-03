package com.soordinary.todo.view.foreground.summarize

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


/**
 * 前台服务广播接收器，防止前台服务被系统错误杀死
 */
class ForegroundRestartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action.equals("error_close_foreground_service")) {
            // 启动服务
            val serviceIntent = Intent(context, ForegroundService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}