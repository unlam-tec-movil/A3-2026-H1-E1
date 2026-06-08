package ar.edu.unlam.mobile.scaffolding.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.RegisterFormState
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.RegisterUiState
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.RegisterViewModel

// RegisterScreen
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) {
            onNavigateToLogin()
        }
    }

    RegisterContent(
        formState = formState,
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
        onRegister = viewModel::onRegister,
        onNavigateToLogin = onNavigateToLogin,
    )
}

// RegisterContent
@Composable
internal fun RegisterContent(
    formState: RegisterFormState,
    uiState: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val isLoading = uiState is RegisterUiState.Loading
    val globalError = (uiState as? RegisterUiState.Error)?.message

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
                modifier = Modifier.size(100.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Crear cuenta",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricIndigo,
            )

            Text(
                text = "Empezá tu recuperación hoy",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo nombre
            OutlinedTextField(
                value = formState.name,
                onValueChange = onNameChange,
                label = { Text("Nombre completo") },
                isError = formState.nameError != null,
                supportingText =
                    formState.nameError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo email
            OutlinedTextField(
                value = formState.email,
                onValueChange = onEmailChange,
                label = { Text("Correo electrónico") },
                isError = formState.emailError != null,
                supportingText =
                    formState.emailError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo contraseña
            OutlinedTextField(
                value = formState.password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña") },
                isError = formState.passwordError != null,
                supportingText =
                    formState.passwordError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                singleLine = true,
                visualTransformation =
                    if (formState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility, enabled = !isLoading) {
                        Icon(
                            imageVector =
                                if (formState.passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription =
                                if (formState.passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        )
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo confirmar contraseña
            OutlinedTextField(
                value = formState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirmar contraseña") },
                isError = formState.confirmPasswordError != null || uiState is RegisterUiState.Error,
                supportingText =
                    when {
                        formState.confirmPasswordError != null ->
                            {
                                { Text(formState.confirmPasswordError, color = MaterialTheme.colorScheme.error) }
                            }
                        globalError != null ->
                            {
                                { Text(globalError, color = MaterialTheme.colorScheme.error) }
                            }
                        else -> null
                    },
                singleLine = true,
                visualTransformation =
                    if (formState.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleConfirmPasswordVisibility, enabled = !isLoading) {
                        Icon(
                            imageVector =
                                if (formState.confirmPasswordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                            contentDescription =
                                if (formState.confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
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
                            onRegister()
                        },
                    ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Crear cuenta
            Button(
                onClick = onRegister,
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
                        text = "Crear cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Link a login
            TextButton(onClick = onNavigateToLogin, enabled = !isLoading) {
                Text(
                    text = "¿Ya tenés cuenta? Iniciá sesión",
                    color = ElectricIndigo,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Register · Idle · Light", showBackground = true, widthDp = 360, heightDp = 850)
@Composable
private fun PreviewRegisterIdleLight() {
    GambAppTheme(darkTheme = false) {
        Surface {
            RegisterContent(
                formState = RegisterFormState(),
                uiState = RegisterUiState.Idle,
                onNameChange = {},
                onEmailChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onTogglePasswordVisibility = {},
                onToggleConfirmPasswordVisibility = {},
                onRegister = {},
                onNavigateToLogin = {},
            )
        }
    }
}

@Preview(name = "Register · Error email duplicado · Light", showBackground = true, widthDp = 360, heightDp = 850)
@Composable
private fun PreviewRegisterErrorLight() {
    GambAppTheme(darkTheme = false) {
        Surface {
            RegisterContent(
                formState =
                    RegisterFormState(
                        name = "Juan Pérez",
                        email = "juan@test.com",
                        password = "password123",
                        confirmPassword = "password123",
                    ),
                uiState = RegisterUiState.Error("El email ya está registrado"),
                onNameChange = {},
                onEmailChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onTogglePasswordVisibility = {},
                onToggleConfirmPasswordVisibility = {},
                onRegister = {},
                onNavigateToLogin = {},
            )
        }
    }
}

@Preview(name = "Register · Validación inline · Light", showBackground = true, widthDp = 360, heightDp = 850)
@Composable
private fun PreviewRegisterValidationLight() {
    GambAppTheme(darkTheme = false) {
        Surface {
            RegisterContent(
                formState =
                    RegisterFormState(
                        name = "J",
                        nameError = "El nombre debe tener al menos 2 caracteres",
                        email = "bad_email",
                        emailError = "Formato de correo inválido",
                        password = "1234",
                        passwordError = "La contraseña debe tener al menos 8 caracteres",
                        confirmPassword = "5678",
                        confirmPasswordError = "Las contraseñas no coinciden",
                    ),
                uiState = RegisterUiState.Idle,
                onNameChange = {},
                onEmailChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onTogglePasswordVisibility = {},
                onToggleConfirmPasswordVisibility = {},
                onRegister = {},
                onNavigateToLogin = {},
            )
        }
    }
}

@Preview(name = "Register · Loading · Light", showBackground = true, widthDp = 360, heightDp = 850)
@Composable
private fun PreviewRegisterLoadingLight() {
    GambAppTheme(darkTheme = false) {
        Surface {
            RegisterContent(
                formState =
                    RegisterFormState(
                        name = "Juan Pérez",
                        email = "juan@test.com",
                        password = "password123",
                        confirmPassword = "password123",
                    ),
                uiState = RegisterUiState.Loading,
                onNameChange = {},
                onEmailChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onTogglePasswordVisibility = {},
                onToggleConfirmPasswordVisibility = {},
                onRegister = {},
                onNavigateToLogin = {},
            )
        }
    }
}

@Preview(name = "Register · Idle · Dark", showBackground = true, widthDp = 360, heightDp = 850)
@Composable
private fun PreviewRegisterIdleDark() {
    GambAppTheme(darkTheme = true) {
        Surface {
            RegisterContent(
                formState = RegisterFormState(),
                uiState = RegisterUiState.Idle,
                onNameChange = {},
                onEmailChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onTogglePasswordVisibility = {},
                onToggleConfirmPasswordVisibility = {},
                onRegister = {},
                onNavigateToLogin = {},
            )
        }
    }
}
