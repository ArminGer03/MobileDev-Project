package com.example.simplenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.simplenote.ui.theme.SimpleNoteTheme

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {

    sealed class Screen {
        data object Onboarding : Screen()
        data object Login : Screen()
        data object Register : Screen()
        data object Home : Screen()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SimpleNoteTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.Onboarding) }

                when (screen) {
                    is Screen.Onboarding -> OnboardingScreen(
                        onGetStarted = { screen = Screen.Login }
                    )

                    is Screen.Login -> LoginScreen(
                        onLoginSuccess = { _, _ -> screen = Screen.Home },
                        onRegisterClick = { screen = Screen.Register } // <-- go to register
                    )

                    is Screen.Register -> RegisterScreen(
                        onBackToLogin = { screen = Screen.Login },
                        onRegistered = { _, _ ->
                            // Option 1: simply go back to login after a successful registration
                            screen = Screen.Login
                        }
                    )

                    is Screen.Home -> Greeting(name = "Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SimpleNoteTheme {
        Greeting("Android")
    }
}