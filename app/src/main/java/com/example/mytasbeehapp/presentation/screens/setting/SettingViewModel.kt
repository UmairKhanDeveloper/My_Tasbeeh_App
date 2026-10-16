package com.example.mytasbeehapp.presentation.screens.setting

import androidx.lifecycle.viewModelScope
import com.example.mytasbeehapp.data.ThemeManager
import com.example.mytasbeehapp.data.repository.TasbeehRepository
import com.example.mytasbeehapp.domain.model.localEpochDay
import com.example.mytasbeehapp.presentation.base.BaseViewModel
import com.example.mytasbeehapp.presentation.screens.tasbeeh.CycleResetBus
import com.example.mytasbeehapp.presentation.screens.tasbeeh.DailyGoalBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val themeManager: ThemeManager,
    private val tasbeehRepository: TasbeehRepository
) : BaseViewModel<SettingState, SettingIntent, SettingEffect>(SettingState()) {

    init {
        loadCycleInfo()
        loadSettings()
        observeSettings()
    }

    private fun loadCycleInfo() {
        val cycleStartDay = tasbeehRepository.getCycleStartDay()
        val today = localEpochDay()
        val dayInCycle = ((today - cycleStartDay) % 7 + 7) % 7 + 1
        setState { copy(cycleDay = dayInCycle.toInt()) }
    }

    private fun loadSettings() {
        setState {
            copy(
                vibration = tasbeehRepository.getVibration(),
                clickSound = tasbeehRepository.getClickSound(),
                keepScreenOn = tasbeehRepository.getKeepScreenOn(),
                volumeButtons = tasbeehRepository.getVolumeButtons(),
                dailyGoal = tasbeehRepository.getDailyGoal(),
                morningEnabled = tasbeehRepository.getMorningEnabled(),
                morningTime = tasbeehRepository.getMorningTime(),
                eveningEnabled = tasbeehRepository.getEveningEnabled(),
                eveningTime = tasbeehRepository.getEveningTime()
            )
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            tasbeehRepository.vibrationFlow.collect { enabled ->
                setState { copy(vibration = enabled) }
            }
        }
        viewModelScope.launch {
            tasbeehRepository.clickSoundFlow.collect { enabled ->
                setState { copy(clickSound = enabled) }
            }
        }
        viewModelScope.launch {
            tasbeehRepository.keepScreenOnFlow.collect { enabled ->
                setState { copy(keepScreenOn = enabled) }
            }
        }
        viewModelScope.launch {
            tasbeehRepository.volumeButtonsFlow.collect { enabled ->
                setState { copy(volumeButtons = enabled) }
            }
        }
    }

    override fun handleIntent(intent: SettingIntent) {
        when (intent) {
            SettingIntent.ThemeClicked -> setState { copy(showThemeDialog = true) }
            is SettingIntent.ThemeSelected -> {
                viewModelScope.launch {
                    themeManager.setThemeMode(intent.theme)
                }
                setState { copy(theme = intent.theme, showThemeDialog = false) }
            }
            SettingIntent.DismissThemeDialog -> setState { copy(showThemeDialog = false) }

            is SettingIntent.VibrationChanged -> {
                tasbeehRepository.saveVibration(intent.enabled)
                setState { copy(vibration = intent.enabled) }
                if (intent.enabled) {
                    sendEffect(SettingEffect.PlayVibration)
                }
            }
            is SettingIntent.ClickSoundChanged -> {
                tasbeehRepository.saveClickSound(intent.enabled)
                setState { copy(clickSound = intent.enabled) }
                if (intent.enabled) {
                    sendEffect(SettingEffect.PlayClickSound)
                }
            }
            is SettingIntent.KeepScreenOnChanged -> {
                tasbeehRepository.saveKeepScreenOn(intent.enabled)
                setState { copy(keepScreenOn = intent.enabled) }
            }
            is SettingIntent.VolumeButtonsChanged -> {
                tasbeehRepository.saveVolumeButtons(intent.enabled)
                setState { copy(volumeButtons = intent.enabled) }
            }

            SettingIntent.DailyGoalIncrement -> {
                val newGoal = (currentState.dailyGoal + 10).coerceAtMost(100_000)
                tasbeehRepository.saveDailyGoal(newGoal)
                DailyGoalBus.update(newGoal)
                setState { copy(dailyGoal = newGoal) }
            }
            SettingIntent.DailyGoalDecrement -> {
                val newGoal = (currentState.dailyGoal - 10).coerceAtLeast(10)
                tasbeehRepository.saveDailyGoal(newGoal)
                DailyGoalBus.update(newGoal)
                setState { copy(dailyGoal = newGoal) }
            }

            is SettingIntent.ReminderToggled -> {
                if (intent.type == ReminderType.Morning) {
                    tasbeehRepository.saveMorningEnabled(intent.enabled)
                    setState { copy(morningEnabled = intent.enabled) }
                    sendEffect(SettingEffect.ScheduleReminder(ReminderType.Morning, intent.enabled, currentState.morningTime))
                } else {
                    tasbeehRepository.saveEveningEnabled(intent.enabled)
                    setState { copy(eveningEnabled = intent.enabled) }
                    sendEffect(SettingEffect.ScheduleReminder(ReminderType.Evening, intent.enabled, currentState.eveningTime))
                }
            }
            is SettingIntent.ReminderTimeClicked -> setState { copy(timePickerFor = intent.type) }
            is SettingIntent.ReminderTimeSelected -> {
                if (intent.type == ReminderType.Morning) {
                    tasbeehRepository.saveMorningTime(intent.time)
                    setState { copy(morningTime = intent.time, timePickerFor = null) }
                    if (currentState.morningEnabled) {
                        sendEffect(SettingEffect.ScheduleReminder(ReminderType.Morning, true, intent.time))
                    }
                } else {
                    tasbeehRepository.saveEveningTime(intent.time)
                    setState { copy(eveningTime = intent.time, timePickerFor = null) }
                    if (currentState.eveningEnabled) {
                        sendEffect(SettingEffect.ScheduleReminder(ReminderType.Evening, true, intent.time))
                    }
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

            SettingIntent.ResetCycleClicked -> setState { copy(showResetCycleDialog = true) }
            SettingIntent.DismissResetCycleDialog -> setState { copy(showResetCycleDialog = false) }
            SettingIntent.ResetCycleConfirmed -> {
                val today = localEpochDay()
                tasbeehRepository.resetCycle(today)
                loadCycleInfo()
                CycleResetBus.trigger()
                setState { copy(showResetCycleDialog = false) }
                sendEffect(SettingEffect.ShowMessage("7-day cycle has been reset"))
            }

            SettingIntent.ReplaySplashClicked -> sendEffect(SettingEffect.NavigateToSplash)
        }
    }
}
