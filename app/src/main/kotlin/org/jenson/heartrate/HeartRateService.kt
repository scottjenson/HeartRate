package org.jenson.heartrate

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.LocusIdCompat
import androidx.wear.ongoing.OngoingActivity

private const val CHANNEL_ID = "heart_rate_channel"
private const val LOCUS_ID    = "hr_session"
const val NOTIFICATION_ID     = 1

class HeartRateService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            @Suppress("DEPRECATION")
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        @Suppress("DEPRECATION")
        stopForeground(true)
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Heart Rate", NotificationManager.IMPORTANCE_LOW)
            )
        }

        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val icon    = Icon.createWithResource(this, R.drawable.ic_heart)
        val locusId = LocusIdCompat(LOCUS_ID)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_heart)
            .setContentTitle("Heart Rate")
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .setCategory("workout")
            .setLocusId(locusId)

        OngoingActivity.Builder(applicationContext, NOTIFICATION_ID, builder)
            .setStaticIcon(icon)
            .setTouchIntent(tapIntent)
            .setLocusId(locusId)
            .build()
            .apply(applicationContext)

        return builder.build()
    }
}
