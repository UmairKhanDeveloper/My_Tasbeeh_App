package com.example.mytasbeehapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mytasbeehapp.presentation.screens.setting.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class ThemeManager(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val themeMode: Flow<AppTheme> = context.themeDataStore.data.map { preferences ->
        val themeString = preferences[PreferencesKeys.THEME_MODE] ?: AppTheme.System.name
        try {
            AppTheme.valueOf(themeString)
        } catch (e: IllegalArgumentException) {
            AppTheme.System
        }
    }

    suspend fun setThemeMode(theme: AppTheme) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = theme.name
        }
    }

    suspend fun clearTheme() {
        context.themeDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.THEME_MODE)
        }
    }
}
