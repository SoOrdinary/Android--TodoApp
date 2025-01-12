package com.todo.android.view.fragment.task

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.todo.android.R
import com.todo.android.data.room.entity.Task
import com.todo.android.databinding.FragmentTaskGridBinding
import com.todo.android.databinding.FragmentTaskLinearBinding
import com.todo.android.utils.DateTimeUtils

/**
 * Task列表适配器 Todo:写一个带基类的RecycleView扩展库，方便不同布局的实现
 *
 * @role1 用于适配Task界面的列表每一项
 *
 * @explain1 依次传入的参数为 当前view、需要显示的列表、显示的方式
 *
 * @improve1 基类BaseViewHolder统管不同的item，拥有抽象函数bind将事件绑定同一归属
 * @improve2 Todo:适配图片等用Gilde，增加流畅度
 * @improve3 Todo:通过diff优化查询更新
 */
class TaskAdapter(private val fragment: TaskFragment, private val taskList: List<Task>, private val itemType: Int) : RecyclerView.Adapter<TaskAdapter.BaseViewHolder>() {

    // 点击事件适配
    val listenTaskItemClick = fragment.ListenTaskItemClick()
    // 内部基类，简化多种适配item与bind的书写
    abstract inner class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(task: Task)
    }

    // 根据返回的布局ID判断加载哪种布局
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        when(viewType){
            1-> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.fragment_task_linear, parent, false)
                return LinearViewHolder(view)
            }
            2-> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.fragment_task_grid, parent, false)
                return GridViewHolder(view)
            }
            else-> {
                Toast.makeText(fragment.context, "传入布局ID不存在,加载默认布局", Toast.LENGTH_SHORT).show()
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.fragment_task_linear, parent, false)
                return LinearViewHolder(view)
            }
        }
    }

    // 三个重写函数，依次说明布局类型、item个数、声明绑定(调用对应布局Holder重写的binding函数)
    override fun getItemViewType(position: Int) =  itemType
    override fun getItemCount() = taskList.size
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) = holder.bind(taskList[position])

    // 线性列表
    inner class LinearViewHolder(view: View) : BaseViewHolder(view){
        private val binding = FragmentTaskLinearBinding.bind(view)
        override fun bind(task: Task) {
            with(binding) {
                // 基本属性赋值与UI优化
                taskLinearTitle.text = task.title
                taskLinearSubtitle.text = task.subtitle
                taskLinearDueDate.text = DateTimeUtils.timestampToString(task.dueDate)
                taskLinearStatus.isChecked = task.isFinish
                // 完成的事件变得透明一点
                taskLinear.alpha = if (task.isFinish) 0.3f else 1.0f
                // 超时没完成的事件变红
                if ((!task.isFinish) && (task.dueDate!! < System.currentTimeMillis())) {
                    taskLinearDueDate.setTextColor(Color.RED)
                } else {
                    taskLinearDueDate.setTextColor(Color.parseColor("#018786"))
                }
                // 绑定点击事件
                taskLinear.setOnClickListener(null)
                taskLinear.setOnLongClickListener(null)
                taskLinearStatus.setOnClickListener(null)
                taskLinear.setOnClickListener {
                    listenTaskItemClick.onClickItem(it,task)
                }
                taskLinear.setOnLongClickListener {
                    listenTaskItemClick.onLongClickItem(it,task)
                    true
                }
                taskLinearStatus.setOnClickListener {
                    listenTaskItemClick.onClickCheckBox(task)
                }
            }
        }
    }

    // 瀑布流列表
    inner class GridViewHolder(view: View) : BaseViewHolder(view){
        private val binding = FragmentTaskGridBinding.bind(view)
        override fun bind(task: Task) {
            with(binding) {
                // 基本属性赋值与UI优化
                taskGridTitle.text = task.title
                // 没有副标题的item副标题不占空间
                if(task.subtitle.isEmpty()){
                    taskGridSubtitle.visibility = View.GONE
                }else{
                    taskGridSubtitle.visibility = View.VISIBLE
                    taskGridSubtitle.text = task.subtitle
                }
                taskGridDueDate.text = DateTimeUtils.timestampToString(task.dueDate)
                taskGridStatus.isChecked = task.isFinish
                // 没有图片的不会占用空间
                if(task.image.isNullOrEmpty()){
                    taskGridCoverImage.visibility = View.GONE
                }else{
                    taskGridCoverImage.visibility = View.VISIBLE
                    // Todo:自适应高度【原方案有问题，失败了过高】
                    Glide.with(taskGridCoverImage.context)
                        .load(task.image)  // 图片的 URL
                        .downsample(DownsampleStrategy.CENTER_INSIDE) // 根据目标区域缩放图片
                        .placeholder(R.drawable.app_icon)  // 占位图
                        .into(taskGridCoverImage)
                }
                // 完成的事件变得透明一点
                taskGrid.alpha = if (task.isFinish) 0.3f else 1.0f
                // 超时没完成的事件变红
                if ((!task.isFinish) && (task.dueDate!! < System.currentTimeMillis()))
                    taskGridDueDate.setTextColor(Color.RED)
                else
                    taskGridDueDate.setTextColor(Color.parseColor("#018786"))
                // Todo:事件绑定
                taskGrid.setOnClickListener(null)
                taskGrid.setOnLongClickListener(null)
                taskGridStatus.setOnClickListener(null)
                taskGrid.setOnClickListener {
                    listenTaskItemClick.onClickItem(it,task)
                }
                taskGrid.setOnLongClickListener {
                    listenTaskItemClick.onLongClickItem(it,task)
                    true
                }
                taskGridStatus.setOnClickListener {
                    listenTaskItemClick.onClickCheckBox(task)
                }
            }
        }
    }


}


/* 加载自适应高度，有点问题

Glide.with(taskGridCoverImage.context)
.asBitmap()  // 加载为 Bitmap
.load(task.image)  // 加载图片的 URL
.placeholder(R.drawable.app_icon)  // 设置占位图
.into(object : CustomTarget<Bitmap>() {
    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        // 设置 LayoutParams 来动态调整高度
        val width = taskGridCoverImage.width
        val height = (width * resource.height.toFloat() / resource.width).toInt()

        taskGridCoverImage.layoutParams = taskGridCoverImage.layoutParams.apply {
            this.width = width
            this.height = height
        }

        // 设置加载的图片
        taskGridCoverImage.setImageBitmap(resource)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        // 图片加载失败时处理：设置占位图或清除图片
        taskGridCoverImage.setImageDrawable(placeholder)
    }
})

*/