package com.todo.android.repository

import androidx.lifecycle.LiveData
import com.todo.android.TodoApplication
import com.todo.android.data.room.database.AlarmDatabase
import com.todo.android.data.room.entity.Alarm
import com.todo.android.data.room.entity.RecordSo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 闹钟事件提取仓库
 */
class AlarmRepository {

    private val alarmDao = AlarmDatabase.getDatabase(TodoApplication.context).alarmDao()

    // 插入记录
    suspend fun insertAlarm(alarm: Alarm) {
        withContext(Dispatchers.IO) {
            alarmDao.insert(alarm)
        }
    }

    // 删除记录
    suspend fun deleteAlarm(alarm: Alarm) {
        withContext(Dispatchers.IO) {
            alarmDao.delete(alarm)
        }
    }

    // 查询全部闹铃
    fun getAllAlarmsSortedByDate(): LiveData<List<Alarm>> =alarmDao.getAllAlarmsSortedByDate()
}