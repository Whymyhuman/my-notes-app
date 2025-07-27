package com.example.notes

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notes.databinding.ActivityBackupBinding
import com.example.notes.utils.BackupUtils
import com.example.notes.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BackupActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBackupBinding
    private val noteViewModel: NoteViewModel by viewModels()
    
    private val createBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportBackup(it) }
    }
    
    private val restoreBackupLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { importBackup(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
        updateBackupInfo()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Backup & Restore"
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnCreateBackup.setOnClickListener {
            createBackup()
        }
        
        binding.btnRestoreBackup.setOnClickListener {
            restoreBackup()
        }
        
        binding.btnExportText.setOnClickListener {
            exportToText()
        }
    }
    
    private fun updateBackupInfo() {
        lifecycleScope.launch {
            val notesCount = noteViewModel.getAllNotesSync().size
            val lastBackup = getLastBackupTime()
            
            binding.tvNotesCount.text = "Total Notes: $notesCount"
            binding.tvLastBackup.text = if (lastBackup.isNotEmpty()) {
                "Last Backup: $lastBackup"
            } else {
                "No backup created yet"
            }
        }
    }
    
    private fun createBackup() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "my_notes_backup_$timestamp.json"
        createBackupLauncher.launch(fileName)
    }
    
    private fun exportBackup(uri: Uri) {
        lifecycleScope.launch {
            try {
                val notes = noteViewModel.getAllNotesSync()
                val success = BackupUtils.exportNotesToJson(this@BackupActivity, notes, uri)
                
                if (success) {
                    saveLastBackupTime()
                    updateBackupInfo()
                    Toast.makeText(this@BackupActivity, "Backup created successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@BackupActivity, "Failed to create backup", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BackupActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun restoreBackup() {
        AlertDialog.Builder(this)
            .setTitle("Restore Backup")
            .setMessage("This will replace all current notes. Are you sure?")
            .setPositiveButton("Restore") { _, _ ->
                restoreBackupLauncher.launch(arrayOf("application/json"))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun importBackup(uri: Uri) {
        lifecycleScope.launch {
            try {
                val notes = BackupUtils.importNotesFromJson(this@BackupActivity, uri)
                
                if (notes.isNotEmpty()) {
                    // Clear existing notes and import new ones
                    noteViewModel.deleteAllNotes()
                    notes.forEach { note ->
                        noteViewModel.insertNote(note)
                    }
                    
                    updateBackupInfo()
                    Toast.makeText(this@BackupActivity, "Backup restored successfully (${notes.size} notes)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@BackupActivity, "No notes found in backup file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BackupActivity, "Failed to restore backup: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun exportToText() {
        lifecycleScope.launch {
            try {
                val notes = noteViewModel.getAllNotesSync()
                val filePath = BackupUtils.exportNotesToText(this@BackupActivity, notes)
                
                if (filePath != null) {
                    Toast.makeText(this@BackupActivity, "Notes exported to: $filePath", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@BackupActivity, "Failed to export notes", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BackupActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun saveLastBackupTime() {
        val prefs = getSharedPreferences("backup_prefs", MODE_PRIVATE)
        val timestamp = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(Date())
        prefs.edit().putString("last_backup", timestamp).apply()
    }
    
    private fun getLastBackupTime(): String {
        val prefs = getSharedPreferences("backup_prefs", MODE_PRIVATE)
        return prefs.getString("last_backup", "") ?: ""
    }
}