package com.example.simplenote.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.database.NoteEntity
import com.example.simplenote.notes.NotesRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val notes: List<NoteEntity> = emptyList()
) {
    val hasNotes: Boolean get() = notes.isNotEmpty()
}

class HomeViewModel(
    private val repo: NotesRepository
) : ViewModel() {

    var uiState = androidx.compose.runtime.mutableStateOf(HomeUiState())
        private set

    fun init(token: String) {
        // Observe local DB
        viewModelScope.launch {
            repo.notesFlow.collectLatest { localNotes ->
                uiState.value = uiState.value.copy(
                    loading = false,
                    notes = localNotes
                )
            }
        }

        // Optionally refresh from server to get latest
        viewModelScope.launch {
            try {
                repo.syncNotesWithServer(token)
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to refresh"
                )
            }
        }
    }

    fun refresh(token: String) {
        viewModelScope.launch {
            try {
                repo.syncNotesWithServer(token)
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(error = e.message ?: "Failed to refresh")
            }
        }
    }
}
