package com.soordinary.todo.view.fragment.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.soordinary.todo.R
import com.soordinary.todo.TodoApplication
import com.soordinary.todo.repository.AlarmRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 创建定时通知的work，与alarm任务列表一一对应
 */
class AlarmNotifyWork(context: Context?, workerParams: WorkerParameters?) : Worker(context!!, workerParams!!) {

    private val alarmRepository: AlarmRepository = AlarmRepository()

    companion object {
        private const val CHANNEL_ID = "闹钟定时通知渠道"
    }

    override fun doWork(): Result {
        // 通过协程删除
        val date=inputData.getLong("date",0)
        GlobalScope.launch {
            delay(15000)
            alarmRepository.deleteAlarmByDate(date)
        }
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
                "闹钟定时通知渠道",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 创建通知
        val notification: Notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentText(title)
            .setSmallIcon(R.drawable.app_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // 发送通知
        notificationManager.notify(1, notification)
    }
}
