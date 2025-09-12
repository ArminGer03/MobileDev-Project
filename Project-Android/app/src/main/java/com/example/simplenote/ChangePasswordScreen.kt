package com.example.simplenote

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.simplenote.network.ApiClient
import com.example.simplenote.network.ChangePasswordRequest
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
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = { Text("Old Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

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
                modifier = Modifier.fillMaxWidth()
            ) {
                if (ui.loading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text("Change Password")
                }
            }
        }
    }
}
