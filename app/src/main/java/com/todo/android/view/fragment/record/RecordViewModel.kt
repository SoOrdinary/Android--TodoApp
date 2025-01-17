package com.todo.android.view.fragment.record

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.todo.android.data.room.entity.RecordSo
import com.todo.android.utils.DateTimeUtils
import com.todo.android.repository.RecordRepository
import kotlinx.coroutines.launch

/**
 * 记录的ViewModel层，原理同TaskViewModel一致
 */
class RecordViewModel(
    private val queryTypeLiveData: MutableLiveData<QueryType> = MutableLiveData<QueryType>(QueryType.DUE_DATE),
    private var startDate: Long = DateTimeUtils.getStartOfDay(0),
    private var endDate: Long = DateTimeUtils.getEndOfDay(0)
): ViewModel() {

    private val recordRepository : RecordRepository = RecordRepository()

    // 查询种类
    enum class QueryType{
        DUE_DATE
    }

    // 缓存信息
    var recordList = ArrayList<RecordSo>()

    var recordLiveData = queryTypeLiveData.switchMap { queryType ->
        when (queryType) {
            QueryType.DUE_DATE->recordRepository.getRecordsByDateRange(startDate,endDate)
            else->recordRepository.getRecordsByDateRange(startDate,endDate)
        }
    }

    // 获取当前viewModel的日期
    fun getNowDay() = startDate

    fun insertRecord(recordSo: RecordSo)=  viewModelScope.launch{
        if (recordSo.finishTime>recordSo.planTime)recordSo.isTimeout=true
        recordRepository.insertRecord(recordSo)
    }

    fun deleteRecord(recordSo: RecordSo)=  viewModelScope.launch {
        recordRepository.deleteRecord(recordSo)
    }

    // 根据完成日期查询
    fun getRecordsByDateRange(startDate: Long, endDate: Long){
        this.startDate = startDate
        this.endDate = endDate
        queryTypeLiveData.value=QueryType.DUE_DATE
    }

}