package com.todo.android.view.fragment.alarm

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.todo.android.BaseActivity
import com.todo.android.R
import com.todo.android.databinding.ActivityAlarmViewBinding
import com.todo.android.utils.DateTimeUtils

/**
 * 沉浸模式页面
 * Todo:UI设计
 */
class AlarmViewActivity : BaseActivity<ActivityAlarmViewBinding>() {

    // 设置定时器
    private lateinit var handler: Handler
    private lateinit var alarmRunnable: Runnable

    companion object{
        // 静态打开方法，指明打开该类需要哪些参数
        fun actionStart(context: Context,time:Long){
            val intent = Intent(context, AlarmViewActivity::class.java).apply{
                putExtra("time",time)
            }
            // 设置进入动画和退出动画Todo：修改
            val options = ActivityOptions.makeCustomAnimation(context, R.anim.activity_add_in, R.anim.activity_add_out)
            context.startActivity(intent, options.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        var willDoTime = intent.getLongExtra("time", -1L)
        // 每秒检查一次，当前分钟数变化就触发更新
        var lastMinute = -1 // 用来存储上一次的分钟数
        handler = Handler(Looper.getMainLooper())
        alarmRunnable = object : Runnable {
            override fun run() {
                // willDoTime大于当前时间，更新UI与viewModel
                val currentTime =System.currentTimeMillis()
                if (willDoTime > currentTime) {
                    val remainTime = DateTimeUtils.convertFromTimestamp(willDoTime-currentTime)
                    val formattedTime = String.format("%02d:%02d:%02d", remainTime[1], remainTime[2], remainTime[3])
                    binding.time.text = formattedTime
                    if (remainTime[0] == 0) {
                        binding.timeDay.visibility = View.INVISIBLE
                    } else {
                        binding.timeDay.visibility = View.VISIBLE
                        binding.timeDay.text = "+${remainTime[0]}"
                    }
                }else{
                    // 保证只在倒计时为0时只提醒一次
                    if(willDoTime !=0L){
                        binding.time.text ="0:0:0"
                        binding.timeDay.visibility = View.INVISIBLE
                        willDoTime = 0
                    }
                }
                // 继续每秒钟检查一次
                handler.postDelayed(this, 1000)
            }
        }
        // 开始定时器
        handler.postDelayed(alarmRunnable, 0)

        Toast.makeText(this,"$willDoTime",Toast.LENGTH_LONG).show()

    }

    override fun finish() {
        super.finish()

        handler.removeCallbacks(alarmRunnable)
    }

    override fun getBindingInflate(): ActivityAlarmViewBinding = ActivityAlarmViewBinding.inflate(layoutInflater)

}