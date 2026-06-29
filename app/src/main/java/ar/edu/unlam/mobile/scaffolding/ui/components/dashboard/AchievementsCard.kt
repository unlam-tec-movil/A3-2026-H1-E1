package ar.edu.unlam.mobile.scaffolding.ui.components.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.unlam.mobile.scaffolding.ui.theme.ElectricIndigo

@Composable
fun AchievementsCard(
    unlockedCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
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
                    .padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Tus Logros y Medallas 🏆",
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                    )
                    Text(
                        text = "Gamificación y progreso del tratamiento",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "$unlockedCount / $totalCount",
                        style =
                            MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectricIndigo,
                            ),
                    )
                    Text(
                        text = "Desbloqueados",
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            ),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MiniMedalBadge(emoji = "🏃‍♂️", isUnlocked = unlockedCount >= 1, color = Color(0xFFF59E0B))
                    MiniMedalBadge(emoji = "🏋️", isUnlocked = unlockedCount >= 2, color = Color(0xFF059669))
                    MiniMedalBadge(emoji = "🔥", isUnlocked = unlockedCount >= 3, color = Color(0xFF7C3AED))
                }
            }
        }
    }
}

@Composable
fun MiniMedalBadge(
    emoji: String,
    isUnlocked: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isUnlocked) color.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f),
                ).border(
                    width = 1.5.dp,
                    color = if (isUnlocked) color else Color.LightGray.copy(alpha = 0.4f),
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            fontSize = 18.sp,
            modifier =
                Modifier.graphicsLayer {
                    if (!isUnlocked) alpha = 0.4f
                },
        )
    }
}
