package com.xenous.storyline.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.xenous.storyline.broadcasts.NotificationBroadcastReceiver
import com.xenous.storyline.utils.MILLIS_IN_DAY
import com.xenous.storyline.utils.createNotificationChannel
import java.util.*

class NotificationService: Service() {
    
    private companion object {
        const val TAG = "NotificationService"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Service has been created")
    
        if(FirebaseAuth.getInstance().currentUser != null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(this)
            }
            startAlarm()
        }
    }
    
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
    
    private fun startAlarm() {
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent: PendingIntent
    
        val calendar: Calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 12
        calendar[Calendar.MINUTE] = 0
    
        val myIntent: Intent = Intent(this, NotificationBroadcastReceiver::class.java)
        pendingIntent =
            PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    
        manager.cancel(pendingIntent)
        manager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis + MILLIS_IN_DAY,
            pendingIntent)
    }
}
