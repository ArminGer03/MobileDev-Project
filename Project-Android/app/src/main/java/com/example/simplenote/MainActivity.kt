package com.example.simplenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.simplenote.ui.theme.SimpleNoteTheme

import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {

    sealed class Screen {
        data object Onboarding : Screen()
        data object Login : Screen()
        data object Register : Screen()
        data object Home : Screen()
        data class Detail(val noteId: String) : Screen()
        data object NewNote : Screen()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
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
                        onAddNote = { screen = Screen.NewNote },
                        onOpenSettings = { /* ... */ },
                        username = lastRegisteredUsername,
                        accessToken = accessToken.orEmpty()
                    )

                    is Screen.NewNote -> NoteEditorScreen(
                        accessToken = accessToken.orEmpty(),
                        onBack = { screen = Screen.Home },
                        onSavedAndExit = { screen = Screen.Home }
                    )

                    is Screen.Detail -> {
                        // TODO: Note details screen (not part of this step)
                        Text("Note detail: ${s.noteId}")
                    }
                }
            }
        }
    }
}