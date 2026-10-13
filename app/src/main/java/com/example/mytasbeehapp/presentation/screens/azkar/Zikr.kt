package com.example.mytasbeehapp.presentation.screens.azkar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Zikr(
    val id: Long,
    val title: String,
    val arabic: String = "",
    val meaning: String = "",
    val count: Int
)

object ZikrSelectionBus {
    private val _pending = MutableStateFlow<Zikr?>(null)
    val pending: StateFlow<Zikr?> = _pending.asStateFlow()

    fun select(zikr: Zikr) {
        _pending.value = zikr
    }

    fun consume(zikr: Zikr) {
        _pending.compareAndSet(zikr, null)
    }
}