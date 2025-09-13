package com.example.simplenote.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.network.NoteDto
import com.example.simplenote.notes.NotesRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlin.math.max
import java.net.SocketTimeoutException

data class HomeUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val count: Int = 0,
    val pageSize: Int = 6,
    val pages: Map<Int, List<NoteDto>> = emptyMap(), // 1-based page -> notes
    val timeoutActive: Boolean = false,               // sticky timeout flag
    val retryInSec: Int = 0                           // seconds until next retry (0 means now/idle)
) {
    val totalPages: Int get() = max(1, (count + pageSize - 1) / pageSize)
    val hasNotes: Boolean get() = count > 0 || pages.values.any { it.isNotEmpty() }
}

class HomeViewModel(
    private val repo: NotesRepository = NotesRepository()
) : ViewModel() {

    var uiState = androidx.compose.runtime.mutableStateOf(HomeUiState())
        private set

    private var retryJob: Job? = null
    private val retryIntervalSec = 15

    /** Fetch page 1 and clear timeout on success */
    private suspend fun fetchFirstPage(token: String) {
        val resp = repo.listNotesPaged(token, page = 1, pageSize = uiState.value.pageSize)
        uiState.value = HomeUiState(
            loading = false,
            count = resp.count,
            pageSize = uiState.value.pageSize,
            pages = mapOf(1 to resp.results),
            timeoutActive = false,
            retryInSec = 0
        )
        stopTimeoutRetry()
    }

    /** Start a 30s retry loop while timeoutActive is true, with 1s countdown ticks */
    private fun startTimeoutRetry(token: String) {
        if (retryJob?.isActive == true) return
        retryJob = viewModelScope.launch {
            while (uiState.value.timeoutActive) {
                // countdown
                for (sec in retryIntervalSec downTo 1) {
                    if (!uiState.value.timeoutActive) return@launch
                    uiState.value = uiState.value.copy(retryInSec = sec)
                    delay(1_000)
                }
                uiState.value = uiState.value.copy(retryInSec = 0)
                // attempt refresh
                try {
                    fetchFirstPage(token) // success clears timeout & stops loop
                    return@launch
                } catch (e: SocketTimeoutException) {
                    // keep timeoutActive = true; loop again
                    uiState.value = uiState.value.copy(
                        loading = false,
                        error = "Request timed out",
                        timeoutActive = true,
                        retryInSec = retryIntervalSec
                    )
                } catch (e: Exception) {
                    // other error; keep looping
                    uiState.value = uiState.value.copy(
                        loading = false,
                        error = e.message ?: "Failed to refresh",
                        retryInSec = retryIntervalSec
                    )
                }
            }
        }
    }

    private fun stopTimeoutRetry() {
        retryJob?.cancel()
        retryJob = null
        uiState.value = uiState.value.copy(retryInSec = 0)
    }

    /** Reset and load page 1 */
    fun init(token: String) {
        uiState.value = HomeUiState(loading = true, pageSize = 6)
        viewModelScope.launch {
            try {
                fetchFirstPage(token)
            } catch (e: SocketTimeoutException) {
                uiState.value = HomeUiState(
                    loading = false,
                    error = "Request timed out",
                    pageSize = 6,
                    timeoutActive = true,
                    retryInSec = retryIntervalSec
                )
                startTimeoutRetry(token)
            } catch (e: Exception) {
                uiState.value = HomeUiState(
                    loading = false,
                    error = e.message ?: "Failed to load notes",
                    pageSize = 6,
                    timeoutActive = uiState.value.timeoutActive,
                    retryInSec = uiState.value.retryInSec
                )
            }
        }
    }

    /** Ensure a page exists in state; if not, fetch it */
    fun ensurePage(token: String, page: Int) {
        val ui = uiState.value
        if (page < 1 || page > ui.totalPages) return
        if (ui.pages.containsKey(page)) return

        viewModelScope.launch {
            try {
                val resp = repo.listNotesPaged(token, page = page, pageSize = ui.pageSize)
                uiState.value = uiState.value.copy(
                    pages = uiState.value.pages + (page to resp.results),
                    timeoutActive = false,
                    retryInSec = 0
                )
                stopTimeoutRetry()
            } catch (e: SocketTimeoutException) {
                uiState.value = uiState.value.copy(
                    error = "Request timed out",
                    timeoutActive = true,
                    retryInSec = retryIntervalSec
                )
                startTimeoutRetry(token)
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    error = e.message ?: "Failed to load page $page"
                )
            }
        }
    }

    fun refresh(token: String) = init(token)
}
