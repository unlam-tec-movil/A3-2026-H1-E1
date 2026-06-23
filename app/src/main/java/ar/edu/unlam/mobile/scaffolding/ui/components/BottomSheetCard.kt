package ar.edu.unlam.mobile.scaffolding.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SwipeDownAlt
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.ui.screens.HorizontalDivider

@Composable
fun BottomSheetCard(
    clinic: Clinic,
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit,
    distance: Double? = null,
    estimatedArrival: String? = null,
    onGenerateRouteClick: () -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val addresssInteractionSource = remember { MutableInteractionSource() }
    val phoneNumberInteractionSource = remember { MutableInteractionSource() }
    val isAddressPressed by addresssInteractionSource.collectIsPressedAsState()
    val isPNPressed by phoneNumberInteractionSource.collectIsPressedAsState()

    var isPNLongPressTriggered by remember { mutableStateOf(false) }
    var isAddressLongPressTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(isAddressPressed) {
        if (!isAddressPressed) {
            isAddressLongPressTriggered = false
        }
    }
    LaunchedEffect(isPNPressed) {
        if (!isPNPressed) {
            isPNLongPressTriggered = false
        }
    }

    Card(
        border =
            BorderStroke(
                width = 1.dp,
                brush =
                    Brush.linearGradient(
                        colors =
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primary,
                            ),
                    ),
            ),
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalHospital,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .size(100.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape,
                            ).padding(8.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = clinic.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    color = (

                                        when {
                                            isAddressLongPressTriggered -> {
                                                MaterialTheme.colorScheme.primary
                                            }

                                            isAddressPressed -> {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            }

                                            else -> {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                            }
                                        }
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                ).combinedClickable(
                                    interactionSource = addresssInteractionSource,
                                    onClick = {
                                        Toast
                                            .makeText(
                                                context,
                                                "press longer to copy the address",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    },
                                    onLongClick = {
                                        isAddressLongPressTriggered = true
                                        val clip = ClipData.newPlainText("Address", clinic.address)

                                        Toast
                                            .makeText(
                                                context,
                                                "address successfully copied",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        clipboardManager.setPrimaryClip(clip)
                                    },
                                ).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint =
                                when {
                                    isAddressLongPressTriggered -> {
                                        MaterialTheme.colorScheme.onPrimary
                                    }

                                    else -> {
                                        MaterialTheme.colorScheme.primary
                                    }
                                },
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            color =
                                when {
                                    isAddressLongPressTriggered -> {
                                        MaterialTheme.colorScheme.onPrimary
                                    }

                                    else -> {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                },
                            modifier =
                            Modifier,
                            text = clinic.address,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (clinic.phone.isNotEmpty()) {
                        Row(
                            modifier =
                                Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = (

                                            when {
                                                isPNLongPressTriggered -> {
                                                    MaterialTheme.colorScheme.primary
                                                }

                                                isPNPressed -> {
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                }

                                                else -> {
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                                }
                                            }
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                    ).combinedClickable(
                                        interactionSource = phoneNumberInteractionSource,
                                        onClick = {
                                            Toast
                                                .makeText(
                                                    context,
                                                    "press longer to copy the phone number",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                        },
                                        onLongClick = {
                                            isPNLongPressTriggered = true
                                            val clip = ClipData.newPlainText("Phone number", clinic.phone)

                                            Toast
                                                .makeText(
                                                    context,
                                                    "phone number successfully copied",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            clipboardManager.setPrimaryClip(clip)
                                        },
                                    ).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint =
                                    when {
                                        isPNLongPressTriggered -> {
                                            MaterialTheme.colorScheme.onPrimary
                                        }

                                        else -> {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    },
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                color =
                                    when {
                                        isPNLongPressTriggered -> {
                                            MaterialTheme.colorScheme.onPrimary
                                        }

                                        else -> {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    },
                                modifier =
                                Modifier,
                                text = clinic.phone,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            if (distance != null || estimatedArrival != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    distance?.let { distance ->
                        InfoChip(
                            icon = Icons.Outlined.NearMe,
                            label = String.format("%.2f km", distance / 1000),
                        )
                    }
                    estimatedArrival?.let { ea ->
                        InfoChip(
                            icon = Icons.Outlined.AccessTime,
                            label = ea,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onGenerateRouteClick,
                    modifier = Modifier.weight(2.9f),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Calculate Route")
                }
                OutlinedButton(
                    border =
                        BorderStroke(
                            width = 2.dp,
                            brush =
                                Brush.linearGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.onPrimary,
                                        ),
                                ),
                        ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    onClick = onCloseClick,
                ) {
                    Icon(imageVector = Icons.Default.SwipeDownAlt, contentDescription = "button to hide card")
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    label: String,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
