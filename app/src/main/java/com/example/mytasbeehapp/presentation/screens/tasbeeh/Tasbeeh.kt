package com.example.mytasbeehapp.presentation.screens.tasbeeh

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mytasbeehapp.R
import com.example.mytasbeehapp.domain.model.DhikrInfo
import com.example.mytasbeehapp.domain.model.Limit
import com.example.mytasbeehapp.domain.model.Preset
import com.example.mytasbeehapp.domain.model.Presets
import com.example.mytasbeehapp.presentation.screens.azkar.ZikrSelectionBus
import com.example.mytasbeehapp.presentation.screens.component.AppColors
import com.example.mytasbeehapp.presentation.theme.ManropeFamily
import com.example.mytasbeehapp.util.SoundManager
import com.example.mytasbeehapp.util.VibrationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Tasbeeh(
    navController: NavController,
    viewModel: TasbeehViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TasbeehEffect.Tick -> {
                    VibrationManager.vibrate(context, 35)
                }
                TasbeehEffect.Completed -> {
                    VibrationManager.vibrate(context, 100)
                }
                TasbeehEffect.PlayClick -> {
                    SoundManager.playClick(context)
                }
                TasbeehEffect.GoalReached -> scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = "🎉 Daily goal complete! MashaAllah",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        VolumeKeyBus.events.collect {
            viewModel.onIntent(TasbeehIntent.Tap)
        }
    }

    LaunchedEffect(Unit) {
        ZikrSelectionBus.pending.collect { zikr ->
            if (zikr != null) {
                viewModel.onIntent(TasbeehIntent.SelectZikr(zikr))
                ZikrSelectionBus.consume(zikr)
            }
        }
    }

    LaunchedEffect(Unit) {
        DailyGoalBus.goal.collect { goal ->
            if (goal != null) {
                viewModel.onIntent(TasbeehIntent.SetDailyGoal(goal))
                DailyGoalBus.consume(goal)
            }
        }
    }

    LaunchedEffect(Unit) {
        CycleResetBus.reset.collect { shouldReload ->
            if (shouldReload) {
                viewModel.onIntent(TasbeehIntent.Reload)
                CycleResetBus.consume()
            }
        }
    }

    ProvideTextStyle(TextStyle(fontFamily = ManropeFamily)) {
        TasbeehContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onIntent = viewModel::onIntent
        )
    }
}

private fun targetText(s: TasbeehState): String {
    val preset = s.preset
    return when (val limit = s.activeLimit) {
        Limit.Unlimited -> "No limit"
        is Limit.Fixed -> when {
            preset != null ->
                "Step ${s.presetStep + 1} of ${preset.steps.size} · ${s.dhikr.name} · Target: ${limit.value}"

            s.isCustomSelected -> "Target: ${limit.value} (custom)"
            s.zikr != null -> "${s.dhikr.name} · Target: ${limit.value}"
            else -> "Target: ${limit.value}"
        }
    }
}

