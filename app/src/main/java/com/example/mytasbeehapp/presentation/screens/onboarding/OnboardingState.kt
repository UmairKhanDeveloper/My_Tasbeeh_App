package com.example.mytasbeehapp.presentation.screens.onboarding

data class OnboardingState(
    val page: Int = 0,
    val goal: Int = 300,
    val remindersOn: Boolean = false
)