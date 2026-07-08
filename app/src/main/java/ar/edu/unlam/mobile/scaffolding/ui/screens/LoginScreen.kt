package ar.edu.unlam.mobile.scaffolding.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.mobile.scaffolding.R
import ar.edu.unlam.mobile.scaffolding.ui.theme.ElectricIndigo
import ar.edu.unlam.mobile.scaffolding.ui.theme.GambAppTheme
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.LoginFormState
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.LoginUiState
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.LoginViewModel

private data class MockUserUi(
    val name: String,
    val email: String,
    val level: String,
    val description: String,
    val color: Color,
)

private val mockUsersList =
    listOf(
        MockUserUi(
            name = "Juan Pérez",
            email = "juan.perez@gambapp.com",
            level = "Principiante",
            description = "3 ses. · ROM 72° · 1.5K pasos",
            color = Color(0xFF0EA5E9), // Sky Blue
        ),
        MockUserUi(
            name = "María Rodríguez",
            email = "maria.rodriguez@gambapp.com",
            level = "Intermedio",
            description = "5 ses. · ROM 102° · 5.5K pasos",
            color = Color(0xFF0D9488), // Teal
        ),
        MockUserUi(
            name = "Carlos Gómez",
            email = "carlos.gomez@gambapp.com",
            level = "Avanzado",
            description = "10 ses. · ROM 122° · 12.5K pasos",
            color = Color(0xFF8B5CF6), // Violet/Purple
        ),
    )

// LoginScreen
@Composable
fun LoginScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onNavigateToDashboard()
        }
    }

    LoginContent(
        formState = formState,
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onLogin = viewModel::onLogin,
        onLoginWithMockUser = viewModel::onLoginWithMockUser,
        onNavigateToRegister = onNavigateToRegister,
    )
}

// LoginContent
@Composable
internal fun LoginContent(
    formState: LoginFormState,
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLogin: () -> Unit,
    onLoginWithMockUser: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val isLoading = uiState is LoginUiState.Loading

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.gambapp_logo_opt3_round),
                contentDescription = "GambApp logo",
                modifier = Modifier.size(130.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "GambApp",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricIndigo,
            )

            Text(
                text = stringResource(R.string.login_title),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email
            OutlinedTextField(
                value = formState.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.login_email_label)) },
                isError = formState.emailError != null,
                supportingText =
                    formState.emailError?.let {
                        {
                            Text(
                                it.asString(),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contraseña
            OutlinedTextField(
                value = formState.password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.login_password_label)) },
                isError = formState.passwordError != null || uiState is LoginUiState.Error,
                supportingText =
                    when {
                        formState.passwordError != null ->
                            {
                                { Text(formState.passwordError.asString(), color = MaterialTheme.colorScheme.error) }
                            }
                        uiState is LoginUiState.Error ->
                            {
                                { Text(uiState.message.asString(), color = MaterialTheme.colorScheme.error) }
                            }
                        else -> null
                    },
                singleLine = true,
                visualTransformation =
                    if (formState.passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility, enabled = !isLoading) {
                        Icon(
                            imageVector =
                                if (formState.passwordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                            contentDescription =
                                if (formState.passwordVisible) {
                                    stringResource(R.string.login_hide_password)
                                } else {
                                    stringResource(R.string.login_show_password)
                                },
                        )
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onLogin()
                        },
                    ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Iniciar sesión
            Button(
                onClick = onLogin,
                enabled = !isLoading,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                shape = MaterialTheme.shapes.medium,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Link registro
            TextButton(onClick = onNavigateToRegister, enabled = !isLoading) {
                Text(
                    text = stringResource(R.string.login_no_account),
                    color = ElectricIndigo,
                    fontSize = 14.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.login_quick_access_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )

            Spacer(modifier = Modifier.height(10.dp))

            mockUsersList.forEach { mockUser ->
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = !isLoading) { onLoginWithMockUser(mockUser.email) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        ),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Avatar Circle
                        Box(
                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .background(color = mockUser.color, shape = CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text =
                                    mockUser.name
                                        .split(" ")
                                        .map { it.first() }
                                        .joinToString(""),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mockUser.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = mockUser.email,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = mockUser.level,
                                color = mockUser.color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier =
                                    Modifier
                                        .background(
                                            mockUser.color.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp),
                                        ).padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = mockUser.description,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Previews
@Preview(name = "Login · Idle · Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewLoginIdleLight() {
    GambAppTheme(darkTheme = false) {
        Surface {
            LoginContent(
                formState = LoginFormState(),
                uiState = LoginUiState.Idle,
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePasswordVisibility = {},
                onLogin = {},
                onLoginWithMockUser = {},
                onNavigateToRegister = {},
            )
        }
    }
}

@Preview(name = "Login · Error · Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewLoginErrorLight() {
    GambAppTheme(darkTheme = false) {
        Surface {
            LoginContent(
                formState = LoginFormState(email = "user@test.com", password = "wrongpass"),
                uiState =
                    LoginUiState.Error(
                        ar.edu.unlam.mobile.scaffolding.ui.utils.UiText
                            .DynamicString("Credenciales incorrectas"),
                    ),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePasswordVisibility = {},
                onLogin = {},
                onLoginWithMockUser = {},
                onNavigateToRegister = {},
            )
        }
    }
}

@Preview(name = "Login · Loading · Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewLoginLoadingLight() {
    GambAppTheme(darkTheme = false) {
        Surface {
            LoginContent(
                formState = LoginFormState(email = "user@test.com", password = "password123"),
                uiState = LoginUiState.Loading,
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePasswordVisibility = {},
                onLogin = {},
                onLoginWithMockUser = {},
                onNavigateToRegister = {},
            )
        }
    }
}

@Preview(name = "Login · Idle · Dark", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewLoginIdleDark() {
    GambAppTheme(darkTheme = true) {
        Surface {
            LoginContent(
                formState = LoginFormState(),
                uiState = LoginUiState.Idle,
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePasswordVisibility = {},
                onLogin = {},
                onLoginWithMockUser = {},
                onNavigateToRegister = {},
            )
        }
    }
}
