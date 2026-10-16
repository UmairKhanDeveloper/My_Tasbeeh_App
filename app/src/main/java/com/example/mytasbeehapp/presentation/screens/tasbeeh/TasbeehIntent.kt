package com.example.mytasbeehapp.presentation.screens.tasbeeh

import com.example.mytasbeehapp.domain.model.Limit
import com.example.mytasbeehapp.domain.model.Preset
import com.example.mytasbeehapp.presentation.screens.azkar.Zikr

sealed interface TasbeehIntent {
    data object Tap : TasbeehIntent
    data object Undo : TasbeehIntent
    data object Reset : TasbeehIntent
    data object ToggleHaptic : TasbeehIntent
    data object RefreshDay : TasbeehIntent
    data object Reload : TasbeehIntent

    data class SelectLimit(val limit: Limit) : TasbeehIntent
    data class CustomLimitChanged(val value: String) : TasbeehIntent
    data class SelectPreset(val preset: Preset) : TasbeehIntent

    // Zikr select karne ke liye
    data class SelectZikr(val zikr: Zikr) : TasbeehIntent

    // Stats screen se daily goal change hone par Home screen ko sync karne ke liye
    data class SetDailyGoal(val goal: Int) : TasbeehIntent
}