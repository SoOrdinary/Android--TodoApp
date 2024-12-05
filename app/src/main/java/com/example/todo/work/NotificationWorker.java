package com.example.todo.work;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.todo.R;

public class NotificationWorker extends Worker {
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // 获取传递的参数
        String message = getInputData().getString("message"); // 获取闹钟的消息
        if (message != null) {
            // 显示通知
            showNotification(getApplicationContext(), message);
        } else {
            return Result.failure(); // 如果消息为空，返回失败
        }
        return Result.success();
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
    }
}

//    调用该work的简单模板
//    // 创建一个简单的任务
//    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
//            .setInitialDelay(10, TimeUnit.SECONDS) // 延迟10秒执行
//            .setInputData(new Data.Builder()
//                    .putString("message", "Your task is due!") // 传递消息数据
//                    .build())
//            .build();
//
//// 使用 WorkManager 提交任务
//        WorkManager.getInstance(requireContext()).enqueue(workRequest);