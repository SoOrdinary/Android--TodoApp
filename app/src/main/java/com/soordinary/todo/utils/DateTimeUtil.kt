package com.soordinary.todo.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 时间转换器，以及一些检验器
 *
 * @role1 将Long型时间转换为"yyyy.MM.dd'  'HH:mm"格式
 * @role2 将字符串时间分隔为年月日时分
 * @role3 获取当天的时间起始与末尾
 * @role4 一些时间单位的转换
 */
object DateTimeUtil {

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

    // 日期字符串转时间戳,不规范返回此刻时间
    fun stringToTimestamp(dateString: String?): Long {
        if (dateString.isNullOrEmpty()) {
            return System.currentTimeMillis()
        }
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        try {
            val date = dateFormat.parse(dateString)
            return date?.time ?: System.currentTimeMillis()
        } catch (e: ParseException) {
            e.printStackTrace()
            return System.currentTimeMillis()
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

    // 将时间长度（毫秒）转换为分钟
    fun millisToMinutes(millis: Long): Long {
        return millis / 1000 / 60
    }

    // 传入天数、时数、分数，计算对应的Long型时间戳
    fun convertToTimestamp(days: Int, hours: Int, minutes: Int): Long {
        val millisecondsInDay = 24 * 60 * 60 * 1000L  // 每天的毫秒数
        val millisecondsInHour = 60 * 60 * 1000L      // 每小时的毫秒数
        val millisecondsInMinute = 60 * 1000L          // 每分钟的毫秒数

        // 计算总的毫秒数
        val totalMilliseconds = (days * millisecondsInDay) +
                (hours * millisecondsInHour) +
                (minutes * millisecondsInMinute)

        return totalMilliseconds
    }

    // 反向转换：将时间戳（毫秒数）转换为天数、小时数和分钟数，返回一个数组
    fun convertFromTimestamp(timestamp: Long): IntArray {
        val millisecondsInDay = 24 * 60 * 60 * 1000L  // 每天的毫秒数
        val millisecondsInHour = 60 * 60 * 1000L      // 每小时的毫秒数
        val millisecondsInMinute = 60 * 1000L          // 每分钟的毫秒数
        val millisecondsInSecond = 1000L               // 每秒的毫秒数

        // 计算天数
        val days = (timestamp / millisecondsInDay).toInt()

        // 计算剩余的毫秒数，计算小时
        val remainingAfterDays = timestamp % millisecondsInDay
        val hours = (remainingAfterDays / millisecondsInHour).toInt()

        // 计算剩余的毫秒数，计算分钟
        val remainingAfterHours = remainingAfterDays % millisecondsInHour
        val minutes = (remainingAfterHours / millisecondsInMinute).toInt()

        // 计算剩余的毫秒数，计算秒
        val remainingAfterMinutes = remainingAfterHours % millisecondsInMinute
        val seconds = (remainingAfterMinutes / millisecondsInSecond).toInt()

        // 返回包含天数、小时数、分钟数和秒数的数组
        return intArrayOf(days, hours, minutes, seconds)
    }
}