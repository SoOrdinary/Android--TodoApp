package com.todo.android.view.fragment.alarm

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.todo.android.BaseActivity
import com.todo.android.R
import com.todo.android.databinding.ActivityAlarmBinding
import com.todo.android.view.AddActivity

/**
 * 沉浸模式页面
 * Todo:UI设计
 */
class AlarmActivity : BaseActivity<ActivityAlarmBinding>() {

    companion object{
        // 静态打开方法，指明打开该类需要哪些参数
        fun actionStart(context: Context,time:Long){
            val intent = Intent(context, AlarmActivity::class.java).apply{
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


    }

    override fun getBindingInflate(): ActivityAlarmBinding = ActivityAlarmBinding.inflate(layoutInflater)

}