package com.todo.android.view.fragment.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.todo.android.repository.TaskRepository
import com.todo.android.data.room.entity.Task
import com.todo.android.utils.DateTimeUtils
import kotlinx.coroutines.launch

/**
 * Task的ViewModel层
 *
 * @role1 查询room数据库Task的相关任务并保存结果
 * @role2 Todo:查询当天天气并用于显示
 *
 * @explain1 ViewModel主构造属性中赋值了 |查询方式+五种查询条件| 可默认值或传参修改，以及调用函数修改，默认为查询当天的所有事件
 * @explain2 查询会返回各种liveData，最后会通过映射只需要观察taskLiveData就可以了
 */
class TaskViewModel(
    private val queryTypeLiveData: MutableLiveData<QueryType> = MutableLiveData<QueryType>(QueryType.DUE_DATE_AND_FINISH),
    private var isFinish: Boolean? = null,
    private var title: String = "",
    private var tag: String = "",
    private var startDate: Long = DateTimeUtils.getStartOfDay(0),
    private var endDate: Long = DateTimeUtils.getEndOfDay(0)
) : ViewModel() {
    private val repository: TaskRepository = TaskRepository()

    // 缓存一些数据或信息
    var taskList = ArrayList<Task>()
    var listCount = 1
    var listType = 1

    // 定义查询类型的枚举
    enum class QueryType {
        FINISH_STATUS,
        TITLE_AND_FINISH,
        TAG_AND_FINISH,
        DUE_DATE_AND_FINISH
    }

    // 根据查询类型和参数来返回不同的查询结果
    var taskLiveData: LiveData<List<Task>> = queryTypeLiveData.switchMap { queryType ->
        when (queryType) {
            QueryType.FINISH_STATUS -> repository.getTasksByFinish(isFinish)
            QueryType.TITLE_AND_FINISH -> repository.getTasksByTitleAndFinish(title, isFinish)
            QueryType.TAG_AND_FINISH -> repository.getTasksByTagAndFinish(tag, isFinish)
            QueryType.DUE_DATE_AND_FINISH -> repository.getTasksByDueDateAndFinish(startDate, endDate, isFinish)
            else -> repository.getTasksByDueDateAndFinish(0, DateTimeUtils.getEndOfDay(0), null)
        }
    }

    // 插入任务
    fun insert(task: Task) = viewModelScope.launch {
        repository.insert(task)
    }

    // 更新任务
    fun update(task: Task) = viewModelScope.launch {
        repository.update(task)
    }

    // 删除任务
    fun delete(task: Task) = viewModelScope.launch {
        repository.delete(task)
    }

    // 设置完成状态查询
    fun setFinishStatus(isFinish: Boolean?) {
        this.isFinish = isFinish
        queryTypeLiveData.value = QueryType.FINISH_STATUS
    }

    // 设置标题和完成状态查询
    fun setTitleSearch(title: String, isFinish: Boolean?) {
        this.title = title
        this.isFinish = isFinish
        queryTypeLiveData.value = QueryType.TITLE_AND_FINISH
    }

    // 设置标签和完成状态查询
    fun setTagSearch(tag: String, isFinish: Boolean?) {
        this.tag = tag
        this.isFinish = isFinish
        queryTypeLiveData.value = QueryType.TAG_AND_FINISH
    }

    // 设置日期范围和完成状态查询
    fun setDueDateRange(startDate: Long, endDate: Long, isFinish: Boolean?) {
        this.startDate = startDate
        this.endDate = endDate
        this.isFinish = isFinish
        queryTypeLiveData.value = QueryType.DUE_DATE_AND_FINISH
    }
}