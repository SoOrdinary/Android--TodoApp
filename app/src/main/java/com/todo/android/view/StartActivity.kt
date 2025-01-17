package com.todo.android.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.todo.android.BaseActivity
import com.todo.android.databinding.ActivityStartBinding
import com.todo.android.view.fragment.user.UserViewModel

/**
 * 制作一些启动动画、用户自定义的全局设置等
 */
class StartActivity : BaseActivity<ActivityStartBinding>() {

    val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentPassword=viewModel.getPasswordLiveData().value
        // 当前密码不为空，直到输入正确密码才能进入主界面
        if(currentPassword.isNullOrEmpty()){
            MainActivity.actionStart(this)
            finish()
        }else{
            binding.password.addTextChangedListener { input->
                if(input.toString() == currentPassword){
                    MainActivity.actionStart(this)
                    finish()
                }
            }
        }
    }

    override fun getBindingInflate() = ActivityStartBinding.inflate(layoutInflater)
}