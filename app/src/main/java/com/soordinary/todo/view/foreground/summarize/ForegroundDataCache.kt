package com.soordinary.todo.view.foreground.summarize

import com.soordinary.todo.data.room.entity.Alarm
import com.soordinary.todo.repository.AlarmRepository
import com.soordinary.todo.repository.TaskRepository
import com.soordinary.todo.utils.DateTimeUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest

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
    var nearestAlarm: Alarm? = null

    // 更新通知
    private val _timestampFlow = MutableStateFlow(System.currentTimeMillis())
    var timestampFlow: StateFlow<Long> = _timestampFlow
    fun update() {
        _timestampFlow.value = System.currentTimeMillis()
    }

    // 已完成任务数量的 Flow
    val completedTaskCountFlow: Flow<Int> =
        taskRepository.getCompletedTaskCount()

    // 某一时间段任务总数的 Flow
    val taskCountInTimeRangeFlow: Flow<Int> =
        taskRepository.getTaskCountInTimeRange(DateTimeUtil.getStartOfDay(0), DateTimeUtil.getEndOfDay(0))

    // 某一时间段完成任务数量的 Flow
    val completedTaskCountInTimeRangeFlow: Flow<Int> =
        taskRepository.getCompletedTaskCountInTimeRange(DateTimeUtil.getStartOfDay(0), DateTimeUtil.getEndOfDay(0))

    // 超时未完成任务数量的 Flow(需要随着时间更新)
    val overdueUncompletedTaskCountFlow: Flow<Int> = timestampFlow.flatMapLatest { timestamp ->
        taskRepository.getOverdueUncompletedTaskCount(timestamp)
    }

    // 最近的闹钟
    val nearestAlarmFlow: Flow<Alarm?> =
        alarmRepository.getNearestAlarm()


}