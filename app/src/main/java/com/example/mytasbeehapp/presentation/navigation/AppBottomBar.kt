package com.example.mytasbeehapp.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mytasbeehapp.presentation.screens.component.AppColors
import com.example.mytasbeehapp.presentation.theme.ManropeFamily

private data class BottomItem(val screen: screens, val label: String)


private val bottomItems = listOf(
    BottomItem(screens.Tasbeeh, "Home"),
    BottomItem(screens.Azkar, "Azkaar"),
    BottomItem(screens.Stats, "Stats"),
    BottomItem(screens.Setting, "Settings")
)

val bottomBarRoutes: List<String> = bottomItems.map { it.screen.route }

@Composable
fun AppBottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Column(modifier = Modifier.background(AppColors.Surface)) {
        HorizontalDivider(color = AppColors.Border)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(70.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomItems.forEach { item ->
                    BottomBarItem(
                        modifier = Modifier.weight(1f),
                        item = item,
                        selected = currentRoute == item.screen.route,
                        onClick = {
                            if (currentRoute != item.screen.route) {
                                navController.navigate(item.screen.route) {
                                    popUpTo(screens.Tasbeeh.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    modifier: Modifier,
    item: BottomItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(50))
                .background(if (selected) AppColors.Primary else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            item.screen.icon?.let { icon ->
                Icon(
                    painter = painterResource(icon),
                    contentDescription = item.label,
                    modifier = Modifier.size(22.dp),
                    tint = if (selected) AppColors.OnPrimary else AppColors.TextMuted
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            fontFamily = ManropeFamily,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) AppColors.TextPrimary else AppColors.TextMuted
        )
    }
}