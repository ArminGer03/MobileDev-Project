package com.example.simplenote.database
// In a new file, e.g., NoteEntity.kt
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val title: String,
    val description: String,
    val updatedAt: Long,
    val isPending: Boolean = false, // marks unsynced changes
    val pendingAction: String? = null // "create", "update", "delete"
)

