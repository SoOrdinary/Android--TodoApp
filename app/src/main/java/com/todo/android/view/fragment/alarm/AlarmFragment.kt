package com.todo.android.view.fragment.alarm

import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.todo.android.R
import com.todo.android.data.room.entity.Alarm
import com.todo.android.databinding.FragmentAlarmAddBinding
import com.todo.android.databinding.FragmentAlarmBinding
import com.todo.android.databinding.FragmentAlarmItemBinding
import com.todo.android.utils.DateTimeUtils

/**
 * 应用的第三个Fragment--alarm，列出当前所有alarm
 *
 * @role1 获取所有提醒事件并列出
 * @role2 将提醒事件放入沉浸模式自习
 * @role3 用定时器更新UI
 * Todo:通知
 */
class AlarmFragment: Fragment(R.layout.fragment_alarm)  {

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
        binding.alarmList.apply{
            layoutManager = LinearLayoutManager(this@AlarmFragment.requireActivity(),LinearLayoutManager.VERTICAL,false)
            adapter = AlarmAdapter(this@AlarmFragment,viewModel.alarmList,0)
        }

        // 每秒检查一次，当前分钟数变化就触发更新
        var lastMinute = -1 // 用来存储上一次的分钟数
        handler = Handler(Looper.getMainLooper())
        alarmRunnable = object : Runnable {
            override fun run() {
                // willDoTime大于当前时间，更新UI与viewModel
                val currentTime =System.currentTimeMillis()
                if (viewModel.willDoTime > currentTime) {
                    val time = DateTimeUtils.convertFromTimestamp(viewModel.willDoTime-currentTime)
                    binding.time.text = "${time[1]}:${time[2]}:${time[3]}"
                    if (time[0] == 0) {
                        binding.timeDay.visibility = View.INVISIBLE
                    } else {
                        binding.timeDay.visibility = View.VISIBLE
                        binding.timeDay.text = "+${time[0]}"
                    }
                }else{
                    // 保证只在倒计时为0时只提醒一次
                    if(viewModel.willDoTime !=0L){
                        binding.time.text ="0:0:0"
                        binding.timeDay.visibility = View.INVISIBLE
                        viewModel.willDoTime = 0
                        //Todo:通知提醒
                    }
                }
                // 当前分钟数变化就触发更新列表分钟数
                val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
                if (currentMinute != lastMinute) {
                    lastMinute = currentMinute
                    binding.alarmList.adapter?.notifyDataSetChanged()
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

    // 观察事件
    private fun FragmentAlarmBinding.initLiveData(){
        // 观察数据变化实时更新ViewModel的缓存并通知列表更新
        viewModel.alarmLiveData.observe(viewLifecycleOwner){
            viewModel.alarmList.clear()
            viewModel.alarmList.addAll(it)
            // Todo:优化更新方式
            binding.alarmList.adapter?.notifyDataSetChanged()
        }
    }

    // 点击事件
    private fun FragmentAlarmBinding.initClick(){
        model.setOnClickListener {
            // 启动沉浸模式并传入时间
            AlarmActivity.actionStart(requireActivity(),12)
        }
    }

    /**
     * 用于alarm的列表项的点击事件处理
     */
    inner class ListenAlarmItemClick{

        // 单击底部栏添加按钮时
        fun onClickAdd(){
            with(FragmentAlarmAddBinding.inflate(LayoutInflater.from(requireActivity()))){
                val dialog= Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                // 点击确定插入该时间
                confirm.setOnClickListener {
                    val day = alarmDueDateDay.text.toString().toInt()
                    val hour = alarmDueDateHour.text.toString().toInt()
                    val minute = alarmDueDateMinute.text.toString().toInt()
                    val dueTimestamp = System.currentTimeMillis() + DateTimeUtils.convertToTimestamp(day,hour,minute)
                    val alarm = Alarm(
                        name = alarmName.text.toString().trim(),
                        alarmDate = dueTimestamp)
                    viewModel.insertAlarm(alarm)
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

        // 点击表项可查看响铃具体时间
        fun onClickItem(binding: FragmentAlarmItemBinding){
            binding.date.visibility = if( binding.date.visibility==View.GONE)View.VISIBLE else View.GONE
        }

        // 长按表项可添加至计时器中
        fun onLongClickItem(name:String,willDoTime:Long){
            binding.willDo.text = name
            viewModel.willDoTime = willDoTime
            // 因为定时器会有一点点延迟，需要直接更新一下UI
            val remainTime=DateTimeUtils.convertFromTimestamp(viewModel.willDoTime)
            binding.time.text = "${remainTime[1]}:${remainTime[2]}:${remainTime[3]}"
            if(remainTime[0]==0){
                binding.timeDay.visibility = View.INVISIBLE
            }else{
                binding.timeDay.visibility = View.VISIBLE
                binding.timeDay.text = "+${remainTime[0]}"
            }
        }
    }
}