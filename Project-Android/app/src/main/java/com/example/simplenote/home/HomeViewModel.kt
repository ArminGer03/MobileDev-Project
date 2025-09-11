package com.example.simplenote.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    fun loadNotes() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(loading = true)
            // TODO: replace with real API call using your access token
            // Simulate latency + sample data
            delay(500)
            val sample = listOf(
                Note(
                    id = "1",
                    title = "Welcome to SimpleNote",
                    body = "Tap the + button to create, edit, and organize your ideas.",
                    prettyDate = "Today"
                ),
                Note(
                    id = "2",
                    title = "Design meeting",
                    body = "Draft agenda: onboarding flow, empty states, and error handling.",
                    prettyDate = "Yesterday"
                )
            )
            _uiState.value = HomeUiState(notes = sample)
        }
    }
}
