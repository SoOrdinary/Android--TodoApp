package com.todo.android.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.todo.android.BaseActivity
import com.todo.android.R
import com.todo.android.databinding.ActivityMainBinding


/**
 * AppTodo主界面
 *
 * @role1 集成4个fragment--task、list、alarm、user，显示、切换逻辑在该活动中实现+一个跳转activity
 * @role2 调整上下UI修正的样式，适应全屏模式，和一些其他控件UI更改
 *
 */
class MainActivity : BaseActivity<ActivityMainBinding>() {

    companion object{
        // 静态打开方法，指明打开该类需要哪些参数
        fun actionStart(context: Context){
            val intent = Intent(context, MainActivity::class.java).apply{
                // putExtra()
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        enableEdgeToEdge()

        // jetpack式底部导航栏与视图绑定
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.findNavController()
        NavigationUI.setupWithNavController(binding.navBottom,navController)
        binding.liner
        binding.navBottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // 如果是 "nav_add" 按钮，进入增加页面,该按钮不要被选中
                R.id.nav_add -> {
                    AddActivity.actionStart(this)
                    false
                }
                // 其他按钮默认导航
                else -> NavigationUI.onNavDestinationSelected(item, navController)
            }
        }
        // 设置上下修正View,以及侧边栏的样式[在这里面才能获取到系统UI参数，异步执行的]
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val topLayoutParams = binding.topImprove.layoutParams as ConstraintLayout.LayoutParams
            val bottomLayoutParams = binding.bottomImprove.layoutParams as ConstraintLayout.LayoutParams
            topLayoutParams.height = systemBars.top
            bottomLayoutParams.height = systemBars.bottom
            binding.topImprove.apply{
                layoutParams = topLayoutParams
            }
            binding.bottomImprove.apply{
                layoutParams = bottomLayoutParams
            }
            binding.navSide.apply{
                // 顶部留高
                getHeaderView(0).setPadding(0,systemBars.top,0,0)
                // 调整宽度
                val screenWidth = resources.displayMetrics.widthPixels
                val params: ViewGroup.LayoutParams = layoutParams
                params.width = (screenWidth * 0.66).toInt()
                layoutParams=params
            }
            insets
        }


    }

    override fun getBindingInflate() = ActivityMainBinding.inflate(layoutInflater)
}