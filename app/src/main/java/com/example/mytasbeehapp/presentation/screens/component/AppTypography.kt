package com.example.mytasbeehapp.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.mytasbeehapp.R

val ManropeFamily = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
)

private val Default = Typography()

val AppTypography = Typography(
    displayLarge = Default.displayLarge.copy(fontFamily = ManropeFamily),
    displayMedium = Default.displayMedium.copy(fontFamily = ManropeFamily),
    displaySmall = Default.displaySmall.copy(fontFamily = ManropeFamily),
    headlineLarge = Default.headlineLarge.copy(fontFamily = ManropeFamily),
    headlineMedium = Default.headlineMedium.copy(fontFamily = ManropeFamily),
    headlineSmall = Default.headlineSmall.copy(fontFamily = ManropeFamily),
    titleLarge = Default.titleLarge.copy(fontFamily = ManropeFamily),
    titleMedium = Default.titleMedium.copy(fontFamily = ManropeFamily),
    titleSmall = Default.titleSmall.copy(fontFamily = ManropeFamily),
    bodyLarge = Default.bodyLarge.copy(fontFamily = ManropeFamily),
    bodyMedium = Default.bodyMedium.copy(fontFamily = ManropeFamily),
    bodySmall = Default.bodySmall.copy(fontFamily = ManropeFamily),
    labelLarge = Default.labelLarge.copy(fontFamily = ManropeFamily),
    labelMedium = Default.labelMedium.copy(fontFamily = ManropeFamily),
    labelSmall = Default.labelSmall.copy(fontFamily = ManropeFamily)
)