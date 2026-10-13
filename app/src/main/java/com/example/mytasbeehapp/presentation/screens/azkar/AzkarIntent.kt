package com.example.mytasbeehapp.presentation.screens.azkar

sealed interface AzkarIntent {
    data class ZikrClicked(val zikr: Zikr) : AzkarIntent
    data class NameChanged(val value: String) : AzkarIntent
    data class CountChanged(val value: String) : AzkarIntent
    data class TextChanged(val value: String) : AzkarIntent
    data object AddZikr : AzkarIntent
    data object RefreshStreak : AzkarIntent
}