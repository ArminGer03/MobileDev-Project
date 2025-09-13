package com.example.simplenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.example.simplenote.auth.TokenStore
import com.example.simplenote.network.ApiClient
import com.example.simplenote.network.RefreshRequest
import com.example.simplenote.ui.theme.SimpleNoteTheme
import java.util.UUID

class MainActivity : ComponentActivity() {

    sealed class Screen {
        data object Onboarding : Screen()
        data object Login : Screen()
        data object Register : Screen()
        data object Home : Screen()
        data class Detail(val noteId: String) : Screen()
        data object NewNote : Screen()
        data class EditNote(val id: Long) : Screen()
        data object Settings : Screen()
        data object ChangePassword : Screen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // unique session key for editor screens
            var editorSessionKey by remember { mutableStateOf<String?>(null) }

            SimpleNoteTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.Onboarding) }
                var accessToken by remember { mutableStateOf<String?>(null) }
                var lastRegisteredUsername by remember { mutableStateOf<String?>(null) }
                var bootChecked by remember { mutableStateOf(false) }

                // ---- BOOTSTRAP (persisted login) ----
                LaunchedEffect(Unit) {
                    // TokenStore.init() is called in Application (SimpleNoteApp)
                    val storedRefresh = TokenStore.refresh()
                    val storedAccess = TokenStore.access()

                    if (storedRefresh != null) {
                        if (storedAccess.isNullOrEmpty()) {
                            // Try to get a fresh access token once
                            runCatching {
                                val newAccess = ApiClient.api.refresh(RefreshRequest(storedRefresh)).access
                                TokenStore.saveAccess(newAccess)
                                accessToken = newAccess
                                screen = Screen.Home
                            }.onFailure {
                                TokenStore.clear()
                                screen = Screen.Login
                            }
                        } else {
                            // We have an access token; authenticator will refresh on 401
                            accessToken = storedAccess
                            screen = Screen.Home
                        }
                    } else {
                        // No tokens yet → onboarding → login
                        screen = Screen.Onboarding
                    }
                    bootChecked = true
                }

                // Safety: if access is cleared (logout), return to Onboarding/Login
                LaunchedEffect(accessToken) {
                    if (bootChecked && accessToken == null) {
                        screen = Screen.Onboarding
                    }
                }

                when (val s = screen) {
                    is Screen.Onboarding -> OnboardingScreen(
                        onGetStarted = { screen = Screen.Login }
                    )

                    is Screen.Login -> LoginScreen(
                        onLoginSuccess = { access, refresh ->
                            // Persist both tokens and go Home
                            TokenStore.saveTokens(access, refresh)
                            accessToken = access
                            screen = Screen.Home
                        },
                        onRegisterClick = { screen = Screen.Register }
                    )

                    is Screen.Register -> RegisterScreen(
                        onBackToLogin = { screen = Screen.Login },
                        onRegistered = { username, _ ->
                            // keep for potential prefill if you like
                            lastRegisteredUsername = username
                        }
                    )

                    is Screen.Home -> HomeScreen(
                        accessToken = accessToken.orEmpty(),
                        onAddNote = {
                            editorSessionKey = UUID.randomUUID().toString()
                            screen = Screen.NewNote
                        },
                        onOpenSettings = { screen = Screen.Settings },
                        onOpenNote = { id ->
                            editorSessionKey = "edit-$id"
                            screen = Screen.EditNote(id)
                        },
                        username = lastRegisteredUsername
                    )

                    is Screen.NewNote -> NoteEditorScreen(
                        accessToken = accessToken.orEmpty(),
                        sessionKey = editorSessionKey ?: UUID.randomUUID().toString(),
                        existingNoteId = null,
                        onBack = { screen = Screen.Home },
                        onSavedAndExit = { screen = Screen.Home }
                    )

                    is Screen.EditNote -> NoteEditorScreen(
                        accessToken = accessToken.orEmpty(),
                        sessionKey = editorSessionKey ?: "edit-${(screen as Screen.EditNote).id}",
                        existingNoteId = (screen as Screen.EditNote).id,
                        onBack = { screen = Screen.Home },
                        onSavedAndExit = { screen = Screen.Home }
                    )

                    is Screen.Detail -> {
                        // (not used right now)
                        Text("Note detail: ${s.noteId}")
                    }

                    is Screen.Settings -> SettingsScreen(
                        accessToken = accessToken.orEmpty(),
                        onChangePassword = { screen = Screen.ChangePassword },
                        onLogoutSuccess = {
                            // Clear tokens and return to Login
                            TokenStore.clear()
                            accessToken = null
                            screen = Screen.Login
                        },
                        onBack = { screen = Screen.Home }
                    )

                    is Screen.ChangePassword -> ChangePasswordScreen(
                        accessToken = accessToken.orEmpty(),
                        onPasswordChanged = { screen = Screen.Settings },
                        onBack = { screen = Screen.Settings }
                    )
                }
            }
        }
    }
}
