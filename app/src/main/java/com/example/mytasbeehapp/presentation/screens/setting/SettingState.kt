package com.example.mytasbeehapp.presentation.screens.setting

import java.time.LocalTime

enum class AppTheme(val label: String) { Light("Light"), Dark("Dark"), System("System default") }

enum class ReminderType { Morning, Evening }

data class SettingState(
    val streakDays: Int = 2,
    val theme: AppTheme = AppTheme.Light,
    val vibration: Boolean = true,
    val clickSound: Boolean = false,
    val keepScreenOn: Boolean = true,
    val volumeButtons: Boolean = true,
    val dailyGoal: Int = 350,
    val morningEnabled: Boolean = true,
    val morningTime: LocalTime = LocalTime.of(5, 30),
    val eveningEnabled: Boolean = true,
    val eveningTime: LocalTime = LocalTime.of(18, 30),
    val widgetZikr: String = "SubhanAllah",
    val widgetCount: Int = 0,
    val versionName: String = "1.0.0",
    val buildNumber: Int = 1,
    val showThemeDialog: Boolean = false,
    val showResetDialog: Boolean = false,
    val timePickerFor: ReminderType? = null,
    val cycleDay: Int = 1,
    val showResetCycleDialog: Boolean = false
)

