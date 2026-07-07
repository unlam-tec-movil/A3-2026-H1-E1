package ar.edu.unlam.mobile.scaffolding.ui.components.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ar.edu.unlam.mobile.scaffolding.R
import ar.edu.unlam.mobile.scaffolding.ui.components.LottieAnimation

@Composable
fun LocationDeniedCard() {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.background,
            ),
        modifier = Modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        LottieAnimation(
            modifier = Modifier.size(height = 300.dp, width = 400.dp),
            resId = R.raw.access_location_2,
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier =
                Modifier
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            border =
                BorderStroke(
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
        ) {
            Text(
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary,
                text = "Gambapp necesita conocer tu ubicacion para poder recomendarte clinicas cercanas.",
            )
        }
    }
}
