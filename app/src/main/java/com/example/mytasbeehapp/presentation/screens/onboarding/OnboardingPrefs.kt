package com.example.mytasbeehapp.presentation.screens.onboarding

import android.content.Context

/** Remembers that the onboarding was shown, plus what the user chose on it. */
object OnboardingPrefs {
    private const val FILE = "onboarding_prefs"
    private const val KEY_DONE = "done"
    private const val KEY_GOAL = "chosen_goal"
    private const val KEY_REMINDERS = "reminders_allowed"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isDone(context: Context): Boolean = prefs(context).getBoolean(KEY_DONE, false)

    fun complete(context: Context, goal: Int?, remindersOn: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_DONE, true)
            .putInt(KEY_GOAL, goal ?: -1)
            .putBoolean(KEY_REMINDERS, remindersOn)
            .apply()
    }

    /** Daily goal picked on the last onboarding page (null if the user skipped). */
    fun chosenGoal(context: Context): Int? =
        prefs(context).getInt(KEY_GOAL, -1).takeIf { it > 0 }

    fun remindersAllowed(context: Context): Boolean = prefs(context).getBoolean(KEY_REMINDERS, false)
}
