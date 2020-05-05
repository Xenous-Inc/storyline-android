package com.xenous.storyline.broadcasts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.xenous.storyline.R
import com.xenous.storyline.activities.MainActivity
import com.xenous.storyline.utils.MILLIS_IN_DAY
import java.util.*

class NotificationBroadcastReceiver: BroadcastReceiver() {
    
    companion object {
        const val NOTIFICATION_ID_KEY = "NewStoryNotify"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        callNotification(context)
    }
    
    private fun callNotification(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cancelingPendingIntent: PendingIntent
    
        val calendar: Calendar = GregorianCalendar()
        calendar[Calendar.HOUR_OF_DAY] = 12
        calendar[Calendar.MINUTE] = 0
    
        val cancelingIntent = Intent(context, NotificationBroadcastReceiver::class.java)
        cancelingPendingIntent =
            PendingIntent.getBroadcast(context, 0, cancelingIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    
        manager.cancel(cancelingPendingIntent)
        manager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + MILLIS_IN_DAY ,
            cancelingPendingIntent)
    
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        //Вызов уведомления
        val builder = NotificationCompat.Builder(context, NOTIFICATION_ID_KEY)
            .setSmallIcon(R.drawable.ic_launcher_background) //ToDO: Replace for new application icon
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true).setContentIntent(pendingIntent)
            .setContentText("Пора повторить слова!").setPriority(NotificationCompat.PRIORITY_HIGH)
    
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(200, builder.build())
    }
}
