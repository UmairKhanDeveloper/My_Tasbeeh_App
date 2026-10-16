package com.example.mytasbeehapp.presentation

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.mytasbeehapp.data.ThemeManager
import com.example.mytasbeehapp.data.repository.TasbeehRepository
import com.example.mytasbeehapp.presentation.navigation.Navigation
import com.example.mytasbeehapp.presentation.screens.setting.AppTheme
import com.example.mytasbeehapp.presentation.screens.tasbeeh.VolumeKeyBus
import com.example.mytasbeehapp.presentation.ui.theme.MyTasbeehAppTheme
import com.example.mytasbeehapp.util.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var repository: TasbeehRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReminderScheduler.rescheduleAll(this, repository)
        enableEdgeToEdge()
        setContent {
            val themeMode by themeManager.themeMode.collectAsState(
                initial = AppTheme.System
            )
            val keepScreenOn by repository.keepScreenOnFlow.collectAsState()

            val systemDarkMode = isSystemInDarkTheme()

            var darkTheme by remember {
                mutableStateOf(
                    when (themeMode) {
                        AppTheme.Light -> false
                        AppTheme.Dark -> true
                        AppTheme.System -> systemDarkMode
                    }
                )
            }

            LaunchedEffect(themeMode, systemDarkMode) {
                darkTheme = when (themeMode) {
                    AppTheme.Light -> false
                    AppTheme.Dark -> true
                    AppTheme.System -> systemDarkMode
                }
            }

            LaunchedEffect(keepScreenOn) {
                if (keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            MyTasbeehAppTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                Navigation(navController)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val isVolumeKey = keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        if (isVolumeKey && repository.getVolumeButtons()) {
            VolumeKeyBus.emitTap()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val isVolumeKey = keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        if (isVolumeKey && repository.getVolumeButtons()) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}
