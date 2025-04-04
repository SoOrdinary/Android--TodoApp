package com.soordinary.todo.view.fragment.record

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.soordinary.todo.R
import com.soordinary.todo.component.ItemSlideDeleteCallback
import com.soordinary.todo.data.room.entity.RecordSo
import com.soordinary.todo.databinding.FragmentRecordItemBinding
import com.soordinary.todo.utils.DateTimeUtil


/**
 * Record的列表适配器，原理同TaskAdapter基本一致
 */
class RecordAdapter(private val fragment: RecordFragment, private val recordList: List<RecordSo>) : RecyclerView.Adapter<RecordAdapter.BaseViewHolder>(), ItemSlideDeleteCallback.SlideDeleteListener {

    init {
        setHasStableIds(true)
    }

    // 点击事件适配
    val listenRecordItemClick = fragment.ListenRecordItemClick()

    // 内部基类，简化多种适配item与bind的书写
    abstract inner class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(recordSo: RecordSo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.fragment_record_item, parent, false)
        return ItemViewHolder(view)
    }

    // 重写函数，依次说明布局类型、item个数、声明绑定(调用对应布局Holder重写的binding函数)
    override fun getItemCount() = recordList.size
    override fun getItemId(position: Int) = recordList[position].id
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) = holder.bind(recordList[position])

    inner class ItemViewHolder(view: View) : BaseViewHolder(view) {
        private val binding = FragmentRecordItemBinding.bind(view)
        override fun bind(recordSo: RecordSo) {
            with(binding) {
                recordItem.setOnLongClickListener(null)
                // UI渲染
                var finishTime = DateTimeUtil.convertFromTimestamp(recordSo.finishTime)
                val formattedTime = String.format("%02d:%02d", (finishTime[1] + 8) % 24, finishTime[2])
                time.text = formattedTime
                if (recordSo.isTimeout) {
                    time.setTextColor(Color.RED)
                } else {
                    time.setTextColor(Color.parseColor("#018786"))
                }
                // 用户自定义日志则自定义写法
                record.text = recordSo.content
                // 点击事件
                recordItem.setOnLongClickListener {
                    listenRecordItemClick.onLongClickItem(recordSo)
                    true
                }
            }
        }
    }

    override fun onSwipedItem(position: Int) {
        listenRecordItemClick.onSwipedItem(recordList[position])
    }
}