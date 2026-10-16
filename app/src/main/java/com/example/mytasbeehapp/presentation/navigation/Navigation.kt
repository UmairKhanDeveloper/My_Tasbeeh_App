package com.example.mytasbeehapp.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mytasbeehapp.R
import com.example.mytasbeehapp.presentation.screens.PrivacyPolicy.PrivacyPolicyScreen
import com.example.mytasbeehapp.presentation.screens.TermsOfUse.TermsOfUseScreen
import com.example.mytasbeehapp.presentation.screens.azkar.Azkar
import com.example.mytasbeehapp.presentation.screens.component.AppColors
import com.example.mytasbeehapp.presentation.screens.onboarding.Onboarding
import com.example.mytasbeehapp.presentation.screens.onboarding.OnboardingPrefs
import com.example.mytasbeehapp.presentation.screens.setting.Setting
import com.example.mytasbeehapp.presentation.screens.splash.Splash
import com.example.mytasbeehapp.presentation.screens.stats.Stats
import com.example.mytasbeehapp.presentation.screens.tasbeeh.Tasbeeh

@Composable
fun Navigation(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = screens.Splash.route,
        modifier = Modifier.fillMaxSize(),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(screens.Splash.route) {
            Splash(navController)
        }

        composable(screens.Onboarding.route) {
            Onboarding(
                onFinished = {
                    val goal = OnboardingPrefs.chosenGoal(context)
                    // goal?.let { DailyGoalBus.<aap-ka-function>(it) }

                    navController.navigate(screens.Tasbeeh.route) {
                        popUpTo(screens.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(screens.Tasbeeh.route) {
            HomeScaffold(navController) { Tasbeeh(navController) }
        }
        composable(screens.Azkar.route) {
            HomeScaffold(navController) { Azkar(navController) }
        }
        composable(screens.Stats.route) {
            HomeScaffold(navController) { Stats(navController) }
        }
        composable(screens.Setting.route) {
            HomeScaffold(navController) { Setting(navController) }
        }

        composable(screens.PrivacyPolicy.route) {
            PrivacyPolicyScreen(navController)
        }
        composable(screens.TermsOfUse.route) {
            TermsOfUseScreen(navController)
        }
    }
}

@Composable
private fun HomeScaffold(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = AppColors.background(),
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = { AppBottomBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

sealed class screens(
    val route: String,
    val title: String,
    val icon: Int
) {
    object Splash : screens("Splash", "Splash", R.drawable.tasbeeh)
    object Onboarding : screens("Onboarding", "Onboarding", R.drawable.tasbeeh)
    object Tasbeeh : screens("Tasbeeh", "Tasbeeh", R.drawable.tasbeeh)
    object Azkar : screens("Azkar", "Azkar", R.drawable.azkaar)
    object Stats : screens("Stats", "Stats", R.drawable.stats)
    object Setting : screens("Setting", "Setting", R.drawable.settings)
    object PrivacyPolicy : screens("PrivacyPolicy", "Privacy Policy", R.drawable.privacy_policy)
    object TermsOfUse : screens("TermsOfUse", "Terms of Use", R.drawable.terms_of_use)
}