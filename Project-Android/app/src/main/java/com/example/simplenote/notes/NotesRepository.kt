package com.example.simplenote.notes

import com.example.simplenote.network.*

class NotesRepository {
    suspend fun createNote(token: String, title: String, description: String): NoteDto =
        ApiClient.api.createNote(CreateNoteRequest(title, description), "Bearer $token")

    suspend fun updateNote(token: String, id: Long, title: String?, description: String?): NoteDto =
        ApiClient.api.updateNote(id, UpdateNoteRequest(title, description), "Bearer $token")

    suspend fun deleteNote(token: String, id: Long) =
        ApiClient.api.deleteNote(id, "Bearer $token")

    // ONE page (fast; good enough for Home)
    suspend fun listNotesFirstPage(token: String, pageSize: Int = 50): List<NoteDto> =
        ApiClient.api.listNotesPaged("Bearer $token", page = 1, pageSize = pageSize).results

    // (Optional) ALL pages – follow page numbers until 'next' is null
    suspend fun listAllNotes(token: String, pageSize: Int = 50): List<NoteDto> {
        val out = mutableListOf<NoteDto>()
        var page = 1
        var hasNext: Boolean
        do {
            val resp = ApiClient.api.listNotesPaged("Bearer $token", page = page, pageSize = pageSize)
            out += resp.results
            hasNext = resp.next != null
            page += 1
        } while (hasNext)
        return out
    }
}
