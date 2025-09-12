// app/src/main/java/com/example/simplenote/home/HomeViewModel.kt
package com.example.simplenote.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.network.NoteDto
import com.example.simplenote.notes.NotesRepository
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val notes: List<NoteDto> = emptyList()
)

class HomeViewModel(
    private val repo: NotesRepository = NotesRepository()
) : ViewModel() {

    var uiState = androidx.compose.runtime.mutableStateOf(HomeUiState())
        private set

    fun loadNotes(token: String, allPages: Boolean = false) {
        uiState.value = HomeUiState(loading = true)
        viewModelScope.launch {
            try {
                val list = if (allPages) repo.listAllNotes(token) else repo.listNotesFirstPage(token)
                uiState.value = HomeUiState(notes = list)
            } catch (e: Exception) {
                uiState.value = HomeUiState(error = e.message ?: "Failed to load notes")
            }
        }
    }
}
