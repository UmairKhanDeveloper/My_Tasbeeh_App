package com.example.mytasbeehapp.presentation.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.mytasbeehapp.R
import com.example.mytasbeehapp.presentation.navigation.screens
import com.example.mytasbeehapp.presentation.theme.ManropeFamily

private val GreenCenter = Color(0xFF16856A)
private val GreenMid = Color(0xFF0B6B4D)
private val GreenEdge = Color(0xFF065440)
private val Gold = Color(0xFFE2B94F)
private val TextSecondary = Color(0xFFD7EBE3)
private val TextMuted = Color(0xFFA9CBBD)

@Composable
fun Splash(
    navController: NavController,
    viewModel: SplashViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onIntent(SplashIntent.StartSplash)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SplashEffect.NavigateToHome -> navController.navigate(screens.Tasbeeh.route) {
                    popUpTo(screens.Splash.route) { inclusive = true }
                }
            }
        }
    }

    ProvideTextStyle(TextStyle(fontFamily = ManropeFamily)) {
        SplashContent(state = state)
    }
}

@Composable
private fun SplashContent(state: SplashState) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.tasbeehsplashlottie)
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to GreenCenter,
                        0.55f to GreenMid,
                        1.0f to GreenEdge
                    )
                )
            )
    ) {
        val compact = maxWidth < 360.dp
        val shortSide = minOf(maxWidth, maxHeight)
        val logoSize = (shortSide * 0.55f).coerceIn(160.dp, 260.dp)
        val titleSize = if (compact) 26.sp else 30.sp
        val progressWidth = (maxWidth * 0.3f).coerceIn(100.dp, 160.dp)

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { state.progress },
                modifier = Modifier.size(logoSize)
            )

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Noor Tasbeeh",
                color = Color.White,
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Count your dhikr, every day",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { state.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .width(progressWidth)
                    .height(2.dp)
                    .clip(RoundedCornerShape(50)),
                color = Gold,
                trackColor = Gold.copy(alpha = 0.25f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = state.versionName,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}