package com.example.simplenote

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.auth.RegisterViewModel

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegistered: (username: String, email: String) -> Unit,
    vm: RegisterViewModel = viewModel()
) {
    val purple = Color(0xFF504EC3)

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var retype by remember { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }

    val ui = vm.uiState.value
    val snackbar = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

    // client-side validation
    val isFirstValid = firstName.isNotBlank()
    val isLastValid = lastName.isNotBlank()
    val isUserValid = username.trim().length >= 3 && !username.contains(" ")
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isPassValid = password.length >= 8  // align with backend minimum
    val isRetypeValid = retype == password && retype.isNotBlank()
    val isFormValid = isFirstValid && isLastValid && isUserValid && isEmailValid && isPassValid && isRetypeValid

    fun tryRegister() {
        attempted = true
        if (isFormValid) {
            vm.register(username.trim(), password, email.trim(), firstName.trim(), lastName.trim())
        }
    }

    // navigate on success
    LaunchedEffect(ui.success) { ui.success?.let { onRegistered(it.username, it.email) } }

    // global backend errors as snackbar (if any)
    LaunchedEffect(ui.error) {
        ui.error?.let {
            snackbar.showSnackbar(it)
            vm.clearError()
        }
    }

    // Helpers
    @Composable
    fun Label(t: String) =
        Text(text = t, fontSize = 14.sp, color = Color(0xFF1C1B1F))

    @Composable
    fun InlineError(t: String) =
        Text(text = t, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))

    @Composable
    fun ErrorBullets(msgs: List<String>) {
        Column(Modifier.padding(top = 4.dp)) {
            msgs.forEach { Text("• $it", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        }
    }

    // Convenience getters for server errors
    fun errs(attr: String) = ui.fieldErrors[attr].orEmpty()
    val hasGeneral = ui.fieldErrors["general"].orEmpty().isNotEmpty()
            || ui.fieldErrors["non_field_errors"].orEmpty().isNotEmpty()
    val generalErrors = (ui.fieldErrors["general"].orEmpty() + ui.fieldErrors["non_field_errors"].orEmpty())

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .imePadding()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Back link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(enabled = !ui.loading) { onBackToLogin() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                Spacer(Modifier.width(6.dp))
                Text(text = "Back to Login", color = purple, fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(text = "Register", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1F))
            Text(text = "And start taking notes", fontSize = 14.sp, color = Color(0xFF9E9E9E), modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))

            // General server errors (e.g., non_field_errors)
            if (hasGeneral) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        generalErrors.forEach { Text("• $it", fontSize = 12.sp) }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // First name
            Label("First Name"); Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it; vm.clearFieldErrorsFor("first_name") },
                singleLine = true,
                placeholder = { Text("Example: Taha") },
                isError = (attempted && !isFirstValid) || errs("first_name").isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            if (attempted && !isFirstValid) InlineError("First name is required")
            if (errs("first_name").isNotEmpty()) ErrorBullets(errs("first_name"))

            Spacer(Modifier.height(14.dp))

            // Last name
            Label("Last Name"); Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it; vm.clearFieldErrorsFor("last_name") },
                singleLine = true,
                placeholder = { Text("Example: Hamifar") },
                isError = (attempted && !isLastValid) || errs("last_name").isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            if (attempted && !isLastValid) InlineError("Last name is required")
            if (errs("last_name").isNotEmpty()) ErrorBullets(errs("last_name"))

            Spacer(Modifier.height(14.dp))

            // Username
            Label("Username"); Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; vm.clearFieldErrorsFor("username") },
                singleLine = true,
                placeholder = { Text("Example: @HamifarTaha") },
                isError = (attempted && !isUserValid) || errs("username").isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            if (attempted && !isUserValid) InlineError("Username must be at least 3 chars, no spaces")
            if (errs("username").isNotEmpty()) ErrorBullets(errs("username"))

            Spacer(Modifier.height(14.dp))

            // Email
            Label("Email Address"); Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; vm.clearFieldErrorsFor("email") },
                singleLine = true,
                placeholder = { Text("Example: hamifar.taha@gmail.com") },
                isError = (attempted && !isEmailValid) || errs("email").isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            if (attempted && !isEmailValid) InlineError("Enter a valid email address")
            if (errs("email").isNotEmpty()) ErrorBullets(errs("email"))

            Spacer(Modifier.height(14.dp))

            // Password
            Label("Password"); Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; vm.clearFieldErrorsFor("password") },
                singleLine = true,
                placeholder = { Text("********") },
                visualTransformation = PasswordVisualTransformation(),
                isError = (attempted && !isPassValid) || errs("password").isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
            )
            if (attempted && !isPassValid) InlineError("Password must be at least 8 characters")
            if (errs("password").isNotEmpty()) ErrorBullets(errs("password"))

            Spacer(Modifier.height(14.dp))

            // Retype
            Label("Retype Password"); Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = retype,
                onValueChange = { retype = it }, // local check only
                singleLine = true,
                placeholder = { Text("********") },
                visualTransformation = PasswordVisualTransformation(),
                isError = attempted && !isRetypeValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { tryRegister() })
            )
            if (attempted && !isRetypeValid) InlineError("Passwords do not match")

            Spacer(Modifier.height(24.dp))

            val btnColors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) purple else purple.copy(alpha = 0.4f),
                contentColor = Color.White
            )
            Button(
                onClick = { tryRegister() },
                enabled = !ui.loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = btnColors
            ) {
                if (ui.loading) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text="Register", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Register")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onBackToLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Already have an account? Login here", color = purple, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
