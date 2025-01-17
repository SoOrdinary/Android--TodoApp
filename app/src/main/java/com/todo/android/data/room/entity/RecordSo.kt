package com.todo.android.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * RecordSo实体类，用于记录task完成情况(Record与库函数重名)
 */
@Entity(tableName = "record_so",indices = [Index(value = ["finish_time"])])
data class RecordSo(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,                   // 自增ID
    @ColumnInfo(name = "is_auto")
    val isAuto:Boolean,                 // 是否为用户自定义日志
    var Name:String,                    // 记录名
    @ColumnInfo(name = "plan_time")
    var planTime:Long,                  // 计划完成日期（时间戳，毫秒值）
    @ColumnInfo(name = "finish_time")
    var finishTime: Long,               // 完成日期（时间戳，毫秒值）
    @ColumnInfo(name = "is_timeout")
    var isTimeout: Boolean,             // 任务是否超时 (true: 超时, false: 未超时)
)