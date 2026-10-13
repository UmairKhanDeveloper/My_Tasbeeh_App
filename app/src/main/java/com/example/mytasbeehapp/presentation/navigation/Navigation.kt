package com.example.mytasbeehapp.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mytasbeehapp.R
import com.example.mytasbeehapp.presentation.screens.azkar.Azkar
import com.example.mytasbeehapp.presentation.screens.component.AppColors
import com.example.mytasbeehapp.presentation.screens.setting.Setting
import com.example.mytasbeehapp.presentation.screens.splash.Splash
import com.example.mytasbeehapp.presentation.screens.stats.Stats
import com.example.mytasbeehapp.presentation.screens.tasbeeh.Tasbeeh

@Composable
fun Navigation(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute != null &&
            currentRoute != screens.Splash.route &&
            currentRoute in bottomBarRoutes

    Scaffold(
        containerColor = AppColors.Background,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn() + slideInVertically { it },
                exit = ExitTransition.None
            ) {
                AppBottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = screens.Splash.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(screens.Splash.route) {
                Splash(navController)
            }
            composable(screens.Tasbeeh.route) {
                Tasbeeh(navController)
            }
            composable(screens.Azkar.route) {
                Azkar(navController)
            }
            composable(screens.Stats.route) {
                Stats(navController)
            }
            composable(screens.Setting.route) {
                Setting(navController)
            }
        }
    }
}

sealed class screens(
    val route: String,
    val title: String,
    val icon: Int
) {
    object Splash : screens("Splash", "Splash", R.drawable.tasbeeh)
    object Tasbeeh : screens("Tasbeeh", "Tasbeeh", R.drawable.tasbeeh)
    object Azkar : screens("Azkar", "Azkar", R.drawable.azkaar)
    object Stats : screens("Stats", "Stats", R.drawable.stats)
    object Setting : screens("Setting", "Setting", R.drawable.settings)
}