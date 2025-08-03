package com.soordinary.todo.utils.sptommkv

import android.content.Context
import com.tencent.mmkv.MMKV

object MMKVMigrationHelper {
    // 标记迁移状态的键
    private const val MIGRATION_COMPLETED = "mmkv_migration_completed"

    /**
     * 迁移所有SP数据到MMKV
     */
    fun migrateAll(context: Context) {
        val migrationMMKV = MMKV.mmkvWithID("migration_status")
        if (migrationMMKV.getBoolean(MIGRATION_COMPLETED, false)) {
            return // 已迁移过，直接返回
        }

        // 迁移任务标签数据
        migrateSPToMMKV(
            context = context,
            spName = "task_tags",
            mmkvId = "task_tags"
        )

        // 迁移用户信息数据
        migrateSPToMMKV(
            context = context,
            spName = "user_info",
            mmkvId = "user_info"
        )

        // 标记迁移完成
        migrationMMKV.encode(MIGRATION_COMPLETED, true)
    }

    /**
     * 单个SP文件迁移到对应MMKV实例
     */
    private fun migrateSPToMMKV(context: Context, spName: String, mmkvId: String) {
        // 获取旧的SP实例
        val sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
        val allEntries = sp.all ?: return

        // 获取对应的MMKV实例
        val mmkv = MMKV.mmkvWithID(mmkvId)

        // 遍历所有键值对，按类型迁移
        for ((key, value) in allEntries) {
            when (value) {
                is String -> mmkv.encode(key, value)
                is Int -> mmkv.encode(key, value)
                is Boolean -> mmkv.encode(key, value)
                is Float -> mmkv.encode(key, value)
                is Long -> mmkv.encode(key, value)
                is Set<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val stringSet = value as Set<String>
                    mmkv.encode(key, stringSet)
                }
            }
        }
    }
}