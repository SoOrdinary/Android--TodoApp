package com.todo.android.view.fragment.task

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.todo.android.R
import com.todo.android.TodoApplication
import com.todo.android.data.room.entity.Task
import com.todo.android.databinding.FragmentTaskBinding
import com.todo.android.databinding.FragmentTaskClickDeleteBinding
import com.todo.android.databinding.FragmentTaskClickEditBinding
import com.todo.android.databinding.FragmentTaskClickViewBinding
import com.todo.android.utils.DateTimeUtils
import com.todo.android.utils.DateTimeUtils.getSeparatedStringFromTimestamp
import com.todo.android.utils.DateTimeUtils.timestampToString
import com.todo.android.view.AddActivity
import com.todo.android.view.MainActivity


/**
 * 应用的第一个Fragment--task，列出筛选后的task
 *
 * @role1 标题栏处单击头像可唤起侧边栏
 * @role2 标题栏处可切换布局
 * @role3 实现监听与Task有关的点击事件内部类ListenTaskItemClick，构造响应事件
 * @role4 头像的观察
 *
 * @improve1 Todo:菜单添加动画效果
 * @improve2 Todo:每天的查看放在页面内
 * @improve3 将拍照等异步事件的处理函数统一，用一个处理器处理多种事件
 */
class TaskFragment:Fragment(R.layout.fragment_task) {

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var binding: FragmentTaskBinding
    // 相册取照的相关回调函数
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var handleWay:(ActivityResult)->Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaskBinding.bind(view)

        // 初始化RecycleView的配置
        binding.taskList.apply{
            layoutManager = StaggeredGridLayoutManager(viewModel.listCount, StaggeredGridLayoutManager.VERTICAL)
            adapter = TaskAdapter(this@TaskFragment,viewModel.taskList,viewModel.listType)
        }

