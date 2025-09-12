package com.example.simplenote.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class NoteEditorUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val fieldErrors: Map<String, List<String>> = emptyMap(),
    val savedNoteId: Long? = null
)

class NoteEditorViewModel(
    val repo: NotesRepository // Inject a Room-backed repo
) : ViewModel() {

    var uiState = androidx.compose.runtime.mutableStateOf(NoteEditorUiState())
        private set

    /**
     * Save or update a note locally first, then trigger sync in background.
     */
    fun saveOrUpdate(token: String, existingId: Long?, title: String, description: String) {
        if (title.isBlank() && description.isBlank()) return

        uiState.value = uiState.value.copy(loading = true, error = null)

        viewModelScope.launch {
            try {
                if (existingId == null) {
                    repo.createNoteOffline(title, description)
                } else {
                    repo.updateNoteOffline(existingId, title, description)
                }

                // Fire-and-forget background sync
                launch { repo.syncNotesWithServer(token) }

                uiState.value = uiState.value.copy(
                    loading = false,
                    fieldErrors = emptyMap()
                )
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to save note"
                )
            }
        }
    }

    /**
     * Delete locally first, then sync when possible.
     */
    fun delete(token: String, id: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                repo.deleteNoteOffline(id)
                launch { repo.syncNotesWithServer(token) }
                onDone()
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(error = e.message ?: "Delete failed")
            }
        }
    }

    fun clearFieldErrors(attr: String) {
        val cur = uiState.value.fieldErrors.toMutableMap()
        if (cur.remove(attr) != null) {
            uiState.value = uiState.value.copy(fieldErrors = cur)
        }
    }

    fun consumeSaved() {
        uiState.value = uiState.value.copy(savedNoteId = null)
    }

    fun clearError() {
        uiState.value = uiState.value.copy(error = null)
    }
}
