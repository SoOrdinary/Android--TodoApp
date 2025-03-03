package com.soordinary.todo.view

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.soordinary.todo.BaseActivity
import com.soordinary.todo.databinding.ActivityStartBinding
import com.soordinary.todo.databinding.DiagStartForgetPasswordBinding
import com.soordinary.todo.utils.encryption.MD5Util
import com.soordinary.todo.view.foreground.summarize.ForegroundService
import com.soordinary.todo.view.fragment.user.UserViewModel

/**
 * 制作一些启动动画、用户自定义的全局设置等
 */
class StartActivity : BaseActivity<ActivityStartBinding>() {

    private val viewModel: UserViewModel by viewModels()
    private var currentPassword: String? = null
    private lateinit var serviceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.version.text = packageManager.getPackageInfo(packageName, 0).versionName
        // 启动前台服务
        startForeground()

        currentPassword = viewModel.getPasswordLiveData().value
        // 当前无密码才能直接进入
        if (currentPassword.isNullOrEmpty()) {
            passwordAfter()
        }

        binding.initClick()
    }

    override fun getBindingInflate() = ActivityStartBinding.inflate(layoutInflater)

    private fun ActivityStartBinding.initClick() {
        password.addTextChangedListener { input ->
            val inputToMD5 = MD5Util.encryptByMD5(input.toString())
            if (inputToMD5 == currentPassword) {
                passwordAfter()
            }
        }

        forgetPassword.setOnClickListener {
            with(DiagStartForgetPasswordBinding.inflate(layoutInflater)) {
                val dialog = Dialog(this@StartActivity)
                dialog.setContentView(root)
                dialog.setCancelable(true)

                confirm.setOnClickListener {
                    val inputName = userName.text.toString().trim()
                    val inputSignature = userSignature.text.toString().trim()
                    val currentName = viewModel.getNameLiveData().value
                    val currentSignature = viewModel.getSignatureLiveData().value
                    if (inputName == currentName && inputSignature == currentSignature) {
                        viewModel.updatePassword("")
                        Toast.makeText(this@StartActivity, "密码已删除", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        MainActivity.actionStart(this@StartActivity)
                        finish()
                    } else {
                        Toast.makeText(this@StartActivity, "用户名或签名输入错误", Toast.LENGTH_SHORT).show()
                    }
                }

                dialog.show()
            }
        }
    }

    // 无密码或输入正确做的事
    private fun passwordAfter() {
        // 重写调用前台服务，开始更新数据
        ForegroundService.passwordFinish = true
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        MainActivity.actionStart(this)
        finish()
    }

    private fun startForeground() {
        // 启动服务
        serviceIntent = Intent(this, ForegroundService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}