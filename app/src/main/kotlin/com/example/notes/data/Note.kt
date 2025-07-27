package com.example.notes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name = "title")
    var title: String,
    
    @ColumnInfo(name = "content")
    var content: String,
    
    @ColumnInfo(name = "timestamp")
    var timestamp: Long,
    
    @ColumnInfo(name = "is_pinned")
    var isPinned: Boolean,
    
    @ColumnInfo(name = "category_id")
    var categoryId: Int? = null
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0
    
    @ColumnInfo(name = "is_deleted")
    var isDeleted: Boolean = false
    
    @ColumnInfo(name = "deleted_at")
    var deletedAt: Long? = null
    
    @ColumnInfo(name = "image_paths")
    var imagePaths: String? = null
    
    @ColumnInfo(name = "reminder_time")
    var reminderTime: Long? = null
    
    // Constructor for backward compatibility
    constructor(
        title: String,
        content: String,
        timestamp: Long,
        isPinned: Boolean
    ) : this(title, content, timestamp, isPinned, null)
}