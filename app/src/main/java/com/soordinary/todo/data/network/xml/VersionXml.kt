package com.soordinary.todo.data.network.xml

import org.jsoup.Jsoup
import java.io.IOException


/**
 * 用于获取版本号和版本名的静态类
 */
object VersionXml {
    fun getAppVersionFromXml(url: String): AppVersionInfo? {
        try {
            // 发起网络请求获取 XML 内容，忽略 MIME 类型
            val response = Jsoup.connect(url)
                .ignoreContentType(true)
                .execute()
            val xmlContent = response.body()
            // 简单验证内容是否包含 XML 标签
            if (xmlContent != null && xmlContent.contains("<") && xmlContent.contains(">")) {
                // 以 XML 模式解析内容
                val doc = Jsoup.parse(xmlContent, "", org.jsoup.parser.Parser.xmlParser())
                // 查找包含 versionCode 和 versionName 的元素
                val versionCodeMeta = doc.select("meta[itemprop=versionCode]").first()
                val versionNameMeta = doc.select("meta[itemprop=versionName]").first()

                val versionCode = versionCodeMeta?.attr("content")
                val versionName = versionNameMeta?.attr("content")

                return AppVersionInfo(versionCode, versionName)
            } else {
                println("Response content is not a valid XML.")
            }
        } catch (e: IOException) {
            println("Network request failed: ${e.message}")
            e.printStackTrace()
        }
        return null
    }
}