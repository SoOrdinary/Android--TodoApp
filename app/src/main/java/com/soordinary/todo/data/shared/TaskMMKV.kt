package com.soordinary.todo.data.shared

import com.tencent.mmkv.MMKV

/**
 * 存储Task的标签信息
 */
object TaskMMKV {

    private val mmkv = MMKV.mmkvWithID("task_tags", MMKV.MULTI_PROCESS_MODE)
    private const val TAG_LIST = "tag_list"

    val tags: MutableSet<String>
        get() = mmkv.decodeStringSet(TAG_LIST, emptySet())?.toSortedSet() ?: sortedSetOf()

    fun addTag(tag: String): Boolean {
        val currentTags = tags.toMutableSet()
        return if (currentTags.add(tag)) {
            mmkv.encode(TAG_LIST, currentTags) // 这里需要返回布尔值，保持不变
            true
        } else {
            false
        }
    }

    fun removeTag(tag: String): Boolean {
        val currentTags = tags.toMutableSet()
        return if (currentTags.remove(tag)) {
            mmkv.encode(TAG_LIST, currentTags) // 这里需要返回布尔值，保持不变
            true
        } else {
            false
        }
    }

    fun isContain(tag: String): Boolean = tags.contains(tag)
}
