package com.todo.android.view.fragment.task

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ContextMenu
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.todo.android.R
import com.todo.android.data.room.entity.Task
import com.todo.android.databinding.FragmentTaskBinding
import com.todo.android.utils.DateTimeUtils
import com.todo.android.view.MainActivity

/**
 * 应用的第一个Fragment--task，列出筛选后的task
 *
 * @role1 标题栏处单击头像可唤起侧边栏
 * @role2 Todo：标题栏处可切换布局
 */
class TaskFragment:Fragment(R.layout.fragment_task) {

    companion object {
        fun newInstance() = TaskFragment()
    }

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var binding: FragmentTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaskBinding.bind(view)

        // 初始化RecycleView的配置
        binding.taskList.apply{
            this.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            this.adapter = TaskAdapter(this@TaskFragment,viewModel.taskList,0)
        }
        // 设立观察者，观察数据变化实时更新ViewModel的缓存并通知列表更新
        viewModel.taskLiveData.observe(viewLifecycleOwner){
            viewModel.taskList.clear()
            viewModel.taskList.addAll(it)
            binding.taskList.adapter?.notifyDataSetChanged()
        }
        var i=0
        binding.add.setOnClickListener {
            i++
            viewModel.insert(Task(
                title = "任务 ${i}",
                subtitle = "副标题 ${i}",
                details = "任务内容 ${i}",
                voice = null,
                image = null,
                dueDate = System.currentTimeMillis() + (i * 10000000L),  // 设置不同的截止日期
                isFinish = i % 2 == 0,  // 偶数任务完成，奇数未完成
                tag = "标签 ${i}"
            ))

        }
        // 点击头像后打开侧边栏
        binding.profile.setOnClickListener{
            (requireActivity() as MainActivity).binding.layoutMain.openDrawer(GravityCompat.START)
        }
    }





    fun init(i: Int): List<Task> {
        return List(i) { index ->
            Task(
                id = index + 1,  // 设置 id 为 1 到 i
                title = "任务 ${index + 1}",
                subtitle = "副标题 ${index + 1}",
                details = "任务内容 ${index + 1}",
                voice = null,
                image = null,
                dueDate = System.currentTimeMillis() + (index * 10000000L),  // 设置不同的截止日期
                isFinish = index % 2 == 0,  // 偶数任务完成，奇数未完成
                tag = "标签 ${index + 1}"
            )
        }
    }


}