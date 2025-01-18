package com.soordinary.todo.utils

import com.soordinary.todo.TodoApplication

/**
 * 提供一些视图的px、sp等与代码中的int转换的公用方法
 *
 * @role1 获取屏幕密度、宽度、高度
 */
object SizeUnits {

    // 系统资源
    private val systemResources = TodoApplication.context.resources

    // 获取屏幕密度
    val screenMetrics get() = systemResources.displayMetrics
    // 获取屏幕宽度
    val screenWidth get() = screenMetrics.widthPixels
    // 获取屏幕高度
    val screenHeight get() = screenMetrics.heightPixels

    // 将dp转换为Int
    fun dpToPx(dp: Int): Int {
        return (dp * screenMetrics.density + 0.5f).toInt()
    }
}