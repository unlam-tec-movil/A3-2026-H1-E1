package ar.edu.unlam.mobile.scaffolding.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun FABShortCut(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
) {
    FloatingActionButton(
        shape = RoundedCornerShape(30.dp),
        onClick = {
            onClick()
        },
        modifier =
            modifier
                .border(
                    shape = RoundedCornerShape(30.dp),
                    border =
                        BorderStroke(
                            width = 1.dp,
                            brush =
                                Brush.linearGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primaryContainer,
                                        ),
                                ),
                        ),
                ),
        content = {
            Icon(
                imageVector = icon,
                modifier = Modifier,
                contentDescription = "",
            )
        },
    )
}
