package com.example.mytasbeehapp.presentation.screens.onboarding

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.mytasbeehapp.presentation.base.BaseViewModel

class OnboardingViewModel(
    private val appContext: Context
) : BaseViewModel<OnboardingState, OnboardingIntent, OnboardingEffect>(OnboardingState()) {

    override fun handleIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.PageChanged -> setState { copy(page = intent.page) }

            is OnboardingIntent.GoalSelected -> setState { copy(goal = intent.goal) }

            is OnboardingIntent.PermissionResult -> {
                setState { copy(remindersOn = intent.granted) }
                sendEffect(OnboardingEffect.ScrollToPage(LAST_PAGE))
            }

            OnboardingIntent.MainAction -> onMainAction()

            OnboardingIntent.Back -> {
                val page = currentState.page
                if (page > 0) sendEffect(OnboardingEffect.ScrollToPage(page - 1))
            }

            OnboardingIntent.Skip -> finish(goal = null, remindersOn = false)

            OnboardingIntent.MaybeLater -> {
                setState { copy(remindersOn = false) }
                sendEffect(OnboardingEffect.ScrollToPage(LAST_PAGE))
            }
        }
    }

    private fun onMainAction() {
        val current = currentState
        when (current.page) {
            0, 1 -> sendEffect(OnboardingEffect.ScrollToPage(current.page + 1))
            2 -> if (Build.VERSION.SDK_INT >= 33) {
                sendEffect(OnboardingEffect.RequestNotificationPermission)
            } else {
                setState { copy(remindersOn = true) }
                sendEffect(OnboardingEffect.ScrollToPage(LAST_PAGE))
            }
            else -> finish(current.goal, current.remindersOn)
        }
    }

    private fun finish(goal: Int?, remindersOn: Boolean) {
        OnboardingPrefs.complete(appContext, goal, remindersOn)
        sendEffect(OnboardingEffect.NavigateToHome)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return OnboardingViewModel(context.applicationContext) as T
        }
    }

    private companion object {
        const val LAST_PAGE = 3
    }
}