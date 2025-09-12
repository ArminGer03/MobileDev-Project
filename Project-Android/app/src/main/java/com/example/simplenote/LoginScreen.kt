package com.example.simplenote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.simplenote.auth.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (access: String, refresh: String) -> Unit,
    onRegisterClick: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val purple = Color(0xFF504EC3)

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }

    val ui = vm.uiState.value
    val snackbarHostState = remember { SnackbarHostState() }

    // validation
    val trimmedUser = username.trim()
    // simple username rule: at least 3 chars, no spaces
    val isUserValid = trimmedUser.length >= 3 && !trimmedUser.contains(' ')
    val isPasswordValid = password.isNotBlank()
    val isFormValid = isUserValid && isPasswordValid

    fun tryLogin() {
        attempted = true
        if (isFormValid) vm.login(trimmedUser, password)
    }

    // success / backend error
    LaunchedEffect(ui.token) {
        ui.token?.let {
            onLoginSuccess(it.access, it.refresh)
            vm.clearTokens()
        }
    }
    LaunchedEffect(ui.error) {
        ui.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingVals ->
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Let’s Login",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    text = "And notes your idea",
                    fontSize = 14.sp,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Username
                Text(text = "Username", fontSize = 14.sp, color = Color(0xFF1C1B1F))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    singleLine = true,
                    placeholder = { Text(text = "Example: @HamifarTaha") },
                    isError = attempted && !isUserValid,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                if (attempted && !isUserValid) {
                    Text(
                        text = "Username must be at least 3 characters (no spaces).",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Password
                Text(text = "Password", fontSize = 14.sp, color = Color(0xFF1C1B1F))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    placeholder = { Text(text = "********") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = attempted && !isPasswordValid,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { tryLogin() })
                )
                if (attempted && !isPasswordValid) {
                    Text(
                        text = "Password cannot be empty.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(28.dp))

                // Keep enabled so first tap triggers validation + error display
                val btnColors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) purple else purple.copy(alpha = 0.4f),
                    contentColor = Color.White
                )
                Button(
                    onClick = { tryLogin() },
                    enabled = !ui.loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
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
                            Text(text = "Login", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "Login")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(Modifier.weight(1f))
                    Text(text = "  Or  ", color = Color(0xFF9E9E9E))
                    Divider(Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Don’t have any account? Register here",
                        color = purple,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
