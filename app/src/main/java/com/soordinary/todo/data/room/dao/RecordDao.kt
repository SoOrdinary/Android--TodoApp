package com.soordinary.todo.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.soordinary.todo.data.room.entity.RecordSo


/**
 * room数据库的Dao层--Record
 */
@Dao
interface RecordDao {
    @Insert
    fun insert(recordSo: RecordSo): Unit

    @Delete
    fun delete(recordSo: RecordSo): Unit

    // 根据 ID 查询记录
    @Query("SELECT * FROM record_so WHERE id = :id")
    fun getRecordById(id: Long): RecordSo

    // 根据完成日期查询记录并按完成时间升序排序
    @Query("SELECT * FROM record_so WHERE finish_time BETWEEN :startDate AND :endDate ORDER BY finish_time ASC")
    fun getRecordsByDateRange(startDate: Long, endDate: Long): LiveData<List<RecordSo>>
}