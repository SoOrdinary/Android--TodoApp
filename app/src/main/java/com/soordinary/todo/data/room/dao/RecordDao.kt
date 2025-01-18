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
    fun insert(recordSo: RecordSo):Unit

    @Delete
    fun delete(recordSo: RecordSo):Unit

    // 根据 ID 查询记录
    @Query("SELECT * FROM record_so WHERE id = :id")
    fun getRecordById(id: Long): RecordSo

    // 根据完成日期查询记录并按完成时间升序排序
    @Query("SELECT * FROM record_so WHERE finish_time BETWEEN :startDate AND :endDate ORDER BY finish_time ASC")
    fun getRecordsByDateRange(startDate: Long, endDate: Long): LiveData<List<RecordSo>>

    // 根据是否超时查询记录并按完成时间升序排序
    @Query("SELECT * FROM record_so WHERE is_timeout = :isTimeout ORDER BY finish_time ASC")
    fun getRecordsByTimeoutStatus(isTimeout: Boolean): LiveData<List<RecordSo>>

    // 根据任务名模糊匹配查询记录并按完成时间降序排序
    @Query("SELECT * FROM record_so WHERE content LIKE '%' || :content || '%' ORDER BY finish_time DESC")
    fun getRecordsByContent(content: String): LiveData<List<RecordSo>>

}