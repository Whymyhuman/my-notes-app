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
    
    suspend fun getAllNotesSync(): List<Note> = noteDao.getAllNotesSync()
    
    suspend fun deleteAllNotes() = noteDao.deleteAllNotes()
    
    // Trash/Recycle Bin functions
    fun getDeletedNotes(): LiveData<List<Note>> = noteDao.getDeletedNotes()
    
    suspend fun moveToTrash(id: Int, deletedAt: Long) = noteDao.moveToTrash(id, deletedAt)
    
    suspend fun restoreFromTrash(id: Int) = noteDao.restoreFromTrash(id)
    
    suspend fun deleteOldTrashedNotes(cutoffTime: Long) = noteDao.deleteOldTrashedNotes(cutoffTime)
    
    suspend fun emptyTrash() = noteDao.emptyTrash()
    
    suspend fun getTrashCount(): Int = noteDao.getTrashCount()
}

