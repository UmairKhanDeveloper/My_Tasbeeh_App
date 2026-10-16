package com.example.mytasbeehapp.presentation.screens.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mytasbeehapp.presentation.screens.component.AppColors
import com.example.mytasbeehapp.presentation.screens.tasbeeh.CycleResetBus
import com.example.mytasbeehapp.presentation.theme.ManropeFamily

private val ChartHeight = 120.dp

@Composable
fun Stats(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Tab har baar khulay to Home screen ki latest activity ke mutabiq refresh ho jaye
    LaunchedEffect(Unit) {
        viewModel.onIntent(StatsIntent.Refresh)
    }

    LaunchedEffect(Unit) {
        CycleResetBus.reset.collect { shouldReload ->
            if (shouldReload) {
                viewModel.onIntent(StatsIntent.Refresh)
                CycleResetBus.consume()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                StatsEffect.GoalChanged -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    ProvideTextStyle(TextStyle(fontFamily = ManropeFamily)) {
        StatsContent(state = state, onIntent = viewModel::onIntent)
    }
}

@Composable
private fun StatsContent(
    state: StatsState,
    onIntent: (StatsIntent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StaggeredEntrance(index = 0) {
                StatsHeader(streakDays = state.dayStreak)
            }

            StaggeredEntrance(index = 1) {
                SummaryRow(
                    today = state.todayCount,
                    streak = state.dayStreak,
                    month = state.monthCount
                )
            }

            StaggeredEntrance(index = 2) {
                WeekCard(state = state)
            }

            StaggeredEntrance(index = 3) {
                ChartCard(state = state)
            }

            StaggeredEntrance(index = 4) {
                GoalCard(
                    goal = state.dailyGoal,
                    onDecrease = { onIntent(StatsIntent.DecreaseGoal) },
                    onIncrease = { onIntent(StatsIntent.IncreaseGoal) }
                )
            }
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
                slideInVertically(tween(450, delayMillis = delay, easing = FastOutSlowInEasing)) { it / 5 }
    ) {
        content()
    }
}

@Composable
private fun StatsCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = AppColors.primary().copy(alpha = 0.06f),
                spotColor = AppColors.primary().copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.surface())
            .padding(horizontal = 14.dp, vertical = 12.dp),
        content = content
    )
}

@Composable
private fun StatsHeader(streakDays: Int) {
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
            text = "Stats",
            color = AppColors.textPrimary(),
            fontSize = 21.sp,
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
                text = "$streakDays days",
                color = AppColors.textPrimary(),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SummaryRow(
    today: Int,
    streak: Int,
    month: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard(
            value = today.toString(),
            label = "Today",
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            value = streak.toString(),
            label = "Day streak",
            emoji = "🔥",
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            value = month.toString(),
            label = "This month",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    emoji: String? = null
) {
    StatsCardContainer(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (emoji != null) {
                    Text(text = emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                AnimatedContent(
                    targetState = value,
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 2 }) togetherWith
                                (fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it / 2 })
                    },
                    label = "summaryValue"
                ) { target ->
                    Text(
                        text = target,
                        color = AppColors.textPrimary(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = AppColors.textMuted(),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WeekCard(state: StatsState) {
    StatsCardContainer(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "This week",
            color = AppColors.textPrimary(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (state.hasActivity) {
                "A day counts when you reach your daily goal."
            } else {
                "Start counting on Home to see your stats here."
            },
            color = AppColors.textMuted(),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            state.week.forEach { day ->
                DayCircle(label = day.label, reached = state.isGoalReached(day))
            }
        }
    }
}

@Composable
private fun DayCircle(
    label: String,
    reached: Boolean
) {
    val background by animateColorAsState(
        targetValue = if (reached) AppColors.primary() else AppColors.surface(),
        animationSpec = tween(300),
        label = "dayBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (reached) AppColors.primary() else AppColors.border(),
        animationSpec = tween(300),
        label = "dayBorder"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (reached) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkScale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(background)
                .border(1.5.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = AppColors.onPrimary(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    scaleX = checkScale
                    scaleY = checkScale
                    alpha = checkScale.coerceIn(0f, 1f)
                }
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = AppColors.textMuted(),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ChartCard(state: StatsState) {
    StatsCardContainer(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Last 7 days",
            color = AppColors.textPrimary(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.week.forEachIndexed { index, day ->
                BarColumn(
                    label = day.label,
                    fraction = (day.count.toFloat() / state.maxCount).coerceIn(0.05f, 1f),
                    isToday = index == state.week.lastIndex,
                    index = index,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BarColumn(
    label: String,
    fraction: Float,
    isToday: Boolean,
    index: Int,
    modifier: Modifier = Modifier
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val animatedFraction by animateFloatAsState(
        targetValue = if (started) fraction else 0f,
        animationSpec = tween(
            durationMillis = 650,
            delayMillis = 250 + index * 60,
            easing = FastOutSlowInEasing
        ),
        label = "barHeight"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ChartHeight),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ChartHeight * animatedFraction)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(if (isToday) AppColors.gold() else AppColors.primary())
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = AppColors.textMuted(),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun GoalCard(
    goal: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    StatsCardContainer(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daily goal",
                    color = AppColors.textPrimary(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Counts per day",
                    color = AppColors.textMuted(),
                    fontSize = 13.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                GoalButton(symbol = "−", description = "Decrease goal", onClick = onDecrease)

                AnimatedContent(
                    targetState = goal,
                    transitionSpec = {
                        val up = targetState > initialState
                        val enter = slideInVertically(tween(180)) { if (up) it / 2 else -it / 2 } + fadeIn(tween(180))
                        val exit = slideOutVertically(tween(180)) { if (up) -it / 2 else it / 2 } + fadeOut(tween(120))
                        enter togetherWith exit
                    },
                    modifier = Modifier.widthIn(min = 56.dp),
                    label = "goalValue"
                ) { value ->
                    Text(
                        text = value.toString(),
                        color = AppColors.textPrimary(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(min = 56.dp)
                    )
                }

                GoalButton(symbol = "+", description = "Increase goal", onClick = onIncrease)
            }
        }
    }
}

@Composable
private fun GoalButton(
    symbol: String,
    description: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "goalButtonScale"
    )

    Box(
        modifier = Modifier
            .size(46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(AppColors.surface())
            .border(1.dp, AppColors.border(), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = AppColors.textPrimary(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}