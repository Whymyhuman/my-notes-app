package com.example.notes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.notes.data.Note
import com.example.notes.data.NoteDatabase
import com.example.notes.repository.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NoteRepository
    private val searchQuery = MutableLiveData<String>()
    
    val allNotes: LiveData<List<Note>>
    
    init {
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        
        allNotes = searchQuery.switchMap { query ->
            if (query.isNullOrBlank()) {
                repository.getAllNotes()
            } else {
                repository.searchNotes(query)
            }
        }
        
        // Initialize with empty search to show all notes
        searchQuery.value = ""
    }
    
    fun searchNotes(query: String) {
        searchQuery.value = query
    }
    
    fun insertNote(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }
    
    fun updateNote(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
    }
    
    fun deleteNote(note: Note) = viewModelScope.launch {
        repository.deleteNote(note)
    }
    
    fun togglePinStatus(note: Note) = viewModelScope.launch {
        repository.updatePinStatus(note.id, !note.isPinned)
    }
    
    suspend fun getNoteById(id: Int): Note? {
        return repository.getNoteById(id)
    }
}

