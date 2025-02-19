package com.soordinary.todo.view.fragment.task

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.soordinary.todo.R
import com.soordinary.todo.data.room.entity.Task
import com.soordinary.todo.databinding.FragmentTaskItemGridBinding
import com.soordinary.todo.databinding.FragmentTaskItemLinearBinding
import com.soordinary.todo.utils.DateTimeUtils

/**
 * Task列表适配器 Todo:写一个带基类的RecycleView扩展库，方便不同布局的实现
 *
 * @role1 用于适配Task界面的列表每一项
 *
 * @explain1 依次传入的参数为 当前view、需要显示的列表、显示的方式
 *
 * @improve1 基类BaseViewHolder统管不同的item，拥有抽象函数bind将事件绑定同一归属
 * @improve2 适配图片等用Glide，增加流畅度
 * @improve3 Todo:通过diff优化查询更新
 */
class TaskAdapter(private val fragment: TaskFragment, private val taskList: List<Task>, private val itemType: Int) : RecyclerView.Adapter<TaskAdapter.BaseViewHolder>() {

    init {
       setHasStableIds(true)
    }

    // 点击事件适配
    val listenTaskItemClick = fragment.ListenTaskItemClick()

    // 内部基类，简化多种适配item与bind的书写
    abstract inner class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(task: Task)
    }

    // 根据返回的布局ID判断加载哪种布局
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        when (viewType) {
            1 -> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.fragment_task_item_linear, parent, false)
                return LinearViewHolder(view)
            }

            2 -> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.fragment_task_item_grid, parent, false)
                return GridViewHolder(view)
            }
        }
        Toast.makeText(fragment.context, "传入布局ID不存在,加载默认布局", Toast.LENGTH_SHORT).show()
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.fragment_task_item_linear, parent, false)
        return LinearViewHolder(view)
    }

    // 重写函数，依次说明布局类型、item个数、声明绑定(调用对应布局Holder重写的binding函数)
    override fun getItemViewType(position: Int) = itemType
    override fun getItemCount() = taskList.size
    override fun getItemId(position: Int) = taskList[position].id
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) = holder.bind(taskList[position])

    // 线性列表
    inner class LinearViewHolder(view: View) : BaseViewHolder(view) {
        private val binding = FragmentTaskItemLinearBinding.bind(view)
        override fun bind(task: Task) {
            with(binding) {
                // 解绑先前的事件Todo:优化
                taskLinear.setOnClickListener(null)
                taskLinear.setOnLongClickListener(null)
                taskLinearStatus.setOnCheckedChangeListener(null)
                // 基本属性赋值与UI优化
                taskLinearTitle.text = task.title
                taskLinearSubtitle.text = task.subtitle
                taskLinearDueDate.text = DateTimeUtils.timestampToString(task.dueDate)
                taskLinearStatus.isChecked = task.isFinish
                // 完成的事件变得透明一点
                taskLinear.alpha = if (task.isFinish) 0.3f else 1.0f
                // 超时没完成的事件变红
                taskLinearDueDate.setTextColor(
                    if ((!task.isFinish) && (task.dueDate < System.currentTimeMillis())) Color.RED
                    else Color.parseColor("#018786")
                )
                // 绑定点击事件[先解绑重用视图的绑定事件再绑定]
                taskLinear.setOnClickListener {
                    listenTaskItemClick.onClickItem(task)
                }
                taskLinear.setOnLongClickListener {
                    listenTaskItemClick.onLongClickItem(task)
                    true
                }
                taskLinearStatus.setOnCheckedChangeListener { _, isChecked ->
                    listenTaskItemClick.onClickCheckBox(task, isChecked)
                }
            }
        }
    }

    // 瀑布流列表
    inner class GridViewHolder(view: View) : BaseViewHolder(view) {
        private val binding = FragmentTaskItemGridBinding.bind(view)
        override fun bind(task: Task) {
            with(binding) {
                // 解绑先前的事件Todo:优化
                taskGrid.setOnClickListener(null)
                taskGrid.setOnLongClickListener(null)
                taskGridStatus.setOnCheckedChangeListener(null)
                // 基本属性赋值与UI优化
                taskGridTitle.text = task.title
                // 没有副标题的item副标题不占空间
                if (task.subtitle.isEmpty()) {
                    taskGridSubtitle.visibility = View.GONE
                } else {
                    taskGridSubtitle.visibility = View.VISIBLE
                    taskGridSubtitle.text = task.subtitle
                }
                taskGridDueDate.text = DateTimeUtils.timestampToString(task.dueDate)
                taskGridStatus.isChecked = task.isFinish
                // 没有图片的不会占用空间
                if (task.image.isNullOrEmpty()) {
                    taskGridCoverImage.visibility = View.GONE
                } else {
                    taskGridCoverImage.visibility = View.VISIBLE
                    // Todo:自适应高度
                    Glide.with(taskGridCoverImage.context)
                        .load(task.image)  // 图片的 URL
                        .downsample(DownsampleStrategy.CENTER_INSIDE) // 根据目标区域缩放图片
                        .placeholder(R.drawable.app_icon)  // 占位图
                        .into(taskGridCoverImage)
                }
                // 完成的事件变得透明一点
                taskGrid.alpha = if (task.isFinish) 0.3f else 1.0f
                // 超时没完成的事件变红
                taskGridDueDate.setTextColor(
                    if ((!task.isFinish) && (task.dueDate < System.currentTimeMillis())) Color.RED
                    else Color.parseColor("#018786")
                )
                // 事件绑定
                taskGrid.setOnClickListener {
                    listenTaskItemClick.onClickItem(task)
                }
                taskGrid.setOnLongClickListener {
                    listenTaskItemClick.onLongClickItem(task)
                    true
                }
                taskGridStatus.setOnCheckedChangeListener { _, isChecked ->
                    listenTaskItemClick.onClickCheckBox(task, isChecked)
                }
            }
        }
    }
}