        // 初始化一个提醒函数体，避免后续未初始化直接调用
        handleWay= {Toast.makeText(requireActivity(),"处理事件未初始化",Toast.LENGTH_SHORT).show()}
        // 注册一个事件返回器
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleWay(result)
        }

        // 初始化一些数据观察者
        binding.initLiveData()

        // 初始化各种点击事件
        binding.initClick()
    }

    // 初始化观察者
    private fun FragmentTaskBinding.initLiveData(){

        // 观察数据变化实时更新ViewModel的缓存并通知列表更新
        viewModel.taskLiveData.observe(viewLifecycleOwner){
            viewModel.taskList.clear()
            viewModel.taskList.addAll(it)
            // Todo:优化更新方式
            binding.taskList.adapter?.notifyDataSetChanged()
        }

        viewModel.getIconUriLiveData().observe(this@TaskFragment){
            Glide.with(iconP.context)
                .load(it)  // 图片的 URL
                .downsample(DownsampleStrategy.CENTER_INSIDE) // 根据目标区域缩放图片
                .placeholder(R.drawable.app_icon)  // 占位图
                .into(iconP)
        }
    }

    // 将组件点击事件绑定放入一个函数中[扩展函数，直接拥有binding上下文，方便设置]
    private fun FragmentTaskBinding.initClick(){

        // 点击头像后打开侧边栏[requireActivity不会有空的情况]
        iconP.setOnClickListener{
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
        // 单击任务打开详细情况页面
        fun onClickItem(task: Task){
            with(FragmentTaskClickViewBinding.inflate(LayoutInflater.from(requireActivity()))){
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                // 组件赋值
                taskTitle.text=task.title
                if(task.subtitle.isEmpty()){
                    taskSubtitle.visibility=View.GONE
                }else{
                    taskSubtitle.text=task.subtitle
                }
                if(task.details.isEmpty()){
                    taskDetails.visibility=View.GONE
                }else{
                    taskDetails.text="  ${task.details}"
                }
                taskTag.text=task.tag
                taskDueDate.text=timestampToString(task.dueDate)
                // 闹钟响应
                taskAlarm.setOnClickListener {
                    Toast.makeText(this@TaskFragment.requireActivity(),"...",Toast.LENGTH_SHORT).show()
                }
                // 编辑响应
                taskEdit.setOnClickListener {
                    onClickAddOrEdit(task){newTask->
                        // 更新现有界面
                        taskTitle.text=newTask.title
                        if(newTask.subtitle.isEmpty()){
                            taskSubtitle.visibility=View.GONE
                        }else{
                            taskSubtitle.text=newTask.subtitle
                        }
                        if(newTask.details.isEmpty()){
                            taskDetails.visibility=View.GONE
                        }else{
                            taskDetails.text="  ${newTask.details}"
                        }
                        taskTag.text=newTask.tag
                        taskDueDate.text=timestampToString(newTask.dueDate)
                    }
                }

                dialog.show()
            }
        }
        // 长按删除
        fun onLongClickItem(task:Task) {
            with(FragmentTaskClickDeleteBinding.inflate(LayoutInflater.from(requireActivity()))) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                confirmDelete.setOnClickListener {
                    viewModel.deleteTask(task)
                    dialog.dismiss()
                }

                dialog.show()
            }
        }
        // 点击单选框
        fun onClickCheckBox(task: Task){
            task.isFinish=!task.isFinish
            viewModel.updateTask(task)
        }
        // 点击底部导航栏加号或详情页面修改，取决于是否传入Task，第二个参数是用来回调异步修改UI的,
        fun onClickAddOrEdit(oldTask:Task?,block:(newTask:Task)->Unit){
            with(FragmentTaskClickEditBinding.inflate(LayoutInflater.from(requireActivity()))){

                val dialog=Dialog(requireActivity())
                dialog.setContentView(root)
                dialog.setCancelable(true)

                // 绑定标签
                val taskTags = listOf("default") + (viewModel.getNowTaskTagsLiveData().value?.toList() ?: emptyList())
                val adapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, taskTags)
                taskTag.adapter = adapter

                // 根据传入值决定ui
                if(oldTask == null){
                    // 分隔当前时间
                    val parts = getSeparatedStringFromTimestamp(timestampToString(System.currentTimeMillis()))
                    val day = parts[0]
                    val hour = parts[1]
                    val minute = parts[2]

                    // 绑定时间
                    taskDueDateDay.setText(day)
                    taskDueDateHour.setText(hour)
                    taskDueDateMinute.setText(minute)
                    // 默认选中 "default"
                    taskTag.setSelection(0)
                }else{
                    taskDetailsParent.visibility = View.VISIBLE
                    val parts = getSeparatedStringFromTimestamp(timestampToString(oldTask.dueDate))
                    val day = parts[0]
                    val hour = parts[1]
                    val minute = parts[2]
                    // 赋值
                    taskTitle.setText(oldTask.title)
                    taskSubtitle.setText(oldTask.subtitle)
                    taskDetails.setText(oldTask.details)
                    val position: Int = taskTags.indexOf(oldTask.tag)
                    if (position != -1)  taskTag.setSelection(position)
                    taskTag.tag = oldTask.tag
                    taskDueDateDay.setText(day)
                    taskDueDateHour.setText(hour)
                    taskDueDateMinute.setText(minute)
                    Glide.with(TodoApplication.context)
                        .load(oldTask.image)
                        .downsample(DownsampleStrategy.CENTER_INSIDE)
                        .placeholder(R.drawable.app_icon)
                        .into(taskImage)
                }


                // Todo:拍照，将相册取下的照片存在应用缓存目录
                with(taskImage){
                    setOnClickListener {
                        // 处理函数重置，将uri存下来,并将图片控件设置
                        handleWay = {
                            if (it.resultCode == RESULT_OK) {
                                it.data?.data?.let { uri ->
                                    taskPhotoUri.text = uri.toString()
                                    val bitmap = BitmapFactory.decodeStream(requireActivity().contentResolver.openInputStream(uri))
                                    setImageBitmap(bitmap)
                                }
                            }
                        }
                        // 开启相册
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "image/*"
                        }
                        // 打开事件回调
                        try {
                            launcher.launch(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this@TaskFragment.requireActivity(), "error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                // 确认按钮
                confirm.setOnClickListener {
                    // 校验输入，错误则直接返回
                    if (!checkInput(taskTitle, taskDueDateDay, taskDueDateHour, taskDueDateMinute)) return@setOnClickListener
                    // 成功则插入数据
                    val day = taskDueDateDay.text.toString().trim()
                    val hour = taskDueDateHour.text.toString().trim()
                    val minute = taskDueDateMinute.text.toString().trim()
                    val dueTimestamp = DateTimeUtils.stringToTimestamp("$day  $hour:$minute}")
                    // 生成最终task
                    val newTask=Task(
                        id = oldTask?.id ?:0,
                        title = taskTitle.text.toString().trim(),
                        subtitle = taskSubtitle.text.toString().trim(),
                        details = taskDetails.text.toString().trim(),
                        voice = null,
                        image = taskPhotoUri.text.toString().trim(),
                        dueDate = dueTimestamp,
                        isFinish = false,
                        tag = taskTag.selectedItem.toString()
                    )

                    // 判断是插入还是更新
                    if(oldTask == null){
                        viewModel.insertTask(newTask)
                    }else{
                        viewModel.updateTask(newTask)
                        // 更新界面,确信block一定不为空
                        block(newTask)
                    }

                    dialog.dismiss()
                }

                dialog.show()
            }
        }

        // 长按底部导航栏加号
        fun onLongClickAdd(){
            AddActivity.actionStart(requireActivity())
        }

        // 使用搜索框搜索时
        fun onSearchByTitle(key:String){
            viewModel.getTasksByTitleAndFinish(key,null)
        }

        // 点击侧边栏菜单查询时
        fun onClickMenuItem(item: MenuItem){
            when(item.groupId){
                // 前两个是默认有的，查询当天/查询所有Todo:当天之前的所有未完成的
                R.id.classify_by_dates->
                    when(item.itemId){
                        R.id.today_task->viewModel.getTasksByDueDateAndFinish(DateTimeUtils.getStartOfDay(0), DateTimeUtils.getEndOfDay(0), null)
                        R.id.list_task->viewModel.getTasksByFinish(null)
                    }
                // 后面的都是tag，直接根据title查询
                R.id.classify_by_tags->viewModel.getTasksByTagAndFinish(item.title.toString(),null)
            }
        }
    }

    // 校验函数(每次修改要保持和AddActivity中的checkInput同步)
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