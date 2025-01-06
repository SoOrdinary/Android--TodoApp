package com.todo.android.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.todo.android.BaseActivity
import com.todo.android.R
import com.todo.android.databinding.ActivityStartBinding

/**
 * 制作一些启动动画、用户自定义的全局设置等
 */
class StartActivity : BaseActivity<ActivityStartBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.actionStart(this)
        finish()
    }

    override fun getBindingInflate() = ActivityStartBinding.inflate(layoutInflater)
}