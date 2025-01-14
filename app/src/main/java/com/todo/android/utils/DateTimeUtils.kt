package com.todo.android.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 时间转换器，以及一些检验器
 *
 * @role1 将Long型时间转换为"yyyy.MM.dd'  'HH:mm"格式
 * @role2 将时间分隔为年月日时分
 * @role3 获取当天的时间起始与末尾
 */
object DateTimeUtils {
    // 日期格式：yyyy.MM.dd'  'HH:mm
    private const val DATE_FORMAT = "yyyy.MM.dd'  'HH:mm"

    // 时间戳转日期字符串
    fun timestampToString(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) {
            return ""
        }
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    // 日期字符串分隔，年月日+时+分
    fun getSeparatedStringFromTimestamp(dateString: String): Array<String> {
        val firstParts =
            dateString.split("  ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val secondParts =
            firstParts[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return arrayOf(firstParts[0], secondParts[0], secondParts[1])
    }

    // 日期字符串转时间戳
    fun stringToTimestamp(dateString: String?): Long {
        if (dateString.isNullOrEmpty()) {
            return 0L
        }
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        try {
            val date = dateFormat.parse(dateString)
            return date?.time ?: 0L
        } catch (e: ParseException) {
            e.printStackTrace()
            return 0L
        }
    }

    // 获取指定日期的起始时间戳（00:00:00），根据天数偏移
    fun getStartOfDay(daysOffset: Int?): Long {
        val calendar = Calendar.getInstance()

        // 如果有天数偏移，调整日期
        if (daysOffset != null) {
            calendar.add(Calendar.DATE, daysOffset) // 正值为未来，负值为过去
        }

        // 设置为当天的起始时间：00:00:00
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.timeInMillis
    }

    // 获取指定日期的结束时间戳（23:59:59.999），根据天数偏移
    fun getEndOfDay(daysOffset: Int?): Long {
        val calendar = Calendar.getInstance()

        // 如果有天数偏移，调整日期
        if (daysOffset != null) {
            calendar.add(Calendar.DATE, daysOffset) // 正值为未来，负值为过去
        }

        // 设置为当天的结束时间：23:59:59.999
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        calendar[Calendar.MILLISECOND] = 999
        return calendar.timeInMillis
    }
}