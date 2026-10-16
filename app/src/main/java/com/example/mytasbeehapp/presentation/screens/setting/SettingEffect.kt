package com.example.mytasbeehapp.presentation.screens.setting

import java.time.LocalTime

sealed interface SettingEffect {
    data class ShowMessage(val message: String) : SettingEffect
    data class OpenUrl(val url: String) : SettingEffect
    data object RateApp : SettingEffect
    data object ShareApp : SettingEffect
    data object SendFeedback : SettingEffect
    data object NavigateToLicenses : SettingEffect
    data object NavigateToSplash : SettingEffect
    data object PlayVibration : SettingEffect
    data object PlayClickSound : SettingEffect
    data class ScheduleReminder(val type: ReminderType, val enabled: Boolean, val time: LocalTime) : SettingEffect
}
