package com.example.mytasbeehapp.presentation.screens.onboarding

import android.Manifest
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.mytasbeehapp.R
import com.example.mytasbeehapp.presentation.screens.component.AppColors
import com.example.mytasbeehapp.presentation.theme.ManropeFamily
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val Brand = Color(0xFF0B6B4D)
private val Gold = Color(0xFFE2B64F)
private val Ink = Color(0xFF13241C)
private val Muted = Color(0xFF587064)
private val Stage3 = listOf(Color(0xFF14946A), Color(0xFF0A5A41), Color(0xFF063C2C))

private const val LAST_PAGE = 3

private class PageInfo(val title: String, val body: String, val action: String)

private val pages = listOf(
    PageInfo("Count every dhikr", "Tap the ring to count your tasbeeh. Set a target and feel a gentle vibration when you reach it.", "Next"),
    PageInfo("Build a daily habit", "Set a daily goal and keep your streak alive. See your whole week at a glance.", "Next"),
    PageInfo("Never miss your azkaar", "Get a gentle reminder every morning and evening. You choose the time.", "Turn on reminders"),
    PageInfo("Set your daily goal", "How many would you like to count each day? You can change this anytime in Settings.", "Get started")
)

private val goals = listOf(
    100 to "Light · about 3 minutes a day",
    300 to "Steady · about 8 minutes a day",
    500 to "Committed · about 12 minutes a day",
    1000 to "Intense · about 25 minutes a day"
)

@Composable
fun Onboarding(onFinished: () -> Unit) {
    val context = LocalContext.current
    val viewModel: OnboardingViewModel = viewModel(factory = remember { OnboardingViewModel.Factory(context) })
    val state by viewModel.state.collectAsState()
    val pager = rememberPagerState(initialPage = state.page, pageCount = { pages.size })
    val animationsOn = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.onIntent(OnboardingIntent.PermissionResult(it))
    }

    LaunchedEffect(pager) {
        snapshotFlow { pager.currentPage }.collect { viewModel.onIntent(OnboardingIntent.PageChanged(it)) }
    }
    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            when (it) {
                is OnboardingEffect.ScrollToPage -> pager.animateScrollToPage(it.page.coerceIn(0, LAST_PAGE))
                OnboardingEffect.RequestNotificationPermission -> permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                OnboardingEffect.NavigateToHome -> onFinished()
            }
        }
    }
    BackHandler(enabled = state.page > 0) { viewModel.onIntent(OnboardingIntent.Back) }

    ProvideTextStyle(TextStyle(fontFamily = ManropeFamily)) {
        OnboardingContent(state, pager, animationsOn, viewModel::onIntent)
    }
}

@Composable
private fun OnboardingContent(
    state: OnboardingState,
    pager: PagerState,
    animationsOn: Boolean,
    onIntent: (OnboardingIntent) -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(AppColors.background())
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        listOf(Color(0xFF128560).copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width / 2f, 0f),
                        radius = size.width * 0.9f
                    )
                )
            }
    ) {
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            TopBar(state.page, { onIntent(OnboardingIntent.Back) }, { onIntent(OnboardingIntent.Skip) })
            HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth()) { p ->
                val active = pager.currentPage == p
                when (p) {
                    0 -> CountPage(active, animationsOn)
                    1 -> StreakPage(active, animationsOn)
                    2 -> ReminderPage(active, animationsOn)
                    else -> GoalPage(active, state.goal) { onIntent(OnboardingIntent.GoalSelected(it)) }
                }
            }
            Footer(state.page, { onIntent(OnboardingIntent.MainAction) }, { onIntent(OnboardingIntent.MaybeLater) })
        }
    }
}

@Composable
private fun Label(
    text: CharSequence,
    color: Color,
    size: TextUnit,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.SemiBold,
    align: TextAlign? = null
) {
    val content = if (text is androidx.compose.ui.text.AnnotatedString) text else androidx.compose.ui.text.AnnotatedString(text.toString())
    Text(content, modifier, color = color, fontSize = size, fontWeight = weight, textAlign = align)
}

private fun Modifier.card(radius: Dp, elevation: Dp): Modifier {
    val shape = RoundedCornerShape(radius)
    return shadow(elevation, shape).background(Color.White, shape)
}

