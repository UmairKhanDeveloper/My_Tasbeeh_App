package com.example.mytasbeehapp.presentation.screens.setting


sealed interface SettingEffect {
    data class ShowMessage(val message: String) : SettingEffect
    data class OpenUrl(val url: String) : SettingEffect
    data object RateApp : SettingEffect
    data object ShareApp : SettingEffect
    data object SendFeedback : SettingEffect
    data object NavigateToLicenses : SettingEffect
    data object NavigateToSplash : SettingEffect
}