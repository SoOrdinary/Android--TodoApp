package com.soordinary.todo.utils

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * MarkDown转换工具类
 *
 * @role1 根据url读取assets文件夹中对应的Markdown文件
 */

object MarkDownUtil {

    // 从markDown中读取文件
    fun loadMarkdownFromAssets(context: Context, url: String): String {
        val stringBuilder = StringBuilder()
        try {
            val inputStream = context.assets.open(url)
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var line: String?
            while ((bufferedReader.readLine().also { line = it }) != null) {
                stringBuilder.append(line).append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }
}