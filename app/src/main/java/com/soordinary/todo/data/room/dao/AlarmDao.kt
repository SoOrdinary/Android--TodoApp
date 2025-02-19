package com.soordinary.todo.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.soordinary.todo.data.room.entity.Alarm
import kotlinx.coroutines.flow.Flow

/**
 * room数据库的Dao层--TaskAlarm
 */
@Dao
interface AlarmDao {
    @Insert
    fun insert(alarm: Alarm):Unit

    // 通过 id 删除 Alarm 数据
    @Query("DELETE FROM task_alarms WHERE id = :id")
    fun deleteById(id: Long):Unit

    // 通过 date 删除 Alarm 数据
    @Query("DELETE FROM task_alarms WHERE alarm_date <= :date")
    fun deleteByDate(date: Long):Unit

    // 根据 ID 查询闹铃记录
    @Query("SELECT * FROM task_alarms WHERE id = :id")
    fun getAlarmById(id: Long): Alarm

    // 查询全部闹铃并按响铃日期升序排序
    @Query("SELECT * FROM task_alarms ORDER BY alarm_date ASC")
    fun getAllAlarmsSortedByDate(): LiveData<List<Alarm>>

    // 获取响铃日期最近的 alarm
    @Query("SELECT * FROM task_alarms ORDER BY alarm_date ASC LIMIT 1")
    fun getNearestAlarm(): Flow<Alarm?>
}