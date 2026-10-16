package com.example.mytasbeehapp.presentation.screens.tasbeeh

import androidx.lifecycle.viewModelScope
import com.example.mytasbeehapp.data.repository.TasbeehRepository
import com.example.mytasbeehapp.data.storage.syncedTo
import com.example.mytasbeehapp.domain.model.DEFAULT_LIMIT
import com.example.mytasbeehapp.domain.model.Limit
import com.example.mytasbeehapp.domain.model.Preset
import com.example.mytasbeehapp.domain.model.localEpochDay
import com.example.mytasbeehapp.presentation.base.BaseViewModel
import com.example.mytasbeehapp.presentation.screens.azkar.Zikr
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasbeehViewModel @Inject constructor(
    private val repository: TasbeehRepository
) : BaseViewModel<TasbeehState, TasbeehIntent, TasbeehEffect>(repository.getTasbeehData()) {

    init {
        viewModelScope.launch {
            repository.vibrationFlow.collect { enabled ->
                if (currentState.hapticEnabled != enabled) {
                    setState { copy(hapticEnabled = enabled) }
                }
            }
        }
    }

    override fun handleIntent(intent: TasbeehIntent) {
        syncDay()
        when (intent) {
            TasbeehIntent.Tap -> tap()
            TasbeehIntent.Undo -> undo()
            TasbeehIntent.Reset -> reset()
            TasbeehIntent.ToggleHaptic -> toggleHaptic()
            TasbeehIntent.RefreshDay -> Unit
            TasbeehIntent.Reload -> syncDayWithNewCycleData()
            is TasbeehIntent.SelectLimit -> selectLimit(intent.limit)
            is TasbeehIntent.CustomLimitChanged -> changeCustomLimit(intent.value)
            is TasbeehIntent.SelectPreset -> selectPreset(intent.preset)
            is TasbeehIntent.SelectZikr -> selectZikr(intent.zikr)
            is TasbeehIntent.SetDailyGoal -> setDailyGoal(intent.goal)
        }
        repository.saveTasbeehData(currentState)
    }

    private fun toggleHaptic() {
        val newHaptic = !currentState.hapticEnabled
        repository.saveVibration(newHaptic)
        setState { copy(hapticEnabled = newHaptic) }
    }

    private fun syncDay() {
        val synced = currentState.syncedTo(localEpochDay())
        if (synced != currentState) setState { synced }
    }

    private fun syncDayWithNewCycleData() {
        val newData = repository.getTasbeehData()
        val synced = newData.syncedTo(localEpochDay())
        setState { synced }
    }

    private fun tap() {
        val s = currentState
        val limit = s.activeLimit
        val newCount = s.count + 1
        val stepDone = limit is Limit.Fixed && newCount >= limit.value
        val goalHit = s.dailyGoal > 0 && s.dailyCount + 1 == s.dailyGoal

        if (stepDone) {
            setState { completeStep(this).copy(dailyCount = dailyCount + 1) }
        } else {
            setState { copy(count = newCount, dailyCount = dailyCount + 1) }
        }

        if (repository.getClickSound()) {
            sendEffect(TasbeehEffect.PlayClick)
        }

        if (repository.getVibration()) {
            emitHaptic(if (stepDone || goalHit) TasbeehEffect.Completed else TasbeehEffect.Tick)
        }

        if (goalHit) sendEffect(TasbeehEffect.GoalReached)
    }

    private fun completeStep(s: TasbeehState): TasbeehState {
        val preset = s.preset
            ?: return s.copy(
                count = 0,
                completedRounds = s.completedRounds + 1,
                completions = s.completions + 1
            )

        val next = s.presetStep + 1
        return if (next < preset.steps.size) {
            s.copy(count = 0, presetStep = next, completions = s.completions + 1)
        } else {
            s.copy(
                count = 0,
                presetStep = 0,
                completedRounds = s.completedRounds + 1,
                completions = s.completions + 1
            )
        }
    }

    private fun undo() = setState {
        if (count == 0) this
        else copy(count = count - 1, dailyCount = (dailyCount - 1).coerceAtLeast(0))
    }

    private fun reset() = setState {
        copy(count = 0, presetStep = 0, completedRounds = 0)
    }

    private fun selectLimit(limit: Limit) = setState {
        copy(limit = limit, customInput = "", preset = null, presetStep = 0, count = 0)
    }

    private fun changeCustomLimit(value: String) {
        val digits = value.filter { it.isDigit() }.take(5)
        val number = digits.toIntOrNull()
        setState {
            when {
                number != null && number > 0 ->
                    copy(customInput = digits, limit = Limit.Fixed(number), preset = null, presetStep = 0, count = 0)
                digits.isEmpty() && isCustomSelected ->
                    copy(customInput = "", limit = Limit.Fixed(DEFAULT_LIMIT), count = 0)
                else -> copy(customInput = digits)
            }
        }
    }

    private fun selectPreset(preset: Preset) {
        if (currentState.preset == preset) return
        setState { copy(preset = preset, zikr = null, presetStep = 0, customInput = "", count = 0) }
    }

    private fun selectZikr(zikr: Zikr) {
        if (currentState.zikr == zikr && currentState.preset == null) return
        setState {
            copy(
                zikr = zikr,
                limit = if (zikr.count > 0) Limit.Fixed(zikr.count) else Limit.Unlimited,
                customInput = "",
                preset = null,
                presetStep = 0,
                count = 0,
                completedRounds = 0
            )
        }
    }

    private fun setDailyGoal(goal: Int) {
        if (currentState.dailyGoal == goal) return
        setState { copy(dailyGoal = goal) }
    }

    private fun emitHaptic(effect: TasbeehEffect) {
        if (currentState.hapticEnabled) sendEffect(effect)
    }
}
