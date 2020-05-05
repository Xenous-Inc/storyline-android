package com.xenous.storyline.utils

import android.content.Context
import android.util.DisplayMetrics
import java.util.*

const val ERROR_CODE = 9000
const val SUCCESS_CODE = 9001
const val CANCEL_CODE = 9002
const val QUERY_IS_EMPTY = 9003
const val MILLIS_IN_DAY = 86400000
const val PREFERENCE_NOT_FOUND = -3718L

fun Int.dpToPx(context: Context): Int =
    this * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)

fun Float.dpToPx(context: Context): Float =
    this * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)

fun getTimeInMillisAtZeroHours(timeInMillis: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeInMillis
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    
    return calendar.timeInMillis
}

fun Long.isDayAfter(anotherLong: Long) =
    getTimeInMillisAtZeroHours(this) - MILLIS_IN_DAY >
            getTimeInMillisAtZeroHours(anotherLong)

fun Long.isDayBefore(anotherLong: Long) =
    getTimeInMillisAtZeroHours(this) <
            getTimeInMillisAtZeroHours(anotherLong) - MILLIS_IN_DAY

fun Long.isInSameDay(anotherLong: Long) =
    getTimeInMillisAtZeroHours(this) ==
            getTimeInMillisAtZeroHours(anotherLong)
            