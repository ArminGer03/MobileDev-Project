package com.example.simplenote.notes

import com.example.simplenote.network.*

class NotesRepository {
    suspend fun createNote(token: String, title: String, description: String): NoteDto =
        ApiClient.api.createNote(CreateNoteRequest(title, description), "Bearer $token")

    suspend fun updateNote(token: String, id: Long, title: String?, description: String?): NoteDto =
        ApiClient.api.updateNote(id, UpdateNoteRequest(title, description), "Bearer $token")

    suspend fun deleteNote(token: String, id: Long) =
        ApiClient.api.deleteNote(id, "Bearer $token")
}
