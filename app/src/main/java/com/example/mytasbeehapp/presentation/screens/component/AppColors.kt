package com.example.mytasbeehapp.presentation.screens.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    @Composable
    fun background() = MaterialTheme.colorScheme.background

    @Composable
    fun surface() = MaterialTheme.colorScheme.surface

    @Composable
    fun primary() = MaterialTheme.colorScheme.primary

    @Composable
    fun onPrimary() = MaterialTheme.colorScheme.onPrimary

    @Composable
    fun textPrimary() = MaterialTheme.colorScheme.onBackground

    @Composable
    fun textMuted() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    @Composable
    fun border() = MaterialTheme.colorScheme.outlineVariant

    @Composable
    fun gold() = Color(0xFFFFB300)

    @Composable
    fun track() = MaterialTheme.colorScheme.surfaceVariant

    @Composable
    fun fieldBackground() = MaterialTheme.colorScheme.surfaceVariant
}