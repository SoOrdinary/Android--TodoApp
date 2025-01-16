package com.todo.android.view.fragment.alarm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.todo.android.data.room.entity.Alarm
import com.todo.android.repository.AlarmRepository
import kotlinx.coroutines.launch

/**
 * Alarm的ViewModel层，因为只有一个查询方法，返回一次LiveData就可以了
 */
class AlarmViewModel : ViewModel() {

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
        alarmRepository.insertAlarm(alarm)
        // 插入成功后触发更新
        _triggerUpdate.value = Unit
    }

    // 删除闹钟
    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        alarmRepository.deleteAlarm(alarm)
        // 删除成功后触发更新
        _triggerUpdate.value = Unit
    }
}
