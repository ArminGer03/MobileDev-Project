package com.example.simplenote.notes

import com.example.simplenote.network.*

class NotesRepository {
    suspend fun createNote(token: String, title: String, description: String): NoteDto =
        ApiClient.api.createNote(CreateNoteRequest(title, description), "Bearer $token")

    suspend fun updateNote(token: String, id: Long, title: String?, description: String?): NoteDto =
        ApiClient.api.updateNote(id, UpdateNoteRequest(title, description), "Bearer $token")

    suspend fun deleteNote(token: String, id: Long) =
        ApiClient.api.deleteNote(id, "Bearer $token")

    suspend fun getNote(token: String, id: Long): NoteDto =
        ApiClient.api.getNote(id, "Bearer $token")

    suspend fun listNotesPaged(token: String, page: Int, pageSize: Int = 6): PagedNotesResponse =
        ApiClient.api.listNotesPaged("Bearer $token", page = page, pageSize = pageSize)

    suspend fun filterNotesPaged(
        token: String,
        query: String,
        page: Int,
        pageSize: Int = 6,
        updatedGte: String? = null,
        updatedLte: String? = null
    ): PagedNotesResponse =
        ApiClient.api.filterNotesPaged(
            auth = "Bearer $token",
            title = query,
            description = query,
            updatedGte = updatedGte,
            updatedLte = updatedLte,
            page = page,
            pageSize = pageSize
        )
}
