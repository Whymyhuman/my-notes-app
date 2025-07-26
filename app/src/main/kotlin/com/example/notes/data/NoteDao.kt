package com.example.notes.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    
    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, timestamp DESC")
    fun getAllNotes(): LiveData<List<Note>>
    
    @Query("SELECT * FROM notes WHERE title LIKE :searchQuery OR content LIKE :searchQuery ORDER BY is_pinned DESC, timestamp DESC")
    fun searchNotes(searchQuery: String): LiveData<List<Note>>
    
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long
    
    @Update
    suspend fun updateNote(note: Note)
    
    @Delete
    suspend fun deleteNote(note: Note)
    
    @Query("UPDATE notes SET is_pinned = :isPinned WHERE id = :id")
    suspend fun updatePinStatus(id: Int, isPinned: Boolean)
    
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
    
    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, timestamp DESC")
    suspend fun getAllNotesSync(): List<Note>
    
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}

