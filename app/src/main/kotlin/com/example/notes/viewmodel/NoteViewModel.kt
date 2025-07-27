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
    val deletedNotes: LiveData<List<Note>>
    
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
        
        deletedNotes = repository.getDeletedNotes()
        
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
    
    suspend fun getAllNotesSync(): List<Note> {
        return repository.getAllNotesSync()
    }
    
    fun deleteAllNotes() = viewModelScope.launch {
        repository.deleteAllNotes()
    }
    
    // Trash/Recycle Bin functions
    fun moveToTrash(note: Note) = viewModelScope.launch {
        repository.moveToTrash(note.id, System.currentTimeMillis())
    }
    
    fun restoreFromTrash(noteId: Int) = viewModelScope.launch {
        repository.restoreFromTrash(noteId)
    }
    
    fun emptyTrash() = viewModelScope.launch {
        repository.emptyTrash()
    }
    
    suspend fun getTrashCount(): Int {
        return repository.getTrashCount()
    }
    
    fun deleteOldTrashedNotes() = viewModelScope.launch {
        // Delete notes older than 30 days
        val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        repository.deleteOldTrashedNotes(cutoffTime)
    }
}

