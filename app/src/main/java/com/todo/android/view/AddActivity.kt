package com.todo.android.view

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.todo.android.BaseActivity
import com.todo.android.R
import com.todo.android.databinding.ActivityAddBinding
import java.io.File

/**
 * 增加Task的界面
 *
 * @role1 Todo:设置并新增Task
 *
 * @improve1 Todo：进入和退出动画
 */
class AddActivity : BaseActivity<ActivityAddBinding>() {
    lateinit var imageUri:Uri
    lateinit var outputImage: File

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

    override fun getBindingInflate()=ActivityAddBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }

    override fun finish() {
        super.finish()
        // Todo:设置退出动画
    }


}