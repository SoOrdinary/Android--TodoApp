package com.soordinary.todo.view.fragment.alarm

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soordinary.todo.R
import com.soordinary.todo.data.room.entity.Alarm
import com.soordinary.todo.databinding.FragmentAlarmItemBinding
import com.soordinary.todo.utils.DateTimeUtil


/**
 * Alarm的列表适配器，原理同TaskAdapter基本一致
 *
 * @role1 当时间为超时时间时，加入一个非阻塞协程在30s后删除该项的数据库存储
 */
class AlarmAdapter(private val fragment: AlarmFragment, private val alarmList: List<Alarm>) : RecyclerView.Adapter<AlarmAdapter.BaseViewHolder>() {

    init {
        setHasStableIds(true)
    }

    // 缓存所有TextView
    val textViewList: MutableSet<TextView> = HashSet()

    // 点击事件适配
    val listenAlarmItemClick = fragment.ListenAlarmItemClick()

    // 内部基类，简化多种适配item与bind的书写
    abstract inner class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(alarm: Alarm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.fragment_alarm_item, parent, false)
        return ItemViewHolder(view)
    }

    // 重写函数，依次说明布局类型、item个数、声明绑定(调用对应布局Holder重写的binding函数)
    override fun getItemCount() = alarmList.size
    override fun getItemId(position: Int) = alarmList[position].id
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) = holder.bind(alarmList[position])

    inner class ItemViewHolder(view: View) : BaseViewHolder(view) {
        private val binding = FragmentAlarmItemBinding.bind(view)
        override fun bind(alarm: Alarm) {
            with(binding) {
                textViewList.add(binding.remainTime)
                alarmItem.setOnLongClickListener(null)
                alarmItem.setOnLongClickListener(null)
                delete.setOnClickListener(null)
                // UI绑定[统一绑定一个时间计时器，逻辑写入Fragment]
                alarmName.text = alarm.name
                val remain = DateTimeUtil.millisToMinutes(alarm.alarmDate - (System.currentTimeMillis() / 60000) * 60000)
                if (remain > 0) {
                    remainTime.text = "${remain} 分钟"
                    remainTime.setTextColor(Color.parseColor("#018786"))
                } else {
                    remainTime.text = "时间到"
                    remainTime.setTextColor(Color.RED)
                }
                alarmDate.text = DateTimeUtil.timestampToString(alarm.alarmDate)
                // 事件绑定
                alarmItem.setOnClickListener {
                    listenAlarmItemClick.onClickItem(binding)
                }
                alarmItem.setOnLongClickListener {
                    listenAlarmItemClick.onLongClickItem(alarm.name, alarm.alarmDate)
                    true
                }
                delete.setOnClickListener {
                    listenAlarmItemClick.onClickToDelete(alarm)
                }
            }
        }
    }
}