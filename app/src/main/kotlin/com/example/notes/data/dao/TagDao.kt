package com.example.notes.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.notes.data.entity.Tag
import com.example.notes.data.entity.NoteTagCrossRef

@Dao
interface TagDao {
    
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): LiveData<List<Tag>>
    
    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTagsSync(): List<Tag>
    
    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): Tag?
    
    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<Tag>): List<Long>
    
    @Update
    suspend fun updateTag(tag: Tag)
    
    @Delete
    suspend fun deleteTag(tag: Tag)
    
    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: Long)
    
    // Note-Tag relationship methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteTagCrossRefs(crossRefs: List<NoteTagCrossRef>)
    
    @Delete
    suspend fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef)
    
    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId")
    suspend fun deleteAllTagsForNote(noteId: Long)
    
    @Query("DELETE FROM note_tag_cross_ref WHERE tagId = :tagId")
    suspend fun deleteAllNotesForTag(tagId: Long)
    
    @Query("""
        SELECT tags.* FROM tags 
        INNER JOIN note_tag_cross_ref ON tags.id = note_tag_cross_ref.tagId 
        WHERE note_tag_cross_ref.noteId = :noteId
        ORDER BY tags.name ASC
    """)
    fun getTagsForNote(noteId: Long): LiveData<List<Tag>>
    
    @Query("""
        SELECT tags.* FROM tags 
        INNER JOIN note_tag_cross_ref ON tags.id = note_tag_cross_ref.tagId 
        WHERE note_tag_cross_ref.noteId = :noteId
        ORDER BY tags.name ASC
    """)
    suspend fun getTagsForNoteSync(noteId: Long): List<Tag>
    
    @Query("""
        SELECT COUNT(*) FROM note_tag_cross_ref 
        WHERE tagId = :tagId
    """)
    suspend fun getNotesCountForTag(tagId: Long): Int
    
    @Query("""
        SELECT DISTINCT tags.* FROM tags 
        INNER JOIN note_tag_cross_ref ON tags.id = note_tag_cross_ref.tagId 
        INNER JOIN notes ON note_tag_cross_ref.noteId = notes.id 
        WHERE notes.isDeleted = 0
        ORDER BY tags.name ASC
    """)
    fun getUsedTags(): LiveData<List<Tag>>
    
    @Query("""
        SELECT tags.* FROM tags 
        WHERE tags.name LIKE '%' || :query || '%'
        ORDER BY tags.name ASC
    """)
    suspend fun searchTags(query: String): List<Tag>
    
    @Query("""
        SELECT COUNT(*) FROM tags
    """)
    suspend fun getTagsCount(): Int
    
    // Get or create tag by name
    suspend fun getOrCreateTag(name: String, color: String? = null): Tag {
        val existingTag = getTagByName(name)
        return if (existingTag != null) {
            existingTag
        } else {
            val newTag = Tag(
                name = name,
                color = color ?: Tag.getRandomColor()
            )
            val id = insertTag(newTag)
            newTag.copy(id = id)
        }
    }
    
    // Assign tags to note
    suspend fun assignTagsToNote(noteId: Long, tagIds: List<Long>) {
        // First remove all existing tags for this note
        deleteAllTagsForNote(noteId)
        
        // Then add the new tags
        val crossRefs = tagIds.map { tagId ->
            NoteTagCrossRef(noteId = noteId, tagId = tagId)
        }
        insertNoteTagCrossRefs(crossRefs)
    }
    
    // Assign tags to note by names (creates tags if they don't exist)
    suspend fun assignTagsToNoteByNames(noteId: Long, tagNames: List<String>) {
        val tags = tagNames.map { name ->
            getOrCreateTag(name)
        }
        val tagIds = tags.map { it.id }
        assignTagsToNote(noteId, tagIds)
    }
}
