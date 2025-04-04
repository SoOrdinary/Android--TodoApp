package com.soordinary.todo.view.foreground.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.app.NotificationCompat
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleService
import com.soordinary.todo.R
import com.soordinary.todo.data.network.file.DownloadListener
import com.soordinary.todo.data.network.file.DownloadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * 用于下载的前台服务，下载进度将在通知里显示，需要传入url
 *
 * @limit 需要传入url、期望文件名、文件存储位置
 */
class DownloadService : LifecycleService() {

    companion object {
        // 静态打开方法，指明打开该类需要哪些参数
        fun serviceStart(context: Context, url: String, name: String, path: String) {
            val serviceIntent = Intent(context, DownloadService::class.java).apply {
                putExtra("url", url)
                putExtra("name", "//$name")
                putExtra("path", path)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    private val CHANNEL_ID = "前台显示下载进度"
    private val NOTIFICATION_ID = 998
    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder
    private var downloadTask: DownloadTask? = null
    private lateinit var downloadUrl: String
    private lateinit var fileName: String
    private lateinit var directory: String
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // 在onCreate中创建渠道和显示第一次通知
    override fun onCreate() {
        super.onCreate()

        // 缓存
        notificationManager = getSystemService(NotificationManager::class.java)
        // 创建渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "前台显示下载进度",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(serviceChannel)
        }
        builder = NotificationCompat.Builder(this, CHANNEL_ID)
        // 绑定前台通知
        val notification = builder
            .setSmallIcon(R.drawable.app_icon)
            .setContentText("启动下载...")
            .setAutoCancel(true)
            .build()
        // 修正通知 ID，保持一致
        startForeground(NOTIFICATION_ID, notification)
        Toast.makeText(this@DownloadService, "启动下载中...", Toast.LENGTH_SHORT).show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent != null) {
            downloadTask = DownloadTask(listener)
            downloadUrl = intent.getStringExtra("url")!!
            fileName = intent.getStringExtra("name")!!
            directory = intent.getStringExtra("path")!!
            coroutineScope.launch {
                val status = downloadTask?.download(downloadUrl, directory, fileName) ?: DownloadTask.TYPE_FAILED
                when (status) {
                    DownloadTask.TYPE_SUCCESS -> listener.onSuccess()
                    DownloadTask.TYPE_FAILED -> listener.onFailed()
                }
            }
        }
        return START_STICKY
    }

    // 接口方法
    private val listener = object : DownloadListener {

        override fun onProgress(progress: Int) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            emitNewNotification("下载中..", progress, null)
        }

        // 下载成功
        override fun onSuccess() {
            downloadTask = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            val file = File(directory, fileName)
            if (file.exists()) {
                val uri: Uri = FileProvider.getUriForFile(
                    this@DownloadService,
                    "${packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, getMimeType(file))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(packageManager) != null) {
                    // 创建 PendingIntent
                    val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(
                        this@DownloadService,
                        0,
                        intent,
                        pendingIntentFlags
                    )
                    // 显示通知
                    emitNewNotification("下载成功", 100, pendingIntent)
                    Toast.makeText(this@DownloadService, "下载完成，点击通知以安装", Toast.LENGTH_SHORT).show()
                } else {
                    // 没有合适的应用处理该 Intent，显示提示信息
                    Toast.makeText(this@DownloadService, "没有找到合适的应用来打开该文件", Toast.LENGTH_SHORT).show()
                }
            } else {
                emitNewNotification("下载失败", 0, null)
                Toast.makeText(this@DownloadService, "下载失败", Toast.LENGTH_SHORT).show()
            }
            // 停止服务
            stopSelf()
        }

        // 下载失败
        override fun onFailed() {
            downloadTask = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            emitNewNotification("下载失败", 0, null)
            Toast.makeText(this@DownloadService, "下载失败", Toast.LENGTH_SHORT).show()
            stopSelf()
        }
    }

    // 获取通知的方法
    private fun emitNewNotification(title: String, progress: Int, pendingIntent: PendingIntent?) {
        val notification = builder
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setContentText("$progress%")
            .setProgress(100, progress, false)
        if (pendingIntent != null) {
            notification.setContentIntent(pendingIntent)
        }
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    // 查找文件类型并返回
    private fun getMimeType(file: File): String {
        val fileName = file.name
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex != -1) {
            val fileExtension = fileName.substring(lastDotIndex + 1).lowercase()
            when (fileExtension) {
                "pdf" -> "application/pdf"
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "apk" -> "application/vnd.android.package-archive" // 新增对 APK 文件的处理
                else -> "application/octet-stream"
            }
        } else {
            "application/octet-stream"
        }
    }
}