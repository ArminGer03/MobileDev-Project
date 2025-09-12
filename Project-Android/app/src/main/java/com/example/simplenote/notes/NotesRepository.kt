package com.example.simplenote.notes

import com.example.simplenote.database.NoteDao
import com.example.simplenote.database.NoteEntity
import com.example.simplenote.network.*

class NotesRepository(
    private val dao: NoteDao
) {
    val notesFlow = dao.getAllNotes()

    suspend fun getNoteById(id: Long): NoteEntity? = dao.getById(id)

    /** Offline-first create: save locally first, mark as pending */
    suspend fun createNoteOffline(title: String, description: String) {
        val localId = System.currentTimeMillis() // temporary local ID
        dao.insert(
            NoteEntity(
                id = localId,
                title = title,
                description = description,
                updatedAt = System.currentTimeMillis(),
                pendingAction = "create"
            )
        )
    }

    suspend fun updateNoteOffline(id: Long, title: String?, description: String?) {
        val current = NoteEntity(
            id = id,
            title = title ?: "",
            description = description ?: "",
            updatedAt = System.currentTimeMillis(),
            pendingAction = "update"
        )
        dao.insert(current)
    }

    suspend fun deleteNoteOffline(id: Long) {
        dao.insert(
            NoteEntity(
                id = id,
                title = "",
                description = "",
                updatedAt = System.currentTimeMillis(),
                pendingAction = "delete"
            )
        )
    }

    /**
     * Full two-way sync:
     * 1. Push pending local changes to server.
     * 2. Pull latest server notes and refresh local DB.
     */
    suspend fun syncNotesWithServer(accessToken: String) {
        pushPendingChanges(accessToken)
        pullLatestFromServer(accessToken)
    }

    /** Push local pending changes (create/update/delete) to server */
    private suspend fun pushPendingChanges(token: String) {
        val pending = dao.getPending()
        for (note in pending) {
            try {
                when (note.pendingAction) {
                    "create" -> {
                        val remote = ApiClient.api.createNote(
                            CreateNoteRequest(note.title, note.description),
                            "Bearer $token"
                        )
                        // Replace local temp note with server version
                        dao.deleteById(note.id)
                        dao.insert(
                            note.copy(
                                id = remote.id,
                                pendingAction = null,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                    "update" -> {
                        ApiClient.api.updateNote(
                            note.id,
                            UpdateNoteRequest(note.title, note.description),
                            "Bearer $token"
                        )
                        dao.insert(note.copy(pendingAction = null))
                    }
                    "delete" -> {
                        ApiClient.api.deleteNote(note.id, "Bearer $token")
                        dao.deleteById(note.id)
                    }
                }
            } catch (e: Exception) {
                // Leave as pending to retry next sync
            }
        }
    }

    /** Fetch notes from server and refresh local DB */
    private suspend fun pullLatestFromServer(token: String) {
        try {
            val page1 = ApiClient.api.listNotesPaged("Bearer $token", page = 1, pageSize = 100)
            // Replace local DB contents with fresh copy
            dao.clearAll()
            page1.results.forEach { dto ->
                dao.insert(
                    NoteEntity(
                        id = dto.id,
                        title = dto.title,
                        description = dto.description,
                        updatedAt = System.currentTimeMillis(),
                        pendingAction = null
                    )
                )
            }
        } catch (e: Exception) {
            // Network error -> keep local cache
        }
    }
}
