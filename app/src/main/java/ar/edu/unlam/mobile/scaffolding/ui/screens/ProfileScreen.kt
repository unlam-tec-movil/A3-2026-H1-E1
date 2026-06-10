package ar.edu.unlam.mobile.scaffolding.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.ui.theme.CoralDanger
import ar.edu.unlam.mobile.scaffolding.ui.theme.ElectricIndigo
import ar.edu.unlam.mobile.scaffolding.ui.theme.EmeraldIdeal
import ar.edu.unlam.mobile.scaffolding.ui.theme.GambAppTheme
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.ProfileUiState
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ProfileScreen
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigateToLogin) {
        if (uiState.navigateToLogin) {
            viewModel.onNavigationConsumed()
            onNavigateToLogin()
        }
    }

    ProfileContent(
        uiState = uiState,
        onStartEditName = viewModel::onStartEditName,
        onEditNameChange = viewModel::onEditNameChange,
        onSaveName = viewModel::onSaveName,
        onCancelEditName = viewModel::onCancelEditName,
        onToggleDarkMode = viewModel::onToggleDarkMode,
        onSignOutRequest = viewModel::onSignOutRequest,
        onSignOutDismiss = viewModel::onSignOutDismiss,
        onSignOutConfirm = viewModel::onSignOutConfirm,
        modifier = modifier,
    )
}

// ProfileContent
@Composable
internal fun ProfileContent(
    uiState: ProfileUiState,
    onStartEditName: () -> Unit,
    onEditNameChange: (String) -> Unit,
    onSaveName: () -> Unit,
    onCancelEditName: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onSignOutRequest: () -> Unit,
    onSignOutDismiss: () -> Unit,
    onSignOutConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ElectricIndigo)
        }
        return
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Avatar + datos del usuario
        AvatarSection(
            initials = uiState.initials,
            name = uiState.name,
            email = uiState.email,
            isEditing = uiState.isEditingName,
            editValue = uiState.editNameValue,
            editError = uiState.editNameError,
            isSaving = uiState.isSavingName,
            onStartEdit = onStartEditName,
            onEditChange = onEditNameChange,
            onSave = onSaveName,
            onCancel = onCancelEditName,
        )

        // Toggle dark / light mode
        DarkModeToggleCard(
            isDarkMode = uiState.isDarkMode,
            onToggle = onToggleDarkMode,
        )

        // Historial de sesiones
        SessionHistoryCard(sessions = uiState.recentSessions)

        // Cerrar sesión
        OutlinedButton(
            onClick = onSignOutRequest,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralDanger),
        ) {
            Text(
                text = "Cerrar sesión",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
        }
    }

    // Diálogo de confirmación de cierre de sesión
    if (uiState.showSignOutDialog) {
        AlertDialog(
            onDismissRequest = onSignOutDismiss,
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que querés cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = onSignOutConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDanger),
                ) {
                    Text("Cerrar sesión", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onSignOutDismiss) {
                    Text("Cancelar")
                }
            },
        )
    }
}

// AvatarSection
@Composable
private fun AvatarSection(
    initials: String,
    name: String,
    email: String,
    isEditing: Boolean,
    editValue: String,
    editError: String?,
    isSaving: Boolean,
    onStartEdit: () -> Unit,
    onEditChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Avatar
            Box(
                modifier =
                    Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(ElectricIndigo.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricIndigo,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = onEditChange,
                    label = { Text("Nombre") },
                    isError = editError != null,
                    supportingText =
                        editError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    trailingIcon = {
                        Row {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                IconButton(onClick = onSave) {
                                    Icon(Icons.Default.Check, "Guardar", tint = EmeraldIdeal)
                                }
                                IconButton(onClick = onCancel) {
                                    Icon(Icons.Default.Close, "Cancelar", tint = CoralDanger)
                                }
                            }
                        }
                    },
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(30.dp))
                    Text(
                        text = name.ifBlank { "Sin nombre" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onStartEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar nombre",
                            tint = ElectricIndigo,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// DarkModeToggleCard
@Composable
private fun DarkModeToggleCard(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = if (isDarkMode) "Modo oscuro" else "Modo claro",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isDarkMode) "🌙 Tema oscuro activo" else "☀️ Tema claro activo",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = { onToggle() },
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ElectricIndigo,
                    ),
            )
        }
    }
}

// SessionHistoryCard
@Composable
private fun SessionHistoryCard(sessions: List<Session>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Historial de sesiones",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Últimas ${sessions.size.coerceAtMost(10)} sesiones completadas",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (sessions.isEmpty()) {
                Text(
                    text = "Todavía no tenés sesiones registradas.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                sessions.forEachIndexed { index, session ->
                    SessionRow(session = session)
                    if (index < sessions.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: Session) {
    val dateStr =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(session.dateTimestamp))
    val durationMin = session.durationSeconds / 60

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateStr,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$durationMin min · ${session.successfulReps} reps exitosas",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
        }
        Text(
            text = "${session.averageRom.toInt()}°",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricIndigo,
        )
    }
}

// Previews
private val sampleSessions =
    listOf(
        Session(1L, "u1", "ex_knee_flexion", System.currentTimeMillis() - 86400000L, 600L, 95f, 12),
        Session(2L, "u1", "ex_knee_flexion", System.currentTimeMillis() - 172800000L, 900L, 108f, 18),
        Session(3L, "u1", "ex_knee_flexion", System.currentTimeMillis() - 259200000L, 1200L, 112f, 20),
    )

@Preview(name = "Profile · Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewProfileLight() {
    GambAppTheme(darkTheme = false) {
        Surface {
            ProfileContent(
                uiState =
                    ProfileUiState(
                        name = "Juan Pérez",
                        email = "juan@test.com",
                        initials = "JP",
                        recentSessions = sampleSessions,
                        isLoading = false,
                    ),
                onStartEditName = {},
                onEditNameChange = {},
                onSaveName = {},
                onCancelEditName = {},
                onToggleDarkMode = {},
                onSignOutRequest = {},
                onSignOutDismiss = {},
                onSignOutConfirm = {},
            )
        }
    }
}

@Preview(name = "Profile · Editing name · Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewProfileEditingLight() {
    GambAppTheme(darkTheme = false) {
        Surface {
            ProfileContent(
                uiState =
                    ProfileUiState(
                        name = "Juan Pérez",
                        email = "juan@test.com",
                        initials = "JP",
                        isEditingName = true,
                        editNameValue = "Juan P.",
                        recentSessions = sampleSessions,
                        isLoading = false,
                    ),
                onStartEditName = {},
                onEditNameChange = {},
                onSaveName = {},
                onCancelEditName = {},
                onToggleDarkMode = {},
                onSignOutRequest = {},
                onSignOutDismiss = {},
                onSignOutConfirm = {},
            )
        }
    }
}

@Preview(name = "Profile · Dark mode · Sign out dialog", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewProfileDark() {
    GambAppTheme(darkTheme = true) {
        Surface {
            ProfileContent(
                uiState =
                    ProfileUiState(
                        name = "Ana Gómez",
                        email = "ana@test.com",
                        initials = "AG",
                        isDarkMode = true,
                        showSignOutDialog = true,
                        recentSessions = emptyList(),
                        isLoading = false,
                    ),
                onStartEditName = {},
                onEditNameChange = {},
                onSaveName = {},
                onCancelEditName = {},
                onToggleDarkMode = {},
                onSignOutRequest = {},
                onSignOutDismiss = {},
                onSignOutConfirm = {},
            )
        }
    }
}
