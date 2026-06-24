package ar.edu.unlam.mobile.scaffolding.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import ar.edu.unlam.mobile.scaffolding.ui.navigation.Screen
import ar.edu.unlam.mobile.scaffolding.ui.theme.CyanWave
import ar.edu.unlam.mobile.scaffolding.ui.theme.ElectricIndigo

private data class NavigationItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
)

@Composable
fun BottomBar(controller: NavHostController) {
    val navBackStackEntry by controller.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Define items for the 4 side tabs
    val leftItems =
        listOf(
            NavigationItem(
                label = "Inicio",
                route = Screen.Dashboard.route,
                icon = Icons.Filled.Home,
            ),
            NavigationItem(
                label = "Progreso",
                route = "progress",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
            ),
        )

    val rightItems =
        listOf(
            NavigationItem(
                label = "Mapas",
                route = Screen.MapScreen.route,
                icon = Icons.Filled.Map,
            ),
            NavigationItem(
                label = "Perfil",
                route = Screen.Profile.route,
                icon = Icons.Filled.Person,
            ),
        )

    val centerRoute = Screen.RoutineList.route
    val isCenterSelected = currentDestination?.hierarchy?.any { it.route == centerRoute } == true

    // Animate scale of the convex button on state changes
    val centerButtonScale by animateFloatAsState(
        targetValue = if (isCenterSelected) 1.15f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "CenterButtonScale",
    )

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Main bottom bar card
        Box(
            modifier =
                Modifier
                    .padding(bottom = 4.dp)
                    .fillMaxWidth()
                    .height(66.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false,
                        ambientColor = ElectricIndigo.copy(alpha = 0.2f),
                        spotColor = ElectricIndigo.copy(alpha = 0.3f),
                    ).background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(24.dp),
                    ).border(
                        shape = RoundedCornerShape(24.dp),
                        width = 2.dp,
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer,
                                    ),
                            ),
                    ),
//            ).border(
//                        width = 1.dp,
//                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
//                        shape = RoundedCornerShape(24.dp),
//                    ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Left Tabs (Inicio, Progreso)
                leftItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    BottomTabItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            if (currentDestination?.route != item.route) {
                                controller.navigate(item.route) {
                                    launchSingleTop = true
                                    popUpTo(Screen.Dashboard.route) {
                                        saveState =
                                            item.route != Screen.MapScreen.route &&
                                            currentDestination?.route != Screen.MapScreen.route
                                    }
                                    restoreState =
                                        item.route != Screen.MapScreen.route &&
                                        currentDestination?.route != Screen.MapScreen.route
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                // Empty space in the middle of the row for the floating Convex button
                Spacer(modifier = Modifier.weight(1.2f))

                // Right Tabs (Mapas, Perfil)
                rightItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    BottomTabItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            if (currentDestination?.route != item.route) {
                                controller.navigate(item.route) {
                                    launchSingleTop = true
                                    popUpTo(Screen.Dashboard.route) {
                                        saveState =
                                            item.route != Screen.MapScreen.route &&
                                            currentDestination?.route != Screen.MapScreen.route
                                    }
                                    restoreState =
                                        item.route != Screen.MapScreen.route &&
                                        currentDestination?.route != Screen.MapScreen.route
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Floating Convex button in the center
        Box(
            modifier =
                Modifier
                    .offset { IntOffset(0, -22.dp.toPx().toInt()) }
                    .scale(centerButtonScale)
                    .size(60.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = ElectricIndigo.copy(alpha = 0.4f),
                        spotColor = ElectricIndigo.copy(alpha = 0.5f),
                    ).background(
                        brush =
                            Brush.linearGradient(
                                colors = listOf(ElectricIndigo, CyanWave),
                            ),
                        shape = CircleShape,
                    ).clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White),
                        onClick = {
                            if (currentDestination?.route != centerRoute) {
                                controller.navigate(centerRoute) {
                                    launchSingleTop = true
                                    popUpTo(Screen.Dashboard.route) {
                                        saveState = currentDestination?.route != Screen.MapScreen.route
                                    }
                                    restoreState = true
                                }
                            }
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Sesión",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }
        }

        // Small dot indicator under center button when selected
        if (isCenterSelected) {
            Box(
                modifier =
                    Modifier
                        .offset(y = (-4).dp)
                        .size(5.dp)
                        .background(color = ElectricIndigo, shape = CircleShape),
            )
        }
    }
}

@Composable
private fun BottomTabItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "BottomTabItemScale",
    )

    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Disable generic ripple to keep UI clean and bespoke
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint =
                    if (isSelected) {
                        ElectricIndigo
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.6f,
                        )
                    },
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = item.label,
                style =
                    TextStyle(
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color =
                            if (isSelected) {
                                ElectricIndigo
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.6f,
                                )
                            },
                    ),
            )
        }
    }
}

// Inline fallback for TextStyle to avoid import resolution issues
private fun TextStyle(
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    color: Color,
) = androidx.compose.ui.text.TextStyle(
    fontSize = fontSize,
    fontWeight = fontWeight,
    color = color,
)
