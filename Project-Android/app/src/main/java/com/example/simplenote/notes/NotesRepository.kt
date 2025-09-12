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

    // NotesRepository.kt
    suspend fun listAllNotes(token: String, pageSize: Int = 100): List<NoteDto> {
        val out = mutableListOf<NoteDto>()
        var page = 1
        var nextUrl: String? = null

        do {
            val resp = ApiClient.api.listNotesPaged(
                "Bearer $token",
                page = page,
                pageSize = pageSize
            )
            out += resp.results
            nextUrl = resp.next
            // If server returns an absolute next URL like "...?page=3", follow it;
            // otherwise just increment page.
            val nextPage = nextUrl?.substringAfter("page=")?.substringBefore("&")?.toIntOrNull()
            page = nextPage ?: (page + 1)
        } while (nextUrl != null)

        return out
    }


    suspend fun getNote(token: String, id: Long): NoteDto =
        ApiClient.api.getNote(id, "Bearer $token")

    suspend fun listNotesPaged(token: String, page: Int, pageSize: Int = 6): PagedNotesResponse =
        ApiClient.api.listNotesPaged("Bearer $token", page = page, pageSize = pageSize)

}
