package com.dhkim139.admin.wheretogo.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dhkim139.admin.wheretogo.R
import com.dhkim139.admin.wheretogo.feature.main.MainActivity

class AdminNotification(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "report_channel"
        const val CHANNEL_NAME = "신고 알림"
        const val INTENT_REPORT_ID = "reportId"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            getManager().createNotificationChannel(channel)
        }
    }

    fun show(
        title: String,
        body: String,
        screen: String? = null,
        targetId: String? = null
    ) {
        val pendingIntent = createPendingIntent(screen, targetId)
        val notification = buildNotification(title, body, pendingIntent)
        getManager().notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createPendingIntent(screen: String?, targetId: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(INTENT_REPORT_ID, targetId)
        }

        return PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildNotification(
        title: String,
        body: String,
        pendingIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun getManager(): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}