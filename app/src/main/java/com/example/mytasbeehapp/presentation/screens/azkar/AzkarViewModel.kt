package com.example.mytasbeehapp.presentation.screens.azkar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mytasbeehapp.presentation.base.BaseViewModel
import com.example.mytasbeehapp.presentation.screens.tasbeeh.DayRecord
import com.example.mytasbeehapp.presentation.screens.tasbeeh.TasbeehStorage
import com.example.mytasbeehapp.presentation.screens.tasbeeh.localEpochDay

class AzkarViewModel(
    private val storage: TasbeehStorage,
    private val today: () -> Long = ::localEpochDay
) : BaseViewModel<AzkarState, AzkarIntent, AzkarEffect>(
    AzkarState(streakDays = storage.loadHistory().currentStreak(today()))
) {

    override fun handleIntent(intent: AzkarIntent) {
        when (intent) {
            is AzkarIntent.ZikrClicked -> {
                ZikrSelectionBus.select(intent.zikr)
                sendEffect(AzkarEffect.NavigateToTasbeeh(intent.zikr))
            }

            is AzkarIntent.NameChanged -> setState { copy(nameInput = intent.value) }
            is AzkarIntent.CountChanged -> onCountChanged(intent.value)
            is AzkarIntent.TextChanged -> setState { copy(textInput = intent.value) }
            AzkarIntent.AddZikr -> addZikr()
            AzkarIntent.RefreshStreak -> refreshStreak()
        }
    }

    private fun refreshStreak() {
        val streak = storage.loadHistory().currentStreak(today())
        if (streak != currentState.streakDays) setState { copy(streakDays = streak) }
    }

    private fun onCountChanged(value: String) {
        if (value.length <= 5 && value.all { it.isDigit() }) {
            setState { copy(countInput = value) }
        }
    }

    private fun addZikr() {
        val current = currentState
        if (!current.canAdd) return

        val newZikr = Zikr(
            id = (current.azkar.maxOfOrNull { it.id } ?: 0L) + 1,
            title = current.nameInput.trim(),
            arabic = current.textInput.trim(),
            count = current.countInput.toInt()
        )
        val updated = current.azkar + newZikr

        setState {
            copy(
                azkar = updated,
                nameInput = "",
                countInput = "33",
                textInput = ""
            )
        }
        sendEffect(AzkarEffect.ZikrAdded(index = updated.lastIndex))
    }
}

class AzkarViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AzkarViewModel(TasbeehStorage(context.applicationContext)) as T
}

fun List<DayRecord>.currentStreak(today: Long): Int {
    val activeDays = filter { it.count > 0 }.map { it.epochDay }.toSet()
    var day = if (today in activeDays) today else today - 1
    var streak = 0
    while (day in activeDays) {
        streak++
        day--
    }
    return streak
}