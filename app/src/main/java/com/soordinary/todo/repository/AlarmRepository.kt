package com.soordinary.todo.repository

import androidx.lifecycle.LiveData
import com.soordinary.todo.TodoApplication
import com.soordinary.todo.data.room.database.AlarmDatabase
import com.soordinary.todo.data.room.entity.Alarm
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