@Composable
private fun TasbeehContent(
    state: TasbeehState,
    snackbarHostState: SnackbarHostState,
    onIntent: (TasbeehIntent) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background())
    ) {
        val compact = this.maxWidth < 360.dp
        val shortScreen = this.maxHeight < 640.dp
        val horizontalPadding = when {
            this.maxWidth < 360.dp -> 10.dp
            this.maxWidth > 600.dp -> 20.dp
            else -> 14.dp
        }
        val titleSize = if (compact) 19.sp else 21.sp

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = horizontalPadding, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StaggeredEntrance(index = 0) {
                HeaderRow(dayInCycle = state.dayInCycle, titleSize = titleSize)
            }

            Spacer(modifier = Modifier.height(8.dp))

            StaggeredEntrance(index = 1) {
                DailyGoalCard(
                    dailyCount = state.dailyCount,
                    dailyGoal = state.dailyGoal,
                    progress = state.dailyProgress,
                    goalReached = state.dailyGoalReached
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            StaggeredEntrance(index = 2) {
                DhikrCard(dhikr = state.dhikr, arabicSize = if (shortScreen) 34.sp else 42.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            StaggeredEntrance(index = 3) {
                LimitRow(state = state, onIntent = onIntent)
            }

            AnimatedVisibility(
                visible = state.preset == null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                TargetLabel(text = targetText(state))
            }

            Spacer(modifier = Modifier.height(6.dp))

            StaggeredEntrance(index = 4) {
                PresetChip(
                    preset = Presets.TasbeehFatima,
                    selected = state.preset == Presets.TasbeehFatima,
                    onClick = { onIntent(TasbeehIntent.SelectPreset(Presets.TasbeehFatima)) }
                )
            }

            AnimatedVisibility(
                visible = state.preset != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                TargetLabel(text = targetText(state))
            }

            StaggeredEntrance(
                index = 5,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val available = minOf(this.maxWidth, this.maxHeight)
                    val diameter = (available - 12.dp).coerceIn(120.dp, 260.dp)
                    CounterCircle(
                        count = state.count,
                        progress = state.ringProgress,
                        completions = state.completions,
                        limit = state.activeLimit,
                        size = diameter,
                        onTap = { onIntent(TasbeehIntent.Tap) }
                    )
                }
            }

            StaggeredEntrance(index = 6) {
                ActionRow(
                    hapticEnabled = state.hapticEnabled,
                    onUndo = { onIntent(TasbeehIntent.Undo) },
                    onReset = { onIntent(TasbeehIntent.Reset) },
                    onToggleHaptic = { onIntent(TasbeehIntent.ToggleHaptic) }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = AppColors.primary(),
                contentColor = AppColors.onPrimary(),
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}

@Composable
private fun StaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    val delay = index * 70

    AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier,
        enter = fadeIn(tween(450, delayMillis = delay, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    tween(
                        450,
                        delayMillis = delay,
                        easing = FastOutSlowInEasing
                    )
                ) { it / 5 }
    ) {
        content()
    }
}

@Composable
private fun HeaderRow(dayInCycle: Int, titleSize: TextUnit) {
    val flameTransition = rememberInfiniteTransition(label = "flame")
    val flameScale by flameTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tasbeeh",
            color = AppColors.textPrimary(),
            fontSize = titleSize,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(AppColors.surface())
                .border(1.dp, AppColors.border(), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔥",
                fontSize = 13.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = flameScale
                    scaleY = flameScale
                }
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Day $dayInCycle",
                color = AppColors.textPrimary(),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun DailyGoalCard(
    dailyCount: Int,
    dailyGoal: Int,
    progress: Float,
    goalReached: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "dailyProgress"
    )
    val animatedCount by animateIntAsState(
        targetValue = dailyCount,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "dailyCount"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = AppColors.primary().copy(alpha = 0.06f),
                spotColor = AppColors.primary().copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.surface())
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Daily goal", color = AppColors.textMuted(), fontSize = 13.sp)
            Text(
                text = "$animatedCount / $dailyGoal",
                color = AppColors.textMuted(),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(50))
                .background(AppColors.track())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.gold())
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (goalReached) "Goal reached ✓" else "${
                (dailyGoal - dailyCount).coerceAtLeast(
                    0
                )
            } to go",
            color = if (goalReached) AppColors.primary() else AppColors.textMuted(),
            fontSize = 11.sp,
            fontWeight = if (goalReached) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DhikrCard(dhikr: DhikrInfo, arabicSize: TextUnit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = AppColors.primary().copy(alpha = 0.06f),
                spotColor = AppColors.primary().copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.surface())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = dhikr,
            transitionSpec = {
                (fadeIn(tween(320, delayMillis = 80)) +
                        slideInHorizontally(tween(320, delayMillis = 80)) { it / 4 }) togetherWith
                        (fadeOut(tween(180)) +
                                slideOutHorizontally(tween(180)) { -it / 4 })
            },
            label = "dhikrContent"
        ) { target: DhikrInfo ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (target.arabic.isNotBlank()) {
                    Text(
                        text = target.arabic,
                        color = AppColors.textPrimary(),
                        fontSize = arabicSize,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                }
                if (target.meaning.isNotBlank()) {
                    Text(
                        text = target.meaning,
                        color = AppColors.textMuted(),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = target.name,
                    color = AppColors.textPrimary(),
                    fontSize = if (target.arabic.isBlank()) 22.sp else 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TargetLabel(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = text,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) },
            label = "targetLabel"
        ) { value: String ->
            Text(
                text = value,
                color = AppColors.primary(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LimitRow(
    state: TasbeehState,
    onIntent: (TasbeehIntent) -> Unit
) {
    val options = listOf(Limit.Fixed(33), Limit.Fixed(99), Limit.Fixed(100), Limit.Unlimited)
    val customActive = state.isCustomSelected
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val containerWidth = this.maxWidth

        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .widthIn(min = containerWidth),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                SelectableChip(
                    text = when (option) {
                        is Limit.Fixed -> option.value.toString()
                        Limit.Unlimited -> "No limit"
                    },
                    selected = state.preset == null && !customActive && state.limit == option,
                    onClick = { onIntent(TasbeehIntent.SelectLimit(option)) }
                )
            }

            CustomLimitField(
                value = state.customInput,
                active = customActive,
                onValueChange = { onIntent(TasbeehIntent.CustomLimitChanged(it)) }
            )
        }
    }
}

@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(30)
    val background by animateColorAsState(
        targetValue = if (selected) AppColors.primary() else AppColors.surface(),
        animationSpec = tween(220),
        label = "chipBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) AppColors.primary() else AppColors.border(),
        animationSpec = tween(220),
        label = "chipBorder"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) AppColors.onPrimary() else AppColors.textPrimary(),
        animationSpec = tween(220),
        label = "chipText"
    )

    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun CustomLimitField(
    value: String,
    active: Boolean,
    onValueChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    val focusManager = LocalFocusManager.current
    val borderColor by animateColorAsState(
        targetValue = if (active) AppColors.primary() else AppColors.border(),
        animationSpec = tween(220),
        label = "fieldBorder"
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = ManropeFamily,
            color = AppColors.textPrimary(),
            fontSize = 12.sp
        ),
        cursorBrush = SolidColor(AppColors.primary()),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier
            .width(72.dp)
            .height(32.dp)
            .clip(shape)
            .background(AppColors.fieldBackground())
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 8.dp),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxHeight()) {
                if (value.isEmpty()) {
                    Text(text = "Custom", color = AppColors.textMuted(), fontSize = 12.sp)
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun PresetChip(
    preset: Preset,
    selected: Boolean,
    onClick: () -> Unit
) {
    SelectableChip(text = preset.label, selected = selected, onClick = onClick)
}

@Composable
private fun CounterCircle(
    count: Int,
    progress: Float,
    completions: Int,
    limit: Limit,
    size: Dp,
    onTap: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val ring = remember { Animatable(progress) }
    val pulse = remember { Animatable(1f) }
    val initialCompletions = remember { completions }
    var handledCompletions by remember { mutableIntStateOf(completions) }

    LaunchedEffect(progress, completions) {
        if (completions != handledCompletions) {
            handledCompletions = completions
            ring.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
            ring.snapTo(0f)
        } else if (progress < ring.value - 0.5f) {
            ring.snapTo(progress)
        }
        ring.animateTo(progress.coerceIn(0f, 1f), tween(350, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(completions) {
        if (completions != initialCompletions) {
            pulse.animateTo(1.05f, tween(110, easing = FastOutSlowInEasing))
            pulse.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val trackColor = AppColors.track()
    val primaryColor = AppColors.primary()
    val backgroundColor = AppColors.surface()

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = pulse.value
                scaleY = pulse.value
            }
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onTap
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = this.size.width * 0.04f
            val inset = this.size.width * 0.025f + strokeWidth / 2
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * ring.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    val up = targetState > initialState
                    val enter = slideInVertically(tween(180)) { if (up) it / 2 else -it / 2 } +
                            fadeIn(tween(180))
                    val exit = slideOutVertically(tween(180)) { if (up) -it / 2 else it / 2 } +
                            fadeOut(tween(120))
                    enter togetherWith exit
                },
                label = "countContent"
            ) { value ->
                Text(
                    text = value.toString(),
                    color = AppColors.textPrimary(),
                    fontSize = (size.value * 0.27f).sp,
                    fontWeight = FontWeight.Light
                )
            }
            Text(
                text = when (limit) {
                    is Limit.Fixed -> "of ${limit.value}"
                    Limit.Unlimited -> "Tap to count"
                },
                color = AppColors.textMuted(),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ActionRow(
    hapticEnabled: Boolean,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onToggleHaptic: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(icon = R.drawable.undo, description = "Undo", filled = false, onClick = onUndo)
        ActionButton(
            icon = R.drawable.reset,
            description = "Reset",
            filled = false,
            onClick = onReset
        )
        ActionButton(
            icon = R.drawable.vibration,
            description = "Vibration",
            filled = hapticEnabled,
            onClick = onToggleHaptic
        )
    }
}

@Composable
private fun ActionButton(
    icon: Int,
    description: String,
    filled: Boolean,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (filled) AppColors.primary() else AppColors.surface(),
        animationSpec = tween(220),
        label = "actionBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (filled) AppColors.primary() else AppColors.border(),
        animationSpec = tween(220),
        label = "actionBorder"
    )
    val tint by animateColorAsState(
        targetValue = if (filled) AppColors.onPrimary() else AppColors.textPrimary(),
        animationSpec = tween(220),
        label = "actionTint"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}