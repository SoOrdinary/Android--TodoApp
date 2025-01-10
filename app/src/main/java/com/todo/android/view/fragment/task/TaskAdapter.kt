package com.todo.android.view.fragment.task

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.todo.android.R
import com.todo.android.data.room.entity.Task
import com.todo.android.databinding.FragmentTaskGridBinding
import com.todo.android.databinding.FragmentTaskLinearBinding
import com.todo.android.utils.DateTimeUtils

/**
 * Task列表适配器 Todo:写一个带基类的RecycleView扩展库，方便不同布局的实现
 *
 * @improve 基类BaseViewHolder统管不同的item
 * @improve Todo：适配图片等用Gilde
 */
class TaskAdapter(private val fragment: TaskFragment, private val taskList: List<Task>, private val itemType: Int) : RecyclerView.Adapter<TaskAdapter.BaseViewHolder>() {

    // 内部基类，简化多种适配item与bind的书写
    abstract inner class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(task: Task)
    }

    // 根据返回的布局ID判断加载哪个布局
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        when(viewType){
            0-> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.fragment_task_linear, parent, false)
                return LinearViewHolder(view)
            }
            1-> {
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
            binding.apply {
                // 基本属性赋值与UI优化
                taskLinearTitle.text = task.title
                taskLinearSubtitle.text = task.subtitle
                taskLinearDueDate.text = DateTimeUtils.timestampToString(task.dueDate)
                taskLinearStatus.isChecked = task.isFinish
                if (task.isFinish) {
                    taskLinear.setAlpha(0.3f);
                } else {
                    taskLinear.setAlpha(1.0f);
                }
                if ((!task.isFinish) && (task.dueDate!! < System.currentTimeMillis())) {
                    taskLinearDueDate.setTextColor(Color.RED);
                } else {
                    taskLinearDueDate.setTextColor(Color.parseColor("#018786"));
                }
                // Todo:绑定点击事件

            }
        }
    }

    // 瀑布流列表
    inner class GridViewHolder(view: View) : BaseViewHolder(view){
        private val binding = FragmentTaskGridBinding.bind(view)
        override fun bind(task: Task) {
            binding.apply {
                // 基本属性赋值与UI优化
                taskGridTitle.text = task.title
                if(task.subtitle.isNotEmpty()){
                    taskGridSubtitle.text = task.subtitle
                    taskGridSubtitle.visibility = View.VISIBLE
                }else{
                    taskGridSubtitle.visibility = View.GONE
                }
                taskGridDueDate.text = DateTimeUtils.timestampToString(task.dueDate)
                taskGridStatus.isChecked = task.isFinish
                if(task.image.isNullOrEmpty()){
                    taskGridCoverImage.visibility = View.GONE
                }else{
                    taskGridCoverImage.visibility = View.VISIBLE
                    // Todo:加载自适应高度的图片
                }
                if (task.isFinish) {
                    taskGrid.setAlpha(0.3f);
                } else {
                    taskGrid.setAlpha(1.0f);
                }
                if ((!task.isFinish) && (task.dueDate!! < System.currentTimeMillis())) {
                    taskGridDueDate.setTextColor(Color.RED);
                } else {
                    taskGridDueDate.setTextColor(Color.parseColor("#018786"));
                }
                // Todo:事件绑定

            }
        }
    }
}