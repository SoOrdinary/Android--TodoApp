package com.todo.android.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.todo.android.data.room.entity.Alarm

/**
 * room数据库的Dao层--TaskAlarm
 */
@Dao
interface AlarmDao {
    @Insert
    fun insert(alarm: Alarm):Unit

    @Delete
    fun delete(alarm: Alarm):Unit

    // 根据 ID 查询闹铃记录
    @Query("SELECT * FROM task_alarms WHERE id = :id")
    fun getAlarmById(id: Long): Alarm

    // 查询全部闹铃并按响铃日期升序排序
    @Query("SELECT * FROM task_alarms ORDER BY alarm_date ASC")
    fun getAllAlarmsSortedByDate(): LiveData<List<Alarm>>
}