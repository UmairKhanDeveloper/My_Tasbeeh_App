package com.example.mytasbeehapp.presentation.screens.setting

import java.time.LocalTime

sealed interface SettingIntent {
    data object ThemeClicked : SettingIntent
    data class ThemeSelected(val theme: AppTheme) : SettingIntent
    data object DismissThemeDialog : SettingIntent

    data class VibrationChanged(val enabled: Boolean) : SettingIntent
    data class ClickSoundChanged(val enabled: Boolean) : SettingIntent
    data class KeepScreenOnChanged(val enabled: Boolean) : SettingIntent
    data class VolumeButtonsChanged(val enabled: Boolean) : SettingIntent

    data object DailyGoalIncrement : SettingIntent
    data object DailyGoalDecrement : SettingIntent

    data class ReminderToggled(val type: ReminderType, val enabled: Boolean) : SettingIntent
    data class ReminderTimeClicked(val type: ReminderType) : SettingIntent
    data class ReminderTimeSelected(val type: ReminderType, val time: LocalTime) : SettingIntent
    data object DismissTimePicker : SettingIntent

    data object WidgetIncrement : SettingIntent

    data object RateAppClicked : SettingIntent
    data object ShareAppClicked : SettingIntent
    data object SendFeedbackClicked : SettingIntent
    data object PrivacyPolicyClicked : SettingIntent
    data object TermsClicked : SettingIntent
    data object LicensesClicked : SettingIntent

    data object ResetClicked : SettingIntent
    data object ResetConfirmed : SettingIntent
    data object DismissResetDialog : SettingIntent

    data object ReplaySplashClicked : SettingIntent
}
