package com.example.todo.data.shared;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * 用于管理 Todo 应用中的标签数据，存储标签列表。
 * 本类采用 Singleton 模式，以确保只使用一个 SharedPreferences 实例来存储标签数据。
 */
public class TodoTagShared {

    public static final String SPDB_NAME = "todo_tags";   // sharedPreferences 文件名称
    public static final String TAG_LIST = "tag_list";     // 存储标签列表的 key

    private final SharedPreferences sharedPreferences;
    private static TodoTagShared instance;

    // 私有化构造函数，确保外部无法直接实例化
    private TodoTagShared(Context context) {
        this.sharedPreferences = context.getSharedPreferences(SPDB_NAME, Context.MODE_PRIVATE);
    }

    // 获取 TodoTagShared 实例（单例模式）
    public static synchronized TodoTagShared getInstance(Context context) {
        if (instance == null) {
            instance = new TodoTagShared(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 获取所有标签[sorted]（如果没有标签，返回一个空的 Set）。
     */
    public Set<String> getTags() {
        // 获取标签列表，如果没有标签，则返回一个空的 HashSet
        Set<String> tags = sharedPreferences.getStringSet(TAG_LIST, new HashSet<>());

        // 如果获取到的标签列表为空，直接返回一个空的 TreeSet
        if (tags == null || tags.isEmpty()) {
            return new TreeSet<>();
        }

        // 将标签集合转换为 TreeSet 来进行字母排序
        return new TreeSet<>(tags);
    }


    /**
     * 添加新标签，如果标签不存在则添加成功并返回 true。
     */
    public boolean addTag(String tag) {
        Set<String> tags = getTags();
        if (!tags.contains(tag)) {
            tags.add(tag); // 如果标签不在列表中，则添加
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(TAG_LIST, tags);  // 更新标签列表
            return editor.commit();  // 同步保存标签数据
        }
        return false;  // 标签已存在，返回 false
    }

    /**
     * 删除指定标签，如果标签存在则删除并返回 true。
     */
    public boolean removeTag(String tag) {
        Set<String> tags = getTags();
        if (tags.contains(tag)) {
            tags.remove(tag);  // 删除标签
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(TAG_LIST, tags);  // 更新标签列表
            return editor.commit();  // 同步保存标签数据
        }
        return false;  // 标签不存在，返回 false
    }

    /**
     * 判断指定标签是否已经存在。
     */
    public boolean isTagExist(String tag) {
        Set<String> tags = getTags();
        return tags.contains(tag);  // 返回标签是否存在
    }

}
