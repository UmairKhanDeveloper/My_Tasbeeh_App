package com.example.mytasbeehapp.presentation.screens.tasbeeh

sealed interface TasbeehEffect {
    data object Tick : TasbeehEffect
    data object Completed : TasbeehEffect
    data object GoalReached : TasbeehEffect
    data object PlayClick : TasbeehEffect
}
