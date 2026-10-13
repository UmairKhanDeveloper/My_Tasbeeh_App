package com.example.mytasbeehapp.presentation.screens.stats

sealed interface StatsIntent {
    data object Refresh : StatsIntent
    data object IncreaseGoal : StatsIntent
    data object DecreaseGoal : StatsIntent
}