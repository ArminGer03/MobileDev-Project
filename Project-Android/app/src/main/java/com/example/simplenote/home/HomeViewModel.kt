package com.example.simplenote.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.network.NoteDto
import com.example.simplenote.notes.NotesRepository
import kotlinx.coroutines.launch
import kotlin.math.max

data class HomeUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val count: Int = 0,
    val pageSize: Int = 6,
    val pages: Map<Int, List<NoteDto>> = emptyMap() // 1-based page -> notes
) {
    val totalPages: Int get() = max(1, (count + pageSize - 1) / pageSize)
    val hasNotes: Boolean get() = count > 0 || pages.values.any { it.isNotEmpty() }
}

class HomeViewModel(
    private val repo: NotesRepository = NotesRepository()
) : ViewModel() {

    var uiState = androidx.compose.runtime.mutableStateOf(HomeUiState())
        private set

    /** Reset and load page 1 */
    fun init(token: String) {
        uiState.value = HomeUiState(loading = true)
        viewModelScope.launch {
            try {
                val resp = repo.listNotesPaged(token, page = 1, pageSize = 6)
                uiState.value = HomeUiState(
                    loading = false,
                    count = resp.count,
                    pageSize = 6,
                    pages = mapOf(1 to resp.results)
                )
            } catch (e: Exception) {
                uiState.value = HomeUiState(loading = false, error = e.message ?: "Failed to load notes")
            }
        }
    }

    /** Ensure a page exists in state; if not, fetch it */
    fun ensurePage(token: String, page: Int) {
        val ui = uiState.value
        if (page < 1 || page > ui.totalPages) return
        if (ui.pages.containsKey(page)) return

        // Mark loading (optional: you can set a small flag; we keep it simple)
        viewModelScope.launch {
            try {
                val resp = repo.listNotesPaged(token, page = page, pageSize = ui.pageSize)
                uiState.value = uiState.value.copy(
                    pages = uiState.value.pages + (page to resp.results)
                )
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(error = e.message ?: "Failed to load page $page")
            }
        }
    }

    fun refresh(token: String) = init(token)
}
