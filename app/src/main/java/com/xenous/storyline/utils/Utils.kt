package com.xenous.storyline.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import androidx.annotation.RequiresApi
import com.xenous.storyline.broadcasts.NotificationBroadcastReceiver
import java.util.*

const val ERROR_CODE = 9000
const val SUCCESS_CODE = 9001
const val CANCEL_CODE = 9002
const val QUERY_IS_EMPTY = 9003
const val DOCUMENT_DOES_NOT_EXIST = 9004
const val PREFERENCE_NOT_FOUND = -3718L
const val MILLIS_IN_DAY = 86400000

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
    getTimeInMillisAtZeroHours(this) - MILLIS_IN_DAY >=
            getTimeInMillisAtZeroHours(anotherLong)

fun Long.isTomorrowOf(anotherLong: Long) =
    getTimeInMillisAtZeroHours(this) ==
            getTimeInMillisAtZeroHours(anotherLong) + MILLIS_IN_DAY

fun Long.isSameDayAs(anotherLong: Long) =
    getTimeInMillisAtZeroHours(this) ==
            getTimeInMillisAtZeroHours(anotherLong)

@RequiresApi(Build.VERSION_CODES.O)
fun createNotificationChannel(context: Context) {
    val name: CharSequence = "StoryLine Notification Channel"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(NotificationBroadcastReceiver.NOTIFICATION_ID_KEY, name, importance)
    channel.enableLights(true)
    channel.enableVibration(true)
    channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    
    val notificationManager: NotificationManager = context.getSystemService(
        NotificationManager::class.java
    )!!
    notificationManager.createNotificationChannel(channel)
}
            