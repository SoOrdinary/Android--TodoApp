package com.todo.android.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.todo.android.utils.DateTimeUtils
import java.util.Objects

/**
 * 任务的实体关系类
 *
 * @property
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,                    // 自增ID
    var title: String,                  // 标题
    var subtitle: String,               // 副标题
    var details: String,                // 具体内容
    var voice: String?,                 // 录音
    var image: String?,                 // 图片URI Todo：多图
    @ColumnInfo(name = "due_date")
    var dueDate: Long?,                 // 截止日期（时间戳，毫秒值）
    @ColumnInfo(name = "is_finish")
    var isFinish: Boolean,              // 任务是否完成 (true: 已完成, false: 未完成)
    var tag: String                     // 任务标签
)