package com.ewan.triviaapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Trivia Reminder"
        val body = intent.getStringExtra("body") ?: "It's time for your trivia challenge!"

        val notificationHelper = NotificationHelper(context)
        val notification: NotificationCompat.Builder = notificationHelper.getNotification(title, body)
        notificationHelper.notify(1001, notification)
    }
}
