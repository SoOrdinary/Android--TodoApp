package com.todo.android.view

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.todo.android.BaseActivity
import com.todo.android.R
import com.todo.android.databinding.ActivityAddBinding

class AddActivity : BaseActivity<ActivityAddBinding>() {

    companion object{
        // 静态打开方法，指明打开该类需要哪些参数
        fun actionStart(context: Context){
            val intent = Intent(context, AddActivity::class.java).apply{
                // putExtra()
            }
            // 设置进入动画和退出动画
            val options = ActivityOptions.makeCustomAnimation(context, R.anim.activity_add_in, R.anim.activity_add_out)
            context.startActivity(intent, options.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        enableEdgeToEdge()
    }

    override fun finish() {
        super.finish()
        // Todo:设置退出动画
    }
    override fun getBindingInflate()=ActivityAddBinding.inflate(layoutInflater)
}