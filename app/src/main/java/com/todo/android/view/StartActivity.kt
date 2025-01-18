package com.todo.android.view

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.todo.android.BaseActivity
import com.todo.android.databinding.ActivityStartBinding
import com.todo.android.databinding.DiagStartForgetPasswordBinding
import com.todo.android.view.fragment.user.UserViewModel

/**
 * 制作一些启动动画、用户自定义的全局设置等
 */
class StartActivity : BaseActivity<ActivityStartBinding>() {

    private val viewModel: UserViewModel by viewModels()
    private var currentPassword : String? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentPassword=viewModel.getPasswordLiveData().value
        // 当前无密码才能直接进入
        if(currentPassword.isNullOrEmpty()){
            MainActivity.actionStart(this)
            finish()
        }

        binding.initClick()
    }

    override fun getBindingInflate() = ActivityStartBinding.inflate(layoutInflater)

    private fun ActivityStartBinding.initClick(){
        password.addTextChangedListener { input->
            if(input.toString() == currentPassword){
                MainActivity.actionStart(this@StartActivity)
                finish()
            }
        }

        forgetPassword.setOnClickListener {
            with(DiagStartForgetPasswordBinding.inflate(layoutInflater)){
                val dialog = Dialog(this@StartActivity)
                dialog.setContentView(root)
                dialog.setCancelable(true)

                confirm.setOnClickListener {
                    val inputName = userName.text.toString().trim()
                    val inputSignature = userSignature.text.toString().trim()
                    val currentName = viewModel.getNameLiveData().value
                    val currentSignature = viewModel.getSignatureLiveData().value
                    if(inputName == currentName&&inputSignature==currentSignature){
                        viewModel.updatePassword("")
                        Toast.makeText(this@StartActivity,"密码已删除",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        MainActivity.actionStart(this@StartActivity)
                        finish()
                    }else{
                        Toast.makeText(this@StartActivity,"用户名或签名输入错误",Toast.LENGTH_SHORT).show()
                    }
                }

                dialog.show()
            }
        }
    }
}