private fun Modifier.tappable(enabled: Boolean, radius: Dp, onClick: () -> Unit): Modifier =
    alpha(if (enabled) 1f else 0f).clip(RoundedCornerShape(radius)).clickable(enabled = enabled, onClick = onClick)

@Composable
private fun TopBar(page: Int, onBack: () -> Unit, onSkip: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 6.dp),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).tappable(page > 0, 22.dp, onBack), Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", Modifier.size(24.dp), AppColors.textMuted())
        }
        Label(
            "Skip", AppColors.textMuted(), 15.sp,
            Modifier.tappable(page < LAST_PAGE, 10.dp, onSkip).padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun Footer(page: Int, onMain: () -> Unit, onLater: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 20.dp),
        Arrangement.spacedBy(16.dp),
        Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(pages.size) { i ->
                val selected = i == page
                val width by animateDpAsState(if (selected) 26.dp else 8.dp, tween(300), label = "dotW")
                val color by animateColorAsState(
                    if (selected) AppColors.primary() else AppColors.border(), tween(300), label = "dotC"
                )
                Box(Modifier.height(8.dp).width(width).clip(CircleShape).background(color))
            }
        }
        Row(
            Modifier
                .widthIn(max = 330.dp)
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(50), spotColor = Brand, ambientColor = Brand)
                .clip(RoundedCornerShape(50))
                .background(AppColors.primary())
                .clickable(onClick = onMain),
            Arrangement.Center,
            Alignment.CenterVertically
        ) {
            Label(pages[page].action, AppColors.onPrimary(), 16.sp, weight = FontWeight.Bold)
            Spacer(Modifier.width(10.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(20.dp), AppColors.onPrimary())
        }
        Label(
            "Maybe later", AppColors.textMuted(), 14.sp,
            Modifier.offset(y = (-6).dp).tappable(page == 2, 10.dp, onLater).padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun Modifier.reveal(active: Boolean, delayMs: Int, scaleFrom: Float = 1f): Modifier {
    val p by animateFloatAsState(
        if (active) 1f else 0f,
        tween(if (active) 600 else 150, if (active) delayMs else 0, FastOutSlowInEasing),
        label = "reveal"
    )
    return graphicsLayer {
        alpha = p
        translationY = (1f - p) * 26.dp.toPx()
        scaleX = scaleFrom + (1f - scaleFrom) * p
        scaleY = scaleX
    }
}

@Composable
private fun stageHeight(short: Boolean = false): Dp {
    val h = LocalConfiguration.current.screenHeightDp
    return if (short) (h * 0.28f).dp.coerceIn(170.dp, 210.dp) else (h * 0.37f).dp.coerceIn(220.dp, 290.dp)
}

@Composable
private fun PageFrame(
    active: Boolean,
    index: Int,
    extra: (@Composable () -> Unit)? = null,
    stage: @Composable () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        Box(Modifier.reveal(active, 50, 0.93f)) { stage() }
        Spacer(Modifier.height(26.dp))
        Label(
            pages[index].title, AppColors.textPrimary(), 27.sp,
            Modifier.reveal(active, 300), FontWeight.ExtraBold, TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Label(
            pages[index].body, AppColors.textMuted(), 15.sp,
            Modifier.reveal(active, 450).widthIn(max = 320.dp), FontWeight.Normal, TextAlign.Center
        )
        extra?.let { Box(Modifier.reveal(active, 550)) { it() } }
    }
}

@Composable
private fun Stage(height: Dp, content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(32.dp)
    val particles by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.onboarding_particles))
    Box(
        Modifier
            .widthIn(max = 330.dp)
            .fillMaxWidth()
            .height(height)
            .shadow(18.dp, shape, ambientColor = Brand, spotColor = Brand)
            .clip(shape)
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        0f to Stage3[0], 0.6f to Stage3[1], 1f to Stage3[2],
                        center = Offset(size.width * 0.5f, size.height * 0.3f),
                        radius = size.width * 0.95f
                    )
                )
                drawPattern()
            },
        Alignment.Center
    ) {
        LottieAnimation(
            particles,
            Modifier.fillMaxSize(),
            iterations = LottieConstants.IterateForever,
            contentScale = ContentScale.Crop
        )
        content()
    }
}

