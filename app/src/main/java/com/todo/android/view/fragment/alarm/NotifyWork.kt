package com.todo.android.view.fragment.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.todo.android.R
import com.todo.android.TodoApplication

/**
 * 创建定时通知的work，与alarm任务列表一一对应
 */
class NotifyWork(context: Context?, workerParams: WorkerParameters?) : Worker(context!!, workerParams!!) {

    companion object {
        private const val CHANNEL_ID = "闹钟定时通知"
    }

    override fun doWork(): Result {
        // 获取传递过来的数据
        val title = inputData.getString("title") ?: "任务到时"
        // 发送通知
        sendNotification(title)
        // 返回成功
        return Result.success()
    }

    private fun sendNotification(title: String) {
        val notificationManager = TodoApplication.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 创建通知渠道（Android 8.0 及以上要求）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "定时通知",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 创建通知
        val notification: Notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.app_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // 发送通知
        notificationManager.notify(1, notification)
    }
}
