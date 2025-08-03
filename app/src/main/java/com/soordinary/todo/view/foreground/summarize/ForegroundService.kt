package com.soordinary.todo.view.foreground.summarize

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.soordinary.foreground.IForeground
import com.soordinary.todo.R
import com.soordinary.todo.utils.DateTimeUtil
import com.soordinary.todo.view.MainActivity
import com.soordinary.todo.view.StartActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


/**
 * 前台服务，实时显示应用的一些数据
 * todo:多进程
 */
class ForegroundService : LifecycleService() {

    private val binder =object : IForeground.Stub() {
        override fun setPasswordFinish(isFinish: Boolean) {
            passwordFinish = isFinish
        }

        override fun setCloseComplete(isClose: Boolean) {
            closeComplete = isClose
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    // 密码输入完成与否标志
    var passwordFinish: Boolean = false

    // 是否需要关闭前台显示[拒绝广播接收器重启服务]
    var closeComplete: Boolean = false

    private val CHANNEL_ID = "前台显示服务渠道"
    private val NOTIFICATION_ID = 999
    private lateinit var foregroundRestartReceiver: ForegroundRestartReceiver
    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var foregroundDataCache: ForegroundDataCache
    private lateinit var view: RemoteViews
    private var countDownTimer: CountDownTimer? = null

    // 在onCreate中创建渠道和显示第一次通知
    override fun onCreate() {
        super.onCreate()
        // 注册广播接收器
        foregroundRestartReceiver = ForegroundRestartReceiver()
        val foregroundFilter = IntentFilter()
        foregroundFilter.addAction("error_close_foreground_service")
        ContextCompat.registerReceiver(this, foregroundRestartReceiver, foregroundFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
        // 缓存
        notificationManager = getSystemService(NotificationManager::class.java)
        builder = NotificationCompat.Builder(this, CHANNEL_ID)
        foregroundDataCache = ForegroundDataCache()
        view = RemoteViews(packageName, R.layout.foreground_service)
        // 创建渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "前台显示服务渠道",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            serviceChannel.setSound(null, null)
            notificationManager.createNotificationChannel(serviceChannel)
        }
        val intent = Intent(this, StartActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        // 创建 PendingIntent
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // 绑定前台通知
        val notification = builder
            .setSmallIcon(R.drawable.app_icon)
            .setContentText("服务正在启动，校验密码...")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    // 更新数据
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("liuyan", "setPasswor55")
        // 首次启动时先不要更新数据，等待用户输入密码后，再次执行该方法时再更新数据
        if (!passwordFinish) {
            return START_STICKY
        }

        // 监听数据变化并更新通知
        lifecycleScope.launch {
            with(foregroundDataCache) {
                // 每分钟更新超时任务的查找范围（特殊情况，因为要此刻的超时任务，参数需要时刻改变），Todo：每次查询更新完第一次delay后lastMinute会-1？
                launch {
                    var lastMinute = -1
                    while (true) {
                        var currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
                        if (currentMinute != lastMinute) {
                            lastMinute = currentMinute
                            update()
                        }
                        delay(1000)
                    }
                }
                // 启动协程同步监听
                launch {
                    // 映射之后，数据库需要重新查询，但是结果不变时，overdueUncompletedTaskCountFlow仍然不会启动
                    overdueUncompletedTaskCountFlow.distinctUntilChanged().collect {
                        overdueUncompletedTaskCount = it
                        if (overdueUncompletedTaskCount == 0) {
                            view.setViewVisibility(R.id.timeout_task, View.GONE)
                        } else {
                            view.setViewVisibility(R.id.timeout_task, View.VISIBLE)
                        }
                        view.setTextViewText(R.id.timeout_task, "超时任务：$overdueUncompletedTaskCount")
                        emitNewNotification()
                    }
                }
                launch {
                    completedTaskCountInTimeRangeFlow.distinctUntilChanged().collect {
                        completedTaskCountInTimeRange = it
                        if (completedTaskCountInTimeRange == taskCountInTimeRange) {
                            view.setTextColor(R.id.today_task, Color.parseColor("#018786"))
                        } else {
                            view.setTextColor(R.id.today_task, Color.parseColor("#000000"))
                        }
                        view.setTextViewText(R.id.today_task, "今日代办：$completedTaskCountInTimeRange/$taskCountInTimeRange")
                        emitNewNotification()
                    }
                }
                launch {
                    taskCountInTimeRangeFlow.distinctUntilChanged().collect {
                        taskCountInTimeRange = it
                        if (completedTaskCountInTimeRange == taskCountInTimeRange) {
                            view.setTextColor(R.id.today_task, Color.parseColor("#018786"))
                        } else {
                            view.setTextColor(R.id.today_task, Color.parseColor("#000000"))
                        }
                        view.setTextViewText(R.id.today_task, "今日代办：$completedTaskCountInTimeRange/$taskCountInTimeRange")
                        emitNewNotification()
                    }
                }
                launch {
                    completedTaskCountFlow.distinctUntilChanged().collect {
                        completedTaskCount = it
                        view.setTextViewText(R.id.completed_total_text, "完成总计：$completedTaskCount")
                        emitNewNotification()
                    }
                }
                launch {
                    nearestAlarmFlow.distinctUntilChanged().collect {
                        nearestAlarm = it
                        if (nearestAlarm == null) {
                            countDownTimer?.cancel()
                            view.setTextViewText(R.id.alarm_name, "Hello World")
                            view.setTextViewText(R.id.time, "00:00:00")
                            view.setViewVisibility(R.id.time_day, View.INVISIBLE)
                            emitNewNotification()
                            return@collect
                        }
                        view.setTextViewText(R.id.alarm_name, nearestAlarm!!.name)
                        startAlarmCountdown(nearestAlarm!!.alarmDate)
                        emitNewNotification()
                    }
                }
            }
        }

        return START_STICKY_COMPATIBILITY
    }


    override fun onDestroy() {
        // 发送错误关闭广播
        if (!closeComplete) {
            val intent = Intent(this, ForegroundRestartReceiver::class.java)
            intent.action = "error_close_foreground_service"
        }
        // 清除闹钟倒计时器，防止内存泄露
        countDownTimer?.cancel()
        super.onDestroy()
    }

    // 启动闹钟倒计时
    private fun startAlarmCountdown(alarmDate: Long) {
        countDownTimer?.cancel()
        val currentTime = System.currentTimeMillis()
        val millisInFuture = alarmDate - currentTime
        if (millisInFuture > 0) {
            countDownTimer = object : CountDownTimer(millisInFuture, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val remainTime = DateTimeUtil.convertFromTimestamp(millisUntilFinished)
                    val formattedTime = String.format("%02d:%02d:%02d", remainTime[1], remainTime[2], remainTime[3])
                    if (remainTime[0] == 0) {
                        view.setViewVisibility(R.id.time_day, View.INVISIBLE)
                    } else {
                        view.setViewVisibility(R.id.time_day, View.VISIBLE)
                        view.setTextViewText(R.id.time_day, "+${remainTime[0]}")
                    }
                    view.setTextViewText(R.id.time, formattedTime)
                    emitNewNotification()
                }

                override fun onFinish() {
                    view.setTextViewText(R.id.time, "00:00:00")
                    emitNewNotification()
                }
            }.start()
        } else {
            view.setTextViewText(R.id.time, "00:00:00")
            emitNewNotification()
        }
    }

    // 更新数据,发送新通知
    private fun emitNewNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        // 创建 PendingIntent
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = builder
            .setSmallIcon(R.drawable.app_icon)
            .setCustomContentView(view)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}