package com.example.notes.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "is_pinned")
    public boolean isPinned;

    @ColumnInfo(name = "category_id")
    public Integer categoryId; // Nullable - notes can exist without category

    @Ignore
    public Note(String title, String content, long timestamp, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.isPinned = isPinned;
        this.categoryId = null;
    }

    public Note(String title, String content, long timestamp, boolean isPinned, Integer categoryId) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.isPinned = isPinned;
        this.categoryId = categoryId;
    }

    // Getters (optional, but good practice for Java POJOs)
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    // Setters (optional)
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
}

