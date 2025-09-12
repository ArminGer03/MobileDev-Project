package com.example.simplenote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplenote.settings.ChangePasswordViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    accessToken: String,
    onPasswordChanged: () -> Unit,
    onBack: () -> Unit,
    vm: ChangePasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val ui = vm.uiState.value
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val purple = Color(0xFF504EC3)

    // Handle success
    LaunchedEffect(ui.success) {
        if (ui.success) {
            snackbarHostState.showSnackbar("Password changed successfully")
            vm.reset()
            onPasswordChanged()
        }
    }

    // Handle error
    LaunchedEffect(ui.error) {
        ui.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Change Password", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = purple)
                        Spacer(Modifier.width(4.dp))
                        Text("Back", color = purple)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // --- Current password ---
            Text(
                text = "Please input your current password first",
                color = purple,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text("Current Password", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                placeholder = { Text("********") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(20.dp))
            Divider()

            // --- New password ---
            Text(
                text = "Now, create your new password",
                color = purple,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Text("New Password", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                placeholder = { Text("********") },
                shape = RoundedCornerShape(12.dp)
            )
            Text(
                text = "Password should contain a-z, A-Z, 0-9",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )

            Spacer(Modifier.height(20.dp))

            // --- Retype password ---
            Text("Retype New Password", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                placeholder = { Text("********") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(32.dp))

            // --- Submit button ---
            Button(
                onClick = {
                    if (newPassword == confirmPassword && newPassword.isNotBlank()) {
                        vm.changePassword(accessToken, oldPassword, newPassword)
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Passwords do not match or are empty")
                        }
                    }
                },
                enabled = !ui.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = purple)
            ) {
                if (ui.loading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Submit New Password",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}
