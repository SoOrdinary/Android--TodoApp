package com.todo.android.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID


/**
 * TaskAlarm实体类，用于记录task完成情况
 */
@Entity(tableName = "task_alarms",indices = [Index(value = ["alarm_date"])])
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,                       // 自增ID
    var name: String,                       // 闹钟名
    @ColumnInfo(name = "alarm_date")
    var alarmDate: Long,                    // 响铃日期（时间戳，毫秒值）
    var alarmWordId:UUID=UUID.randomUUID()  // 设置提醒的UUid,默认为随机
)
