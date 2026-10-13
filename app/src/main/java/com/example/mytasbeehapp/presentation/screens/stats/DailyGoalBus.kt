package com.example.mytasbeehapp.presentation.screens.tasbeeh

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stats screen par daily goal change hota hai to yahan se Home (Tasbeeh) screen ko turant
 * bataya jata hai, taake dono screens hamesha ek hi goal dikhayein. Same pattern jo
 * [com.example.mytasbeehapp.presentation.screens.azkar.ZikrSelectionBus] use karta hai.
 */
object DailyGoalBus {
    private val _goal = MutableStateFlow<Int?>(null)
    val goal: StateFlow<Int?> = _goal.asStateFlow()

    fun update(value: Int) {
        _goal.value = value
    }

    /** Home screen ne apply kar liya, ab dobara apply na ho. */
    fun consume(value: Int) {
        _goal.compareAndSet(value, null)
    }
}