package com.wujia.foundation.toolkit.time

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 时间日期工具类
 *
 * 提供时间戳与字符串的互相转换、日期各字段获取、
 * 日期比较、闰年判断、友好时间跨度格式化等功能。
 */
object TimeUtils {

    private const val DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val TIME_FORMAT = "HH:mm:ss"

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 当前时间戳
     */
    fun now(): Long {
        return System.currentTimeMillis()
    }

    /**
     * 将时间戳格式化为字符串
     *
     * @param timestamp 时间戳（毫秒）
     * @param pattern   格式模式，默认 "yyyy-MM-dd HH:mm:ss"
     * @return 格式化后的时间字符串
     */
    fun format(timestamp: Long, pattern: String = DEFAULT_FORMAT): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }

    /**
     * 将 Date 格式化为字符串
     *
     * @param date    日期对象
     * @param pattern 格式模式，默认 "yyyy-MM-dd HH:mm:ss"
     * @return 格式化后的时间字符串
     */
    fun format(date: Date, pattern: String = DEFAULT_FORMAT): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }

    /**
     * 将时间字符串解析为 Date
     *
     * @param timeStr 时间字符串
     * @param pattern 格式模式，默认 "yyyy-MM-dd HH:mm:ss"
     * @return Date 对象，解析失败返回 null
     */
    fun parse(timeStr: String, pattern: String = DEFAULT_FORMAT): Date? {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).parse(timeStr)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 将时间字符串解析为时间戳（毫秒）
     *
     * @param timeStr 时间字符串
     * @param pattern 格式模式，默认 "yyyy-MM-dd HH:mm:ss"
     * @return 时间戳（毫秒），解析失败返回 0
     */
    fun parseToLong(timeStr: String, pattern: String = DEFAULT_FORMAT): Long {
        return parse(timeStr, pattern)?.time ?: 0L
    }

    /**
     * 获取日期字符串（yyyy-MM-dd）
     *
     * @param timestamp 时间戳（毫秒），默认为当前时间
     * @return 日期字符串
     */
    fun getDateString(timestamp: Long = now()): String {
        return format(timestamp, DATE_FORMAT)
    }

    /**
     * 获取时间字符串（HH:mm:ss）
     *
     * @param timestamp 时间戳（毫秒），默认为当前时间
     * @return 时间字符串
     */
    fun getTimeString(timestamp: Long = now()): String {
        return format(timestamp, TIME_FORMAT)
    }

    /**
     * 获取日期时间字符串（yyyy-MM-dd HH:mm:ss）
     *
     * @param timestamp 时间戳（毫秒），默认为当前时间
     * @return 日期时间字符串
     */
    fun getDateTimeString(timestamp: Long = now()): String {
        return format(timestamp, DEFAULT_FORMAT)
    }

    /**
     * 将时间戳转为 Calendar 对象
     *
     * @param timestamp 时间戳（毫秒），默认为当前时间
     * @return Calendar 对象
     */
    fun getCalendar(timestamp: Long = now()): Calendar {
        return Calendar.getInstance().apply { timeInMillis = timestamp }
    }

    /**
     * 获取年份
     *
     * @param timestamp 时间戳（毫秒），默认为当前时间
     * @return 年份，如 2024
     */
    fun getYear(timestamp: Long = now()): Int {
        return getCalendar(timestamp).get(Calendar.YEAR)
    }

    /**
     * 获取月份（1~12）
     *
     * @param timestamp 时间戳（毫秒），默认为当前时间
     * @return 月份，1 表示一月，12 表示十二月
     */
    fun getMonth(timestamp: Long = now()): Int {
        return getCalendar(timestamp).get(Calendar.MONTH) + 1
    }

    /**
     * 获取日期（当月第几天）
     *
     * @param timestamp 时间戳（毫秒），默认为当前时间
     * @return 日期，1~31
     */
    fun getDay(timestamp: Long = now()): Int {
        return getCalendar(timestamp).get(Calendar.DAY_OF_MONTH)
    }

    /**
     * 获取小时（24 小时制）
     *
     * @param timestamp 时间戳（毫秒），默认为当前时间
     * @return 小时，0~23
     */
    fun getHour(timestamp: Long = now()): Int {
        return getCalendar(timestamp).get(Calendar.HOUR_OF_DAY)
    }

    /**
     * 获取分钟
     *
     * @param timestamp 时间戳（毫秒），默认为当前时间
     * @return 分钟，0~59
     */
    fun getMinute(timestamp: Long = now()): Int {
        return getCalendar(timestamp).get(Calendar.MINUTE)
    }

    /**
     * 获取秒数
     *
     * @param timestamp 时间戳（毫秒），默认为当前时间
     * @return 秒数，0~59
     */
    fun getSecond(timestamp: Long = now()): Int {
        return getCalendar(timestamp).get(Calendar.SECOND)
    }

    /**
     * 获取星期几（周一为 1，周日为 7）
     *
     * @param timestamp 时间戳（毫秒），默认为当前时间
     * @return 星期几，1~7（周一~周日）
     */
    fun getDayOfWeek(timestamp: Long = now()): Int {
        val day = getCalendar(timestamp).get(Calendar.DAY_OF_WEEK)
        return if (day == Calendar.SUNDAY) 7 else day - 1
    }

    /**
     * 获取一年中的第几天
     *
     * @param timestamp 时间戳（毫秒），默认为当前时间
     * @return 一年中的天数，1~366
     */
    fun getDayOfYear(timestamp: Long = now()): Int {
        return getCalendar(timestamp).get(Calendar.DAY_OF_YEAR)
    }

    /**
     * 获取一年中的第几周
     *
     * @param timestamp 时间戳（毫秒），默认为当前时间
     * @return 一年中的周数，1~53
     */
    fun getWeekOfYear(timestamp: Long = now()): Int {
        return getCalendar(timestamp).get(Calendar.WEEK_OF_YEAR)
    }

    /**
     * 判断指定时间戳是否为今天
     *
     * @param timestamp 时间戳（毫秒）
     * @return 今天返回 true，否则返回 false
     */
    fun isToday(timestamp: Long): Boolean {
        val cal = getCalendar(timestamp)
        val today = getCalendar()
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * 判断两个时间戳是否为同一天
     *
     * @param timestamp1 第一个时间戳（毫秒）
     * @param timestamp2 第二个时间戳（毫秒）
     * @return 同一天返回 true，否则返回 false
     */
    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = getCalendar(timestamp1)
        val cal2 = getCalendar(timestamp2)
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * 判断是否为闰年
     *
     * @param year 年份
     * @return 闰年返回 true，否则返回 false
     */
    fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0
    }

    /**
     * 获取指定月份的天数
     *
     * @param year  年份
     * @param month 月份（1~12）
     * @return 该月天数，28~31
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * 将毫秒数转换为友好的时间跨度字符串
     *
     * 例如：3661000 -> "1小时1分钟1秒"
     *
     * @param millis    毫秒数
     * @param precision 精度，显示的最大单位数量，默认 4
     * @return 友好的时间跨度字符串，如 "1年2天3小时"、"5分钟30秒"
     */
    fun millisToFitTimeSpan(millis: Long, precision: Int = 4): String {
        val units = intArrayOf(
            365 * 24 * 60 * 60 * 1000,
            24 * 60 * 60 * 1000,
            60 * 60 * 1000,
            60 * 1000,
            1000,
            1
        )
        val unitNames = arrayOf("年", "天", "小时", "分钟", "秒", "毫秒")
        val sb = StringBuilder()
        var remainder = millis
        var count = 0
        for (i in units.indices) {
            if (remainder >= units[i]) {
                val value = remainder / units[i]
                remainder %= units[i]
                sb.append(value).append(unitNames[i])
                count++
                if (count >= precision) break
            }
        }
        return if (sb.isEmpty()) "0毫秒" else sb.toString()
    }
}