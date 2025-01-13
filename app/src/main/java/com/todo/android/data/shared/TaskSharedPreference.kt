package com.todo.android.data.shared

import android.content.Context
import android.content.SharedPreferences
import com.todo.android.TodoApplication

/**
 * 存储Task的标签信息
 */
object TaskSharedPreference {

    private const val SP_NAME = "task_tags"
    private const val TAG_LIST = "tag_list"

    private val sharedPreferences: SharedPreferences=TodoApplication.context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)

    val tags: MutableSet<String>
        get() = sharedPreferences.getStringSet(TAG_LIST, emptySet())?.toSortedSet() ?: sortedSetOf()

    // 增加标签
    fun addTag(tag: String): Boolean {
        val currentTags = tags.toMutableSet()
        return if (currentTags.add(tag)) {
            sharedPreferences.edit().putStringSet(TAG_LIST, currentTags).commit()
        } else {
            false
        }
    }

    // 移除标签
    fun removeTag(tag: String): Boolean {
        val currentTags = tags.toMutableSet()
        return if (currentTags.remove(tag)) {
            sharedPreferences.edit().putStringSet(TAG_LIST, currentTags).commit()
        } else {
            false
        }
    }

    // 判断某标签是否包含
    fun isContain(tag: String):Boolean = sharedPreferences.getStringSet(TAG_LIST, emptySet())?.contains(tag) ?:false
}
