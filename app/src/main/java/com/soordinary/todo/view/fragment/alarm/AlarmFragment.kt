package com.soordinary.todo.view.fragment.alarm

import android.app.Dialog
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.soordinary.todo.R
import com.soordinary.todo.data.room.entity.Alarm
import com.soordinary.todo.databinding.DialogAlarmAddDateBinding
import com.soordinary.todo.databinding.DialogAlarmAddRemainBinding
import com.soordinary.todo.databinding.FragmentAlarmBinding
import com.soordinary.todo.databinding.FragmentAlarmItemBinding
import com.soordinary.todo.utils.DateTimeUtil
import com.soordinary.todo.utils.DateTimeUtil.getSeparatedStringFromTimestamp
import com.soordinary.todo.utils.DateTimeUtil.timestampToString
import com.soordinary.todo.utils.Diff

/**
 * 应用的第三个Fragment--alarm，列出当前所有alarm
 *
 * @role1 获取所有提醒事件并列出
 * @role2 将提醒事件放入沉浸模式自习
 * @role3 用定时器更新UI
 * @role4 通知
 *
 */
class AlarmFragment : Fragment(R.layout.fragment_alarm) {

    // 适配器需要使用该viewModel
    val viewModel: AlarmViewModel by activityViewModels()
    private lateinit var binding: FragmentAlarmBinding

    // 设置定时器
    private lateinit var handler: Handler
    private lateinit var alarmRunnable: Runnable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmBinding.bind(view)

        // 初始化RecycleView的配置
        binding.alarmList.apply {
            layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            adapter = AlarmAdapter(this@AlarmFragment, viewModel.alarmList)
        }

        // 每秒检查一次，当前分钟数变化就触发更新
        var lastMinute = -1 // 用来存储上一次的分钟数
        handler = Handler(Looper.getMainLooper())
        alarmRunnable = object : Runnable {
            override fun run() {
                // willDoTime大于当前时间，更新UI与viewModel
                val currentTime = System.currentTimeMillis()
                if (viewModel.willDoTime > currentTime) {
                    val remainTime = DateTimeUtil.convertFromTimestamp(viewModel.willDoTime - currentTime)
                    val formattedTime = String.format("%02d:%02d:%02d", remainTime[1], remainTime[2], remainTime[3])
                    binding.willDo.text = viewModel.willDoName
                    binding.time.text = formattedTime
                    if (remainTime[0] == 0) {
                        binding.timeDay.visibility = View.INVISIBLE
                    } else {
                        binding.timeDay.visibility = View.VISIBLE
                        binding.timeDay.text = "+${remainTime[0]}"
                    }
                } else {
                    // 保证只在倒计时为0时只提醒一次
                    if (viewModel.willDoTime != 0L) {
                        viewModel.willDoName = "Hello world"
                        binding.time.text = "00:00:00"
                        binding.timeDay.visibility = View.INVISIBLE
                        viewModel.willDoTime = 0
                    }
                }
                // 当前分钟数变化就触发更新列表分钟数
                val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
                if (currentMinute != lastMinute) {
                    lastMinute = currentMinute
                    // 优化，不再需要视图全部渲染
                    for (remainTimeView in (binding.alarmList.adapter as? AlarmAdapter)?.textViewList ?: ArrayList<TextView>()) {
                        val currentText = remainTimeView.text.toString()
                        if (currentText == "时间到") {
                            continue
                        } else {
                            val remain = currentText.dropLast(3).toInt() - 1
                            if (remain > 0) {
                                remainTimeView.text = "${remain} 分钟"
                                remainTimeView.setTextColor(Color.parseColor("#018786"))
                            } else {
                                remainTimeView.text = "时间到"
                                remainTimeView.setTextColor(Color.RED)
                            }
                        }
                    }
                }
                // 继续每秒钟检查一次
                handler.postDelayed(this, 1000)
            }
        }
        // 开始定时器
        handler.postDelayed(alarmRunnable, 0)

