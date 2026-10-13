package com.example.mytasbeehapp.presentation.screens.splash

sealed interface SplashIntent {
    data object StartSplash : SplashIntent
}
