package com.soordinary.todo.view.fragment.alarm

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import com.soordinary.todo.BaseActivity
import com.soordinary.todo.databinding.ActivityAlarmViewBinding
import com.soordinary.todo.utils.DateTimeUtil

/**
 * alarm倒计时沉浸模式
 */
class AlarmViewActivity : BaseActivity<ActivityAlarmViewBinding>() {

    private lateinit var handler: Handler
    private lateinit var alarmRunnable: Runnable

    companion object {
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

        // 启用横屏模式
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // 唤醒屏幕
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        var willDoTime = intent.getLongExtra("time", -1L)
        handler = Handler(Looper.getMainLooper())
        alarmRunnable = object : Runnable {
            override fun run() {
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
                    if (willDoTime != 0L) {
                        binding.time.text = "00:00:00"
                        binding.timeDay.visibility = View.INVISIBLE
                        willDoTime = 0
                    }
                }
                handler.postDelayed(this, 1000)
            }
        }
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

    private fun enableImmersiveMode() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

}