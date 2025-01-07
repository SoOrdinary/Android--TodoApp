package com.todo.android.view.fragment.task

import android.os.Bundle
import android.view.ContextMenu
import android.view.View
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.todo.android.R
import com.todo.android.databinding.FragmentTaskBinding
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
        // 点击头像后打开侧边栏
        binding.profile.setOnClickListener{
            (activity as? MainActivity)?.binding?.layoutMain?.openDrawer(GravityCompat.START)
        }
    }



}