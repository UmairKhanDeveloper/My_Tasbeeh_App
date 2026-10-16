package com.example.mytasbeehapp.presentation.screens.tasbeeh

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Settings screen par 7-day cycle reset hota hai to yahan se Home (Tasbeeh) screen ko turant
 * bataya jata hai, taake cycle day sahi update ho.
 */
object CycleResetBus {
    private val _reset = MutableStateFlow<Boolean>(false)
    val reset: StateFlow<Boolean> = _reset.asStateFlow()

    fun trigger() {
        _reset.value = true
    }

    /** Home screen ne apply kar liya, ab dobara apply na ho. */
    fun consume() {
        _reset.value = false
    }
}
