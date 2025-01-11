package com.todo.android.view.fragment.task

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.todo.android.R
import com.todo.android.data.room.entity.Task
import com.todo.android.databinding.FragmentTaskBinding
import com.todo.android.databinding.FragmentTaskClickDeleteBinding
import com.todo.android.databinding.FragmentTaskClickEditBinding
import com.todo.android.utils.DateTimeUtils
import com.todo.android.utils.DateTimeUtils.getSeparatedStringFromTimestamp
import com.todo.android.utils.DateTimeUtils.timestampToString
import com.todo.android.view.AddActivity
import com.todo.android.view.MainActivity
import java.util.Locale


/**
 * 应用的第一个Fragment--task，列出筛选后的task
 *
 * @role1 标题栏处单击头像可唤起侧边栏
 * @role2 标题栏处可切换布局
 * @role3 实现监听与Task有关的点击事件内部类ListenTaskItemClick，构造响应事件
 *
 * @improve1 Todo:菜单添加动画效果
 * @improve2 Todo:每天的查看放在页面内
 */
class TaskFragment:Fragment(R.layout.fragment_task) {

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var binding: FragmentTaskBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaskBinding.bind(view)

        // 初始化RecycleView的配置
        binding.taskList.apply{
            layoutManager = StaggeredGridLayoutManager(viewModel.listCount, StaggeredGridLayoutManager.VERTICAL)
            adapter = TaskAdapter(this@TaskFragment,viewModel.taskList,viewModel.listType)
        }

        // 设立观察者，观察数据变化实时更新ViewModel的缓存并通知列表更新
        viewModel.taskLiveData.observe(viewLifecycleOwner){
            viewModel.taskList.clear()
            viewModel.taskList.addAll(it)
            binding.taskList.adapter?.notifyDataSetChanged()
        }
        // 初始化各种点击事件
        binding.initClick()
    }

    // 将组件点击事件放入一个函数中[扩展函数，直接拥有binding上下文，方便设置]
    private fun FragmentTaskBinding.initClick(){
        // 点击头像后打开侧边栏[requireActivity不会有空的情况]
        profile.setOnClickListener{
            (requireActivity() as MainActivity).binding.layoutMain.openDrawer(GravityCompat.START)
        }
        // 点击图标切换风格
        taskShow.setOnClickListener {
            with(viewModel){
                listType++
                when(listType){
                    1 -> {
                        listCount = 1
                        taskShow.setImageResource(R.drawable.task_linear)
                    }
                    2 -> {
                        listCount = 2
                        taskShow.setImageResource(R.drawable.task_waterfall)
                    }
                    // 其他值重新赋为0，终止本次，重复执行下一次（0会++变成1），因为递归调用了，所以要额外注意一下判断逻辑，这样写是因为默认情况的可能代码很多，避免重复性
                    else -> {
                        listType = 0
                        taskShow.performClick()
                        return@setOnClickListener
                    }
                }
            }
            // 应用该风格
            binding.taskList.apply{
                layoutManager = StaggeredGridLayoutManager(viewModel.listCount, StaggeredGridLayoutManager.VERTICAL)
                adapter = TaskAdapter(this@TaskFragment,viewModel.taskList,viewModel.listType)
            }
        }
    }




    /**
     * task的RecycleView的item在点击时的响应事件实现
     *
     * @role1 在单击时
     * @role2 在长按时
     * @role3 一些个性化组件的单击事件
     *
     * @improve1 Todo：加一些Toast响应（要用协程根据返回值成功与否改正）
     */
    inner class ListenTaskItemClick {
        // Todo:单击时应该打开详细情况页面,dialog or activity?
        fun onClickItem(view: View, task: Task){
            Toast.makeText(requireActivity(),"click ${task.title}",Toast.LENGTH_SHORT).show()
        }
        // 长按删除
        fun onLongClickItem(view:View,task:Task){
            when (view.id) {
                R.id.task_linear,R.id.task_grid -> {
                    with(FragmentTaskClickDeleteBinding.inflate(LayoutInflater.from(requireActivity()))){
                        val dialog = Dialog(requireActivity())
                        dialog.setContentView(root)
                        dialog.setCancelable(true)

                        confirmDelete.setOnClickListener {
                            viewModel.delete(task)
                            dialog.dismiss()
                        }

                        dialog.show()
                    }
                }
                else -> {}
            }
        }
        // 点击单选框
        fun onClickCheckBox(task: Task){
            task.isFinish=!task.isFinish
            viewModel.update(task)
        }
        // 点击底部导航栏加号
        fun onClickAdd(){
            with(FragmentTaskClickEditBinding.inflate(LayoutInflater.from(requireActivity()))){
                val dialog=Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                // 分隔时间
                val parts = getSeparatedStringFromTimestamp(timestampToString(System.currentTimeMillis()))
                val day = parts[0]
                val hour = parts[1]
                val minute = parts[2]
                // 绑定时间
                taskDueDateDay.setText(day)
                taskDueDateHour.setText(hour)
                taskDueDateMinute.setText(minute)
                // 绑定标签 Todo:自定义增删标签
                taskTag.adapter=ArrayAdapter<String>(requireActivity(),android.R.layout.simple_spinner_item,listOf("default","b","c"))
                // Todo:照片

                confirm.setOnClickListener {
                    // 校验输入
                    if (!checkInput(taskTitle, taskDueDateDay, taskDueDateHour, taskDueDateMinute)) return@setOnClickListener
                    // 成功则插入数据
                    val dueTimestamp = DateTimeUtils.stringToTimestamp("${taskDueDateDay.text.toString().trim()}  ${taskDueDateHour.text.toString().trim()}:${taskDueDateMinute.text.toString().trim()}")
                    viewModel.insert(Task(
                        title = taskTitle.text.toString().trim(),
                        subtitle = taskSubtitle.text.toString().trim(),
                        details = "",
                        voice = null,
                        image = null,//Todo
                        dueDate = dueTimestamp,
                        isFinish = false,
                        tag = taskTag.selectedItem.toString()
                    ))
                    dialog.dismiss()
                }
                dialog.show()
            }
        }
        // 长按底部导航栏加号
        fun onLongClickAdd(){
            AddActivity.actionStart(requireActivity())
        }

        private fun checkInput(taskTitle: EditText, taskDueDateDay: EditText, taskDueDateHour: EditText, taskDueDateMinute: EditText): Boolean {

            val title = taskTitle.text.toString().trim()
            val day = taskDueDateDay.text.toString().trim()
            val hour = taskDueDateHour.text.toString().trim()
            val minute = taskDueDateMinute.text.toString().trim()

            // 校验任务标题
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "标题不可为空", Toast.LENGTH_SHORT).show()
                return false // 如果标题为空，返回 false
            }

            // 校验日期输入
            if (day.isEmpty() || hour.isEmpty() || minute.isEmpty()) {
                Toast.makeText(requireContext(), "日期不可为空", Toast.LENGTH_SHORT).show()
                return false // 如果日期不完整，返回 false
            }
            // Todo:检测具体时间是否规范
            
            // 所有校验通过，返回 true
            return true
        }

    }
}