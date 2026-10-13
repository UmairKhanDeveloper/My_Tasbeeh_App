package com.example.mytasbeehapp.presentation.screens.azkar

sealed interface AzkarEffect {
    data class NavigateToTasbeeh(val zikr: Zikr) : AzkarEffect
    data class ZikrAdded(val index: Int) : AzkarEffect
}