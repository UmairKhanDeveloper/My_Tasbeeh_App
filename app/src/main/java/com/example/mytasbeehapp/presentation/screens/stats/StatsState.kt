package com.example.mytasbeehapp.presentation.screens.stats

import com.example.mytasbeehapp.presentation.screens.tasbeeh.DEFAULT_DAILY_GOAL

/** Ek din ka chart/week data — asli [com.example.mytasbeehapp.presentation.screens.tasbeeh.DayRecord] se banta hai. */
data class DayStat(
    val epochDay: Long,
    val label: String,
    val count: Int
)

data class StatsState(
    /** Pichle 7 din, purane se naye (aaj) tak order mein. */
    val week: List<DayStat> = emptyList(),
    val monthCount: Int = 0,
    val dailyGoal: Int = DEFAULT_DAILY_GOAL,
    /** Lagatar kitne din se goal pura ho raha hai (poori history se, sirf is week tak mehdood nahi). */
    val dayStreak: Int = 0
) {
    val todayCount: Int
        get() = week.lastOrNull()?.count ?: 0

    val maxCount: Int
        get() = (week.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)

    /** Ab tak koi activity record nahi hui — empty-state copy dikhane ke liye. */
    val hasActivity: Boolean
        get() = week.any { it.count > 0 } || monthCount > 0

    fun isGoalReached(day: DayStat): Boolean = day.count >= dailyGoal
}