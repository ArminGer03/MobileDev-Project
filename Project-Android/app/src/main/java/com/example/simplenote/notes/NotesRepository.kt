package com.example.simplenote.notes

import com.example.simplenote.network.*

class NotesRepository {
    suspend fun createNote(title: String, description: String): NoteDto =
        ApiClient.api.createNote(CreateNoteRequest(title, description))

    suspend fun updateNote(id: Long, title: String?, description: String?): NoteDto =
        ApiClient.api.updateNote(id, UpdateNoteRequest(title, description))

    suspend fun deleteNote(id: Long) =
        ApiClient.api.deleteNote(id)

    suspend fun getNote(id: Long): NoteDto =
        ApiClient.api.getNote(id)

    suspend fun listNotesPaged(page: Int, pageSize: Int = 6): PagedNotesResponse =
        ApiClient.api.listNotesPaged(page = page, pageSize = pageSize)

    suspend fun filterNotesPaged(
        query: String,
        page: Int,
        pageSize: Int = 6,
        updatedGte: String? = null,
        updatedLte: String? = null
    ): PagedNotesResponse =
        ApiClient.api.filterNotesPaged(
            title = query,
            description = query,
            updatedGte = updatedGte,
            updatedLte = updatedLte,
            page = page,
            pageSize = pageSize
        )
}
