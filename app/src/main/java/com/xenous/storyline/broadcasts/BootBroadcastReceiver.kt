package com.xenous.storyline.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xenous.storyline.services.NotificationService

class BootBroadcastReceiver: BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        callNotificationBroadcast(context)
    }
    
    private fun callNotificationBroadcast(context: Context) {
        context.startService(Intent(context, NotificationService::class.java))
    }
}
