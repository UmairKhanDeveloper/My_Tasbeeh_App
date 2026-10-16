package com.example.mytasbeehapp.presentation.screens.onboarding

sealed interface OnboardingEffect {
    data class ScrollToPage(val page: Int) : OnboardingEffect
    object RequestNotificationPermission : OnboardingEffect
    object NavigateToHome : OnboardingEffect
}