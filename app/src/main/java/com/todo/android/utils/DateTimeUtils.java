package com.todo.android.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// Task时间转换工具
public class DateTimeUtils {

    // 日期格式：yyyy.MM.dd'  'HH:mm
    private static final String DATE_FORMAT = "yyyy.MM.dd'  'HH:mm";

    // 时间戳转日期字符串
    public static String timestampToString(Long timestamp) {
        if (timestamp == null || timestamp == 0L) {
            return "";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    // 日期字符串分隔，年月日+时+分
    public static String[] getSeparatedStringFromTimestamp(String dateString) {
        String[] firstParts = dateString.split("  ");
        String[] secondParts = firstParts[1].split(":");
        return new String[]{firstParts[0], secondParts[0], secondParts[1]};
    }

    // 日期字符串转时间戳
    public static Long stringToTimestamp(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return 0L;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        try {
            Date date = dateFormat.parse(dateString);
            return date != null ? date.getTime() : 0L;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    // 获取指定日期的起始时间戳（00:00:00），根据天数偏移
    public static long getStartOfDay(Integer daysOffset) {
        Calendar calendar = Calendar.getInstance();

        // 如果有天数偏移，调整日期
        if (daysOffset != null) {
            calendar.add(Calendar.DATE, daysOffset);  // 正值为未来，负值为过去
        }

        // 设置为当天的起始时间：00:00:00
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    // 获取指定日期的结束时间戳（23:59:59.999），根据天数偏移
    public static long getEndOfDay(Integer daysOffset) {
        Calendar calendar = Calendar.getInstance();

        // 如果有天数偏移，调整日期
        if (daysOffset != null) {
            calendar.add(Calendar.DATE, daysOffset);  // 正值为未来，负值为过去
        }

        // 设置为当天的结束时间：23:59:59.999
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
}
