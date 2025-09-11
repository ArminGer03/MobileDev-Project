package com.example.simplenote

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding

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

    // validation
    val isFirstValid = firstName.isNotBlank()
    val isLastValid = lastName.isNotBlank()
    val isUserValid = username.trim().length >= 3 && !username.contains(" ")
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isPassValid = password.length >= 6
    val isRetypeValid = retype == password && retype.isNotBlank()
    val isFormValid = isFirstValid && isLastValid && isUserValid && isEmailValid && isPassValid && isRetypeValid

    fun tryRegister() {
        attempted = true
        if (isFormValid) {
            vm.register(username.trim(), password, email.trim(), firstName.trim(), lastName.trim())
        }
    }

    // navigate on success
    LaunchedEffect(ui.success) {
        ui.success?.let { onRegistered(it.username, it.email) }
    }
    // show backend errors
    LaunchedEffect(ui.error) {
        ui.error?.let {
            snackbar.showSnackbar(it)
            vm.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { pad ->
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .imePadding()                // <— moves content above the keyboard
                .verticalScroll(scroll)      // <— enable vertical scrolling
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Back link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(enabled = !ui.loading) { onBackToLogin() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                Spacer(Modifier.width(6.dp))
                Text(text = "Back to Login", color = purple, fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Register",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )
            Text(
                text = "And start taking notes",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Helpers
            @Composable
            fun Label(t: String) = Text(text = t, fontSize = 14.sp, color = Color(0xFF1C1B1F))
            @Composable
            fun Error(t: String) = Text(text = t, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))

            // First name
            Label("First Name"); Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = firstName, onValueChange = { firstName = it }, singleLine = true,
                placeholder = { Text("Example: Taha") },
                isError = attempted && !isFirstValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            if (attempted && !isFirstValid) Error("First name is required")

            Spacer(Modifier.height(14.dp))

            // Last name
            Label("Last Name"); Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = lastName, onValueChange = { lastName = it }, singleLine = true,
                placeholder = { Text("Example: Hamifar") },
                isError = attempted && !isLastValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            if (attempted && !isLastValid) Error("Last name is required")

            Spacer(Modifier.height(14.dp))

            // Username
            Label("Username"); Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = username, onValueChange = { username = it }, singleLine = true,
                placeholder = { Text("Example: @HamifarTaha") },
                isError = attempted && !isUserValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            if (attempted && !isUserValid) Error("Username must be at least 3 chars, no spaces")

            Spacer(Modifier.height(14.dp))

            // Email
            Label("Email Address"); Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = email, onValueChange = { email = it }, singleLine = true,
                placeholder = { Text("Example: hamifar.taha@gmail.com") },
                isError = attempted && !isEmailValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            if (attempted && !isEmailValid) Error("Enter a valid email address")

            Spacer(Modifier.height(14.dp))

            // Password
            Label("Password"); Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it }, singleLine = true,
                placeholder = { Text("********") },
                visualTransformation = PasswordVisualTransformation(),
                isError = attempted && !isPassValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
            )
            if (attempted && !isPassValid) Error("Password must be at least 6 characters")

            Spacer(Modifier.height(14.dp))

            // Retype Password
            Label("Retype Password"); Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = retype, onValueChange = { retype = it }, singleLine = true,
                placeholder = { Text("********") },
                visualTransformation = PasswordVisualTransformation(),
                isError = attempted && !isRetypeValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { tryRegister() })
            )
            if (attempted && !isRetypeValid) Error("Passwords do not match")

            Spacer(Modifier.height(24.dp))

            // Register button (enabled while invalid so first tap shows errors)
            val buttonColors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) purple else purple.copy(alpha = 0.4f),
                contentColor = Color.White
            )
            Button(
                onClick = { tryRegister() },
                enabled = !ui.loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = buttonColors
            ) {
                if (ui.loading) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Register", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Register")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Already have an account? Login here",
                    color = purple,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp)) // small bottom breathing room
        }
    }
}
