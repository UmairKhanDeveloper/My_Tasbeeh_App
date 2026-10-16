package com.example.mytasbeehapp.presentation.screens.onboarding

sealed interface OnboardingIntent {
    data class PageChanged(val page: Int) : OnboardingIntent
    data class GoalSelected(val goal: Int) : OnboardingIntent
    data class PermissionResult(val granted: Boolean) : OnboardingIntent
    object MainAction : OnboardingIntent
    object Back : OnboardingIntent
    object Skip : OnboardingIntent
    object MaybeLater : OnboardingIntent
}