package ar.edu.unlam.mobile.scaffolding.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.unlam.mobile.scaffolding.ui.components.SnackbarVisualsWithError
import kotlinx.coroutines.launch

const val FORM_ROUTE = "form"

data class ValidationResult(
    val isValid: Boolean,
    val message: String,
)

fun validateForm(
    name: String,
    email: String,
): ValidationResult {
    if (name.isEmpty()) {
        return ValidationResult(
            isValid = false,
            message = "El nombre no puede estar vacío",
        )
    }

    if (!email.contains("@")) {
        return ValidationResult(
            isValid = false,
            message = "El email debe ser válido",
        )
    }
    return ValidationResult(
        isValid = true,
        message = "Formulario válido 😎",
    )
}

@Composable
fun FormScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val nameState = rememberTextFieldState()
        val emailState = rememberTextFieldState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Formulario de Registro",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Complete la información para actualizar su perfil",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                label = { Text("Nombre") },
                state = nameState,
                supportingText = { Text("Ingrese su nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                label = { Text("Email") },
                state = emailState,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                label = { Text("Contraseña") },
                state = rememberTextFieldState(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                label = { Text("Repetir Contraseña") },
                state = rememberTextFieldState(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                content = { Text("Limpiar Nombre") },
                onClick = {
                    nameState.clearText()
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                content = { Text("Enviar") },
                onClick = {
                    val res = validateForm(nameState.text.toString(), emailState.text.toString())
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            SnackbarVisualsWithError(res.message, !res.isValid),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Preview
@Composable
fun FormScreenPreview() {
    val snackBarHostState = remember { SnackbarHostState() }
    FormScreen(snackBarHostState)
}
