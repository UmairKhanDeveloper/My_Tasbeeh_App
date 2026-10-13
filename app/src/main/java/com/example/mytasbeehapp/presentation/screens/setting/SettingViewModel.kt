package com.example.mytasbeehapp.presentation.screens.setting

import com.example.mytasbeehapp.presentation.base.BaseViewModel

class SettingViewModel :
    BaseViewModel<SettingState, SettingIntent, SettingEffect>(SettingState()) {

    override fun handleIntent(intent: SettingIntent) {
        when (intent) {
            SettingIntent.ThemeClicked -> setState { copy(showThemeDialog = true) }
            is SettingIntent.ThemeSelected -> setState { copy(theme = intent.theme, showThemeDialog = false) }
            SettingIntent.DismissThemeDialog -> setState { copy(showThemeDialog = false) }

            is SettingIntent.VibrationChanged -> setState { copy(vibration = intent.enabled) }
            is SettingIntent.ClickSoundChanged -> setState { copy(clickSound = intent.enabled) }
            is SettingIntent.KeepScreenOnChanged -> setState { copy(keepScreenOn = intent.enabled) }
            is SettingIntent.VolumeButtonsChanged -> setState { copy(volumeButtons = intent.enabled) }

            SettingIntent.DailyGoalIncrement -> setState { copy(dailyGoal = (dailyGoal + 10).coerceAtMost(100_000)) }
            SettingIntent.DailyGoalDecrement -> setState { copy(dailyGoal = (dailyGoal - 10).coerceAtLeast(10)) }

            is SettingIntent.ReminderToggled -> setState {
                when (intent.type) {
                    ReminderType.Morning -> copy(morningEnabled = intent.enabled)
                    ReminderType.Evening -> copy(eveningEnabled = intent.enabled)
                }
            }
            is SettingIntent.ReminderTimeClicked -> setState { copy(timePickerFor = intent.type) }
            is SettingIntent.ReminderTimeSelected -> setState {
                when (intent.type) {
                    ReminderType.Morning -> copy(morningTime = intent.time, timePickerFor = null)
                    ReminderType.Evening -> copy(eveningTime = intent.time, timePickerFor = null)
                }
            }
            SettingIntent.DismissTimePicker -> setState { copy(timePickerFor = null) }

            SettingIntent.WidgetIncrement -> setState { copy(widgetCount = widgetCount + 1) }

            SettingIntent.RateAppClicked -> sendEffect(SettingEffect.RateApp)
            SettingIntent.ShareAppClicked -> sendEffect(SettingEffect.ShareApp)
            SettingIntent.SendFeedbackClicked -> sendEffect(SettingEffect.SendFeedback)
            SettingIntent.PrivacyPolicyClicked -> sendEffect(SettingEffect.OpenUrl("https://example.com/privacy"))
            SettingIntent.TermsClicked -> sendEffect(SettingEffect.OpenUrl("https://example.com/terms"))
            SettingIntent.LicensesClicked -> sendEffect(SettingEffect.NavigateToLicenses)

            SettingIntent.ResetClicked -> setState { copy(showResetDialog = true) }
            SettingIntent.DismissResetDialog -> setState { copy(showResetDialog = false) }
            SettingIntent.ResetConfirmed -> {
                setState { SettingState(versionName = versionName, buildNumber = buildNumber) }
                sendEffect(SettingEffect.ShowMessage("All data has been reset"))
            }

            SettingIntent.ReplaySplashClicked -> sendEffect(SettingEffect.NavigateToSplash)
        }
    }
}