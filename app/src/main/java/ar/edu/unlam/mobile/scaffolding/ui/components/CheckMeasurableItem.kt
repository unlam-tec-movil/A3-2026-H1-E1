package ar.edu.unlam.mobile.scaffolding.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurCircular
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ar.edu.unlam.mobile.scaffolding.ui.theme.EmeraldIdeal
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.LightLevel

@Composable
fun CheckMeasurableItem(
    lightLevel: LightLevel,
    onProperLight: (Boolean) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val animatedSize by animateDpAsState(
        targetValue =
            if (isExpanded) {
                30.dp
            } else {
                6.dp
            },
    )

    val animatedColor by animateColorAsState(
        targetValue =
            when (lightLevel) {
                LightLevel.POOR -> Color.Red
                LightLevel.FAIR -> Color.Yellow
                LightLevel.GOOD -> Color.Green
            },
    )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(targetState = lightLevel) { lightLevel ->
            when (lightLevel) {
                LightLevel.POOR -> {
                    Icon(
                        imageVector = Icons.Default.RemoveCircle,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp),
                    )
                    onProperLight(false)
                }

                LightLevel.FAIR -> {
                    Icon(
                        imageVector = Icons.Default.BlurCircular,
                        contentDescription = null,
                        tint = Color.Yellow,
                        modifier = Modifier.size(20.dp),
                    )
                    onProperLight(true)
                }

                else -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldIdeal,
                        modifier = Modifier.size(20.dp),
                    )
                    onProperLight(true)
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { isExpanded = !isExpanded })
                    .height(animatedSize),
            colors = CardDefaults.cardColors(containerColor = animatedColor),
        ) {
            if (isExpanded) {
                AnimatedContent(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                    targetState = lightLevel,
                ) { lightLevel ->

                    Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(12.dp))
                        when (lightLevel) {
                            LightLevel.POOR -> {
                                Text(
                                    text = "Luz insuficiente.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            LightLevel.FAIR -> {
                                Text(
                                    text = "Iluminacion poco recomendable.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            else -> {
                                Text(
                                    text = "Iluminacion optima.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
