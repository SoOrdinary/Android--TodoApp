package com.soordinary.todo.view.fragment.alarm

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import com.soordinary.todo.BaseActivity
import com.soordinary.todo.databinding.ActivityAlarmViewBinding
import com.soordinary.todo.utils.DateTimeUtil

/**
 * 沉浸模式页面
 * Todo:UI设计
 */
class AlarmViewActivity : BaseActivity<ActivityAlarmViewBinding>() {

    // 设置定时器
    private lateinit var handler: Handler
    private lateinit var alarmRunnable: Runnable

    companion object {
        // 静态打开方法，指明打开该类需要哪些参数
        fun actionStart(context: Context, time: Long) {
            val intent = Intent(context, AlarmViewActivity::class.java).apply {
                putExtra("time", time)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 启用横屏模式,会丢失数据?
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        var willDoTime = intent.getLongExtra("time", -1L)
        // 每秒检查一次，当前分钟数变化就触发更新
        handler = Handler(Looper.getMainLooper())
        alarmRunnable = object : Runnable {
            override fun run() {
                // willDoTime大于当前时间，更新UI与viewModel
                val currentTime = System.currentTimeMillis()
                if (willDoTime > currentTime) {
                    val remainTime = DateTimeUtil.convertFromTimestamp(willDoTime - currentTime)
                    val formattedTime = String.format("%02d:%02d:%02d", remainTime[1], remainTime[2], remainTime[3])
                    binding.time.text = formattedTime
                    if (remainTime[0] == 0) {
                        binding.timeDay.visibility = View.INVISIBLE
                    } else {
                        binding.timeDay.visibility = View.VISIBLE
                        binding.timeDay.text = "+${remainTime[0]}"
                    }
                } else {
                    // 保证只在倒计时为0时只提醒一次
                    if (willDoTime != 0L) {
                        binding.time.text = "00:00:00"
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

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(alarmRunnable)
    }

    override fun getBindingInflate(): ActivityAlarmViewBinding = ActivityAlarmViewBinding.inflate(layoutInflater)

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableImmersiveMode()
        }
    }

    // Todo:为什么都是弃用的？
    private fun enableImmersiveMode() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }


}