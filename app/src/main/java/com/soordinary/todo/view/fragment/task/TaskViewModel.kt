package com.soordinary.todo.view.fragment.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.soordinary.todo.repository.TaskRepository
import com.soordinary.todo.data.room.entity.Task
import com.soordinary.todo.repository.UserRepository
import com.soordinary.todo.utils.DateTimeUtils
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

    private val taskRepository: TaskRepository = TaskRepository()
    private val userRepository: UserRepository = UserRepository()

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
            QueryType.FINISH_STATUS -> taskRepository.getTasksByFinish(isFinish)
            QueryType.TITLE_AND_FINISH -> taskRepository.getTasksByTitleAndFinish(title, isFinish)
            QueryType.TAG_AND_FINISH -> taskRepository.getTasksByTagAndFinish(tag, isFinish)
            QueryType.DUE_DATE_AND_FINISH -> taskRepository.getTasksByDueDateAndFinish(startDate, endDate, isFinish)
            else -> taskRepository.getTasksByDueDateAndFinish(0, DateTimeUtils.getEndOfDay(0), null)
        }
    }

    // 获取当前的个人头像LiveData
    fun getIconUriLiveData () = userRepository.userIconUriLiveData

    // 获取当前标签组
    fun getNowTaskTagsLiveData() = taskRepository.taskTagsLiveData

    // 插入任务
    fun insertTask(task: Task) = viewModelScope.launch {
        taskRepository.insertTask(task)
    }

    // 更新任务
    fun updateTask(task: Task) = viewModelScope.launch {
        taskRepository.updateTask(task)
    }

    // 删除任务
    fun deleteTask(task: Task) = viewModelScope.launch {
        taskRepository.deleteTask(task)
    }

    // 根据完成状态查询
    fun getTasksByFinish(isFinish: Boolean?) {
        this.isFinish = isFinish
        queryTypeLiveData.value = QueryType.FINISH_STATUS
    }

    // 根据标题和完成状态查询
    fun getTasksByTitleAndFinish(title: String, isFinish: Boolean?) {
        this.title = title
        this.isFinish = isFinish
        queryTypeLiveData.value = QueryType.TITLE_AND_FINISH
    }

    // 根据标签和完成状态查询
    fun getTasksByTagAndFinish(tag: String, isFinish: Boolean?) {
        this.tag = tag
        this.isFinish = isFinish
        queryTypeLiveData.value = QueryType.TAG_AND_FINISH
    }

    // 根据日期范围和完成状态查询
    fun getTasksByDueDateAndFinish(startDate: Long, endDate: Long, isFinish: Boolean?) {
        this.startDate = startDate
        this.endDate = endDate
        this.isFinish = isFinish
        queryTypeLiveData.value = QueryType.DUE_DATE_AND_FINISH
    }
}
