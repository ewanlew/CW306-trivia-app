package com.ewan.triviaapp.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.ewan.triviaapp.R

class NotificationHelper (base: Context) : ContextWrapper(base){

    private var notifManager: NotificationManager? = null

    private val manager: NotificationManager?
        get() {
            if (notifManager == null) {
                notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            return notifManager
        }

    init{
        createChannels()
    }

    fun createChannels() {
        val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.setShowBadge(true)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        manager!!.createNotificationChannel(notificationChannel)
    }

    fun getNotification(title: String, body: String) : NotificationCompat.Builder{
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setAutoCancel(true)
    }

    fun scheduleNotification(title: String, body: String, triggerAtMillis: Long) {
        val notificationBuilder = getNotification(title, body)
        notify(1001, notificationBuilder)
    }

    fun cancelAllNotifications() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }


    fun notify(id: Int, notification: NotificationCompat.Builder) {
        manager!!.notify(id, notification.build())
    }

    companion object {
        const val CHANNEL_ID = "trivia_notification_channel"
        const val CHANNEL_NAME = "Notification Channel"
    }
}