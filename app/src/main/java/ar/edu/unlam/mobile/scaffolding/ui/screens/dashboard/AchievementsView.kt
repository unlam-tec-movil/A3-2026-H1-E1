package ar.edu.unlam.mobile.scaffolding.ui.screens.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.mobile.scaffolding.domain.model.Achievement
import ar.edu.unlam.mobile.scaffolding.ui.theme.*
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.AchievementsViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsView(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AchievementsViewModel = hiltViewModel(),
) {
    val achievements by viewModel.achievements.collectAsState()
    val totalAchievements = achievements.size
    val unlockedCount = achievements.count { it.isUnlocked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vitrina de Medallas",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Progress Header Card
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Tu Progreso General",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Has desbloqueado $unlockedCount de $totalAchievements logros",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }

                    Box(
                        modifier =
                            Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ElectricIndigo.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "🏆",
                            fontSize = 28.sp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (achievements.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = ElectricIndigo)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                ) {
                    itemsIndexed(achievements) { index, achievement ->
                        AchievementMedalCard(
                            achievement = achievement,
                            index = index,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AchievementMedalCard(
    achievement: Achievement,
    index: Int,
    modifier: Modifier = Modifier,
) {
    var isAnimated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Stagger the items entry for a premium feel
        delay(index * 120L)
        isAnimated = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isAnimated) 1f else 0f,
        animationSpec =
            spring(
                dampingRatio = 0.6f, // bouncy
                stiffness = 120f, // low stiffness = visible rebound
            ),
        label = "MedalBounceAnimation",
    )

    val opacity by animateFloatAsState(
        targetValue = if (isAnimated) 1f else 0f,
        label = "MedalOpacity",
    )

    val gradientBrush =
        remember(achievement.id, achievement.isUnlocked) {
            if (achievement.isUnlocked) {
                when (achievement.id) {
                    "10k_steps" ->
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFCD34D), Color(0xFFF59E0B)), // Amber/Gold
                        )
                    "first_session" ->
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF34D399), Color(0xFF059669)), // Emerald/Green
                        )
                    "master_rom" ->
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFC084FC), Color(0xFF7C3AED)), // Purple/Violet
                        )
                    else ->
                        Brush.radialGradient(
                            colors = listOf(ElectricIndigo, CyanWave),
                        )
                }
            } else {
                Brush.radialGradient(
                    colors = listOf(Color.LightGray.copy(alpha = 0.2f), Color.LightGray.copy(alpha = 0.4f)),
                )
            }
        }

    val emoji =
        remember(achievement.id) {
            when (achievement.id) {
                "10k_steps" -> "🏃‍♂️"
                "first_session" -> "🏋️"
                "master_rom" -> "🔥"
                else -> "🏅"
            }
        }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = opacity
                },
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (achievement.isUnlocked) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surface
                            .copy(
                                alpha = 0.5f,
                            )
                    },
            ),
        border =
            if (achievement.isUnlocked) {
                BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(ElectricIndigo.copy(alpha = 0.5f), CyanWave.copy(alpha = 0.5f)),
                    ),
                )
            } else {
                BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            },
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (achievement.isUnlocked) 4.dp else 0.dp,
            ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Medal circle with gradient
                Box(
                    modifier =
                        Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(gradientBrush)
                            .graphicsLayer {
                                // Subtle desaturation if locked
                                if (!achievement.isUnlocked) {
                                    alpha = 0.5f
                                }
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = emoji,
                        fontSize = 38.sp,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (achievement.isUnlocked) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface
                                .copy(
                                    alpha = 0.5f,
                                )
                        },
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                )

                if (achievement.isUnlocked && achievement.unlockedAtTimestamp != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val dateStr =
                        remember(achievement.unlockedAtTimestamp) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            sdf.format(Date(achievement.unlockedAtTimestamp))
                        }
                    Text(
                        text = "Obtenido: $dateStr",
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = EmeraldIdeal,
                            ),
                    )
                }
            }

            // Lock overlay at top-right corner
            if (!achievement.isUnlocked) {
                Box(
                    modifier =
                        Modifier
                            .padding(12.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Bloqueado",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}
