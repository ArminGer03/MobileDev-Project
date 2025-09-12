package com.example.simplenote.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class NoteEditorUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val fieldErrors: Map<String, List<String>> = emptyMap(),
    val savedNoteId: Long? = null
)

// Parse DRF-style 400 body
data class ApiFieldError(val code: String?, val detail: String?, val attr: String?)
data class ApiErrorResponse(val type: String?, val errors: List<ApiFieldError>?)

class NoteEditorViewModel(
    private val repo: NotesRepository = NotesRepository()
) : ViewModel() {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val errAdapter = moshi.adapter(ApiErrorResponse::class.java)

    var uiState = androidx.compose.runtime.mutableStateOf(NoteEditorUiState())
        private set

    fun saveOrUpdate(existingId: Long?, title: String, description: String) {
        if (title.isBlank() && description.isBlank()) return
        uiState.value = uiState.value.copy(loading = true, error = null)

        viewModelScope.launch {
            try {
                val dto = if (existingId == null) {
                    repo.createNote(title, description)
                } else {
                    repo.updateNote(existingId, title, description)
                }
                uiState.value = uiState.value.copy(
                    loading = false,
                    savedNoteId = dto.id,
                    fieldErrors = emptyMap()
                )
            } catch (e: HttpException) {
                val raw = e.response()?.errorBody()?.string()
                val parsed = raw?.let { runCatching { errAdapter.fromJson(it) }.getOrNull() }
                if (parsed?.errors?.isNotEmpty() == true) {
                    val map = parsed.errors.groupBy({ it.attr ?: "general" }) {
                        it.detail ?: it.code ?: "Invalid value"
                    }
                    uiState.value = uiState.value.copy(loading = false, fieldErrors = map)
                } else {
                    uiState.value =
                        uiState.value.copy(loading = false, error = "Save failed (${e.code()})")
                }
            } catch (e: Exception) {
                uiState.value =
                    uiState.value.copy(loading = false, error = e.message ?: "Save failed")
            }
        }
    }

    fun delete(id: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                repo.deleteNote(id); onDone()
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(error = e.message ?: "Delete failed")
            }
        }
    }

    fun clearFieldErrors(attr: String) {
        val cur = uiState.value.fieldErrors.toMutableMap()
        if (cur.remove(attr) != null) uiState.value = uiState.value.copy(fieldErrors = cur)
    }

    fun consumeSaved() {
        uiState.value = uiState.value.copy(savedNoteId = null)
    }

    fun clearError() {
        uiState.value = uiState.value.copy(error = null)
    }
}
