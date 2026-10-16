package com.example.mytasbeehapp.presentation.screens.setting

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VolumeDown
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mytasbeehapp.R
import com.example.mytasbeehapp.util.ReminderScheduler
import com.example.mytasbeehapp.util.SoundManager
import com.example.mytasbeehapp.util.VibrationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val Green = Color(0xFF0B6B4B)
private val GreenDark = Color(0xFF0A5A40)
private val Gold = Color(0xFFE5B84B)
private val DangerRed = Color(0xFFD64545)

@Composable
fun Setting(
    navController: NavController,
    viewModel: SettingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingEffect.ShowMessage ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()

                is SettingEffect.OpenUrl ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)))

                SettingEffect.RateApp -> context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${context.packageName}")
                    )
                )

                SettingEffect.ShareApp -> context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Try Noor Tasbeeh: https://play.google.com/store/apps/details?id=${context.packageName}"
                            )
                        }, "Share app"
                    )
                )

                SettingEffect.SendFeedback -> context.startActivity(
                    Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@example.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Noor Tasbeeh feedback")
                    }
                )

                SettingEffect.NavigateToLicenses -> { }
                SettingEffect.NavigateToSplash -> { }
                SettingEffect.PlayVibration -> VibrationManager.vibrate(context)
                SettingEffect.PlayClickSound -> SoundManager.playClick(context)
                is SettingEffect.ScheduleReminder -> {
                    if (effect.enabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                (context as? Activity)?.requestPermissions(
                                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                    101
                                )
                            }
                        }
                        ReminderScheduler.schedule(context, effect.type, effect.time)
                        val name = if (effect.type == ReminderType.Morning) "Morning" else "Evening"
                        Toast.makeText(context, "$name azkaar reminder set for ${formatTime(effect.time)}", Toast.LENGTH_SHORT).show()
                    } else {
                        ReminderScheduler.cancel(context, effect.type)
                        val name = if (effect.type == ReminderType.Morning) "Morning" else "Evening"
                        Toast.makeText(context, "$name azkaar reminder turned off", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    SettingContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun SettingContent(
    state: SettingState,
    onIntent: (SettingIntent) -> Unit
) {

    var reminderDialogFor by remember { mutableStateOf<ReminderType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Settings", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔥", fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${state.streakDays} days",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedSettingsItem(index = 0) {
                AppInfoCard(version = state.versionName)
            }

            AnimatedSettingsItem(index = 1) {
                SectionTitle("Appearance")
                SettingsCard {
                    SettingRow(
                        icon = R.drawable.theme,
                        title = "Theme",
                        subtitle = state.theme.label,
                        onClick = { onIntent(SettingIntent.ThemeClicked) },
                        trailing = { Chevron() }
                    )
                }
            }

            AnimatedSettingsItem(index = 2) {
                SectionTitle("Counting")
                SettingsCard {
                    SettingRow(
                        icon = R.drawable.vibration,
                        title = "Vibration",
                        subtitle = "Feel every tap",
                        trailing = {
                            GreenSwitch(state.vibration) {
                                onIntent(SettingIntent.VibrationChanged(it))
                            }
                        }
                    )
                    RowDivider()
                    SettingRow(
                        icon = Icons.Outlined.VolumeUp,
                        title = "Click sound",
                        subtitle = "Soft tick on each count",
                        trailing = {
                            GreenSwitch(state.clickSound) {
                                onIntent(SettingIntent.ClickSoundChanged(it))
                            }
                        }
                    )
                    RowDivider()
                    SettingRow(
                        icon = Icons.Outlined.PhoneAndroid,
                        title = "Keep screen on",
                        subtitle = "While you count",
                        trailing = {
                            GreenSwitch(state.keepScreenOn) {
                                onIntent(SettingIntent.KeepScreenOnChanged(it))
                            }
                        }
                    )
                    RowDivider()
                    SettingRow(
                        icon = Icons.Outlined.VolumeDown,
                        title = "Volume buttons",
                        subtitle = "Count with the volume keys",
                        trailing = {
                            GreenSwitch(state.volumeButtons) {
                                onIntent(SettingIntent.VolumeButtonsChanged(it))
                            }
                        }
                    )
                }
            }

            AnimatedSettingsItem(index = 3) {
                SectionTitle("Goals and reminders")
                SettingsCard {
                    SettingRow(
                        icon = R.drawable.daily_goal,
                        title = "Daily goal",
                        subtitle = "Counts per day",
                        trailing = {
                            GoalStepper(
                                value = state.dailyGoal,
                                onMinus = { onIntent(SettingIntent.DailyGoalDecrement) },
                                onPlus = { onIntent(SettingIntent.DailyGoalIncrement) }
                            )
                        }
                    )
                    RowDivider()
                    SettingRow(
                        icon = Icons.Outlined.Schedule,
                        title = "7-day cycle",
                        subtitle = "Day ${state.cycleDay} of 7",
                        onClick = { onIntent(SettingIntent.ResetCycleClicked) },
                        trailing = { Chevron() }
                    )
                    RowDivider()
                    ReminderRow(
                        title = "Morning azkaar",
                        time = state.morningTime,
                        enabled = state.morningEnabled,
                        onToggle = {
                            onIntent(SettingIntent.ReminderToggled(ReminderType.Morning, it))
                        },
                        onTimeClick = {
                            onIntent(SettingIntent.ReminderTimeClicked(ReminderType.Morning))
                        },
                        onIconClick = { reminderDialogFor = ReminderType.Morning }
                    )
                    RowDivider()
                    ReminderRow(
                        title = "Evening azkaar",
                        time = state.eveningTime,
                        enabled = state.eveningEnabled,
                        onToggle = {
                            onIntent(SettingIntent.ReminderToggled(ReminderType.Evening, it))
                        },
                        onTimeClick = {
                            onIntent(SettingIntent.ReminderTimeClicked(ReminderType.Evening))
                        },
                        onIconClick = { reminderDialogFor = ReminderType.Evening }
                    )
                }
            }

            AnimatedSettingsItem(index = 4) {
                SectionTitle("Support")
                SettingsCard {
                    SettingRow(
                        icon = R.drawable.rate_app,
                        title = "Rate this app",
                        subtitle = "Tell others what you think",
                        onClick = { onIntent(SettingIntent.RateAppClicked) },
                        trailing = { Chevron() }
                    )
                    RowDivider()
                    SettingRow(
                        icon = R.drawable.share_app,
                        title = "Share app",
                        subtitle = "Invite family and friends",
                        onClick = { onIntent(SettingIntent.ShareAppClicked) },
                        trailing = { Chevron() }
                    )
                    RowDivider()
                    SettingRow(
                        icon = R.drawable.send_feedback,
                        title = "Send feedback",
                        subtitle = "Report a problem or suggest a feature",
                        onClick = { onIntent(SettingIntent.SendFeedbackClicked) },
                        trailing = { Chevron() }
                    )
                }
            }

            AnimatedSettingsItem(index = 5) {
                SectionTitle("Privacy and legal")
                SettingsCard {
                    SettingRow(
                        icon = R.drawable.privacy_policy,
                        title = "Privacy policy",
                        onClick = { onIntent(SettingIntent.PrivacyPolicyClicked) },
                        trailing = { Chevron() }
                    )
                    RowDivider()
                    SettingRow(
                        icon = R.drawable.terms_of_use,
                        title = "Terms of use",
                        onClick = { onIntent(SettingIntent.TermsClicked) },
                        trailing = { Chevron() }
                    )
                    RowDivider()
                    SettingRow(
                        icon = R.drawable.about_info,
                        title = "Open-source licenses",
                        onClick = { onIntent(SettingIntent.LicensesClicked) },
                        trailing = { Chevron() }
                    )
                }
            }

            AnimatedSettingsItem(index = 6) {
                SectionTitle("Data")
                SettingsCard {
                    SettingRow(
                        icon = R.drawable.reset_data_red,
                        title = "Reset all data",
                        subtitle = "Deletes counts, history and custom zikr",
                        iconTint = Color(0xFFD64545),
                        onClick = { onIntent(SettingIntent.ResetClicked) },
                        trailing = { Chevron() }
                    )
                }
            }

            AnimatedSettingsItem(index = 7) {
                SectionTitle("About")
                SettingsCard {
                    SettingRow(
                        icon = R.drawable.about_info,
                        title = "Version",
                        subtitle = "${state.versionName} (build ${state.buildNumber})"
                    )
                }
            }

            AnimatedSettingsItem(index = 8) {
                Text(
                    "Made for your daily dhikr",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (state.showThemeDialog) {
        AnimatedVisibility(
            visible = state.showThemeDialog,
            enter = scaleIn(tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(300)),
            exit = scaleOut(tween(200, easing = FastOutSlowInEasing)) + fadeOut(tween(200))
        ) {
            ThemeDialog(
                current = state.theme,
                onDismiss = { onIntent(SettingIntent.DismissThemeDialog) },
                onSelect = { onIntent(SettingIntent.ThemeSelected(it)) }
            )
        }
    }

    if (state.showResetDialog) {
        AnimatedVisibility(
            visible = state.showResetDialog,
            enter = scaleIn(tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(300)),
            exit = scaleOut(tween(200, easing = FastOutSlowInEasing)) + fadeOut(tween(200))
        ) {
            ActionConfirmDialog(
                icon = Icons.Outlined.DeleteForever,
                iconTint = DangerRed,
                iconBackground = DangerRed.copy(alpha = 0.1f),
                title = "Reset all data?",
                message = "This will permanently delete all your counts, streaks and custom zikr. This action cannot be undone.",
                confirmText = "Reset",
                confirmColor = DangerRed,
                onConfirm = { onIntent(SettingIntent.ResetConfirmed) },
                onDismiss = { onIntent(SettingIntent.DismissResetDialog) }
            )
        }
    }

    if (state.showResetCycleDialog) {
        AnimatedVisibility(
            visible = state.showResetCycleDialog,
            enter = scaleIn(tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(300)),
            exit = scaleOut(tween(200, easing = FastOutSlowInEasing)) + fadeOut(tween(200))
        ) {
            ActionConfirmDialog(
                icon = Icons.Outlined.Schedule,
                iconTint = Green,
                iconBackground = Green.copy(alpha = 0.1f),
                title = "Reset 7-day cycle?",
                message = "This will start a new 7-day cycle from today. Your counting progress will not be affected.",
                confirmText = "Reset",
                confirmColor = Green,
                onConfirm = { onIntent(SettingIntent.ResetCycleConfirmed) },
                onDismiss = { onIntent(SettingIntent.DismissResetCycleDialog) }
            )
        }
    }

    reminderDialogFor?.let { type ->
        val isMorning = type == ReminderType.Morning
        val label = if (isMorning) "morning" else "evening"
        val currentlyEnabled = if (isMorning) state.morningEnabled else state.eveningEnabled
        val time = if (isMorning) state.morningTime else state.eveningTime
        val primaryColor = MaterialTheme.colorScheme.primary

        AnimatedVisibility(
            visible = reminderDialogFor != null,
            enter = scaleIn(tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(300)),
            exit = scaleOut(tween(200, easing = FastOutSlowInEasing)) + fadeOut(tween(200))
        ) {
            ActionConfirmDialog(
                icon = if (currentlyEnabled) Icons.Outlined.NotificationsOff else Icons.Outlined.NotificationsActive,
                iconTint = primaryColor,
                iconBackground = primaryColor.copy(alpha = 0.1f),
                title = if (currentlyEnabled) "Turn off $label reminder?" else "Enable $label reminder?",
                message = if (currentlyEnabled)
                    "You won't be reminded to recite your $label azkaar at ${formatTime(time)} anymore. You can turn it back on anytime."
                else
                    "You'll get a daily notification at ${formatTime(time)} to recite your $label azkaar.",
                confirmText = if (currentlyEnabled) "Turn off" else "Enable",
                confirmColor = primaryColor,
                onConfirm = {
                    onIntent(SettingIntent.ReminderToggled(type, !currentlyEnabled))
                    reminderDialogFor = null
                },
                onDismiss = { reminderDialogFor = null }
            )
        }
    }

    state.timePickerFor?.let { type ->
        val initial = if (type == ReminderType.Morning) state.morningTime else state.eveningTime
        AnimatedVisibility(
            visible = state.timePickerFor != null,
            enter = expandVertically(tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(300)),
            exit = shrinkVertically(tween(200, easing = FastOutSlowInEasing)) + fadeOut(tween(200))
        ) {
            ReminderTimePickerDialog(
                initial = initial,
                onDismiss = { onIntent(SettingIntent.DismissTimePicker) },
                onConfirm = { onIntent(SettingIntent.ReminderTimeSelected(type, it)) }
            )
        }
    }
}

@Composable
private fun AnimatedSettingsItem(
    index: Int,
    content: @Composable () -> Unit
) {
    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = true

    AnimatedVisibility(
        visibleState = visibleState,
        enter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = index * 50,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = index * 50,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        content()
    }
}

@Composable
private fun AppInfoCard(version: String) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppLogo()
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "Noor Tasbeeh",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Version $version · Free, no account needed",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}


@Composable
private fun AppLogo() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = Color(0xFFFFB300)
    val centerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(primaryColor, primaryColor.copy(alpha = 0.8f)))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(44.dp)) {
            val radius = size.minDimension / 2f - 4.dp.toPx()
            val dots = 16
            repeat(dots) { i ->
                val angle = Math.toRadians(360.0 / dots * i - 90)
                drawCircle(
                    color = goldColor,
                    radius = 2.6.dp.toPx(),
                    center = Offset(
                        center.x + (radius * cos(angle)).toFloat(),
                        center.y + (radius * sin(angle)).toFloat()
                    )
                )
            }
            drawCircle(color = centerColor, radius = 7.dp.toPx())
            drawCircle(color = goldColor, radius = 4.dp.toPx())
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier.padding(start = 6.dp, top = 10.dp, bottom = 2.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(content = content)
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun IconBadge(icon: Int, tint: Color = Green) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun IconBadge(icon: ImageVector, tint: Color = Green) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingRow(
    icon: Int,
    title: String,
    subtitle: String? = null,
    iconTint: Color = Green,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    SettingRowLayout(
        badge = { IconBadge(icon, iconTint) },
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        trailing = trailing
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = Green,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    SettingRowLayout(
        badge = { IconBadge(icon, iconTint) },
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        trailing = trailing
    )
}

@Composable
private fun SettingRowLayout(
    badge: @Composable () -> Unit,
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)?,
    trailing: @Composable (() -> Unit)?
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "settingRowScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        badge()
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            if (subtitle != null) {
                Text(subtitle, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun Chevron() {
    Icon(
        painter = painterResource(id = R.drawable.chevron_right),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.size(22.dp)
    )
}

@Composable
private fun GreenSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (checked) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "switchScale"
    )

    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            uncheckedBorderColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}

@Composable
private fun GoalStepper(value: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepperButton(Icons.Outlined.Remove, onMinus)
        Text(
            "$value",
            modifier = Modifier.widthIn(min = 44.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        StepperButton(Icons.Outlined.Add, onPlus)
    }
}

@Composable
private fun StepperButton(icon: ImageVector, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "stepperButtonScale"
    )

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        interactionSource = interactionSource,
        modifier = Modifier
            .size(34.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ReminderRow(
    title: String,
    time: LocalTime,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
    onIconClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReminderIconBadge(enabled = enabled, onClick = onIconClick)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
            Surface(
                onClick = onTimeClick,
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatTime(time), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        GreenSwitch(enabled, onToggle)
    }
}

@Composable
private fun ReminderIconBadge(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (enabled) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsNone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ThemeDialog(
    current: AppTheme,
    onDismiss: () -> Unit,
    onSelect: (AppTheme) -> Unit
) {
    var selectedTheme by remember { mutableStateOf(current) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            "Appearance",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Choose how Noor Tasbeeh looks",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppTheme.entries.forEach { theme ->
                        ThemeOptionRow(
                            theme = theme,
                            selected = theme == selectedTheme,
                            onClick = { selectedTheme = theme }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        onSelect(selectedTheme)
                        onDismiss()
                    }) {
                        Text("Done", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    theme: AppTheme,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "themeOptionBorder"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        label = "themeOptionBackground"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = themeIcon(theme),
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = theme.label,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun themeIcon(theme: AppTheme): ImageVector = when {
    theme.name.contains("dark", ignoreCase = true) -> Icons.Outlined.DarkMode
    theme.name.contains("light", ignoreCase = true) -> Icons.Outlined.LightMode
    else -> Icons.Outlined.Brightness6
}


@Composable
private fun ActionConfirmDialog(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String = "Cancel"
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                    ) {
                        Text(dismissText, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Text(confirmText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val pickerState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        rememberTimePickerState(
            initialHour = initial.hour,
            initialMinute = initial.minute,
            is24Hour = false
        )
    } else {
        TODO("VERSION.SDK_INT < O")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            TimePicker(
                state = pickerState,
                colors = TimePickerDefaults.colors(
                    selectorColor = MaterialTheme.colorScheme.primary,
                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.primary,
                    periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    periodSelectorSelectedContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    onConfirm(LocalTime.of(pickerState.hour, pickerState.minute))
                }
            }) {
                Text("OK", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
        }
    )
}

private fun formatTime(time: LocalTime): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
    } else {
        TODO("VERSION.SDK_INT < O")
    }
