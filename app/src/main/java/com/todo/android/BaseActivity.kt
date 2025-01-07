package com.todo.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import java.lang.ref.WeakReference

/**
 * 建立Activity基类，方便后续为所有Activity增加共有变量or方法
 *
 * @role1 定义静态内部类ActivityCollector，在每个活动create|destroy时分别改变记录器的值
 * @role2 VB 填充泛型，为每个Activity自动绑定binding--需要子类实现getBinding方法,private后子类只能get不能set
 * @role3 完成布局的共有初始化工作，绑定视图、全屏、自动对齐边角等
 */
abstract class BaseActivity<VB:ViewBinding>: AppCompatActivity() {

    private  var _binding: VB? = null

    val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
        _binding=getBindingInflate()
    }

    // 抽象方法，取得加载实例视图
    abstract fun getBindingInflate(): VB


    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.removeActivity(this)
        _binding = null
    }

    // 活动记录器[使用weakReference引用，防止直接引用导致垃圾回收无法工作]
    object ActivityCollector {

        private val activities = ArrayList<WeakReference<Activity>>()

        fun addActivity(activity: Activity) {
            activities.add(WeakReference(activity))
        }

        fun removeActivity(activity: Activity) {
            activities.removeAll { it.get() == activity }
        }

        fun finishAll() {
            // 清理所有活动
            activities.forEach {
                it.get()?.let { activity ->
                    if (!activity.isFinishing) {
                        activity.finish()
                    }
                }
            }
            activities.clear()
        }
    }

}
