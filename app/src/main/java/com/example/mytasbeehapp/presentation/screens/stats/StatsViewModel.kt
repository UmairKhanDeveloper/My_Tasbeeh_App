package com.example.mytasbeehapp.presentation.screens.stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mytasbeehapp.presentation.base.BaseViewModel
import com.example.mytasbeehapp.presentation.screens.tasbeeh.DailyGoalBus
import com.example.mytasbeehapp.presentation.screens.tasbeeh.DayRecord
import com.example.mytasbeehapp.presentation.screens.tasbeeh.TasbeehStorage
import com.example.mytasbeehapp.presentation.screens.tasbeeh.epochDayToMillis
import com.example.mytasbeehapp.presentation.screens.tasbeeh.localEpochDay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatsViewModel(
    private val storage: TasbeehStorage,
    private val today: () -> Long = ::localEpochDay
) : BaseViewModel<StatsState, StatsIntent, StatsEffect>(buildStatsState(storage, today())) {

    override fun handleIntent(intent: StatsIntent) {
        when (intent) {
            StatsIntent.Refresh -> setState { buildStatsState(storage, today()) }
            StatsIntent.IncreaseGoal -> changeGoal(GOAL_STEP)
            StatsIntent.DecreaseGoal -> changeGoal(-GOAL_STEP)
        }
    }

    private fun changeGoal(delta: Int) {
        val newGoal = (currentState.dailyGoal + delta).coerceIn(MIN_GOAL, MAX_GOAL)
        if (newGoal == currentState.dailyGoal) return

        // Ek hi storage Home screen bhi use karta hai, isliye goal dono jagah sync rehta hai.
        storage.saveDailyGoal(newGoal)
        DailyGoalBus.update(newGoal)

        setState { buildStatsState(storage, today()) }
        sendEffect(StatsEffect.GoalChanged)
    }

    private companion object {
        const val GOAL_STEP = 50
        const val MIN_GOAL = 50
        const val MAX_GOAL = 5000
    }
}

class StatsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        StatsViewModel(TasbeehStorage(context)) as T
}

/** Home screen ki asli persisted history se Stats screen ki poori state banata hai. */
private fun buildStatsState(storage: TasbeehStorage, todayDay: Long): StatsState {
    val goal = storage.loadDailyGoal()
    val historyByDay = storage.loadHistory().associateBy { it.epochDay }

    val week = (6 downTo 0).map { offset ->
        val day = todayDay - offset
        DayStat(
            epochDay = day,
            label = dayLabel(day),
            count = historyByDay[day]?.count ?: 0
        )
    }

    val monthCount = historyByDay.values
        .filter { isSameMonth(it.epochDay, todayDay) }
        .sumOf { it.count }

    val streak = currentStreak(historyByDay, todayDay, goal)

    return StatsState(
        week = week,
        monthCount = monthCount,
        dailyGoal = goal,
        dayStreak = streak
    )
}

/** Aaj se peeche, lagatar jitne din goal pura hua — poori saved history mein se (sirf is week tak mehdood nahi). */
private fun currentStreak(
    historyByDay: Map<Long, DayRecord>,
    todayDay: Long,
    goal: Int
): Int {
    if (goal <= 0) return 0
    var streak = 0
    var day = todayDay
    while ((historyByDay[day]?.count ?: 0) >= goal) {
        streak++
        day--
    }
    return streak
}

private val dayLabelFormat = SimpleDateFormat("EEE", Locale.getDefault())

private fun dayLabel(epochDay: Long): String =
    dayLabelFormat.format(Date(epochDayToMillis(epochDay)))

private fun isSameMonth(epochDay: Long, referenceDay: Long): Boolean {
    val a = Calendar.getInstance().apply { timeInMillis = epochDayToMillis(epochDay) }
    val b = Calendar.getInstance().apply { timeInMillis = epochDayToMillis(referenceDay) }
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
}