package ar.edu.unlam.mobile.scaffolding.ui.screens.rehab

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
import ar.edu.unlam.mobile.scaffolding.ui.navigation.Screen
import ar.edu.unlam.mobile.scaffolding.ui.theme.CyanWave
import ar.edu.unlam.mobile.scaffolding.ui.theme.ElectricIndigo
import ar.edu.unlam.mobile.scaffolding.ui.theme.EmeraldIdeal
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.RoutineListViewModel

@Composable
fun RoutineListScreen(
    controller: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: RoutineListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
            )
        } else if (uiState.error != null) {
            Text(
                text = "Error: ${uiState.error}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful Header with overall stats
                RoutineHeader(
                    totalExercises = uiState.exercises.size,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // List section title
                Text(
                    text = "Tus Ejercicios de Hoy",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                if (uiState.exercises.isEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No tienes rutinas asignadas para hoy.",
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                ),
                        )
                    }
                } else {
                    // LazyColumn for smooth, high-performance scrolling
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        itemsIndexed(
                            items = uiState.exercises,
                            key = { _, exercise -> exercise.id },
                        ) { index, exercise ->
                            // Animated transition for each card item on entry
                            val animatedProgress = remember { Animatable(0f) }
                            LaunchedEffect(key1 = exercise.id) {
                                animatedProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec =
                                        tween(
                                            durationMillis = 400,
                                            delayMillis = index * 80,
                                            easing = FastOutSlowInEasing,
                                        ),
                                )
                            }

                            ExerciseCard(
                                exercise = exercise,
                                onStartClick = {
                                    controller.navigate(Screen.EnvironmentCheck.createRoute(exercise.id))
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            alpha = animatedProgress.value
                                            translationY = 40f * (1f - animatedProgress.value)
                                            scaleX = 0.95f + (0.05f * animatedProgress.value)
                                            scaleY = 0.95f + (0.05f * animatedProgress.value)
                                        },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineHeader(
    totalExercises: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
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
                    .background(
                        brush =
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        ElectricIndigo.copy(alpha = 0.05f),
                                        CyanWave.copy(alpha = 0.05f),
                                    ),
                            ),
                    ).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Rutina Activa 🎯",
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo,
                        ),
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Ejercicios recomendados para tu rehabilitación hoy.",
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        ),
                )
            }

            Box(
                modifier =
                    Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(ElectricIndigo.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$totalExercises",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo,
                            fontSize = 22.sp,
                        ),
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Exercise Name
                Text(
                    text = exercise.name,
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    modifier = Modifier.weight(1f),
                )

                // Chevron or active indicator
                Box(
                    modifier =
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyanWave.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "💪",
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Exercise Description
            Text(
                text = exercise.description,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Details/Metrics row (sets, reps, target angle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Sets Badge
                DetailBadge(
                    label = "Series",
                    value = "${exercise.sets}",
                    color = ElectricIndigo,
                    modifier = Modifier.weight(1f),
                )

                // Reps Badge
                DetailBadge(
                    label = "Repeticiones",
                    value = "${exercise.repetitions}",
                    color = EmeraldIdeal,
                    modifier = Modifier.weight(1f),
                )

                // Target Angle Badge
                DetailBadge(
                    label = "Ángulo Obj.",
                    value = "${exercise.endAngle.toInt()}°",
                    color = CyanWave,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start Exercise Action Button
            Button(
                onClick = onStartClick,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = ElectricIndigo,
                        contentColor = Color.White,
                    ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Iniciar Ejercicio",
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        ),
                )
            }
        }
    }
}

@Composable
fun DetailBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.08f))
                .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label.uppercase(),
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = color.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp,
                    ),
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = color,
                        fontSize = 16.sp,
                    ),
            )
        }
    }
}
