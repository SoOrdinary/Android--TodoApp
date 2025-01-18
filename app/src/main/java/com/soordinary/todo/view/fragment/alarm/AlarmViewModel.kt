package com.soordinary.todo.view.fragment.alarm

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.soordinary.todo.TodoApplication
import com.soordinary.todo.data.room.entity.Alarm
import com.soordinary.todo.data.room.entity.RecordSo
import com.soordinary.todo.repository.AlarmRepository
import com.soordinary.todo.repository.RecordRepository
import com.soordinary.todo.utils.DateTimeUtils
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Alarm的ViewModel层，因为只有一个查询方法，返回一次LiveData就可以了
 */
class AlarmViewModel : ViewModel() {

    private val recordRepository:RecordRepository = RecordRepository()
    private val alarmRepository: AlarmRepository = AlarmRepository()

    // 用于触发数据更新的标志(LiveData开始时设置值，才能触发第一次映射)
    private val _triggerUpdate = MutableLiveData<Unit>(Unit)
    val alarmLiveData: LiveData<List<Alarm>> = _triggerUpdate.switchMap {
        alarmRepository.getAllAlarmsSortedByDate()
    }

    // 缓存
    var willDoTime:Long = 0
    var alarmList = ArrayList<Alarm>()

    // 插入闹钟
    fun insertAlarm(alarm: Alarm) = viewModelScope.launch {
        // 同时增加通知的定时任务Todo：第一次时请求权限
        val inputData = Data.Builder()
            .putString("title", alarm.name)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<NotifyWork>()
            .setInitialDelay(alarm.alarmDate-System.currentTimeMillis(),TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(TodoApplication.context).enqueue(workRequest)
        alarm.alarmWordId=workRequest.id
        alarmRepository.insertAlarm(alarm)
        // 插入成功后触发更新并提醒用户
        _triggerUpdate.value = Unit
        Toast.makeText(TodoApplication.context, "Alarm set : " + DateTimeUtils.timestampToString(alarm.alarmDate), Toast.LENGTH_LONG).show()
        // 随后更新日志
        val currentTime = System.currentTimeMillis()
        val remain=DateTimeUtils.millisToMinutes(alarm.alarmDate-(currentTime/ 60000) * 60000)
        val record = RecordSo(
            content="设置定时任务：${alarm.name},倒计时${remain}分钟",
            planTime = alarm.alarmDate,
            finishTime = currentTime
        )
        recordRepository.insertRecord(record)
    }

    // 移除超时闹钟
    fun removeAlarm(alarm: Alarm) = viewModelScope.launch {
        alarmRepository.deleteAlarm(alarm)
        // 删除成功后触发更新
        _triggerUpdate.value = Unit
    }
    // 删除未使用闹钟
    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        WorkManager.getInstance(TodoApplication.context).cancelWorkById(alarm.alarmWordId)
        alarmRepository.deleteAlarm(alarm)
        // 删除成功后触发更新
        _triggerUpdate.value = Unit
    }
}
