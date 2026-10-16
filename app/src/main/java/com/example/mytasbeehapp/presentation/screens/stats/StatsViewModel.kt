package com.example.mytasbeehapp.presentation.screens.stats

import androidx.lifecycle.viewModelScope
import com.example.mytasbeehapp.data.repository.TasbeehRepository
import com.example.mytasbeehapp.domain.model.DayRecord
import com.example.mytasbeehapp.domain.model.localEpochDay
import com.example.mytasbeehapp.domain.model.epochDayToMillis
import com.example.mytasbeehapp.presentation.base.BaseViewModel
import com.example.mytasbeehapp.presentation.screens.tasbeeh.DailyGoalBus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: TasbeehRepository
) : BaseViewModel<StatsState, StatsIntent, StatsEffect>(buildStatsState(repository, localEpochDay())) {

    override fun handleIntent(intent: StatsIntent) {
        when (intent) {
            StatsIntent.Refresh -> setState { buildStatsState(repository, localEpochDay()) }
            StatsIntent.IncreaseGoal -> changeGoal(GOAL_STEP)
            StatsIntent.DecreaseGoal -> changeGoal(-GOAL_STEP)
        }
    }

    private fun changeGoal(delta: Int) {
        val newGoal = (currentState.dailyGoal + delta).coerceIn(MIN_GOAL, MAX_GOAL)
        if (newGoal == currentState.dailyGoal) return

        // Ek hi storage Home screen bhi use karta hai, isliye goal dono jagah sync rehta hai.
        repository.saveDailyGoal(newGoal)
        DailyGoalBus.update(newGoal)

        setState { buildStatsState(repository, localEpochDay()) }
        sendEffect(StatsEffect.GoalChanged)
    }

    private companion object {
        const val GOAL_STEP = 50
        const val MIN_GOAL = 50
        const val MAX_GOAL = 5000
    }
}

/** Home screen ki asli persisted history se Stats screen ki poori state banata hai. */
private fun buildStatsState(repository: TasbeehRepository, todayDay: Long): StatsState {
    val goal = repository.getDailyGoal()
    val tasbeehData = repository.getTasbeehData()
    val historyByDay = repository.getHistory().associateBy { it.epochDay }.toMutableMap()

    // Ensure today's count reflects the latest live dailyCount from repository/storage
    val todayRecord = historyByDay[todayDay]
    if (todayRecord == null || tasbeehData.dailyCount > todayRecord.count) {
        historyByDay[todayDay] = DayRecord(todayDay, tasbeehData.dailyCount)
    }

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
    while (true) {
        val count = historyByDay[day]?.count ?: 0
        if (count < goal) break
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