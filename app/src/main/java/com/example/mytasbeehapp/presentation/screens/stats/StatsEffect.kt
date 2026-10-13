package com.example.mytasbeehapp.presentation.screens.stats

sealed interface StatsEffect {
    data object GoalChanged : StatsEffect
}