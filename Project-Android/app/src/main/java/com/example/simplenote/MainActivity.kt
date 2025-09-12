package com.example.simplenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.simplenote.ui.theme.SimpleNoteTheme
import java.util.UUID
import androidx.compose.material3.Text

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
            var editorSessionKey by remember { mutableStateOf<String?>(null) }

            SimpleNoteTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.Onboarding) }
                var accessToken by remember { mutableStateOf<String?>(null) }
                var lastRegisteredUsername by remember { mutableStateOf<String?>(null) }

                when (val s = screen) {
                    is Screen.Onboarding -> OnboardingScreen(
                        onGetStarted = { screen = Screen.Login }
                    )

                    is Screen.Login -> LoginScreen(
                        onLoginSuccess = { access, _ ->
                            accessToken = access
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
                        sessionKey = editorSessionKey ?: UUID.randomUUID().toString(), // pass key
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
                        // TODO: Note details screen (not part of this step)
                        Text("Note detail: ${s.noteId}")
                    }

                    is Screen.Settings -> SettingsScreen(
                        accessToken = accessToken.orEmpty(),
                        onChangePassword = { screen = Screen.ChangePassword },
                        onLogoutSuccess = {
                            accessToken = null
                            screen = Screen.Login
                        }
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