private fun DrawScope.drawPattern() {
    val step = 48.dp.toPx()
    val r = step / 2f
    val line = Stroke(1.dp.toPx())
    val color = Color.White.copy(alpha = 0.10f)
    var x = 0f
    while (x <= size.width + step) {
        var y = 0f
        while (y <= size.height + step) {
            drawCircle(color, r, Offset(x, y), style = line)
            drawCircle(color, r, Offset(x + r, y + r), style = line)
            y += step
        }
        x += step
    }
}

@Composable
private fun rememberLoop(active: Boolean, animationsOn: Boolean, durationMs: Int, staticAt: Float): Animatable<Float, *> {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(active, animationsOn) {
        if (!animationsOn) {
            progress.snapTo(staticAt)
            return@LaunchedEffect
        }
        progress.snapTo(0f)
        while (active) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMs, easing = LinearEasing))
        }
    }
    return progress
}

@Composable
private fun LogoMark(modifier: Modifier) {
    Canvas(modifier) {
        val s = size.width / 68f
        val c = Offset(size.width / 2f, size.height / 2f)
        repeat(24) { i ->
            val a = -Math.PI / 2 + 2 * Math.PI * i / 24
            drawCircle(
                if (i < 17) Gold else Color(0xFF8FC7B0).copy(alpha = 0.55f),
                3.1f * s,
                Offset(c.x + (26 * s * cos(a)).toFloat(), c.y + (26 * s * sin(a)).toFloat())
            )
        }
        drawCircle(Color(0xFFF3E3B0), 12f * s, c)
        drawCircle(Brand, 7.4f * s, c, style = Stroke(1.2f * s))
    }
}

@Composable
private fun CountPage(active: Boolean, animationsOn: Boolean) {
    val progress = rememberLoop(active, animationsOn, 5280, 33.5f / 48f)
    val n = min((progress.value * 48).toInt(), 33)
    val ring by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.onboarding_count))

    PageFrame(active, 0) {
        Stage(stageHeight()) {
            Box(Modifier.fillMaxWidth(0.76f).widthIn(max = 212.dp).aspectRatio(1f), Alignment.Center) {
                LottieAnimation(ring, { progress.value }, Modifier.fillMaxSize())
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Label("$n", Brand, 38.sp, weight = FontWeight.ExtraBold)
                    Label("of 33", Muted, 12.sp)
                }
            }
            AnimatedVisibility(
                visible = n == 33,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 9.dp),
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Label(
                    "Target reached ✔", Color(0xFF3B2A00), 12.5.sp,
                    Modifier.clip(RoundedCornerShape(50)).background(Gold).padding(horizontal = 12.dp, vertical = 5.dp),
                    FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StreakPage(active: Boolean, animationsOn: Boolean) {
    val progress = rememberLoop(active, animationsOn, 5400, 7.5f / 12f)
    val days = min((progress.value * 12).toInt(), 7)
    val week by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.onboarding_week))
    val confetti by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.onboarding_confetti))

    PageFrame(active, 1) {
        Stage(stageHeight()) {
            Column(Modifier.fillMaxWidth(0.86f).card(22.dp, 14.dp).padding(horizontal = 14.dp, vertical = 16.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Label(
                        buildAnnotatedString {
                            append("🔥 ")
                            withStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)) { append("$days") }
                            append(" day streak")
                        },
                        Ink, 15.sp, weight = FontWeight.Bold
                    )
                    Label(
                        "Daily goal", Brand, 11.5.sp,
                        Modifier.clip(RoundedCornerShape(50)).background(Color(0xFFE4ECE6)).padding(horizontal = 9.dp, vertical = 4.dp),
                        FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(14.dp))
                LottieAnimation(week, { progress.value }, Modifier.fillMaxWidth().aspectRatio(5f))
                Row(Modifier.fillMaxWidth()) {
                    "MTWTFSS".forEach { Label("$it", Muted, 11.sp, Modifier.weight(1f), align = TextAlign.Center) }
                }
                Spacer(Modifier.height(14.dp))
                Label(
                    if (days < 7) "Keep going" else "Streak complete. Great consistency!",
                    if (days == 7) Brand else Muted, 12.5.sp, Modifier.fillMaxWidth(), align = TextAlign.Center
                )
            }
            LottieAnimation(confetti, { progress.value }, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
private fun ReminderPage(active: Boolean, animationsOn: Boolean) {
    var first by remember { mutableStateOf(!animationsOn) }
    var second by remember { mutableStateOf(!animationsOn) }
    LaunchedEffect(active, animationsOn) {
        first = !animationsOn
        second = !animationsOn
        if (animationsOn && active) {
            delay(500)
            first = true
            delay(1000)
            second = true
        }
    }
    val bell by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.onboarding_bell))
    val height = stageHeight()
    val compact = height < 250.dp

    PageFrame(active, 2) {
        Stage(height) {
            Column(Modifier, Arrangement.spacedBy(10.dp, Alignment.CenterVertically), Alignment.CenterHorizontally) {
                Box(Modifier.size(if (compact) 72.dp else 92.dp), Alignment.Center) {
                    LottieAnimation(
                        bell,
                        Modifier.requiredSize(if (compact) 108.dp else 138.dp),
                        isPlaying = active && animationsOn,
                        iterations = LottieConstants.IterateForever
                    )
                }
                NotificationCard(first, "6:00 AM", "Time for your morning azkaar", "SubhanAllah × 33 is waiting for you")
                NotificationCard(second, "6:30 PM", "Keep your streak alive", "Evening azkaar is ready when you are")
            }
        }
    }
}

