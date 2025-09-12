package com.example.simplenote

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.simplenote.ui.theme.SimpleNoteTheme
import java.util.UUID
import androidx.compose.material3.Text
import androidx.lifecycle.lifecycleScope
import com.example.simplenote.auth.AuthState
import com.example.simplenote.network.ApiClient
import kotlinx.coroutines.launch
import com.example.simplenote.network.TokenManager

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

        val tokenManager = TokenManager(applicationContext)

        setContent {
            var editorSessionKey by remember { mutableStateOf<String?>(null) }

            SimpleNoteTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.Onboarding) }
                var ready by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                val loggedOut by AuthState.loggedOut.collectAsState()
                LaunchedEffect(loggedOut) {
                    if (loggedOut) {
                        screen = Screen.Login
                        AuthState.reset()
                    }
                }

                LaunchedEffect(Unit) {
                    coroutineScope.launch {
                        val refresh = tokenManager.getRefreshToken()
                        val access = tokenManager.getAccessToken()
                        Log.d("MainActivity", "Startup tokens: access=${access?.take(10)}..., refresh=${refresh?.take(10)}...")

                        if (!refresh.isNullOrBlank() && !access.isNullOrBlank()) {
                            try {
                                val user = ApiClient.api.getUserInfo()
                                Log.i("MainActivity", "✅ getUserInfo success for ${user.email}")
                                screen = Screen.Home
                            } catch (e: Exception) {
                                    Log.e("MainActivity", "❌ getUserInfo failed: ${e.message}", e)
                                tokenManager.clear()
                                AuthState.triggerLogout()
                                screen = Screen.Login
                            }
                        } else {
                            Log.w("MainActivity", "No tokens → going to login")
                            screen = Screen.Login
                        }
                        ready = true
                    }
                }


                if (!ready) return@SimpleNoteTheme


                when (val s = screen) {
                    is Screen.Onboarding -> OnboardingScreen(
                        onGetStarted = { screen = Screen.Login }
                    )

                    is Screen.Login -> LoginScreen(
                        onLoginSuccess = { access, refresh ->
                            lifecycleScope.launch {
                                tokenManager.saveTokens(access, refresh)
                            }
                            screen = Screen.Home
                        },
                        onRegisterClick = { screen = Screen.Register }
                    )

                    is Screen.Register -> RegisterScreen(
                        onBackToLogin = { screen = Screen.Login },
                        onRegistered = { username, email ->
                            // optional: store for prefill, but DO NOT navigate here
                            // lastRegisteredUsername = username
                        }
                    )

                    is Screen.Home -> HomeScreen(
                        onAddNote = {
                            editorSessionKey = UUID.randomUUID().toString()
                            screen = Screen.NewNote
                        },
                        onOpenSettings = { screen = Screen.Settings },
                        onOpenNote = { id ->
                            editorSessionKey = "edit-$id"
                            screen = Screen.EditNote(id)
                        },
                    )


                    is Screen.NewNote -> NoteEditorScreen(
                        sessionKey = editorSessionKey ?: UUID.randomUUID().toString(), // pass key
                        existingNoteId = null,
                        onBack = { screen = Screen.Home },
                        onSavedAndExit = { screen = Screen.Home }
                    )

                    is Screen.EditNote -> NoteEditorScreen(
                        sessionKey = editorSessionKey ?: "edit-${(screen as Screen.EditNote).id}",
                        existingNoteId = (screen as Screen.EditNote).id,
                        onBack = { screen = Screen.Home },
                        onSavedAndExit = { screen = Screen.Home }
                    )

                    is Screen.Detail -> {
                        // TODO: Note details screen (not part of this step)
                        Text("Note detail: ${s.noteId}")
                    }

                    is Screen.Settings -> SettingsScreen(
                        onChangePassword = { screen = Screen.ChangePassword },
                        onLogoutSuccess = {
                            lifecycleScope.launch {
                                tokenManager.clear()
                            }
                            screen = Screen.Login

                        },
                        onBack = { screen = Screen.Home }
                    )

                    is Screen.ChangePassword -> ChangePasswordScreen(
                        onPasswordChanged = { screen = Screen.Settings },
                        onBack = { screen = Screen.Settings }
                    )

                }
            }
        }
    }
}