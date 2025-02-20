package com.soordinary.todo.repository

import com.soordinary.todo.TodoApplication
import com.soordinary.todo.data.room.database.RecordDatabase
import com.soordinary.todo.data.room.entity.RecordSo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 记录提取仓库--仅服务record界面
 */
class RecordRepository {

    private val recordDao = RecordDatabase.getDatabase(TodoApplication.context).recordDao()

    // 插入记录
    suspend fun insertRecord(recordSo: RecordSo) {
        if (recordSo.finishTime > recordSo.planTime) recordSo.isTimeout = true
        withContext(Dispatchers.IO) {
            recordDao.insert(recordSo)
        }
    }

    // 删除记录
    suspend fun deleteRecord(recordSo: RecordSo) {
        withContext(Dispatchers.IO) {
            recordDao.delete(recordSo)
        }
    }

    // 根据完成日期查询
    fun getRecordsByDateRange(startDate: Long, endDate: Long) =
        recordDao.getRecordsByDateRange(startDate, endDate)
}