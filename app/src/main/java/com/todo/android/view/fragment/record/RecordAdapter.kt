package com.todo.android.view.fragment.record

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.todo.android.data.room.entity.Alarm
import com.todo.android.data.room.entity.RecordSo
import com.todo.android.data.room.entity.Task
import com.todo.android.databinding.FragmentAlarmItemBinding
import com.todo.android.databinding.FragmentRecordBinding
import com.todo.android.view.fragment.alarm.AlarmAdapter
import com.todo.android.view.fragment.task.TaskAdapter.BaseViewHolder


/**
 * Record的列表适配器，原理同TaskAdapter基本一致
 */
class RecordAdapter(private val fragment: RecordFragment, private val recordList: List<RecordSo>, private val itemType: Int) : RecyclerView.Adapter<RecordAdapter.BaseViewHolder>(){

    // 点击事件适配
    val listenRecordItemClick = fragment.ListenRecordItemClick()
    // 内部基类，简化多种适配item与bind的书写
    abstract inner class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(recordSo: RecordSo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        TODO("Not yet implemented")
    }

    // 三个重写函数，依次说明布局类型、item个数、声明绑定(调用对应布局Holder重写的binding函数)
    override fun getItemViewType(position: Int) =  0
    override fun getItemCount() = recordList.size
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) = holder.bind(recordList[position])

    inner class ItemViewHolder(view:View): BaseViewHolder(view){
        private val binding = FragmentRecordBinding.bind(view)
        override fun bind(recordSo: RecordSo) {

        }
    }
}