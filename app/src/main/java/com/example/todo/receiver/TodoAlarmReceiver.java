package com.example.todo.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.todo.R;

public class TodoAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 在接收到广播时，执行通知
        String message = intent.getStringExtra("message"); // 获取闹钟的消息
        showNotification(context, message);
    }

    // 显示通知
    private void showNotification(Context context, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 如果你使用的是 Android 8.0 以上版本，需要设置渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "todo_notification_channel";
            // 渠道ID、渠道名称、渠道重要性
            NotificationChannel channel = new NotificationChannel(channelId, "Todo Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            // 注册渠道
            notificationManager.createNotificationChannel(channel);
        }
        // 给通知加点击事件，用PendingIntent

        // 创建通知
        Notification notification = new NotificationCompat.Builder(context, "todo_notification_channel")
                .setContentTitle("Task Reminder")
                .setContentText(message)
                .setSmallIcon(R.drawable.app_icon)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(1, notification);  // 1 是通知的ID
        Toast.makeText(context, "notify", Toast.LENGTH_SHORT).show();
    }
}