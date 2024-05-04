package com.tonapps.tonkeeper.helper

import android.icu.text.SimpleDateFormat
import java.util.Calendar

object DateHelper {

    private val todayCalendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private val dateFormatWeekDay = SimpleDateFormat("EEEE")
    private val dateFormatMonth = SimpleDateFormat("MMM dd")
    private val dateFormatYear = SimpleDateFormat("yyyy")

    private val dateFormatHour = SimpleDateFormat("HH:mm")
    private val dateFormatHourMonth = SimpleDateFormat("dd MMM, HH:mm")
    private val dateFormatHourYear = SimpleDateFormat("MMM dd yyyy, HH:mm")

    fun formatWeekDay(timestamp: Long): String {
        return dateFormatWeekDay.format(timestamp * 1000)
    }

    fun formatMonth(timestamp: Long): String {
        return dateFormatMonth.format(timestamp * 1000)
    }

    fun formatYear(timestamp: Long): String {
        return dateFormatYear.format(timestamp * 1000)
    }

    fun formatHour(timestamp: Long): String {
        return dateFormatHour.format(timestamp * 1000)
    }

    fun formatHourMonth(timestamp: Long): String {
        return dateFormatHourMonth.format(timestamp * 1000)
    }

    fun formatHourYear(timestamp: Long): String {
        return dateFormatHourYear.format(timestamp * 1000)
    }

    fun formatTime(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
        return when {
            isToday(timestamp) -> formatHour(timestamp)
            isYesterday(timestamp) -> formatHour(timestamp)
            isThisYear(timestamp) -> formatHourMonth(timestamp)
            else -> formatHourYear(timestamp)
        }
    }

    fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
        return calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == todayCalendar.get(Calendar.DAY_OF_MONTH)
    }

    fun isYesterday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
        return calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == todayCalendar.get(Calendar.DAY_OF_MONTH) - 1
    }

    fun isThisMonth(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
        return calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH)
    }

    fun isThisYear(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
        return calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR)
    }
}