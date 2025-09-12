package com.example.simplenote.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.simplenote.database.NoteDao
import com.example.simplenote.notes.NotesRepository

class HomeViewModelFactory(private val dao: NoteDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(NotesRepository(dao)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
