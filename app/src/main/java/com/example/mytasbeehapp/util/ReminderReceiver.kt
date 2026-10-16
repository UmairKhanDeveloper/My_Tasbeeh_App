package com.example.mytasbeehapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mytasbeehapp.R
import com.example.mytasbeehapp.data.repository.TasbeehRepositoryImpl
import com.example.mytasbeehapp.data.storage.TasbeehStorage
import com.example.mytasbeehapp.presentation.MainActivity
import com.example.mytasbeehapp.presentation.screens.setting.ReminderType

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val typeName = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_TYPE) ?: return
        val type = try {
            ReminderType.valueOf(typeName)
        } catch (e: Exception) {
            return
        }

        showNotification(context, type)

        try {
            val storage = TasbeehStorage(context)
            val repository = TasbeehRepositoryImpl(storage)
            if (type == ReminderType.Morning && repository.getMorningEnabled()) {
                ReminderScheduler.schedule(context, ReminderType.Morning, repository.getMorningTime())
            } else if (type == ReminderType.Evening && repository.getEveningEnabled()) {
                ReminderScheduler.schedule(context, ReminderType.Evening, repository.getEveningTime())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNotification(context: Context, type: ReminderType) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channelId = "azkar_reminders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Azkaar Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders for morning and evening azkaar"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val notificationId = if (type == ReminderType.Morning) 2001 else 2002
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (type == ReminderType.Morning) "🌅 Morning Azkaar" else "🌙 Evening Azkaar"
        val message = if (type == ReminderType.Morning)
            "Start your day with the remembrance of Allah. Tap to recite your morning azkaar."
        else
            "End your day with the remembrance of Allah. Tap to recite your evening azkaar."

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.azkaar)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
