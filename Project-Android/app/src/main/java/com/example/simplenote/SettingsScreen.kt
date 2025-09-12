package com.example.simplenote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.simplenote.R
import com.example.simplenote.settings.SettingsViewModel

@Composable
fun SettingsScreen(
    accessToken: String,
    onChangePassword: () -> Unit,
    onLogoutSuccess: () -> Unit,
    vm: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val ui = vm.uiState.value
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Load user info on entry
    LaunchedEffect(accessToken) { vm.loadUserInfo(accessToken) }

    // Navigate back to login if logged out
    LaunchedEffect(ui.loggedOut) {
        if (ui.loggedOut) onLogoutSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            ui.loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            ui.error != null -> {
                Text("Error: ${ui.error}", color = MaterialTheme.colorScheme.error)
            }
            ui.user != null -> {
                // Profile info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "Profile",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            ui.user.first_name + " " + ui.user.last_name,
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize
                        )
                        Text(ui.user.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider()

                // Change Password
                ListItem(
                    headlineContent = { Text("Change Password") },
                    leadingContent = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    modifier = Modifier.clickable { onChangePassword() }
                )

                Divider()

                // Logout
                ListItem(
                    headlineContent = { Text("Log Out", color = Color.Red) },
                    leadingContent = { Icon(Icons.Outlined.Logout, contentDescription = null, tint = Color.Red) },
                    modifier = Modifier.clickable { showLogoutDialog = true }
                )

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Log Out") },
                        text = { Text("Are you sure you want to log out from the application?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showLogoutDialog = false
                                vm.logout()
                            }) { Text("Yes") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }
        }
    }
}
