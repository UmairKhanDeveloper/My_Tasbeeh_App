package com.example.mytasbeehapp.presentation.screens.splash

sealed interface SplashEffect {
    data object NavigateToHome : SplashEffect
}