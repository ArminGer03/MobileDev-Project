package com.example.simplenote.home

import androidx.compose.ui.graphics.Color

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val prettyDate: String,
    val color: Color? = null
)

data class HomeUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val notes: List<Note> = emptyList()
)
