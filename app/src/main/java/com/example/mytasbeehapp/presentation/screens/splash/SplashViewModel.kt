package com.example.mytasbeehapp.presentation.screens.splash

import androidx.lifecycle.viewModelScope
import com.example.mytasbeehapp.presentation.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : BaseViewModel<SplashState, SplashIntent, SplashEffect>(SplashState()) {

    private var started = false

    override fun handleIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.StartSplash -> startSplash()
        }
    }

    private fun startSplash() {
        if (started) return
        started = true
        viewModelScope.launch {
            val steps = TOTAL_DURATION_MS / STEP_MS
            for (i in 1..steps) {
                delay(STEP_MS)
                setState { copy(progress = i.toFloat() / steps) }
            }
            sendEffect(SplashEffect.NavigateToHome)
        }
    }

    private companion object {
        const val TOTAL_DURATION_MS = 3000L
        const val STEP_MS = 30L
    }
}