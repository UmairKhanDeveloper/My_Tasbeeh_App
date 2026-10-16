package com.example.mytasbeehapp.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mytasbeehapp.data.repository.TasbeehRepositoryImpl
import com.example.mytasbeehapp.data.storage.TasbeehStorage
import com.example.mytasbeehapp.presentation.screens.setting.ReminderType

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val storage = TasbeehStorage(context)
            val repository = TasbeehRepositoryImpl(storage)

            if (repository.getMorningEnabled()) {
                ReminderScheduler.schedule(context, ReminderType.Morning, repository.getMorningTime())
            }
            if (repository.getEveningEnabled()) {
                ReminderScheduler.schedule(context, ReminderType.Evening, repository.getEveningTime())
            }
        }
    }
}
