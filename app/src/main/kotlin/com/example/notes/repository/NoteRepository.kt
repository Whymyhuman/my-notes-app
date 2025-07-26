package com.example.notes.repository

import androidx.lifecycle.LiveData
import com.example.notes.data.Note
import com.example.notes.data.NoteDao

class NoteRepository(private val noteDao: NoteDao) {
    
    fun getAllNotes(): LiveData<List<Note>> = noteDao.getAllNotes()
    
    fun searchNotes(query: String): LiveData<List<Note>> {
        return noteDao.searchNotes("%$query%")
    }
    
    suspend fun getNoteById(id: Int): Note? = noteDao.getNoteById(id)
    
    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)
    
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
    
    suspend fun updatePinStatus(id: Int, isPinned: Boolean) = 
        noteDao.updatePinStatus(id, isPinned)
    
    suspend fun deleteNoteById(id: Int) = noteDao.deleteNoteById(id)
}

