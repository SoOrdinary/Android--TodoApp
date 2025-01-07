package com.todo.android.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.todo.android.utils.DateTimeUtils
import java.util.Objects

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,                  // 自增ID
    var title: String,                // 标题
    var subtitle: String,             // 副标题
    var details: String,              // 具体内容
    @ColumnInfo(name = "due_date")
    var dueDate: Long?,               // 截止日期（时间戳，毫秒值）
    @ColumnInfo(name = "is_finish")
    var isFinish: Boolean?,           // 任务是否完成 (true: 已完成, false: 未完成)
    var coverImage: String?,         // 封面图片URI
    var tag: String?                  // 任务标签
) {

    // 如果你需要自定义的 toString 方法
    override fun toString(): String {
        return "Task{id=$id, title='$title', subtitle='$subtitle', details='$details', dueDate='${DateTimeUtils.timestampToString(dueDate)}', isFinish=$isFinish, coverImage='$coverImage', tag='$tag'}"
    }

    // equals 和 hashCode 方法
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val task = other as Task
        return id == task.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }
}
