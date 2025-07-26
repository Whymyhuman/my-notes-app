package com.example.notes.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.notes.data.Note
import com.example.notes.data.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object BackupHelper {
    
    private const val BACKUP_FILE_PREFIX = "my_notes_backup"
    private const val BACKUP_FILE_EXTENSION = ".json"
    
    suspend fun createBackup(
        context: Context,
        notes: List<Note>,
        categories: List<Category>
    ): File? = withContext(Dispatchers.IO) {
        try {
            val backupData = JSONObject().apply {
                put("version", 1)
                put("timestamp", System.currentTimeMillis())
                put("app_version", "1.1")
                
                // Add categories
                val categoriesArray = JSONArray()
                categories.forEach { category ->
                    val categoryJson = JSONObject().apply {
                        put("id", category.id)
                        put("name", category.name)
                        put("color", category.color)
                        put("created_at", category.createdAt)
                    }
                    categoriesArray.put(categoryJson)
                }
                put("categories", categoriesArray)
                
                // Add notes
                val notesArray = JSONArray()
                notes.forEach { note ->
                    val noteJson = JSONObject().apply {
                        put("id", note.id)
                        put("title", note.title)
                        put("content", note.content)
                        put("timestamp", note.timestamp)
                        put("is_pinned", note.isPinned)
                        put("category_id", note.categoryId)
                    }
                    notesArray.put(noteJson)
                }
                put("notes", notesArray)
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
            val fileName = "${BACKUP_FILE_PREFIX}_${timestamp}${BACKUP_FILE_EXTENSION}"
            
            val backupFile = File(context.getExternalFilesDir(null), fileName)
            FileWriter(backupFile).use { writer ->
                writer.write(backupData.toString(2))
            }
            
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun exportNotesToText(
        context: Context,
        notes: List<Note>,
        categories: List<Category>
    ): File? = withContext(Dispatchers.IO) {
        try {
            val categoryMap = categories.associateBy { it.id }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
            val fileName = "my_notes_export_${timestamp}.txt"
            
            val exportFile = File(context.getExternalFilesDir(null), fileName)
            FileWriter(exportFile).use { writer ->
                writer.write("My Notes Export\\n")
                writer.write("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\\n")
                writer.write("Total notes: ${notes.size}\\n")
                writer.write("\\n" + "=".repeat(50) + "\\n\\n")
                
                notes.sortedByDescending { it.timestamp }.forEach { note ->
                    writer.write("Title: ${note.title}\\n")
                    if (note.categoryId != null) {
                        val categoryName = categoryMap[note.categoryId]?.name ?: "Unknown"
                        writer.write("Category: $categoryName\\n")
                    }
                    writer.write("Created: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(note.timestamp))}\\n")
                    if (note.isPinned) {
                        writer.write("📌 Pinned\\n")
                    }
                    writer.write("\\n${note.content}\\n")
                    writer.write("\\n" + "-".repeat(30) + "\\n\\n")
                }
            }
            
            exportFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun shareFile(context: Context, file: File, mimeType: String = "text/plain") {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "Share backup file"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    data class BackupData(
        val version: Int,
        val timestamp: Long,
        val appVersion: String,
        val notes: List<Note>,
        val categories: List<Category>
    )
    
    suspend fun parseBackup(backupContent: String): BackupData? = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject(backupContent)
            val version = json.getInt("version")
            val timestamp = json.getLong("timestamp")
            val appVersion = json.getString("app_version")
            
            // Parse categories
            val categoriesArray = json.getJSONArray("categories")
            val categories = mutableListOf<Category>()
            for (i in 0 until categoriesArray.length()) {
                val categoryJson = categoriesArray.getJSONObject(i)
                val category = Category(
                    categoryJson.getString("name"),
                    categoryJson.getString("color"),
                    categoryJson.getLong("created_at")
                ).apply {
                    id = categoryJson.getInt("id")
                }
                categories.add(category)
            }
            
            // Parse notes
            val notesArray = json.getJSONArray("notes")
            val notes = mutableListOf<Note>()
            for (i in 0 until notesArray.length()) {
                val noteJson = notesArray.getJSONObject(i)
                val note = Note(
                    noteJson.getString("title"),
                    noteJson.getString("content"),
                    noteJson.getLong("timestamp"),
                    noteJson.getBoolean("is_pinned"),
                    if (noteJson.isNull("category_id")) null else noteJson.getInt("category_id")
                ).apply {
                    id = noteJson.getInt("id")
                }
                notes.add(note)
            }
            
            BackupData(version, timestamp, appVersion, notes, categories)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}