@Composable
private fun NotificationCard(visible: Boolean, time: String, title: String, text: String) {
    AnimatedVisibility(
        visible,
        enter = fadeIn(tween(600)) + slideInVertically(tween(600, easing = FastOutSlowInEasing)) { -it / 2 }
    ) {
        Row(
            Modifier.fillMaxWidth(0.86f).card(18.dp, 10.dp).padding(horizontal = 12.dp, vertical = 10.dp),
            Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Brand), Alignment.Center) {
                LogoMark(Modifier.size(24.dp))
            }
            Column {
                Label("Noor Tasbeeh · $time", Muted, 11.sp)
                Label(title, Ink, 13.sp, weight = FontWeight.Bold)
                Label(text, Muted, 12.sp, weight = FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun GoalPage(active: Boolean, goal: Int, onGoal: (Int) -> Unit) {
    val sweep by animateFloatAsState(min(goal / 1000f, 1f), tween(500, easing = FastOutSlowInEasing), label = "sweep")
    val shown by animateIntAsState(goal, tween(350), label = "goal")

    PageFrame(
        active, 3,
        extra = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    goals.forEach { (value, _) -> GoalChip("%,d".format(value), goal == value) { onGoal(value) } }
                }
                Spacer(Modifier.height(10.dp))
                Label(goals.firstOrNull { it.first == goal }?.second.orEmpty(), AppColors.textMuted(), 13.sp)
            }
        }
    ) {
        Stage(stageHeight(short = true)) {
            Box(Modifier.size(140.dp), Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = size.width * 9f / 120f
                    val radius = size.width * 52f / 120f
                    val topLeft = Offset(size.width / 2f - radius, size.height / 2f - radius)
                    val arc = Size(radius * 2, radius * 2)
                    drawArc(Color.White.copy(alpha = 0.2f), 0f, 360f, false, topLeft, arc, style = Stroke(stroke))
                    drawArc(Gold, -90f, 360f * sweep, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Label("$shown", Color.White, 34.sp, weight = FontWeight.ExtraBold)
                    Label("counts a day", Color.White.copy(alpha = 0.85f), 11.5.sp)
                }
            }
        }
    }
}

@Composable
private fun GoalChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val background by animateColorAsState(if (selected) AppColors.primary() else AppColors.surface(), tween(200), label = "bg")
    val border by animateColorAsState(if (selected) AppColors.primary() else AppColors.border(), tween(200), label = "border")
    val text by animateColorAsState(if (selected) AppColors.onPrimary() else AppColors.textPrimary(), tween(200), label = "text")
    val shape = RoundedCornerShape(50)
    Label(
        label, text, 14.sp,
        Modifier.clip(shape).background(background).border(2.dp, border, shape).clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        FontWeight.Bold
    )
}