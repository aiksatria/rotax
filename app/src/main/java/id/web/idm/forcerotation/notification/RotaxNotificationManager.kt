package id.web.idm.forcerotation.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import id.web.idm.forcerotation.MainActivity
import id.web.idm.forcerotation.R
import id.web.idm.forcerotation.domain.OrientationMode
import id.web.idm.forcerotation.receiver.RotaxActionReceiver

object RotaxNotificationManager {

    const val CHANNEL_ID = "rotax_control_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(
        context: Context,
        mode: OrientationMode,
        isOverrideActive: Boolean,
        isFloatingActive: Boolean
    ): Notification {
        createNotificationChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val portraitIntent = Intent(context, RotaxActionReceiver::class.java).apply {
            action = RotaxActionReceiver.ACTION_SET_PORTRAIT
        }
        val portraitPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            portraitIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val landscapeIntent = Intent(context, RotaxActionReceiver::class.java).apply {
            action = RotaxActionReceiver.ACTION_SET_LANDSCAPE
        }
        val landscapePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            landscapeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val autoIntent = Intent(context, RotaxActionReceiver::class.java).apply {
            action = RotaxActionReceiver.ACTION_SET_AUTO
        }
        val autoPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            autoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = if (isOverrideActive) "System override ACTIVE" else "System override INACTIVE"
        val contentText = "Orientation: ${mode.displayName} • $statusText"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_rotax_tile)
            .setContentTitle("ROTAX")
            .setContentText(contentText)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .addAction(0, "Portrait", portraitPendingIntent)
            .addAction(0, "Landscape", landscapePendingIntent)
            .addAction(0, "Auto", autoPendingIntent)

        if (isFloatingActive) {
            val stopFloatIntent = Intent(context, RotaxActionReceiver::class.java).apply {
                action = RotaxActionReceiver.ACTION_STOP_FLOAT
            }
            val stopFloatPendingIntent = PendingIntent.getBroadcast(
                context,
                4,
                stopFloatIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Float Off", stopFloatPendingIntent)
        }

        return builder.build()
    }

    fun updateNotification(
        context: Context,
        mode: OrientationMode,
        isOverrideActive: Boolean,
        isFloatingActive: Boolean
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = buildNotification(context, mode, isOverrideActive, isFloatingActive)
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID)
    }
}
