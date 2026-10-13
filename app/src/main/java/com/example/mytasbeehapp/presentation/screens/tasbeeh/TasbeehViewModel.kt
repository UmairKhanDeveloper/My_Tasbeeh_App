package com.example.mytasbeehapp.presentation.screens.tasbeeh

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mytasbeehapp.presentation.base.BaseViewModel
import com.example.mytasbeehapp.presentation.screens.azkar.Zikr

class TasbeehViewModel(
    private val storage: TasbeehStorage,
    private val today: () -> Long = ::localEpochDay
) : BaseViewModel<TasbeehState, TasbeehIntent, TasbeehEffect>(storage.load(today())) {

    override fun handleIntent(intent: TasbeehIntent) {
        syncDay()
        when (intent) {
            TasbeehIntent.Tap -> tap()
            TasbeehIntent.Undo -> undo()
            TasbeehIntent.Reset -> reset()
            TasbeehIntent.ToggleHaptic -> setState { copy(hapticEnabled = !hapticEnabled) }
            TasbeehIntent.RefreshDay -> Unit
            is TasbeehIntent.SelectLimit -> selectLimit(intent.limit)
            is TasbeehIntent.CustomLimitChanged -> changeCustomLimit(intent.value)
            is TasbeehIntent.SelectPreset -> selectPreset(intent.preset)
            is TasbeehIntent.SelectZikr -> selectZikr(intent.zikr)
            is TasbeehIntent.SetDailyGoal -> setDailyGoal(intent.goal)
        }
        storage.save(currentState)
    }

    private fun syncDay() {
        val synced = currentState.syncedTo(today())
        if (synced != currentState) setState { synced }
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
        emitHaptic(if (stepDone || goalHit) TasbeehEffect.Completed else TasbeehEffect.Tick)

        // Snackbar vibration setting par depend nahi karta
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

    /** Current session restart hota hai; aaj ka daily progress barqarar rehta hai. */
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
        // Wahi zikr dobara tap kiya to progress na khoyein
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

    /** Stats screen se goal badla ho to Home screen ka state bhi usi value par le aata hai. */
    private fun setDailyGoal(goal: Int) {
        if (currentState.dailyGoal == goal) return
        setState { copy(dailyGoal = goal) }
    }

    private fun emitHaptic(effect: TasbeehEffect) {
        if (currentState.hapticEnabled) sendEffect(effect)
    }
}

class TasbeehViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TasbeehViewModel(TasbeehStorage(context)) as T
}