package com.example.mytasbeehapp.presentation.screens.azkar

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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.mytasbeehapp.presentation.navigation.screens
import com.example.mytasbeehapp.presentation.screens.component.AppColors
import com.example.mytasbeehapp.presentation.theme.ManropeFamily

@Composable
fun Azkar(
    navController: NavController,
    viewModel: AzkarViewModel = viewModel(
        factory = AzkarViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(AzkarIntent.RefreshStreak)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AzkarEffect.NavigateToTasbeeh -> {
                    navController.navigate(screens.Tasbeeh.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                is AzkarEffect.ZikrAdded -> {
                    focusManager.clearFocus()
                    listState.animateScrollToItem(effect.index + 1)
                }
            }
        }
    }

    ProvideTextStyle(TextStyle(fontFamily = ManropeFamily)) {
        AzkarContent(
            state = state,
            listState = listState,
            onIntent = viewModel::onIntent
        )
    }
}

@Composable
private fun AzkarContent(
    state: AzkarState,
    listState: LazyListState,
    onIntent: (AzkarIntent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                start = 14.dp,
                end = 14.dp,
                top = 10.dp,
                bottom = 24.dp
            )
        ) {
            item(key = "header") {
                StaggeredEntrance(index = 0) {
                    AzkarHeader(streakDays = state.streakDays)
                }
            }

            itemsIndexed(items = state.azkar, key = { _, item -> item.id }) { index, zikr ->
                StaggeredEntrance(
                    index = index + 1,
                    modifier = Modifier.animateItem()
                ) {
                    ZikrCard(
                        zikr = zikr,
                        onClick = { onIntent(AzkarIntent.ZikrClicked(zikr)) }
                    )
                }
            }

            item(key = "add_form") {
                StaggeredEntrance(
                    index = state.azkar.size + 1,
                    modifier = Modifier.animateItem()
                ) {
                    AddZikrCard(state = state, onIntent = onIntent)
                }
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
    if (index > MaxStaggeredIndex) {
        Box(modifier = modifier) { content() }
        return
    }

    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    val delay = index * 70

    AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier,
        enter = fadeIn(tween(450, delayMillis = delay, easing = FastOutSlowInEasing)) +
                slideInVertically(tween(450, delayMillis = delay, easing = FastOutSlowInEasing)) { it / 5 } +
                expandVertically(tween(300, delayMillis = delay))
    ) {
        content()
    }
}

private const val MaxStaggeredIndex = 7

@Composable
private fun AzkarHeader(streakDays: Int) {
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
            text = "Azkaar",
            color = AppColors.TextPrimary,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(AppColors.Surface)
                .border(1.dp, AppColors.Border, RoundedCornerShape(50))
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
                text = if (streakDays == 1) "1 day" else "$streakDays days",
                color = AppColors.TextPrimary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ZikrCard(
    zikr: Zikr,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = AppColors.Primary.copy(alpha = 0.06f),
                spotColor = AppColors.Primary.copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = zikr.title,
                color = AppColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            CountPill(count = zikr.count)
        }

        if (zikr.arabic.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = zikr.arabic,
                color = AppColors.TextPrimary,
                fontSize = 34.sp,
                lineHeight = 48.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (zikr.meaning.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = zikr.meaning,
                color = AppColors.TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CountPill(count: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$count times",
            color = AppColors.TextPrimary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun AddZikrCard(
    state: AzkarState,
    onIntent: (AzkarIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = AppColors.Primary.copy(alpha = 0.06f),
                spotColor = AppColors.Primary.copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .padding(14.dp)
    ) {
        Text(
            text = "Add your own zikr",
            color = AppColors.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ZikrField(
                value = state.nameInput,
                onValueChange = { onIntent(AzkarIntent.NameChanged(it)) },
                placeholder = "Name, e.g. Ya Allah",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Next
            )
            ZikrField(
                value = state.countInput,
                onValueChange = { onIntent(AzkarIntent.CountChanged(it)) },
                placeholder = "33",
                modifier = Modifier.width(72.dp),
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        ZikrField(
            value = state.textInput,
            onValueChange = { onIntent(AzkarIntent.TextChanged(it)) },
            placeholder = "Zikr text (optional)",
            modifier = Modifier.fillMaxWidth(),
            imeAction = ImeAction.Done
        )

        Spacer(modifier = Modifier.height(10.dp))

        AddButton(
            enabled = state.canAdd,
            onClick = { onIntent(AzkarIntent.AddZikr) }
        )
    }
}

@Composable
private fun ZikrField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    val shape = RoundedCornerShape(12.dp)
    var focused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (focused) AppColors.Primary else AppColors.Border,
        animationSpec = tween(220),
        label = "fieldBorder"
    )
    val background by animateColorAsState(
        targetValue = if (focused) AppColors.Surface else AppColors.FieldBackground,
        animationSpec = tween(220),
        label = "fieldBackground"
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = ManropeFamily,
            color = AppColors.TextPrimary,
            fontSize = 14.sp
        ),
        cursorBrush = SolidColor(AppColors.Primary),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .onFocusChanged { focused = it.isFocused }
            .padding(horizontal = 12.dp),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxSize()) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = AppColors.TextMuted,
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun AddButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        animationSpec = tween(220),
        label = "buttonAlpha"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
            .clip(RoundedCornerShape(50))
            .background(AppColors.Primary)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Add zikr",
            color = AppColors.OnPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}