        binding.initLiveData()
        binding.initClick()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 移除所有已经调度的任务
        handler.removeCallbacks(alarmRunnable)
    }

    // 观察事件 Todo:删除总会让最后一个alarm闪一下
    private fun FragmentAlarmBinding.initLiveData() {
        // 观察数据变化实时更新ViewModel的缓存并通知列表更新
        with(viewModel) {
            alarmLiveData.observe(viewLifecycleOwner) {
                val oldAlarm = ArrayList<Alarm>(alarmList)
                alarmList.clear()
                alarmList.addAll(it)
                val diffTask = Diff<Alarm> { old, new -> old.id == new.id }
                diffTask.buildCRD(oldAlarm, alarmList, { binding.alarmList.adapter?.notifyItemRemoved(it) }, { binding.alarmList.adapter?.notifyItemInserted(it) })
            }
        }
    }

    // 点击事件
    private fun FragmentAlarmBinding.initClick() {
        model.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            var willDoTime = 0L
            if (viewModel.willDoTime > currentTime) {
                willDoTime = viewModel.willDoTime
            }
            // 启动沉浸模式并传入时间
            AlarmViewActivity.actionStart(requireActivity(), willDoTime)
        }
    }

    /**
     * 用于alarm的列表项的点击事件处理
     */
    inner class ListenAlarmItemClick {

        // 单击底部栏添加按钮时
        fun onClickAdd() {
            with(DialogAlarmAddRemainBinding.inflate(LayoutInflater.from(requireActivity()))) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                // 点击确定插入该时间
                confirm.setOnClickListener {
                    // 校验输入，错误则直接返回
                    if (!checkInput(alarmName, alarmDueDateDay, alarmDueDateHour, alarmDueDateMinute)) return@setOnClickListener
                    val day = alarmDueDateDay.text.toString().toInt()
                    val hour = alarmDueDateHour.text.toString().toInt()
                    val minute = alarmDueDateMinute.text.toString().toInt()
                    // 约去毫秒
                    val dueTimestamp = ((System.currentTimeMillis() + DateTimeUtil.convertToTimestamp(day, hour, minute)) / 60000) * 60000
                    val alarm = Alarm(
                        name = alarmName.text.toString().trim(),
                        alarmDate = dueTimestamp
                    )
                    viewModel.insertAlarm(alarm)

                    dialog.dismiss()
                }

                dialog.show()
            }
        }

        // 长按底部加号时
        fun onLongClickAdd() {
            with(DialogAlarmAddDateBinding.inflate(LayoutInflater.from(requireActivity()))) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                // 分隔时间
                val parts = getSeparatedStringFromTimestamp(timestampToString(System.currentTimeMillis()))
                val day = parts[0]
                val hour = parts[1]
                val minute = parts[2]
                // 绑定时间
                alarmDueDateDay.setText(day)
                alarmDueDateHour.setText(hour)
                alarmDueDateMinute.setText(minute)

                // 点击确定插入该时间
                confirm.setOnClickListener {
                    // 校验输入，错误则直接返回
                    if (!checkInput(alarmName, alarmDueDateDay, alarmDueDateHour, alarmDueDateMinute)) return@setOnClickListener
                    // 成功则插入数据
                    val dueTimestamp = DateTimeUtil.stringToTimestamp("${alarmDueDateDay.text.toString().trim()}  ${alarmDueDateHour.text.toString().trim()}:${alarmDueDateMinute.text.toString().trim()}")
                    val alarm = Alarm(
                        name = alarmName.text.toString().trim(),
                        alarmDate = dueTimestamp
                    )
                    viewModel.insertAlarm(alarm)
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

        // 校验函数(每次修改要保持和TaskFragment中的checkInput同步)
        private fun checkInput(taskTitle: EditText, taskDueDateDay: EditText, taskDueDateHour: EditText, taskDueDateMinute: EditText): Boolean {

            // 校验任务标题
            if (taskTitle.text.isNullOrEmpty()) {
                Toast.makeText(requireActivity(), "标题不可为空", Toast.LENGTH_SHORT).show()
                return false // 如果标题为空，返回 false
            }

            // 校验日期输入
            if (taskDueDateDay.text.isNullOrEmpty() || taskDueDateHour.text.isNullOrEmpty() || taskDueDateMinute.text.isNullOrEmpty()) {
                Toast.makeText(requireActivity(), "日期不可为空", Toast.LENGTH_SHORT).show()
                return false // 如果日期不完整，返回 false
            }
            // Todo:检测具体时间是否规范

            // 所有校验通过，返回 true
            return true
        }


        // 点击表项可查看响铃具体时间
        fun onClickItem(binding: FragmentAlarmItemBinding) {
            binding.date.visibility = if (binding.date.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // 长按表项的时间可添加至计时器中
        fun onLongClickItem(name: String, willDoTime: Long) {
            val currentTime = System.currentTimeMillis()
            if (willDoTime > currentTime) {
                viewModel.willDoName = name
                viewModel.willDoTime = willDoTime
                // 因为定时器会有一点点延迟，需要直接更新一下UI
                binding.willDo.text = name
                val remainTime = DateTimeUtil.convertFromTimestamp(viewModel.willDoTime - currentTime)
                val formattedTime = String.format("%02d:%02d:%02d", remainTime[1], remainTime[2], remainTime[3])
                binding.time.text = formattedTime
                if (remainTime[0] == 0) {
                    binding.timeDay.visibility = View.INVISIBLE
                } else {
                    binding.timeDay.visibility = View.VISIBLE
                    binding.timeDay.text = "+${remainTime[0]}"
                }
            } else {
                binding.willDo.text = name
                binding.time.text = "00:00:00"
                viewModel.willDoTime = currentTime
            }
        }

        // 单击删除键删除该闹钟
        fun onClickToDelete(alarm: Alarm) {
            viewModel.deleteAlarm(alarm)
            // 删除该部件后需要把菜单栏收回，不然视图重用会效果出错
            binding.alarmList.closeMenu()
        }

    }
}