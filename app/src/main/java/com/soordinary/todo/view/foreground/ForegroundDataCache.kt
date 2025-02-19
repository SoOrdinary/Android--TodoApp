package com.soordinary.todo.view.foreground

import androidx.lifecycle.ViewModel
import com.soordinary.todo.data.room.entity.Alarm
import com.soordinary.todo.repository.AlarmRepository
import com.soordinary.todo.repository.TaskRepository
import com.soordinary.todo.utils.DateTimeUtils
import kotlinx.coroutines.flow.Flow

/**
 * 用于前台显示的数据缓存
 */
class ForegroundDataCache {

    private val taskRepository: TaskRepository = TaskRepository()
    private val alarmRepository: AlarmRepository = AlarmRepository()

    // 缓存
    var completedTaskCount = 0
    var taskCountInTimeRange = 0
    var completedTaskCountInTimeRange = 0
    var overdueUncompletedTaskCount = 0
    var nearestAlarm:Alarm? = null

    // 已完成任务数量的 Flow
    val  completedTaskCountFlow: Flow<Int> =
        taskRepository.getCompletedTaskCount()
    // 某一时间段任务总数的 Flow
    val taskCountInTimeRangeFlow: Flow<Int> =
        taskRepository.getTaskCountInTimeRange(DateTimeUtils.getStartOfDay(0), DateTimeUtils.getEndOfDay(0))
    // 某一时间段完成任务数量的 Flow
    val completedTaskCountInTimeRangeFlow: Flow<Int> =
        taskRepository.getCompletedTaskCountInTimeRange(DateTimeUtils.getStartOfDay(0), DateTimeUtils.getEndOfDay(0))
    // 超时未完成任务数量的 Flow
    val overdueUncompletedTaskCountFlow: Flow<Int> =
        taskRepository.getOverdueUncompletedTaskCount(System.currentTimeMillis())
    // 最近的闹钟
    val nearestAlarmFlow: Flow<Alarm?> =
        alarmRepository.getNearestAlarm()


}