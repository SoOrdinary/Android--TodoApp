package com.soordinary.todo.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.soordinary.todo.BaseActivity
import com.soordinary.todo.R
import com.soordinary.todo.databinding.ActivityMainBinding
import com.soordinary.todo.databinding.NavSideHeaderBinding
import com.soordinary.todo.utils.SizeUnits
import com.soordinary.todo.view.foreground.ForegroundService
import com.soordinary.todo.view.fragment.alarm.AlarmFragment
import com.soordinary.todo.view.fragment.alarm.AlarmViewModel
import com.soordinary.todo.view.fragment.record.RecordFragment
import com.soordinary.todo.view.fragment.task.TaskFragment
import com.soordinary.todo.view.fragment.task.TaskViewModel
import com.soordinary.todo.view.fragment.user.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * AppTodo主界面
 *
 * @role1 集成4个fragment--task、list、alarm、user[在view.fragment.*中]，显示、切换逻辑在该活动中实现+一个跳转addActivity
 * @role2 调整上下UI修正的样式，适应全屏模式，和一些其他控件UI更改
 * @role3 不同Fragment下add的不同效果
 * @role4 侧边栏和底部导航栏的点击事件
 * @role5 侧边栏的个人信息动态更新
 * @role6 侧边栏的task的自定义标签动态更新
 * @role7 删除超时alarm
 *
 * @improve1 底部导航栏更换为jetpack的方式，结构更清晰点
 * @improve2 Todo:底部导航栏切换时顶部自适应
 */
class MainActivity : BaseActivity<ActivityMainBinding>() {

    companion object {
        // 静态打开方法，指明打开该类需要哪些参数
        fun actionStart(context: Context) {
            val intent = Intent(context, MainActivity::class.java).apply {
                // putExtra()
            }
            context.startActivity(intent)
        }
    }

    // 闹钟列表需要发出提醒,且其他fragment可能需要调用
    private val taskViewModel: TaskViewModel by viewModels()
    private val alarmViewModel: AlarmViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    override fun getBindingInflate() = ActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 设置样式[在这里面才能获取到系统UI参数，异步执行的]
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 主界面顶部与底部留高检测设置
            val topLayoutParams = binding.topImprove.layoutParams as ConstraintLayout.LayoutParams
            val bottomLayoutParams = binding.bottomImprove.layoutParams as ConstraintLayout.LayoutParams
            topLayoutParams.height = systemBars.top
            bottomLayoutParams.height = systemBars.bottom
            binding.topImprove.layoutParams = topLayoutParams
            binding.bottomImprove.layoutParams = bottomLayoutParams
            binding.navBottom.setPadding(0, 0, 0, 0)
            // 侧边栏UI修正高度，默认选择today_task
            binding.navSide.getHeaderView(0).setPadding(0, systemBars.top, 0, 0)
            binding.navSide.setCheckedItem(R.id.today_task)
            insets
        }

        // 侧边栏UI修正
        with(binding.navSide) {
            // 调整宽度,个人设置成了整个屏幕的2/3
            val params: ViewGroup.LayoutParams = layoutParams
            params.width = (SizeUnits.screenWidth * 0.66).toInt()
            layoutParams = params
            // 观察个人信息
            with(NavSideHeaderBinding.bind(getHeaderView(0))) {
                userViewModel.getIconUriLiveData().observe(this@MainActivity) {
                    Glide.with(icon.context)
                        .load(it)  // 图片的 URL
                        .downsample(DownsampleStrategy.CENTER_INSIDE) // 根据目标区域缩放图片
                        .placeholder(R.drawable.app_icon)  // 占位图
                        .into(icon)
                }
                userViewModel.getNameLiveData().observe(this@MainActivity) {
                    name.text = it
                }
                userViewModel.getSignatureLiveData().observe(this@MainActivity) {
                    signature.text = it
                }
            }
            // 获取当前所有的tags并渲染
            taskViewModel.getNowTaskTagsLiveData().observe(this@MainActivity) {
                // 清空所有item
                menu.clear()
                // 先更新固定的，再根据tags更新菜单项
                menu.add(R.id.classify_by_dates, R.id.today_task, Menu.NONE, "Today Task")
                menu.add(R.id.classify_by_dates, R.id.list_task, Menu.NONE, "List  Task")
                it.forEach { tag ->
                    // 为每个tag动态创建菜单项并添加到菜单中
                    menu.add(R.id.classify_by_tags, View.generateViewId(), Menu.NONE, tag)
                }
            }
        }

        // 在每次打开应用的时候都检查并删除超时闹钟，删除第一个后又会触发下一个，不用担心一直不点开alarm页面就一直不清理了
        GlobalScope.launch(Dispatchers.Main) {
            delay(5000)
            alarmViewModel.removeAllFinishAlarm()
        }

        // 初始化各种点击事件
        binding.initClick()
    }


    // 点击事件初始化扩展函数
    private fun ActivityMainBinding.initClick() {

        // 获取一些必要的组件实例
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val sideHeaderBinding = NavSideHeaderBinding.bind(navSide.getHeaderView(0))

        // jetpack式底部导航栏点击与视图绑定
        val navController = navHostFragment.findNavController()
        NavigationUI.setupWithNavController(binding.navBottom, navController)
        with(binding.navBottom) {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    // 如果是 "nav_add" 按钮，根据当前fragment设置逻辑，同时返回else表示add按钮不被选中
                    R.id.nav_add -> {
                        when (val fragment = navHostFragment.childFragmentManager.fragments[0]) {
                            is TaskFragment -> fragment.ListenTaskItemClick().onClickAddOrEdit(null) {}
                            is AlarmFragment -> fragment.ListenAlarmItemClick().onClickAdd()
                            is RecordFragment -> fragment.ListenRecordItemClick().onClickAdd()
                            else -> {}
                        }
                        false
                    }
                    // 其他按钮默认导航
                    else -> NavigationUI.onNavDestinationSelected(item, navController)
                }
            }
            // 长按加号会进入更详细的页面去编辑
            findViewById<View>(R.id.nav_add).setOnLongClickListener {
                when (val fragment = navHostFragment.childFragmentManager.fragments[0]) {
                    is TaskFragment -> fragment.ListenTaskItemClick().onLongClickAdd()
                    is AlarmFragment -> fragment.ListenAlarmItemClick().onLongClickAdd()
                    is RecordFragment -> fragment.ListenRecordItemClick().onLongClickAdd()
                    else -> {}
                }
                true
            }
        }

        // 搜索框输入内容并按下回车，回调Fragment自定义方法，并清空内容,以及取消菜单栏按钮的选中情况Todo:回车后隐藏键盘，改为能够实时查询，以及单开一个dialog来查询？还有查询时取消其他tag的选择
        sideHeaderBinding.searchTask.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (v.text.isNullOrEmpty()) return@setOnEditorActionListener true
                val fragment = navHostFragment.childFragmentManager.fragments[0]
                if (fragment is TaskFragment) fragment.ListenTaskItemClick().onSearchByTitle(v.text.toString().trim())
                v.text = ""
                layoutMain.closeDrawers()
            }
            true
        }

        // 点击侧边栏的某一菜单后回调Fragment自定义好的逻辑，然后关闭侧边栏并选中该菜单
        navSide.setNavigationItemSelectedListener {
            val fragment = navHostFragment.childFragmentManager.fragments[0]
            if (fragment is TaskFragment) fragment.ListenTaskItemClick().onClickMenuItem(it)
            layoutMain.closeDrawers()
            true
        }
    }
}