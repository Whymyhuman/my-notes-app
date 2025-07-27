package com.example.notes.utils

import android.content.Context
import android.net.Uri
import com.example.notes.data.Note
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object BackupUtils {
    
    fun exportNotesToJson(context: Context, notes: List<Note>, uri: Uri): Boolean {
        return try {
            val jsonArray = JSONArray()
            
            notes.forEach { note ->
                val jsonNote = JSONObject().apply {
                    put("id", note.id)
                    put("title", note.title)
                    put("content", note.content)
                    put("timestamp", note.timestamp)
                    put("isPinned", note.isPinned)
                    put("categoryId", note.categoryId)
                    put("isDeleted", note.isDeleted)
                    put("deletedAt", note.deletedAt)
                    put("imagePaths", note.imagePaths)
                    put("reminderTime", note.reminderTime)
                }
                jsonArray.put(jsonNote)
            }
            
            val backupData = JSONObject().apply {
                put("version", 1)
                put("exportDate", System.currentTimeMillis())
                put("notesCount", notes.size)
                put("notes", jsonArray)
            }
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(backupData.toString(2).toByteArray())
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun importNotesFromJson(context: Context, uri: Uri): List<Note> {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return emptyList()
            
            val backupData = JSONObject(jsonString)
            val notesArray = backupData.getJSONArray("notes")
            val notes = mutableListOf<Note>()
            
            for (i in 0 until notesArray.length()) {
                val jsonNote = notesArray.getJSONObject(i)
                
                val note = Note(
                    title = jsonNote.getString("title"),
                    content = jsonNote.getString("content"),
                    timestamp = jsonNote.getLong("timestamp"),
                    isPinned = jsonNote.getBoolean("isPinned"),
                    categoryId = if (jsonNote.isNull("categoryId")) null else jsonNote.getInt("categoryId")
                ).apply {
                    // Set additional fields if they exist
                    if (jsonNote.has("isDeleted")) {
                        isDeleted = jsonNote.getBoolean("isDeleted")
                    }
                    if (jsonNote.has("deletedAt") && !jsonNote.isNull("deletedAt")) {
                        deletedAt = jsonNote.getLong("deletedAt")
                    }
                    if (jsonNote.has("imagePaths") && !jsonNote.isNull("imagePaths")) {
                        imagePaths = jsonNote.getString("imagePaths")
                    }
                    if (jsonNote.has("reminderTime") && !jsonNote.isNull("reminderTime")) {
                        reminderTime = jsonNote.getLong("reminderTime")
                    }
                }
                
                notes.add(note)
            }
            
            notes
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    fun exportNotesToText(context: Context, notes: List<Note>): String? {
        return try {
            val fileName = "my_notes_export_${System.currentTimeMillis()}.txt"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            val content = buildString {
                append("My Notes Export\n")
                append("Generated on: ${SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(Date())}\n")
                append("Total notes: ${notes.size}\n")
                append("=".repeat(50))
                append("\n\n")
                
                notes.forEachIndexed { index, note ->
                    append("${index + 1}. ${note.title.ifEmpty { "Untitled" }}\n")
                    append("-".repeat(30))
                    append("\n")
                    append(note.content)
                    append("\n\n")
                    
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                    append("Created: ${dateFormat.format(Date(note.timestamp))}")
                    if (note.isPinned) append(" (Pinned)")
                    if (note.categoryId != null) append(" (Category: ${note.categoryId})")
                    append("\n")
                    append("=".repeat(50))
                    append("\n\n")
                }
            }
            
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}